package mai_onsyn.compose_gl.config

/**
 * 安卓端的承载方式。**仅安卓生效**，桌面忽略。
 *
 * 在第一个 `GLCanvas` 进入组合之前设置（`Application.onCreate` / `main`），
 * 运行中修改不会生效。
 */
enum class AndroidSurfaceMode {

    /**
     * 离屏 EGL（1x1 pbuffer）+ FBO + 回读 → `Bitmap` → `ImageBitmap`，用普通 Compose `Image` 承载。
     *
     * 层级与命中测试完全走 Compose 自己的体系：GL 层上下的组件都能正常显示、正常点击。
     * 代价是每帧一次 GPU→CPU 回读 + 一次 Bitmap 拷贝。**默认值。**
     */
    ComposeImage,

    /**
     * `TextureView` 直出，零 CPU 拷贝，性能最好。
     *
     * **代价**：`TextureView` 必须通过 `AndroidView` 放进 Compose，而 `AndroidView` 的宿主
     * `AndroidViewsHandler` 把 `dispatchTouchEvent` 硬编码为 `true`，会吞掉其范围内**所有**触摸。
     * 也就是说 GL 层下面的 Compose 组件虽然是画出来的，但点不到。
     * 详见 `docs/DESIGN.md` 7.5。
     */
    TextureView,
}
