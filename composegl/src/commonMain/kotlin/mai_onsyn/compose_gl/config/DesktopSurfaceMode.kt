package mai_onsyn.compose_gl.config

/**
 * 桌面端的承载方式。
 *
 * 和安卓的 [AndroidSurfaceMode] 是同一个思路：启动期一次性设置，运行中不要改。
 *
 * ```
 * ComposeGL.desktopSurfaceMode = DesktopSurfaceMode.NativeSurface
 * ```
 */
enum class DesktopSurfaceMode {
    /**
     * 回读 + Skia 合成（默认）。
     *
     * GL 渲染到离屏 FBO，回读成 Skia 位图交给 Compose 画。好处是 **Compose 内容可以随意
     * 叠在 GL 画面上面**；代价是每帧一次回读 + 一次整帧原生分配，需要配合
     * `-XX:+ExplicitGCInvokesConcurrent` 才不会卡（见 README 第 7 节）。
     */
    ComposeImage,

    /**
     * 重量级原生画布（`SwingPanel` + `AWTGLCanvas`）。
     *
     * GL 直接渲染到 AWT 原生画布上：**零回读、零每帧分配**，帧率最高、不产生 GC 压力。
     *
     * 代价是它是一块重量级组件：
     * - **Compose 内容不能叠在它上面**（会被它盖住）；
     * - 需要把叠加内容挪到 GL 区域之外，或用 `Column`/`Row` 排布。
     */
    NativeSurface,

    /**
     * 零拷贝共享纹理（实验性，Windows 专用）。
     *
     * GL 渲染进一张与 Skiko 共享的纹理，Skia 直接采样它 —— **没有回读、没有每帧位图分配**。
     * Compose 内容依然可以叠在 GL 画面上（和 [NativeSurface] 不同）。
     *
     * 前提：必须用 `-Dskiko.renderApi=OPENGL` 启动，且 `composegl` 的影子类在 classpath
     * 上排在 skiko 之前。不满足时会通过 `onFailure` 报错，而不是静默白屏。
     *
     * 当前是第一版：每帧新建 GL 纹理与底层 SkiaImage（display list 按底层 Image 缓存，
     * 必须如此），EDT 保留最近 3 帧、更早的显式 close，内存有界。
     */
    SharedTexture,
}
