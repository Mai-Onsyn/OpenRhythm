package mai_onsyn.compose_gl.internal.android.egl

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import mai_onsyn.compose_gl.config.GLConfig
import mai_onsyn.compose_gl.config.contextSignature
import mai_onsyn.compose_gl.error.GLException

/**
 * 进程级共享的 EGL 会话。
 *
 * 同一个 [key] 的所有 GLCanvas 共用**同一个常驻上下文**：画布卸载时上下文不销毁，
 * 下次挂载直接复用 —— 着色器、纹理这些昂贵资源不用重新编译/创建。
 *
 * EGL 允许把一个上下文在释放之后绑定到另一条线程（不像 WGL 有 DC 像素格式的限制），
 * 所以这里每个画布仍然可以有自己的渲染线程，只是上下文是共享的。
 *
 * 同时存在多个画布时，第二个画布会拿到一个**共享对象**的子上下文
 * （`eglCreateContext` 的 shareContext 参数），两边都能看到同一批 GL 对象。
 */
internal class EglSession private constructor(
    val key: String,
    val display: EGLDisplay,
    val eglConfig: EGLConfig,
    private val rootContext: EGLContext,
) {

    /** 上下文销毁时回调（由客户端注册）。 */
    @Volatile
    var onContextDestroy: (() -> Unit)? = null

    /** 根上下文是否正在被某个画布使用。 */
    private var rootInUse = false

    /** 上下文创建后的第一个使用者 —— 它需要初始化资源。 */
    private var firstUse = true

    /** 借用结果：[isFirstContextUse] 表示上下文是不是刚创建的。 */
    class Lease(val context: EGLContext, val isFirstContextUse: Boolean)

    @Synchronized
    fun acquire(): Lease {
        if (!rootInUse) {
            rootInUse = true
            val isFirst = firstUse
            firstUse = false
            return Lease(rootContext, isFirst)
        }
        // 根上下文正被别人用：开一个共享对象的子上下文
        val child = EGL14.eglCreateContext(
            display, eglConfig, rootContext, contextAttributes(), 0,
        )
        if (child == EGL14.EGL_NO_CONTEXT) {
            throw GLException(
                "创建共享上下文的子上下文失败，错误码 0x${EGL14.eglGetError().toString(16)}",
            )
        }
        return Lease(child, isFirstContextUse = true)
    }

    @Synchronized
    fun release(context: EGLContext) {
        if (context == rootContext) {
            rootInUse = false
            return
        }
        EGL14.eglDestroyContext(display, context)
    }

    /** 彻底丢弃会话：下次挂载会重建上下文（着色器需要重新编译）。 */
    @Synchronized
    fun destroy() {
        EGL14.eglDestroyContext(display, rootContext)
        EGL14.eglTerminate(display)
    }

    companion object {
        private val sessions = HashMap<String, EglSession>()

        /** 取（或建）会话。相同 [key] 拿到同一个常驻上下文。 */
        @Synchronized
        fun obtain(key: String, config: GLConfig, surfaceType: Int): EglSession {
            sessions[key]?.let { return it }
            val created = create(key, config, surfaceType)
            sessions[key] = created
            return created
        }

        /** 丢弃会话。 */
        @Synchronized
        fun invalidate(key: String) {
            sessions.remove(key)?.also {
                it.onContextDestroy?.invoke()
                it.onContextDestroy = null
                it.destroy()
            }
        }

        private fun create(key: String, config: GLConfig, surfaceType: Int): EglSession {
            val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            if (display == EGL14.EGL_NO_DISPLAY) {
                throw GLException("eglGetDisplay 失败，错误码 0x${EGL14.eglGetError().toString(16)}")
            }
            val version = IntArray(2)
            if (!EGL14.eglInitialize(display, version, 0, version, 1)) {
                throw GLException("eglInitialize 失败，错误码 0x${EGL14.eglGetError().toString(16)}")
            }

            val attributes = intArrayOf(
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, config.alphaBits.coerceAtLeast(8),
                EGL14.EGL_DEPTH_SIZE, config.depthBits,
                EGL14.EGL_STENCIL_SIZE, config.stencilBits,
                EGL14.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT,
                EGL14.EGL_SURFACE_TYPE, surfaceType,
                EGL14.EGL_NONE,
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val count = IntArray(1)
            val ok = EGL14.eglChooseConfig(display, attributes, 0, configs, 0, 1, count, 0)
            if (!ok || count[0] <= 0 || configs[0] == null) {
                throw GLException(
                    "找不到合适的 EGLConfig（GLES3 + ALPHA8 + surfaceType=0x${surfaceType.toString(16)}），" +
                        "错误码 0x${EGL14.eglGetError().toString(16)}",
                )
            }
            val eglConfig = configs[0]!!

            val context = createContextWithFallback(display, eglConfig, config)
            return EglSession(key, display, eglConfig, context)
        }

        /**
         * 按 `config.androidGlesVersion` 请求上下文，失败时逐级回退 3.x。
         *
         * 有些驱动不认 `EGL_CONTEXT_MINOR_VERSION_KHR`，所以每一档都再试一次
         * 只带 major 的老写法，避免在少数设备上直接起不来。
         */
        private fun createContextWithFallback(
            display: EGLDisplay,
            eglConfig: EGLConfig,
            config: GLConfig,
        ): EGLContext {
            val requestedMajor = config.androidGlesVersion.first.coerceAtLeast(3)
            val requestedMinor = config.androidGlesVersion.second.coerceIn(0, 2)
            val attempts = buildList {
                add(requestedMajor to requestedMinor)
                add(3 to 2)
                add(3 to 1)
                add(3 to 0)
            }.distinct()

            var lastError = 0
            for ((major, minor) in attempts) {
                for (withMinor in listOf(true, false)) {
                    val attributes = if (withMinor) {
                        intArrayOf(
                            EGL_CONTEXT_MAJOR_VERSION_KHR, major,
                            EGL_CONTEXT_MINOR_VERSION_KHR, minor,
                            EGL14.EGL_NONE,
                        )
                    } else {
                        intArrayOf(
                            EGL14.EGL_CONTEXT_CLIENT_VERSION, major,
                            EGL14.EGL_NONE,
                        )
                    }
                    val created = EGL14.eglCreateContext(
                        display, eglConfig, EGL14.EGL_NO_CONTEXT, attributes, 0,
                    )
                    if (created != EGL14.EGL_NO_CONTEXT) return created
                    lastError = EGL14.eglGetError()
                }
            }
            throw GLException(
                "eglCreateContext 失败（已尝试 3.2/3.1/3.0），错误码 0x${lastError.toString(16)}",
            )
        }

        /** 子上下文用同样的版本属性。 */
        private fun contextAttributes(): IntArray = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
            EGL14.EGL_NONE,
        )

        /** 与 EGL_OPENGL_ES3_BIT_KHR 同值。 */
        const val EGL_OPENGL_ES3_BIT = 0x40

        /** 与 EGL_CONTEXT_CLIENT_VERSION 同值，显式写出更清楚。 */
        private const val EGL_CONTEXT_MAJOR_VERSION_KHR = 0x3098
        private const val EGL_CONTEXT_MINOR_VERSION_KHR = 0x30FB

        /** 会话 key：加上 surface 类型与配置特征，变了就是新上下文。 */
        fun sessionKey(sessionKey: String, config: GLConfig, surfaceType: Int): String =
            "$sessionKey|$surfaceType|${config.contextSignature()}"
    }
}
