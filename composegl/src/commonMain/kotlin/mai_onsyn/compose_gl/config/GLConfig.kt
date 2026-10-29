package mai_onsyn.compose_gl.config

/**
 * GLCanvas 的渲染配置。
 *
 * 修改本对象的任何字段都会导致 render target 重建（桌面重建 FBO，安卓重建 EGLSurface），
 * 因此不要在每帧修改它。
 */
data class GLConfig(
    /** 颜色缓冲的 alpha 位数。透明必需 8。 */
    val alphaBits: Int = 8,

    /** 深度缓冲位数，0 表示不需要。 */
    val depthBits: Int = 0,

    /** 模板缓冲位数，0 表示不需要。 */
    val stencilBits: Int = 0,

    /**
     * MSAA 采样数：1 表示关闭，常用 4。
     *
     * 实际采样数会被 `GL_MAX_SAMPLES` 收敛；驱动不支持多重采样 FBO 时**自动退回单采样**，
     * 不会因此报错。开启后每条路径都会多一次 resolve（安卓窗口模式还要再 blit 一次到窗口表面）。
     */
    val msaa: Int = 1,

    /**
     * 渲染分辨率相对显示尺寸的比例，取值范围 (0, 1]。小于 1 时显示会被拉伸。
     *
     * 主要用来降压：回读开销与像素数成正比，0.5 大约是原来的四分之一。
     * 安卓 `TextureView` 模式忽略此项——缓冲尺寸由系统按视图管理。
     */
    val resolutionScale: Float = 1f,

    /** 桌面 GL 版本（major to minor）。驱动不支持时按 4.6 -> 4.3 -> 3.3 回退。 */
    val desktopGlVersion: Pair<Int, Int> = 4 to 6,

    /** 安卓 GLES 版本。设备不支持时按 3.2 -> 3.1 -> 3.0 回退。 */
    val androidGlesVersion: Pair<Int, Int> = 3 to 2,

    /**
     * 桌面调试上下文（驱动级错误检查）。开销较大，仅排查问题时开启。
     *
     * 开启后会注册 GL 调试回调，把驱动报的错误（GL_INVALID_OPERATION 之类）
     * 转到 [mai_onsyn.compose_gl.error.GLLog] 输出。排查"资源 id 跨上下文失效"这类
     * 静默错误时，这是唯一能拿到线索的手段。
     */
    val debugContext: Boolean = false,

    /**
     * 每帧发布产生的**原生内存**预算（MB）。超出后桌面端会主动触发一次 GC。
     *
     * Compose 桌面端每画一帧更新过的位图都会新建一个 Skia Image 并上传纹理，这部分是原生内存，
     * JVM 堆看不见、GC 不会自然触发。连续渲染下实测 RSS 会以 ~66MB/s 增长，必须人为钳住。
     * 默认 64MB（60fps / 约 0.3MP 画布时约每秒回收一次，对帧率影响约 1fps）。
     * 设为 0 关闭该机制 —— 只在你自己接管内存管理时才这么做。
     */
    val nativeCopyBudgetMb: Int = 64,
)

/**
 * 影响 GL 上下文的配置特征。
 *
 * 常驻上下文是按这个特征分组的：特征变了就必须换上下文（着色器等资源要重建），
 * 否则同一个会话可以跨挂载复用。
 */
internal fun GLConfig.contextSignature(): String =
    "$desktopGlVersion/$androidGlesVersion/$alphaBits/$depthBits/$stencilBits/$debugContext"
