package mai_onsyn.compose_gl.internal.desktop.skiko

import org.lwjgl.opengl.WGL
import org.lwjgl.opengl.WGLARBCreateContext
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.jawt.JAWTWin32DrawingSurfaceInfo
import org.lwjgl.system.windows.GDI32
import org.lwjgl.system.windows.PIXELFORMATDESCRIPTOR
import org.lwjgl.system.windows.User32
import org.lwjgl.system.windows.WinBase
import java.util.concurrent.ConcurrentHashMap

/**
 * 接管 Skiko 在 Windows 上的 GL 上下文创建，使它的上下文与我们的共享同一个 Share Group。
 *
 * 背景：Skiko 的 `WindowsOpenGLRedrawerKt.createContext` 是 private static native，
 * 签名里**没有** shareContext 参数，事后 `wglShareLists` 又会因为 Skiko 的上下文常年
 * current 在自己的渲染线程上而返回 `ERROR_BUSY`（实测 77 个像素格式全部失败）。
 *
 * 唯一可行的切入点就是**创建时**把它拉进同一个 Share Group：
 *
 * ```
 * 我们先建 bootstrap 上下文（自己的隐藏窗口）
 *        ↓
 * Skiko 建上下文时以 bootstrap 为 share
 *        ↓
 * 两者从创建起就在同一个 Share Group 里，可以分别 current 在不同线程
 * ```
 *
 * 这个对象就是 "Skiko 的上下文创建" 的替身：影子类
 * `org.jetbrains.skiko.redrawer.WindowsOpenGLRedrawerKt` 把 7 个 native 全部转到这里。
 */
object SkikoGlShare {

    /** bootstrap 上下文（我们的渲染线程之后会用它）。0 表示不可用。 */
    @Volatile
    var bootstrapContext: Long = 0L
        private set

    /** bootstrap 用的窗口/DC（顶替 Skiko 创建上下文时要用它做 makeCurrent）。 */
    @Volatile
    var bootstrapDc: Long = 0L
        private set

    @Volatile
    var bootstrapHwnd: Long = 0L
        private set

    /** 是否真的把 Skiko 的上下文拉进了共享组（false 说明退回了独立上下文）。 */
    @Volatile
    var skikoContextShared: Boolean = false
        private set

    private const val WS_POPUP = -2147483648
    private const val WGL_CONTEXT_MAJOR_VERSION_ARB = 0x2091
    private const val WGL_CONTEXT_MINOR_VERSION_ARB = 0x2092
    private const val WGL_CONTEXT_PROFILE_MASK_ARB = 0x9126
    private const val WGL_CONTEXT_CORE_PROFILE_BIT_ARB = 0x00000001

    private val dcByHwnd = ConcurrentHashMap<Long, Long>()

    private fun log(msg: String) {
        runCatching {
            java.io.File(System.getProperty("java.io.tmpdir"), "composegl-skiko-share.log")
                .appendText("[${Thread.currentThread().name}] $msg\n")
        }
    }

    private inline fun <T> withStack(block: (MemoryStack) -> T): T {
        val s = MemoryStack.stackPush()
        try {
            return block(s)
        } finally {
            s.pop()
        }
    }

    /** 像素格式描述符。bootstrap 与 Skiko 的窗口必须用**同一个**描述符，格式才能保持一致。 */
    private fun descriptor(transparent: Boolean): PIXELFORMATDESCRIPTOR {
        val pfd = PIXELFORMATDESCRIPTOR.calloc()
        pfd.nSize(PIXELFORMATDESCRIPTOR.SIZEOF.toShort())
        pfd.nVersion(1)
        pfd.dwFlags(0x4 or 0x20 or 0x1) // DRAW_TO_WINDOW | SUPPORT_OPENGL | DOUBLEBUFFER
        pfd.cColorBits(32)
        if (transparent) pfd.cAlphaBits(8)
        pfd.cDepthBits(24)
        pfd.cStencilBits(8)
        return pfd
    }

    private fun setFormat(dc: Long, transparent: Boolean): Boolean {
        val pfd = descriptor(transparent)
        try {
            val fmt = withStack { st -> GDI32.ChoosePixelFormat(st.ints(1), dc, pfd) }
            if (fmt == 0) {
                log("ChoosePixelFormat 失败 WinError=${WinBase.GetLastError()}")
                return false
            }
            // 已经设置过格式的窗口会失败，这是正常的（一个窗口只能设一次）
            withStack { st -> GDI32.SetPixelFormat(st.ints(1), dc, fmt, pfd) }
            return true
        } finally {
            pfd.free()
        }
    }

    /** 创建 GL 上下文；[share] 为 0 表示不共享。会依次尝试兼容档与 core 档。 */
    private fun createGlContext(dc: Long, share: Long): Long {
        // 兼容档优先：Skia 的 GL 后端在 Windows 上走的是传统路径
        val attempts = listOf(
            intArrayOf(
                WGL_CONTEXT_MAJOR_VERSION_ARB, 4,
                WGL_CONTEXT_MINOR_VERSION_ARB, 5,
                0,
            ),
            intArrayOf(
                WGL_CONTEXT_MAJOR_VERSION_ARB, 4,
                WGL_CONTEXT_MINOR_VERSION_ARB, 5,
                WGL_CONTEXT_PROFILE_MASK_ARB, WGL_CONTEXT_CORE_PROFILE_BIT_ARB,
                0,
            ),
            intArrayOf(
                WGL_CONTEXT_MAJOR_VERSION_ARB, 3,
                WGL_CONTEXT_MINOR_VERSION_ARB, 3,
                0,
            ),
        )
        for (attribs in attempts) {
            val ctx = runCatching {
                WGLARBCreateContext.wglCreateContextAttribsARB(dc, share, attribs)
            }.getOrDefault(0L)
            if (ctx != 0L) return ctx
        }
        return 0L
    }

    /**
     * 建一个隐藏窗口给 bootstrap 上下文用（不能和 Skiko 共用 DC：一个 DC 同时只能有一个 current）。
     *
     * 影子类创建 Skiko 上下文时会调它；零拷贝模式的渲染线程也会主动调它，
     * 以免会话线程比 SkiaLayer 先起来时拿到 0。
     */
    @Synchronized
    fun ensureBootstrap(): Long {
        if (bootstrapContext != 0L) return bootstrapContext
        val hwnd = withStack { st ->
            User32.CreateWindowEx(
                st.ints(1), 0, "STATIC", "composegl-bootstrap", WS_POPUP, 0, 0, 8, 8,
                0L, 0L, 0L, 0L,
            )
        }
        if (hwnd == 0L) {
            log("bootstrap 窗口创建失败 WinError=${WinBase.GetLastError()}")
            return 0
        }
        val dc = User32.GetDC(hwnd)
        setFormat(dc, transparent = false)
        val ctx = createGlContext(dc, share = 0L)
        log("bootstrap hwnd=$hwnd dc=$dc ctx=0x${java.lang.Long.toHexString(ctx)}")
        if (ctx == 0L) {
            withStack { st -> User32.DestroyWindow(st.ints(1), hwnd) }
            return 0
        }
        bootstrapHwnd = hwnd
        bootstrapDc = dc
        bootstrapContext = ctx
        return ctx
    }

    // ---------------- 下面是影子类转进来的 7 个操作 ----------------

    /**
     * Skiko 传进来的**不是 HWND**，而是 JAWT 的 `platformInfo` 指针
     * （指向 `JAWTWin32DrawingSurfaceInfo`）。要按结构体偏移把 HDC 取出来。
     *
     * 返回值我们自己定义：直接用 HDC —— 因为 createContext / makeCurrent / swapBuffers
     * 都是我们实现的，只要三处口径一致即可。
     */
    fun deviceFor(platformInfo: Long): Long {
        var hdc = 0L
        var hwnd = 0L
        if (platformInfo != 0L) {
            runCatching {
                hwnd = MemoryUtil.memGetAddress(platformInfo + JAWTWin32DrawingSurfaceInfo.HWND)
                hdc = MemoryUtil.memGetAddress(platformInfo + JAWTWin32DrawingSurfaceInfo.HDC)
            }.onFailure { log("按 JAWT 解析失败: $it") }
        }
        // JAWT 里那个 hdc 直接拿去 ChoosePixelFormat 会报 ERROR_INVALID_HANDLE（实测），
        // 所以统一用 hwnd 自己 GetDC，拿到的才是可设像素格式的窗口 DC
        if (hwnd != 0L) {
            val own = User32.GetDC(hwnd)
            log("GetDC($hwnd) -> $own (JAWT 的 hdc=$hdc 弃用)")
            if (own != 0L) hdc = own
        }
        if (hdc == 0L) {
            hdc = User32.GetDC(platformInfo)
            log("按 HWND 兜底 GetDC($platformInfo) -> $hdc")
        }
        log("getDevice platformInfo=$platformInfo hwnd=$hwnd hdc=$hdc")
        // 顺手把 bootstrap 建好，保证 Skiko 建上下文时已经有可共享的对象
        ensureBootstrap()
        // 同一个 HDC 复用，避免重复 GetDC
        return dcByHwnd.getOrPut(platformInfo) { hdc }
    }

    fun createContext(device: Long, hwnd: Long, transparent: Boolean): Long {
        val dc = if (device != 0L) device else deviceFor(hwnd)
        setFormat(dc, transparent)
        val share = ensureBootstrap()
        var ctx = createGlContext(dc, share)
        skikoContextShared = ctx != 0L && share != 0L
        if (ctx == 0L && share != 0L) {
            // 共享失败不能让整个 UI 起不来：退回独立上下文
            log("带 share 建上下文失败，退回独立上下文（WinError=${WinBase.GetLastError()}）")
            ctx = createGlContext(dc, 0L)
            skikoContextShared = false
        }
        log("createContext dc=$dc share=0x${java.lang.Long.toHexString(share)} -> 0x${java.lang.Long.toHexString(ctx)} shared=$skikoContextShared")
        return ctx
    }

    fun makeCurrent(device: Long, context: Long) {
        val ok = withStack { st -> WGL.wglMakeCurrent(st.ints(1), device, context) }
        if (!ok) log("makeCurrent 失败 dc=$device ctx=$context WinError=${WinBase.GetLastError()}")
    }

    fun swapBuffers(device: Long) {
        withStack { st -> GDI32.SwapBuffers(st.ints(1), device) }
    }

    fun deleteContext(context: Long) {
        if (context == 0L) return
        withStack { st -> WGL.wglDeleteContext(st.ints(1), context) }
        log("deleteContext 0x${java.lang.Long.toHexString(context)}")
    }

    fun setSwapInterval(interval: Int) {
        // wglSwapIntervalEXT 需要自行取函数指针；暂不处理，保持驱动默认（1 = 跟随垂直同步）
        log("setSwapInterval($interval) 已忽略")
    }

    fun dwmFlush() {
        // 透明窗口与 DWM 的同步；不处理不影响不透明窗口
    }
}
