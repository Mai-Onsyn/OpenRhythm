# ComposeGL 设计与需求规格

| 项 | 值 |
|---|---|
| 文档版本 | v1.1 |
| 状态 | 基本完工 |
| 更新日期 | 2026-10-29 |
| 目标读者 | 接手本项目的人（默认对方对本项目零背景） |

> **本文档是唯一权威来源。** 任何人接手时先读完第 1、2、3 章，再读第 5 章动手，第 7 章是"不要踩的坑"。若有冲突，以本文档为准；若需偏离，先改文档再改代码。

---

## 0. 一句话

在 Compose Multiplatform（Windows + Android）里提供一个 `GLCanvas` composable：**把 OpenGL 的上下文、渲染线程、尺寸跟随、像素回读、Compose 合成、生命周期全部接好，让你在 `onDraw` 回调里直接敲 GL 命令，而画面像普通 Compose 元素一样参与透明合成与层级穿插。**

**库的职责边界（本项目的宪法）**

| 库负责 | 库一律不管 |
|---|---|
| 建 GL 上下文与渲染线程 | 所有 GL 命令 |
| 跟随 `Modifier` 管理 render target 尺寸与重建 | 所有 GL 状态 |
| 在 `onDraw` 里把 context 设为 current，然后交控制权 | 所有数据上传、VBO/VAO 管理 |
| 把结果合成进 Compose（桌面位图 / 安卓 TextureView） | draw call、`glClear` |
| 双向透明、帧调度、生命周期、可见性、失败降级 | 实例化绘制的一切 |

凡是"你打算怎么画"的问题，都不是本库的问题。库里不出现任何绘制辅助、几何打包、批处理优化。

> **例外说明（v1.1 修订）**：`gl/` 下有一套「统一调用面」——把两端同名的 GL 命令转发出去。
> 它是**转发**不是**抽象**：没有状态封装、没有资源对象、没有默认值，一行 `glDrawArraysInstanced`
> 就是一行 `GL46.glDrawArraysInstanced` / `GLES32.glDrawArraysInstanced`。
> 库自己一行都不会用它来画画，用不用完全由使用方决定。见第 17 章。

---

## 1. 背景与选型理由

### 1.1 为什么不用现成的库

| | karmakrafts/ComposeGL | silenium-dev/compose-gl |
|---|---|---|
| 许可证 | Apache-2.0（可参考，需保留 NOTICE） | **GPL-3.0（一行都不能抄，只能借鉴思路）** |
| 活跃度 | 24★，最后提交 2025-06，已停更 | 9★，仍在更新 |
| GL 抽象 | 自带 GLES11/20/30 全量接口 + 常量绑定，跨 6 平台，大量 TODO | 不自带，直接用 LWJGL / GLES |
| 桌面路线 | LWJGLX-AWT + **强制 Skiko 切到 OpenGL 后端** | EGL + Skia 纹理共享 |
| 致命问题 | 平台铺太宽（iOS/Web/ANGLE），核心没写完 | 私有 Nexus 仓库 + LWJGL/EGL/Skia 版本锁死，撞 Compose 版本 |

**实测反馈**：使用者曾因 silenium 的 compose-gl 强制要求更高版本 Compose，在版本冲突上折腾 3 小时后放弃。

### 1.2 本项目的三条差异

1. **不强制 Skia 后端。** karmakrafts 在 README 里明写 "forces Compose to use the specified Skiko backend on the respective platforms"，这是它最毒的坑——一旦 Windows 默认的 D3D12 后端被换掉，整个窗口的渲染路径都被牵扯，一出问题全窗口受影响。本库的桌面路径与 Skia 后端**完全解耦**，D3D12 / OpenGL / 软件渲染三种后端下都能跑。
2. **不铺平台。** 只做 Windows + Android。iOS/Web 不实现、不预留空壳（karmakrafts 就是死在"预留"上）。
3. **门面是可选的转发层，不是绑定层。** `gl/` 里的顶层函数只做"同名转发"，覆盖 GL 4.6 core ∩ GLES 3.2 的交集，够写实例化渲染；
   但库不强制使用它，demo 至今仍直接用 `GL46` / `GLES32`。平台专有接口、Buffer 映射、固定管线一律不补（见 17.1）。

### 1.3 为什么这个库值得自己写

目标使用场景是**用 GPU 实例化绘制百万级 2D 矩形**，作为现有 CPU 光栅化渲染器在极端情况下的可选加速路径。这类场景要求：高帧率、可控的帧驱动节奏、绝对不能有隐藏的同步点。现成库要么没写完，要么把依赖和版本锁死。自己写一个薄的、只做"接线"的库，风险最低。

---

## 2. 产品需求

### 2.1 目标

- **G1** 在 Windows 与 Android 上，用同一个 `@Composable GLCanvas(...)` API 完成"在 Compose 界面里渲染 OpenGL 内容"。
- **G2** 画面支持**双向透明层级**：GLCanvas 的透明区域能透出下层的 Compose 组件；上层的 Compose 组件也能正确覆盖在 GLCanvas 之上。
- **G3** 帧由使用方显式驱动（`requestRender()`），库不自作主张转帧。
- **G4** 失败时可降级（渲染 `fallbackContent` + 一条警告），不崩溃、不静默黑屏。
- **G5** 代码可整包复制进其它项目直接使用，不依赖任何发布仓库。

### 2.2 非目标（明确不做）

| 非目标 | 原因 |
|---|---|
| iOS / Web / macOS / Linux | 只做 Windows + Android。留下空壳等于埋雷 |
| `expect object GL` 形式的门面（全量绑定） | 已决策不做，见 ADR-3；现采用顶层 `expect fun` 的**最小转发面**，见第 17 章 |
| 输入事件（点击/拖拽/滚轮/多点触控）回传 | 本库只做显示。且**不得拦截**任何事件 |
| 同屏多个 GLCanvas 实例 | 已确认为单实例场景 |
| 统一 Maven 发布 / CI 制品 | 交付方式是代码复制，见 ADR-4 |
| 绘制辅助（几何打包、批处理、纹理管理、矩阵库） | 属于使用方的渲染器，见第 0 章宪法 |
| Zero-copy（GL 纹理直接交给 Skia） | 已证伪，见 3.6 |
| 强制切换 Skia 渲染后端 | 见 1.2 |

### 2.3 使用场景

1. **主要场景**：在 Compose 界面里嵌一块 GPU 渲染区域，用实例化绘制画出百万级 2D 矩形（散点/图元密度可视化一类）。
2. **次要场景**：任何需要 OpenGL 内容的 Compose 界面（3D 预览、自定义特效），只要能在 `onDraw` 里用 GL 命令画出来就能用。
3. **不适用场景**：纯 2D UI 装饰、简单图形——这些直接用 Compose 的 `Canvas` 更快，不要引入 GL。

### 2.4 功能需求

| 编号 | 需求 | 验收方式 |
|---|---|---|
| FR-1 | 提供 `GLCanvas` composable，尺寸由 `Modifier` 决定 | 用 `Modifier.size(300.dp)` / `fillMaxSize()` 均正确 |
| FR-2 | 渲染结果参与 Compose 合成，支持 alpha | 半透明清屏压在一张图片上，颜色正确、无黑边 |
| FR-3 | 双向层级：下层 Compose 可见、上层 Compose 可覆盖 | 上下各放一个 `Text`，均正确渲染 |
| FR-4 | `requestRender()` 触发一帧；帧完成后 Compose 自动重绘 | 手动调用后画面更新，不调用时画面不变 |
| FR-5 | 尺寸变化时回调 `onSurfaceResized` 并重建 render target | 窗口拖动缩放、屏幕旋转后画面不拉伸不变形 |
| FR-6 | 提供 `onSurfaceCreated` / `onSurfaceDestroyed` 管理 GL 资源生命周期 | 组件进出组合时资源正确创建/释放，无泄漏 |
| FR-7 | 上下文创建失败时渲染 `fallbackContent` 并输出警告 | 人为让上下文创建失败，界面显示 fallback 而非崩溃 |
| FR-8 | 不拦截任何指针事件 | GLCanvas 上下的按钮均可点击 |
| FR-9 | 提供帧统计（绘制/回读/发布耗时）用于性能观测 | `scope.stats` 有合理数值 |

### 2.5 非功能需求

| 编号 | 需求 | 指标 |
|---|---|---|
| NFR-1 | 桌面端 1080p 下，回读+发布链路的额外开销不高于 4ms/帧 | M2 实测 |
| NFR-2 | 帧回调期间不产生额外堆分配（除使用方自己分配的） | 用 JFR / 分配计数验证 |
| NFR-3 | Compose 侧每帧不得触发重组，只触发重绘 | 用重组计数器验证 |
| NFR-4 | 安卓端渲染线程与 UI 线程完全隔离，UI 线程零 GL 调用 | 代码审查 |
| NFR-5 | 组件不可见 / 窗口最小化时停止转帧 | 观察 CPU 占用 |
| NFR-6 | 不引入任何会顶替 Compose / Skiko 版本的传递依赖 | `./gradlew :composegl:dependencies` 审查 |

---

## 3. 产品架构

### 3.1 分层

```
┌─────────────────────────────────────────────────────────────┐
│  使用方代码                                                  │
│    GLCanvas(modifier, onDraw = { /* 原生 GL 命令 */ })       │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│  composegl（公共层，commonMain）                             │
│    GLCanvas / GLConfig / FramePolicy / GLRenderScope        │
│    ── 只定义契约，不含任何平台代码 ──                        │
└───────────────────────────┬─────────────────────────────────┘
                            │ expect / actual
        ┌───────────────────┴───────────────────┐
        ▼                                       ▼
┌───────────────────────┐             ┌───────────────────────┐
│ 桌面（JVM / Windows）  │             │ Android               │
│  隐藏 GLFW 窗口持上下文 │             │  TextureView          │
│  离屏 FBO              │             │  EGL + SurfaceTexture │
│  回读 → Skia 位图      │             │  直接 GPU 合成         │
│  ImageBitmap → Compose │             │  参与 View 层级        │
└───────────────────────┘             └───────────────────────┘
```

### 3.2 桌面帧管线（Windows）

```
[渲染线程]                                  [Compose/UI 线程]
  onDraw(scope)                                  │
      │ 用户 GL 命令写入 FBO                      │
      ▼                                          │
  glReadPixels ──► Skia Bitmap 的内存地址         │
      │  （零 CPU 拷贝，见 7.1）                   │
      ▼                                          │
  Image / ImageBitmap 发布 ── atomic state ──► 读 state（draw 阶段）
                                                 │
                                                 ▼
                                          drawImage(bitmap)
                                                 │
                                                 ▼
                                        Skia 上传纹理并绘制
```

- **两次传输不可避免**：显存→内存（回读）、内存→显存（Skia 上传）。这是跨图形 API 的物理下限，详见 3.6。
- 1080p RGBA8 单次 8.3 MB。60Hz ≈ 0.5 GB/s，120Hz ≈ 1.0 GB/s。PCIe 3.0 x16 的 memcpy 引擎实测 6~8 GB/s，带宽不是瓶颈，**同步点才是**。

### 3.3 安卓帧管线（双模式）

安卓端提供两种承载方式，由启动期开关 `ComposeGL.androidSurfaceMode` 选择。两者的 GL 渲染部分完全相同，只在"这一帧怎么送到屏幕"上分叉。

**模式 A：`ComposeImage`（默认，正确优先）**

```
[渲染线程]                                  [UI 线程]
  onDraw(scope)                                  │
      │ 用户 GL 命令写入 FBO                      │
      ▼                                          │
  glReadPixels ──► 直接 ByteBuffer ──► Bitmap     │
      │                                          │
      ▼                                          │
  ImageBitmap 发布 ── atomic state ──────────► 读 state（draw 阶段）
                                                 │
                                                 ▼
                                          drawImage(bitmap)
```

与桌面端**同构**：离屏 EGL（1x1 pbuffer）+ FBO + 回读 + Bitmap → ImageBitmap。
层级、裁剪、命中测试全部走 Compose 自己的体系，GL 层上下的组件都能点。
代价：每帧一次 GPU→CPU 回读 + 一次 `copyPixelsFromBuffer`（`Bitmap` 没有公开的底层内存地址 API，这一步省不掉）。

**模式 B：`TextureView`（可选，零拷贝）**

```
[渲染线程]                       [UI 线程]
  onDraw(scope)                     │
      │ 用户 GL 命令                 │
      ▼                             │
  eglSwapBuffers ──► SurfaceTexture ┴─► TextureView（通过 AndroidView 挂载）
      │  （0 次 CPU 拷贝）
      ▼
```

`SurfaceTexture` 是 GPU 侧消费者，全程不碰 CPU 内存，性能最好。
**代价**：`AndroidView` 的宿主 `AndroidViewsHandler` 把 `dispatchTouchEvent` 硬编码为 `true`，
会吞掉其范围内**所有**触摸 —— GL 层下面的 Compose 组件画得出来，但点不到。详见 7.5。

| | 模式 A `ComposeImage` | 模式 B `TextureView` |
|---|---|---|
| CPU 拷贝 | 每帧 1 次回读 + 1 次 Bitmap 拷贝 | 0 |
| 上层 Compose 显示 | ✅ | ✅ |
| 下层 Compose 显示 | ✅ | ✅ |
| **下层 Compose 可点击** | ✅ | ❌（`AndroidView` 固有行为） |
| 适用 | 默认；需要完整交互时 | 大面积、高频、不需要下层交互时 |

切换方式（启动期，`Application.onCreate` 或 `setContent` 之前）：

```kotlin
ComposeGL.androidSurfaceMode = AndroidSurfaceMode.TextureView
```

### 3.4 线程模型

| 线程 | 桌面 | 安卓 |
|---|---|---|
| UI / Compose | 组合、布局、绘制（`drawImage`） | 同左 |
| 渲染线程 | 独立 `Thread`，持有 GLFW context，负责全部 GL 与回读 | 独立 `Thread`，持有 EGL context，负责全部 GL |
| 上下文归属 | **只有在渲染线程上 context 才 current** | 同左 |

铁律：

1. 用户的所有 GL 命令只能出现在 `onSurfaceCreated` / `onSurfaceResized` / `onDraw` / `onSurfaceDestroyed` 四个回调里——它们全部在渲染线程执行。
2. 库在回调返回后负责把结果发布到 UI 线程，使用方不需要、也不允许自己做线程切换。
3. 任何跨线程共享的数据，同步责任在使用方（库里只有"位图引用"这一个共享对象，用原子引用 + 单写单读保证安全）。

### 3.5 合成与透明契约

**双向透明**是硬需求，它同时排除了两类方案：heavyweight 图层（`SwingPanel` / `SurfaceView`，只能压在整个窗口之上），以及 `AndroidView` 互操作（会吞掉其范围内的所有触摸，见 7.5）。

| 平台 | 承载方式 | 双向层级 |
|---|---|---|
| 桌面 | 普通 Compose `Image`（`drawImage`），无重量级组件 | 天然正确 |
| 安卓 | 普通 Compose `Image`（`drawImage`），**不用 `AndroidView`** | 天然正确 |

**预乘 alpha 全链路约定**（这是透明正确性的关键，写错就会出黑边）：

- 使用方在 fragment shader 里输出**预乘**颜色（`rgb *= a`）。
- 使用方的混合模式应为 `glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA)`。
- 桌面回读后按 `ColorAlphaType.PREMUL` 交给 Skia。
- 安卓 `Bitmap` 用 `ARGB_8888` + `setPremultiplied(true)`。
- 清屏用 `glClearColor(0, 0, 0, 0)` + `glClear`。

### 3.6 已证伪：zero-copy 不可行

这是本项目最重要的技术结论，别再花时间尝试。

**结论**：无法把 GL 纹理直接交给 Skia 使用，FBO + 回读是唯一路径。

**证据**（读取使用者本机真实依赖 `skiko-awt-0.144.6-sources.jar`，对应 Compose 1.11.1）：

| API | 实际情况 |
|---|---|
| `org.jetbrains.skia.Image.makeFromTexture(...)` | **不存在**。`Image` 只有 `makeRaster` / `makeFromBitmap` / `makeFromPixmap` / `makeFromEncoded` |
| `BackendTexture.makeGL(w,h,isMipmapped,textureId,target,format)` | 存在，能把 GL 纹理包成 `BackendTexture` |
| `BackendRenderTarget.makeGL(w,h,sampleCnt,stencilBits,fbId,fbFormat)` | 存在 |
| `Surface.makeFromBackendRenderTarget(ctx, rt, ...)` | 存在，但方向是「让 Skia 渲染**进**我们的 FBO」，不是「把已有纹理给 Skia 看」 |
| `Surface._context: DirectContext?` | `internal val`，反射能取到实例 |
| `Canvas` 暴露 surface/context | 完全不暴露 |

**为什么反射救不了**：`DirectContext` 本体是 C++ 的 `GrDirectContext`，Kotlin 侧只是 `class DirectContext internal constructor(ptr: NativePointer) : RefCnt(ptr)` 的指针壳。反射能拿到壳，但**没有任何一个函数接收「DirectContext + BackendTexture」来造出 Image**。这不是访问权限问题，是 API 面缺失。

> 因此：桌面端与 Skia 后端切换**不产生任何性能收益**。使用方若有"CPU/GPU 后端开关"，那是他自己的业务，与本库无关；本库在任意后端下行为一致。

---

## 4. 代码结构

### 4.1 模块划分

只有一个库模块 `composegl`，加两个 demo 应用模块。**不做** `buildSrc`、`convention plugin`、version catalog 里为库单独定义条目——交付方式是代码复制，任何构建魔法都是负担。

```
ComposeGL/
├── composegl/          ← 库本体（KMP: jvm + android），整包复制走即可
├── desktopApp/         ← 桌面 demo（验证 + 视觉走查）
├── androidApp/         ← 安卓 demo
├── docs/
│   └── DESIGN.md       ← 本文档
└── gradle/libs.versions.toml
```

### 4.2 目录树

```
composegl/src/
├── commonMain/kotlin/mai_onsyn/compose_gl/
│   ├── GLCanvas.kt                     // 唯一的 composable 入口
│   ├── config/
│   │   ├── GLConfig.kt                 // 渲染配置（纯数据）
│   │   └── FramePolicy.kt              // 帧策略（纯数据）
│   ├── scope/
│   │   ├── GLRenderScope.kt            // 回调句柄接口
│   │   ├── GLInfo.kt                   // 上下文信息（纯数据）
│   │   └── FrameStats.kt               // 帧统计（纯数据）
│   ├── error/
│   │   ├── GLException.kt              // 异常类型
│   │   └── GLLog.kt                    // 平台无关日志门面
│   ├── gl/
│   │   ├── GL.kt                       // 125 个顶层 expect fun（无 object）
│   │   ├── GLConst.kt                  // 两端同值的常量（163 个）
│   │   └── GLError.kt                  // glCheckError / glErrorName
│   └── internal/
│       ├── RenderSurface.kt            // 平台无关抽象（interface）
│       ├── FrameScheduler.kt           // 请求合并 / vsync 对齐
│       ├── PublishedFrame.kt           // 已发布帧的不可变快照引用
│       └── Platform.kt                 // expect fun createRenderSurface
├── jvmMain/kotlin/mai_onsyn/compose_gl/
│   ├── internal/desktop/
│   │   ├── DesktopRenderSurface.kt     // 编排：起线程、串流程
│   │   ├── context/
│   │   │   └── GlfwContext.kt          // 隐藏窗口 + GL 上下文 + 版本回退
│   │   ├── framebuffer/
│   │   │   ├── Framebuffer.kt          // FBO/纹理/RBO 生命周期与重建
│   │   │   └── MsaaResolve.kt          // MSAA 多重采样缓冲的 resolve
│   │   ├── readback/
│   │   │   ├── FrameReader.kt          // glReadPixels → Skia 位图内存
│   │   │   └── SkiaFrameBuffer.kt      // 双缓冲位图 + 指向其内存的直接缓冲
│   │   └── compose/
│   │       └── ComposeImageHost.kt     // ImageBitmap 发布 + draw 阶段消费
│   ├── gl/GL.jvm.kt                    // actual：全部转发到 GL46
│   └── GLPlatform.jvm.kt               // actual：DesktopRenderSurface 工厂
└── androidMain/kotlin/mai_onsyn/compose_gl/
    ├── internal/android/
    │   ├── AndroidCallbacks.kt         // 用户回调持有者
    │   ├── AndroidRenderSurface.kt     // 编排：按模式起线程、转发尺寸与帧请求
    │   ├── egl/
    │   │   ├── EglCore.kt              // display/config/context（不含 surface）
    │   │   ├── EglPbufferSurface.kt    // 1x1 pbuffer（离屏模式的占位表面）
    │   │   └── EglWindowSurface.kt     // SurfaceTexture 窗口表面（零拷贝模式）
    │   ├── framebuffer/
    │   │   └── GlesFramebuffer.kt      // FBO/纹理/RBO 生命周期与重建
    │   ├── readback/
    │   │   └── AndroidFrameReader.kt   // glReadPixels → Bitmap
    │   ├── thread/
    │   │   ├── FrameLoopThread.kt      // 两种模式共用的帧循环骨架
    │   │   ├── OffscreenRenderThread.kt// 离屏：pbuffer + FBO + 回读 + 发布
    │   │   └── WindowRenderThread.kt   // 零拷贝：window surface + swapBuffers
    │   ├── view/
    │   │   └── GLTextureView.kt        // 零拷贝模式的 TextureView
    │   └── compose/
    │       └── AndroidImageHost.kt     // ImageBitmap 发布（仅离屏模式）
    ├── gl/GL.android.kt                // actual：全部转发到 GLES32
    └── GLPlatform.android.kt           // actual：按模式分派
```

### 4.3 文件职责表（每个文件只做一件事）

| 文件 | 职责 | **不该做什么** |
|---|---|---|
| `GLCanvas.kt` | 组合、remember、LaunchedEffect、把平台 surface 接到 Compose 上 | 不含任何 GL 调用、不含线程逻辑 |
| `config/GLConfig.kt` | 数据类，描述要什么样的 render target | 不含默认值的推导逻辑 |
| `config/FramePolicy.kt` | 密封接口，描述帧怎么来 | 不含调度实现 |
| `scope/GLRenderScope.kt` | 回调句柄接口定义 | 不含实现 |
| `internal/RenderSurface.kt` | 5 个方法的平台抽象 | 不含平台分支 |
| `internal/FrameScheduler.kt` | 请求合并、节流、vsync 对齐 | 不含 GL、不含 UI |
| `internal/PublishedFrame.kt` | 一帧结果的不可变引用（原子交换用） | 不含像素数据本身 |
| `DesktopRenderSurface.kt` | 编排：创建 → 循环 → 销毁 | **不直接调用任何 gl\* 函数** |
| `GlfwContext.kt` | 只负责拿到一个可用的 GL context | 不管 FBO、不管帧 |
| `Framebuffer.kt` | 只管 FBO/纹理/RBO 的创建、重建、绑定、销毁 | 不管读像素 |
| `MsaaResolve.kt` | 只管多重采样缓冲的 blit resolve | 不管别的 |
| `FrameReader.kt` | 只管把像素从 FBO 搬到 Skia 位图 | 不管发布、不管 Compose |
| `SkiaFrameBuffer.kt` | 只管双缓冲位图与指向其像素内存的直接缓冲 | 不管 GL、不管发布 |
| `ComposeImageHost.kt` | 只管把位图变成 Compose 可见的绘制，并触发重绘 | 不管生成位图 |
| `AndroidRenderSurface.kt` | 编排：按模式起线程、尺寸/帧请求转发 | **不直接调用任何 GLES 函数** |
| `EglCore.kt` | 只管 EGL display/config/context | 不管 surface、不管 FBO |
| `EglPbufferSurface.kt` | 只管 1x1 pbuffer 的创建/current/销毁 | 不管 FBO |
| `EglWindowSurface.kt` | 只管窗口表面的创建/current/swap/销毁 | 不管 context |
| `GlesFramebuffer.kt` | 只管 GLES 侧 FBO/纹理/RBO 的创建、重建、绑定、销毁 | 不管读像素 |
| `AndroidFrameReader.kt` | 只管把像素从 FBO 搬进 Bitmap | 不管发布、不管 Compose |
| `FrameLoopThread.kt` | 两种模式共用的帧循环骨架：请求合并、尺寸分发、帧统计 | 不含 EGL、不含呈现逻辑 |
| `OffscreenRenderThread.kt` | 只管离屏模式的"建上下文 → 画 → 回读 → 发布" | 不管 Compose 绘制 |
| `WindowRenderThread.kt` | 只管零拷贝模式的"建上下文 → 画 → swap" | 不管 Compose 绘制 |
| `GLTextureView.kt` | 只管"是 TextureView 且自身不消费触摸"（但挡不住 `AndroidView`，见 7.5） | 不管渲染 |
| `AndroidImageHost.kt` | 只管把 Bitmap 变成 Compose 可见的绘制 | 不管生成位图 |
| `gl/GL.kt` | 只声明 125 个顶层 `expect fun`（契约 + 收录原则） | 不含任何实现、不含对象包装 |
| `gl/GL.jvm.kt` | 只做「数组 ↔ 直接缓冲」的搬运与转发到 `GL46` | 不做状态缓存、不查错误、不切线程 |
| `gl/GL.android.kt` | 同上，转发到 `GLES32`；只多一步 `int[]+offset` 与 `Long→Int` 偏移 | 同上 |
| `gl/GLConst.kt` | 只放两端数值一致的常量 | 不放任何一端独有的枚举 |
| `gl/GLError.kt` | 只做错误码翻译与「抽干并打日志」 | 不在正常路径上被调用 |

### 4.4 依赖

| 模块 | 新增依赖 | 说明 |
|---|---|---|
| commonMain | 无 | 只有 Compose 运行时 |
| jvmMain | `org.lwjgl:lwjgl`、`lwjgl-glfw`、`lwjgl-opengl` + `natives-windows` | 约 3MB。纯 Java 库，不碰 Compose/Skiko 的传递版本 |
| androidMain | 无 | `android.opengl.*` 是 framework 自带 |

**版本红线**：Compose 1.11.1 / Kotlin 2.4.0 / AGP 9.0.0 / compileSdk 36 一律不动。使用方的 IDE 不支持更高版本，任何顶版本的改动都等于让库不可用。若某个需求必须靠更高版本 Compose 才能实现，**停下来先问，不要自己升**。

---

## 5. API 接口

### 5.1 入口

```kotlin
@Composable
fun GLCanvas(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    config: GLConfig = GLConfig(),
    framePolicy: FramePolicy = FramePolicy.OnRequest,
    onSurfaceCreated: (GLRenderScope) -> Unit = {},
    onSurfaceResized: (GLRenderScope, IntSize) -> Unit = { _, _ -> },
    onDraw: (GLRenderScope) -> Unit,
    onSurfaceDestroyed: (GLRenderScope) -> Unit = {},
    onError: (GLException) -> Unit = { GLLog.warn("ComposeGL", it) },
    fallbackContent: @Composable () -> Unit = {},
)
```

**参数语义**

| 参数 | 语义 |
|---|---|
| `modifier` | 决定尺寸。库不自己算尺寸，`0.dp` 时自然不可见 |
| `enabled` | `false` 时完全停帧、释放上下文，仅保留占位；再置 `true` 时重建 |
| `config` | render target 的形态。**变化会触发上下文/render target 重建**，不要每帧改 |
| `framePolicy` | `OnRequest`（默认）/ `Continuous` |
| `onSurfaceCreated` | 上下文就绪、render target 已创建。**建 shader/VBO/VAO 放这里** |
| `onSurfaceResized` | 尺寸变化且 render target 已重建。**重建与尺寸相关的资源放这里** |
| `onDraw` | 画一帧。库已 `glViewport` 到目标尺寸，但**不会帮你 `glClear`** |
| `onSurfaceDestroyed` | 上下文即将销毁。**释放 GL 资源放这里**（虽然上下文销毁本身也会回收，但显式释放可避免某些驱动的延迟） |
| `onError` | 上下文创建失败、FBO 不完整、EGL 出错。默认实现打一条警告 |
| `fallbackContent` | 出错时渲染的内容。默认空 |

**关键约定**

- 所有回调都在**渲染线程**执行，且此时 context 已 current。
- 库不重置任何 GL 状态。同一帧内使用方自己负责状态管理；跨帧不做保证。
- `onDraw` 的语义是「**这一帧如何重画整个画面**」，不是增量补画。

### 5.1.1 全局启动开关

```kotlin
object ComposeGL {
    /** 仅安卓生效。默认 ComposeImage。启动期设置一次，运行中修改不保证生效。 */
    var androidSurfaceMode: AndroidSurfaceMode = AndroidSurfaceMode.ComposeImage
}

enum class AndroidSurfaceMode {
    ComposeImage,  // 离屏 + 回读，交互完整，每帧多一次拷贝（默认）
    TextureView,   // 零拷贝直出，但 GL 层下方不可点击
}
```

必须在第一个 `GLCanvas` 进入组合**之前**设置（`Application.onCreate` / `setContent` 之前）。
`GLCanvas` 在首次组合时读一次并 `remember`，之后改这个字段不会有任何效果。

### 5.2 GLConfig
```kotlin
data class GLConfig(
    /** 颜色缓冲位数，透明必需 8 */
    val alphaBits: Int = 8,
    /** 深度缓冲位数，0 = 不要 */
    val depthBits: Int = 0,
    /** 模板缓冲位数，0 = 不要 */
    val stencilBits: Int = 0,
    /** MSAA 采样数：1 = 关闭，4/8 = 开启 */
    val msaa: Int = 1,
    /** 渲染分辨率相对显示尺寸的比例，(0,1] 用于降负载 */
    val resolutionScale: Float = 1f,
    /** 桌面 GL 版本（major to minor），默认 4.6 core */
    val desktopGlVersion: Pair<Int, Int> = 4 to 6,
    /** 安卓 GLES 版本，默认 3.2 */
    val androidGlesVersion: Pair<Int, Int> = 3 to 2,
    /** 调试上下文（驱动级错误检查），仅桌面 */
    val debugContext: Boolean = false,
    /** 每帧发布产生的原生内存预算（MB），超出主动 GC；0 = 关闭。见 13.7 */
    val nativeCopyBudgetMb: Int = 64,
)
```

- 桌面 `4.6` 是为了给使用方后续用计算着色器留空间；驱动不支持时按 `4.6 → 4.3 → 3.3` 回退，并在 `GLInfo` 里如实报告实际版本。
- `resolutionScale < 1` 时，render target 按比例缩小，显示时由 Compose 拉伸。

### 5.3 FramePolicy

```kotlin
sealed interface FramePolicy {
    /** 只有调用 requestRender() 才渲染（默认）。省电，符合"改 state 才重画"的心智模型 */
    data object OnRequest : FramePolicy

    /** 持续按显示器刷新率转帧。maxFps = 0 表示跟随显示器 */
    data class Continuous(val maxFps: Int = 0) : FramePolicy
}
```

### 5.4 GLRenderScope

```kotlin
interface GLRenderScope {
    /** render target 的像素宽度（已含 resolutionScale） */
    val width: Int
    val height: Int
    /** Compose 的 density，用于把 dp 换算成像素 */
    val density: Float
    /** 从 0 开始递增的帧号 */
    val frameIndex: Long
    /** 本帧的起始时间（纳秒），单调递增 */
    val frameTimeNanos: Long
    /** 上下文信息：实际 GL 版本、渲染器、厂商 */
    val info: GLInfo
    /** 上一帧的耗时统计 */
    val stats: FrameStats
    /** 请求下一帧。在 OnRequest 模式下这是唯一的驱动入口 */
    fun requestRender()
}
```

**注意**：因为两个平台的 GL 绑定都是静态对象（桌面 `GL46.xxx`、安卓 `GLES32.xxx`），`GLRenderScope` **不携带任何 GL 句柄**。库只需保证"回调执行时 context 在当前线程上 current"。这是本库不做统一 GL 层却依然可用的关键。

```kotlin
data class GLInfo(
    val versionString: String,   // 例："4.6 (Core Profile) Mesa 24.0"
    val rendererString: String,  // 例："NVIDIA GeForce RTX 4070"
    val vendorString: String,
    val major: Int,
    val minor: Int,
)

data class FrameStats(
    val frameIndex: Long,
    val drawNanos: Long,        // onDraw 耗时
    val readbackNanos: Long,    // 回读耗时（安卓为 0）
    val presentNanos: Long,     // 发布到 Compose 的耗时
    val droppedFrames: Long,    // 因不可见/尺寸为 0 而跳过的帧
)
```

### 5.5 错误与降级

```kotlin
class GLException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
```

| 场景 | 行为 |
|---|---|
| GL 上下文创建失败（无 GPU / 远程桌面 / 驱动异常） | 捕获 → `onError` → 渲染 `fallbackContent` → 停止重试直到 `enabled` 或 `config` 变化 |
| FBO 不完整 | 同上 |
| EGL 初始化失败 | 同上 |
| `onDraw` 内部抛异常 | 捕获 → `onError` → 该帧放弃，**不停止渲染**（下一帧继续，避免一个瞬时错误打死整个画面） |
| 尺寸为 0 | 不渲染、不报错、计入 `droppedFrames` |

### 5.6 使用示例

**桌面（原生 LWJGL）**

```kotlin
@Composable
fun Demo() {
    Box(Modifier.fillMaxSize()) {
        Text("这一层在 GLCanvas 下方")
        GLCanvas(
            modifier = Modifier.size(320.dp),
            config = GLConfig(),
            onSurfaceCreated = { scope ->
                GL.createCapabilities()          // 已 current，直接可用
                glClearColor(0f, 0f, 0f, 0f)
            },
            onDraw = { scope ->
                glClear(GL_COLOR_BUFFER_BIT)
                // 这里敲任意 GL 命令，包括 instancing
            },
            fallbackContent = { Text("GL 不可用") },
        )
        Text("这一层在 GLCanvas 上方")
    }
}
```

**安卓（原生 GLES）**

```kotlin
GLCanvas(
    modifier = Modifier.size(320.dp),
    onSurfaceCreated = { GLES32.glClearColor(0f, 0f, 0f, 0f) },
    onDraw = { GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT) },
)
```

两边命令不同、API 相同——这就是本库的形态。

### 5.7 调用时序

```
组合
  └─ onSurfaceCreated(scope)          ← 建资源
       └─ onSurfaceResized(scope, sz) ← 尺寸相关资源
            └─ onDraw(scope)          ← 每帧
                 └─ ... 使用方 requestRender() 循环 ...
  └─ onSurfaceDestroyed(scope)        ← 释放（离开组合 / enabled=false）
```

---

## 6. 实现方式

### 6.1 桌面实现

**上下文**：`GlfwContext`

1. `glfwInit()`（进程内只初始化一次，用引用计数保护）。
2. `glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)`，`GLFW_CONTEXT_VERSION_MAJOR/MINOR` 按 `GLConfig`，`GLFW_OPENGL_PROFILE = GLFW_OPENGL_CORE_PROFILE`。
3. `glfwCreateWindow(1, 1, "ComposeGL", NULL, NULL)` —— 一个 1×1 的隐藏窗口，**只用来持上下文**，永不显示。
4. `glfwMakeContextCurrent(win)`，`GL.createCapabilities()`。
5. 版本回退：4.6 失败 → 4.3 → 3.3，每次失败先 `glfwDefaultWindowHints()`。
6. 销毁：`glfwDestroyWindow` + 引用计数归零时 `glfwTerminate`。

> 该窗口必须在**渲染线程**上创建和使用，线程退出前销毁。

**render target**：`Framebuffer`

- 颜色附件：`GL_RGBA8` 纹理（不使用 renderbuffer，方便后续可能的扩展）。
- 深度/模板：按 `GLConfig` 决定挂 `GL_DEPTH24_STENCIL8` renderbuffer。
- MSAA：`msaa > 1` 时创建多重采样 renderbuffer 作为绘制目标，帧末用 `glBlitFramebuffer` resolve 到单采样纹理。
- 尺寸变化：`glDeleteFramebuffers/Textures` + 重建，不做原地 resize。
- 每帧开头由库执行 `glBindFramebuffer` + `glViewport(0,0,w,h)`，**但不 `glClear`**。

**回读**：`FrameReader`

1. `bitmap.peekPixels()!!.addr` 拿到 Skia 位图的原始内存地址（`NativePointer` / `Long`）。
2. `MemoryUtil.memByteBuffer(addr, width * height * 4)` 包成 `ByteBuffer`。
3. `glReadPixels(0, 0, w, h, GL_RGBA, GL_UNSIGNED_BYTE, buffer)` —— **像素直接落进 Skia 位图，零 CPU 拷贝**。
4. 翻转留到绘制时做（见 7.3），不在回读侧做行倒序。

> `Bitmap.installPixels()` 只有 `ByteArray` 重载，走那条路每帧会多一次 8MB memcpy，不要用。

**发布**：`ComposeImageHost`

- `PublishedFrame` 持有 `ImageBitmap`，用 `AtomicReference` 交换。
- Compose 侧在 `Modifier.drawBehind { }` 里读取该引用并 `drawImage`。**读取必须发生在 draw 阶段**，这样只触发重绘、不触发重组。
- 帧发布后用 `ComposeImageHost.invalidate()` 触发重绘。

**两个候选方案（M2 定案）**

| | 方案 A（基线） | 方案 B（目标） |
|---|---|---|
| 做法 | 每帧 `Image.makeFromBitmap(bitmap)` 生成不可变 Image | 双缓冲 Skia `Bitmap` + `asComposeImageBitmap()` 包装 + `notifyPixelsChanged()` |
| CPU 拷贝 | 每帧一次（bitmap → image） | 0 |
| 风险 | 无 | 撕裂（Skia 绘制期间位图被改写）、纹理缓存是否随 generation 失效 |
| 结论 | M1 用 A 保证正确 | M2 实测 B，通过则切换 |

### 6.2 安卓实现

安卓端有两条承载路径，**共享 EGL 上下文代码与帧循环骨架**，只在这一帧怎么呈现上分叉。
模式在 `PlatformGLCanvas.android` 首次组合时读一次 `ComposeGL.androidSurfaceMode` 并 `remember`。

**EGL**：`EglCore` + 两个 Surface 类

- `EglCore` 只建 display / config / context；`EGL_SURFACE_TYPE` 由调用方传入（pbuffer 或 window）。
- 离屏模式用 `EglPbufferSurface`：1x1 pbuffer，仅为了让 EGL 有 current surface，渲染目标是 FBO。
- 零拷贝模式用 `EglWindowSurface`：包住 TextureView 的 SurfaceTexture，每帧 `eglSwapBuffers`。
- `eglCreateContext` 要求 `majorVersion = 3`（实际 minor 由设备决定，`GLInfo` 如实上报）。

**离屏模式的 FBO 与回读**：`GlesFramebuffer` + `AndroidFrameReader`

- 颜色附件 `GL_RGBA` 纹理；深度/模板按 `GLConfig` 挂 renderbuffer。
- 尺寸变化时整组删除重建，不做原地 resize。
- 每帧 `beforeDraw` 里 `glBindFramebuffer` + `glViewport`，**但不 `glClear`**。
- 回读复用一块直接 `ByteBuffer`，`Bitmap.copyPixelsFromBuffer` 落到 `ARGB_8888` + `setPremultiplied(true)`。
- 安卓没有公开的"取 Bitmap 底层内存地址"API，**这一次 CPU 拷贝省不掉**（与桌面不同）。

**线程**：`FrameLoopThread`（骨架）+ `OffscreenRenderThread` / `WindowRenderThread`（子类）

- 骨架负责：帧请求合并与等待、尺寸分发、帧号与耗时统计、`onStart/onResize/beforeDraw/present/onStop` 五段式扩展点。
- 子类只实现各自的上下文与呈现方式。
- `OnRequest` 模式阻塞等待；`Continuous` 模式按 `maxFps` 定时唤醒；停止走 `join(1500)`。

### 6.3 尺寸与 DPI

- 尺寸来源：`Modifier` 布局后由 Compose 给出 `IntSize`（已含 density 换算）。
- render target 尺寸 = `floor(size.width * resolutionScale)`，安卓另需 `surfaceTexture.setDefaultBufferSize`。
- `scope.density` 透传 Compose 的 `LocalDensity`，供使用方做 dp → px。
- Android 屏幕旋转、桌面窗口缩放都走同一条 `onSurfaceResized` 路径。

### 6.4 生命周期

| 事件 | 桌面 | 安卓 |
|---|---|---|
| 进入组合 | 建线程 + 上下文 + FBO | 建线程 + 离屏 EGL + FBO |
| 离开组合 | 停线程 + 销毁上下文 | 停线程 + 销毁 EGL |
| 不可见 | 暂停转帧，保留资源 | 同左 |
| `enabled = false` | 完全释放，恢复时重建 | 同左 |

### 6.5 帧调度

`FrameScheduler` 只做三件事：

1. **请求合并**：一帧渲染期间收到的多个 `requestRender()` 折叠成一个。
2. **节流**：`Continuous` 模式下按 `maxFps` 或显示器刷新率限速。
3. **可见性门控**：不可见 / 尺寸为 0 时丢弃请求并计入 `droppedFrames`。

---

## 7. 实现细节与已知陷阱

### 7.1 Skiko API 事实（已实测，别再猜）

| 事实 | 影响 |
|---|---|
| `Image` 没有 `makeFromTexture` | zero-copy 不可行 |
| `Bitmap.installPixels` 只收 `ByteArray` | 不要走这条路，会多一次 memcpy |
| `Bitmap.peekPixels().addr` 是原始内存地址 | 回读直写 Skia 位图的关键 |
| `Canvas` 不暴露 surface/context | 无法从绘制调用反推 Skia 的 DirectContext |
| `Surface._context` 是 `internal` | 反射能拿，但拿到也无用（见 3.6） |
| `DirectContext` 是 C++ 对象的指针壳 | 反射解决不了"没有目标 API"的问题 |

### 7.2 预乘 alpha

见 3.5。**这是透明正确性的第一杀手**。验收标准：半透明清屏压在白色背景上，边缘无黑边、无灰边；压在图片上颜色与预期一致。

### 7.3 Y 轴翻转

GL 的原点在左下，Skia 在左上。**不要在回读侧做行倒序**（那需要逐行 256 次 `glReadPixels` 或一次全量 memcpy 翻转，都会毁掉 7.1 的零拷贝收益）。正确做法：绘制时用垂直翻转的变换。

```kotlin
Modifier.drawBehind {
    withTransform({ scale(1f, -1f, pivot = center) }) {
        drawImage(image, dstSize = IntSize(width, height))
    }
}
```

（具体写法在 M1 调整，原则是翻转发生在绘制阶段。）

### 7.4 只重绘不重组

Compose 的 `mutableStateOf` 在**组合阶段**读取会触发重组，在**绘制阶段**读取只触发重绘。高频场景下重组会把 CPU 吃光。

- 错误：`val frame by state; Image(frame)` —— 每帧重组
- 正确：`Modifier.drawBehind { drawImage(state.value) }` —— 只重绘

### 7.5 `AndroidView` 会吞掉触摸（已实测定论，别再试）

**现象**：把 `GLCanvas` 放在某个 Compose 组件的上层（z 序），该组件在安卓上点不到；放在下层则正常。桌面没有这个问题。

**根因**（读 `androidx.compose.ui:ui-android` 源码确认）：

```kotlin
// androidx.compose.ui.platform.AndroidViewsHandler
// Touch events forwarding will be handled by component nodes.
override fun dispatchTouchEvent(ev: MotionEvent?) = true
```

`AndroidView` 的宿主容器 `AndroidViewsHandler` 把 `dispatchTouchEvent` 硬编码为 `true`，**其范围内的一切触摸都被无条件消费**。Compose 层没有任何开关可以关闭。

**排除过程**（别再重复）：

1. `TextureView` 设 `isClickable = false` + 覆盖 `onTouchEvent` 返回 `false` —— **无效**。
2. 把 `TextureView` 换成任意普通 `android.view.View` —— **结果完全相同**，说明与具体 View 无关。

**结论**：任何通过 `AndroidView` 承载的图层，都无法做到"下层 Compose 可交互"。
因此安卓端默认走离屏 EGL + 回读；`TextureView` 作为可选的零拷贝快速路径保留（见 ADR-2），
用它的前提是"不需要 GL 层下方的交互"。

**为什么反射救不了**（别再花时间）：

| 想法 | 为什么不行 |
|---|---|
| 用反射改掉 `dispatchTouchEvent` 的返回值 | 反射能读写**字段**、调用**方法**，但改不了**方法体**。`return true` 是编译进字节码的指令，没有任何 JVM/Kotlin 反射 API 能改它 |
| 反射把 `AndroidViewsHandler` 的实例换成一个子类 | ① `AndroidViewsHandler` 是 Kotlin `internal class`，默认 **final**，不能继承；② 就算能，还要替换 `AndroidComposeView._androidViewsHandler` 私有字段并迁移 `holderToLayoutNode` / `layoutNodeToHolder` 两张映射表，任何一步出错都是崩溃 |
| 反射给 TextureView 挂 `OnTouchListener` 让它"不消费" | 无用。实验已证明换成任意普通 `View` 结果相同，消费发生在**容器**而不是被承载的 View |
| 在 Compose 父容器上挂 `pointerInput` 接管 | 触摸在到达 Compose 命中测试之前就被 `AndroidView` 吃掉了，挂在哪层都收不到 |

唯一能真正改掉的方法是**编译期字节码改写**（Gradle + ASM 对 `androidx.compose.ui` 打补丁），
或者运行期 Java Agent（ByteBuddy 重定义类）。两者都会让库强依赖特定 Compose 版本的内部实现，
一次 Compose 升级就断，与"代码复制即用"的交付方式彻底冲突。**本项目不做。**

> 如果哪天真的需要"零拷贝 + 下层可交互"同时成立，正确方向不是打补丁，而是等 Compose 官方提供
> 不消费触摸的互操作容器（或自己写一个不走 `AndroidView` 的宿主）。

### 7.6 版本锁死

Compose 1.11.1、Kotlin 2.4.0、AGP 9.0.0、compileSdk 36。**不要升级任何版本**。新增依赖只能是 LWJGL。

### 7.7 GL 版本回退链

桌面：`4.6 core` → `4.3 core` → `3.3 core`。全部失败则走 `fallbackContent`，并通过 `onError` 报告。

安卓：`GLES 3.2` → `3.1` → `3.0`。全部失败同上。

`GLInfo` 必须报告**实际**拿到的版本，不能报告请求的版本。

---

## 8. 里程碑与验收

每个里程碑一个 feature 分支，验收通过后更新文档 + 提交，禁止直接提交 main。

### M0 骨架

- `git init` + `main` + `feature/composegl-skeleton`
- 把 `shared` 改造为 `composegl`，按 4.2 建立空文件（只有签名和 TODO）
- 两个 demo 能编译、能跑，`GLCanvas` 显示 `fallbackContent`

**验收**：`./gradlew :desktopApp:run` 起窗口；`./gradlew :androidApp:assembleDebug` 出包并装进模拟器不崩。

### M1 清屏闭环（核心里程碑）

- 桌面：GLFW 隐藏窗口 + FBO + 回读 + `Image` 发布
- 安卓：TextureView + EGL + swap
- 双向透明验收

**验收**（缺一不可）：
1. 两端都能 `glClearColor(0.2f, 0.4f, 0.8f, 0.5f)` + `glClear`，画面呈半透明蓝
2. 半透明区域压在图片/文字上，颜色正确、无黑边
3. 上下各放一个 `Text`，两者都可见
4. 拖动窗口缩放，画面跟着变且不变形
5. 上下层的按钮都能点击（未拦截事件）

### M2 性能与发布机制（已完成，数据见第 13 章）

- 方案 A / 方案 B 对比实测，定案 → **方案 B（`Image.makeFromPixmap`）**，`present` 2.36 → 0.09ms
- PBO 异步回读 → **判定不做**：回读耗时与像素数线性相关，是带宽成本不是同步阻塞
- `FrameStats` 落地，demo 里显示帧耗时 → 完成（HUD + 连续渲染/填满开关）
- 顺带修掉两个真问题：`Continuous` 帧节奏算错（45 → 58.5fps）；尺寸变化时旧位图被提前释放
- 桌面后端 D3D12 vs OpenGL 对照 → **已做**（13.11）：总帧时 3.84 vs 4.82ms，差异在噪声量级，
  确认不需要为性能切后端。

**验收结果**

| 验收项 | 结果 |
|---|---|
| 1080p 回读+发布 ≤ 4ms | ❌ **10.26ms**。回读是带宽成本（0.68 GB/s），不是实现缺陷。缓解手段：`resolutionScale` 或安卓零拷贝模式。原目标定得过于乐观，已按实测修订（13.4） |
| 无每帧堆分配 | ✅ 帧循环里已清零（13.11）。剩余 per-frame 对象仅 `FrameStats` 快照与 `ImageBitmap` 包装 |
| Compose 侧重组次数为 0 | ✅ 2190 帧内重组 2 次，连续渲染期间 0 次（13.11） |
| 方案 A/B 定案 | ✅ 方案 B（13.3） |
| PBO | ✅ 判定不做（13.4） |

### M3 打磨（已完成，记录见第 14 章）

- `enabled`、可见性门控、错误降级全覆盖 → 完成（`enabled=false` 走完整销毁；安卓按生命周期暂停）
- `resolutionScale`、MSAA 路径 → 完成（MSAA 三处路径全覆盖，见 14.1）
- 文档补全（本文件 + `README.md` 集成指南）→ 完成

### 附：demo 内容

- `desktopApp`：一个盒子，中间嵌 `GLCanvas`，上下各有 `Text`，用 LWJGL 直接写 GL 命令
- `androidApp`：同一套 UI，用 `GLES32` 写 GL 命令
- **两份 GL 代码是故意不同的**——它们证明"库只接线，不抽象 GL"

---

## 9. 决策记录

| 编号 | 决策 | 备选 | 理由 |
|---|---|---|---|
| ADR-1 | 桌面走离屏 FBO + 回读 | SwingPanel 嵌 heavyweight / 共享 Skiko context | 前者的双向透明与裁剪不可能实现；后者的 zero-copy 已被 3.6 证伪 |
| ADR-2 | 安卓提供**双模式**，默认「离屏 EGL + 回读」，`TextureView` 作为可选零拷贝路径 | ① 只用 `TextureView` ② 只用离屏回读 ③ `SurfaceView` + ZOrderOnTop ④ `AndroidExternalSurface` | ① 唯一缺陷是下层不可点击（`AndroidView` 吞触摸，7.5），但零拷贝的性能优势明显；② 正确但每帧多两次传输；③④ 是硬件图层，只能压在最上层，连"上层覆盖"都做不到。取 ①+② 的并集，用启动开关切换，让使用方按场景取舍 |
| ADR-3 | **不做** `expect object GL` 式的全量绑定；改为顶层 `expect fun` 的最小转发面（GL 4.6 core ∩ GLES 3.2） | karmakrafts 式的 `expect` 全量绑定；完全不做门面 | 全量绑定是 karmakrafts 烂尾的直接原因（跨 6 平台、大量 TODO）。顶层函数的转发成本为零、没有对象包装、`import` 后直接用，且可以按需增量补齐。使用方需要在两端写同一份渲染代码时才有价值，所以保持"可选" |
| ADR-4 | 不发布 Maven，代码复制交付 | publishToMavenLocal / Maven Central | 使用方只需要自己用；复制式交付要求模块少、无构建魔法 |
| ADR-5 | 不强制 Skia 后端 | karmakrafts 的强制切后端 | 切了也没收益（3.6），还会拖累整个窗口的 UI 渲染 |
| ADR-6 | 桌面依赖 LWJGL + 隐藏 GLFW 窗口 | 由使用方提供上下文 / JOGL | 无上下文就没有 GL；GLFW 隐藏窗口是最轻的自建方案，3MB 可接受 |
| ADR-7 | 单模块、两包 | 多模块拆分 | 代码复制场景下，复制一个文件夹远优于复制多个模块 |

---

## 10. 风险登记

| 编号 | 风险 | 影响 | 应对 |
|---|---|---|---|
| R-1 | ~~安卓上 Compose 内容盖不住 `TextureView`~~ **已关闭**：绘制层级本身没问题（Compose 可以画在互操作视图之上），真正的问题是 `AndroidView` 吞触摸。已通过放弃 `AndroidView` 解决（ADR-2），双端实测通过 | 双向层级 + 下层可交互 | 已解决，见 7.5 |
| R-2 | 桌面回读在部分驱动上同步阻塞明显 | 帧率下降 | M2 上 PBO；实测再决定 |
| R-2b | 安卓改离屏后每帧多一次 GPU→CPU 回读 + 一次 Bitmap 拷贝 | 帧率与功耗上升 | M2 实测；若成为瓶颈，再评估"仅在不需要下层交互时启用 TextureView 快速路径" |
| R-3 | 模拟器（x86_64）与真机 GL 行为差异 | 验收通过但真机异常 | 关键节点请使用方在真机复验 |
| R-4 | 隐藏 GLFW 窗口与使用方自己的 GLFW 冲突 | 上下文异常 | `glfwInit` 引用计数保护；文档注明 |
| R-6 | 大画布（≈1080p）+ 连续渲染会触发显卡驱动 TDR，导致整机显示重置、其它 D3D 应用一起崩 | 严重的整机影响 | 见 13.6。避免大画布长时间连续渲染；建议 `OnRequest` + `resolutionScale`；压测放到独立环境 |
| R-5 | 未来 Compose 版本升级改内部实现 | 库失效 | 版本锁死；升级时重新验证 7.1 的 API 事实 |

---

## 11. 术语

| 术语 | 含义 |
|---|---|
| render target | 实际被渲染的目标，桌面是 FBO，安卓是 EGLSurface |
| 回读（readback） | 把 GPU 显存里的像素搬回 CPU 内存 |
| 预乘 alpha | 颜色分量已乘以 alpha，透明合成的正确表示 |
| 双向层级 | GLCanvas 既能在 Compose 内容之上，也能在其之下 |
| OnRequest | 帧由使用方显式请求，不请求就不渲染 |
| fallbackContent | GL 不可用时渲染的替代内容 |

---

## 12. M1 实装记录

状态：**已通过双端验收**（2026-10-28）。

### 12.1 验收结果

| 项 | 桌面（Windows） | 安卓（MuMu / Android 12 / API 32） |
|---|---|---|
| 上下文 | GL 4.6 core · Intel(R) UHD Graphics | GLES 3.2 · Adreno (TM) 640 |
| 半透明清屏 | ✅ 半透明蓝压在文字上 | ✅ 同上 |
| 下方 Compose 透出 | ✅ | ✅ |
| 上方 Compose 覆盖 GL | ✅ | ✅ |
| **下方 Compose 可点击** | ✅ | ✅ （M1.5 修复，见 12.5） |
| 事件不拦截 | ✅ 上下按钮均可点击 | ✅ 同上 |
| 圆角裁剪 | ✅ | ✅ |

截图存于 `build/shots/`（`desktop_m1*.png`、`android_m1*.png`、`touch_fixed.png`）。

### 12.2 相对设计的偏差（均为已决定延后，不是遗漏）

| 项 | 现状 | 计划 |
|---|---|---|
| `gl` 门面 | **已完整化**：125 个顶层 `expect fun` + 完整 `GLConst` + `glCheckError`，覆盖实例化 2D 渲染 | 完成，见第 17 章 |
| `GLConfig.msaa` | 字段存在但未生效 | M3 |
| `GLConfig.resolutionScale` | **已实现**（桌面 + 安卓离屏；TextureView 模式忽略），见 13.5 | 完成 |
| `GLConfig.androidGlesVersion` | 未使用：EGL 只请求 major 3，实际版本由设备决定 | 需要精确 minor 时再补 `EGL_CONTEXT_MINOR_VERSION_KHR` |
| 桌面回读 | 同步 `glReadPixels` | **定案不做 PBO**：实测为带宽瓶颈而非同步阻塞，见 13.4 | 关闭 |
| 位图→Image | **已改方案 B**：`Image.makeFromPixmap` 共享像素 | 完成，见 13.3 |

### 12.3 结构上的两处落地调整

1. **`PlatformGLCanvas` 的 actual 文件放在 `internal/` 下**（`PlatformGLCanvas.jvm.kt` / `.android.kt`），而不是 `internal/desktop/`。原因：Kotlin 要求 actual 与 expect 同包，而平台实现内部仍按 `desktop/`、`android/` 子包组织。
2. **回读的 Y 轴翻转在 Compose 绘制阶段做**（`withTransform { scale(1f, -1f, center) }`），不在回读侧做行倒序——否则会破坏零拷贝。

### 12.4 调试工具（不属于库）

`tools/` 下有两个临时脚本，仅用于无 GUI 交互时的视觉走查，**不要复制进使用方的项目**：

- `capture.py`：用 GDI BitBlt 截全屏并编码 PNG（本机没有 PowerShell 权限时的替代方案）
- `click.py`：`SetCursorPos` + `mouse_event` 模拟点击

安卓侧走查用 `adb install / am start / input tap / exec-out screencap`，无需额外工具。

### 12.5 M1.5：安卓触摸修复（ADR-2 反转）

**问题**：把 Compose 组件放在 `GLCanvas` 之下（z 序）时，桌面上点得到，安卓上点不到；放在之上的点得到。

**定位过程**

1. 复现：上下各一个按钮，底部按钮（z 序在上）计入点击数，顶部按钮（z 序在下）始终为 0。
2. 对照实验：把 `GLTextureView` 换成任意 `android.view.View`，**现象完全一致** → 与 `TextureView` 无关。
3. 读 `androidx.compose.ui:ui-android` 源码，在 `AndroidViewsHandler` 找到根因：
   `override fun dispatchTouchEvent(ev: MotionEvent?) = true` —— 无条件消费。

**修复**：安卓端彻底放弃 `AndroidView`，改为与桌面同构的「离屏 EGL（1x1 pbuffer）+ FBO + 回读 → Bitmap → ImageBitmap」，
用普通 Compose `Image` 承载。层级与命中测试完全回到 Compose 自己的体系里。

**代价**：每帧多一次 GPU→CPU 回读与一次 `copyPixelsFromBuffer`（安卓 Bitmap 无公开底层地址 API）。
这是可交互性换来的，已登记为 R-2b。

**验证**：修复后两个按钮各计数 1 次，GL 半透明内容与上下层文字均正常（`build/shots/touch_fixed.png`）。

### 12.6 M1.6：恢复零拷贝路径，改为双模式开关

**背景**：M1.5 为了可交互性砍掉了 `TextureView`，代价是安卓每帧多两次传输。
使用方判断"下层不可点"可以用挂 handler 等方式自行规避，而回读开销是硬成本，因此要求两种模式共存。

**改法**

- 新增 `ComposeGL.androidSurfaceMode`（启动期一次性开关）与 `AndroidSurfaceMode` 枚举。
- `EglCore` 拆成"只管上下文"，surface 交给 `EglPbufferSurface` / `EglWindowSurface`。
- 抽出 `FrameLoopThread` 骨架，两个子类只实现上下文与呈现方式。
- `PlatformGLCanvas.android` 按模式分派：`ComposeImage` 走 `Box + drawBehind`，`TextureView` 走 `AndroidView`。
- 默认 `ComposeImage`（正确优先），零拷贝需显式开启。

**验证**（MuMu / Android 12 / GLES 3.2 / Adreno 640）

| 模式 | GL 画面 | 下方按钮（z 序在下） | 上方按钮（z 序在上） |
|---|---|---|---|
| `ComposeImage` | ✅ | ✅ 计数 1 | ✅ 计数 1 |
| `TextureView` | ✅ | ❌ 计数 0（预期） | ✅ 计数 1 |

截图：`build/shots/mode_composeimage.png`、`build/shots/mode_textureview.png`。

**反思结论**：`dispatchTouchEvent = true` 是方法体，反射改不了（详见 7.5 的表），
所以这里只能做"二选一"，不存在"零拷贝 + 下层可交互"的第三条路。

---

## 13. M2 实测结论

环境：Windows 10 / i5-12450H / Intel UHD Graphics / JDK 21 / Compose 1.11.1。
画布尺寸指 render target 实际像素，数据取自 demo 的帧统计 HUD（每 30 帧采样一次）。

### 13.1 数据

| 轮次 | 画布 | draw | readback | present | total | fps |
|---|---|---|---|---|---|---|
| 方案 A + 旧帧节奏 | 943 × 514 | 0.00 | 3.32 | 2.36 | 5.68 | **44.9** |
| 方案 A + 新帧节奏 | 943 × 514 | 0.00 | 3.11 | 1.70 | 4.82 | **58.5** |
| 方案 B（makeFromPixmap） | 1863 × 924 | 0.00 | **10.17** | **0.09** | 10.27 | 58.0 |

### 13.2 结论一：`Continuous` 的帧节奏原来是错的（已修）

旧实现是"每帧 `wait(16ms)` 之后再干活"，于是实际周期 = 16.7 + 耗时 ≈ 21.7ms → 45fps。
改为以**本帧起始时刻**推进的 deadline（`nextDeadline = frameStart + period`），耗时不再叠加到周期上，
恢复 58.5fps（余下 1.5 是调度抖动）。

影响面：桌面 `DesktopRenderSurface` 与安卓 `FrameLoopThread` 各一份，两处都改了。

### 13.3 结论二：发布路径定案为 `Image.makeFromPixmap`

`present` 从 2.36ms 降到 0.09ms（大画布）/ 1.70ms（小画布）。

原因：`Image.makeFromBitmap`（以及 Compose 桌面端 `SkiaBackedCanvas.drawImageRect` 内部对
Bitmap 型 ImageBitmap 的调用）会**整帧拷贝**并新建一个原生 Skia Image。
改用 `Image.makeFromPixmap` 后像素被共享，不再拷贝。

同时修掉一个此前没发现的缺陷：`SkiaFrameBuffer` 在尺寸变化时立即 `close()` 旧位图，
而共享像素的 Image 可能仍被 Compose 持有 → 指向已释放内存。现在旧位图进入 retired 列表，
统一在 `dispose()` 释放。

### 13.4 结论三：**不做 PBO**（按 M2 的预设条件判定）

回读耗时与像素数**线性相关**：

| 画布 | 像素 | readback | 等效带宽 |
|---|---|---|---|
| 943 × 514 | 0.48 MP | 3.11 ms | ~0.62 GB/s |
| 1863 × 924 | 1.72 MP | 10.17 ms | ~0.68 GB/s |

两点近似常数带宽，说明 10ms 是**拷贝本身**的成本（GPU→CPU + 驱动路径），
不是"等 GPU 画完"的同步阻塞。PBO 只能把阻塞藏起来，改不了要搬多少字节。
所以 PBO 不做，M2 预设的"仅在实测证明是同步瓶颈时才做"条件不成立。

**这也定量解释了 TextureView 模式的价值**：1080p 下回读本身就要 ~10ms，
已经吃掉 60fps 预算的 60%。只要场景允许"GL 层下方不可交互"，就应该切到零拷贝模式。

### 13.5 结论四：`resolutionScale` 已实现

桌面与安卓离屏模式现在都按 `GLConfig.resolutionScale` 决定 render target 尺寸，
显示时由 Compose 拉伸。回读开销随像素数线性下降，0.5x 约为原来的 1/4。
（安卓 `TextureView` 模式忽略该字段——缓冲尺寸由系统按视图管理。）

### 13.6 新增风险：大画布连续渲染会触发显卡驱动 TDR

**现象**：把窗口拉到 1920 × 1160、开启填满画布 + 连续渲染后，约 1~2 分钟内进程崩溃，
屏幕黑屏一次，**同机其它使用 D3D 的应用（含编辑器本体）一并崩溃**。

**已确认的事实**：崩溃日志显示原生内存分配失败（`malloc` 1MB 失败），
但触发点是驱动层的显示重置（TDR），内存失败是表象。

**尚未定位的部分**：不确定是大尺寸 FBO 连续回读、还是"GL 上下文与 Skia 的 D3D12 合成并存"
让 Intel UHD 驱动超时。

**规避**：

- 不要把大画布 + 连续渲染当压测手段，尤其不要在用户正在使用的机器上跑；
- 产品侧建议 `OnRequest` + 必要的 `resolutionScale`；
- 真要压测，请在独立机器或虚拟机上做，并盯着显存与温度。

已登记为 R-6。


### 13.7 连续渲染的原生内存增长（已修）

**现象**：桌面端开连续渲染，进程 RSS 以 **~66MB/s** 线性增长。
实测 24.5 秒从 918MB 涨到 2553MB，且**不是 JVM 堆**（`jcmd GC.heap_info` 里堆很小）。
涨到几个 GB 后 Skia 抛 `std::bad_alloc` 越过 JNI 打死 JVM；在大画布上还会拖垮显卡驱动（见 13.6）。

**定位过程**（两次隔离实验，别再重复）：

| 实验 | 结果 | 结论 |
|---|---|---|
| 保留回读，跳过 `host.publish` | 仍 ~75MB/s | 与 Compose 绘制/上传无关 |
| 保留回读，跳过每帧 `Image.makeRaster` | **368.9 → 370.8MB，完全持平** | 元凶是"每帧新建 Skia Image" |

另一个关键证据：`present` 只有 **0.05ms**。1MB 级的 memcpy 不可能是这个耗时，
说明像素拷贝**不发生在回读链路里**，而是每帧的新 Image 被 Compose 当纹理上传，占的是原生内存。

**三条走不通的路（都已实测，别重走）**：

1. `Image.makeFromBitmap` / `Bitmap.asComposeImageBitmap` —— Compose 桌面端
   `SkiaBackedCanvas.drawImageRect` 内部**就是**调用 `Image.makeFromBitmap`，对可变位图整帧拷贝。
2. `Image.makeFromPixmap` —— 看似共享像素，但 Skiko 传的 `releaseProc` 是 null，
   Skia 无法确认内存寿命，**仍然整帧拷贝**。实测同样是 66MB/s。
3. `Image.makeRaster(info, data, rowBytes)` + `Data.makeWithoutCopy` —— 像素引用确实零拷贝，
   但每帧仍会新建 SkImage，Compose 会把新 Image 再上传一次纹理，纹理缓存持续增长。

根因是结构性的：**桌面端 Skia 跑在 D3D12 上，我们的像素在 OpenGL 里，跨 API 必然有一次传输**，
且这次传输占用的是 JVM 看不见的原生内存 → GC 永远不会因为堆压力而自然触发。

**采用的修法：预算制主动回收**

`GLConfig.nativeCopyBudgetMb`（默认 64MB）。渲染线程按 `width × height × 4` 累计已发布字节，
超过预算就 `System.gc()` 一次并清零。这把峰值原生内存钳在预算量级，代价是定期一次回收。

实测效果（943 × 320 画布，连续渲染）：

| 指标 | 修前 | 修后 |
|---|---|---|
| RSS 趋势 | 线性增长，24.5s 涨 1.6GB | **459 ~ 632MB 锯齿波动**，45s 无累积 |
| fps | 58.5（在涨到上限前） | 57.0 |
| present | 0.05ms | 0.05ms |

> 这是权衡不是最优解：真正零拷贝需要 Skia 与 GL 共享纹理，
> 而桌面端 Skia 后端是 D3D12，做不到（3.6 已证）。想彻底摆脱这笔开销，只有安卓的 `TextureView` 模式。


### 13.8 resize 泄漏与帧策略热切换（两个 bug，已修）

**bug 1：拖拽窗口内存线性增长、回不到基线**

`SkiaFrameBuffer` 在尺寸变化时把旧位图塞进 `retired` 列表并**永久保留**，本意是防止
"共享像素的 Image 还活着、位图被提前 close"。但拖拽会连续触发几十上百次 resize，
每次丢两块整帧位图，于是内存一路上涨且永不回落。

修法：**保留最近 2 代，更早的直接 close**。2 代 ≈ 2 帧，足够让在途的 Image 画完；
同时把丢弃量报给 13.7 的内存预算，计入主动回收。

实测（943×514 画布 + 连续渲染，用"填满画布"开关反复触发 resize）：

| 切换次数 | RSS |
|---|---|
| 0（基线） | 624MB |
| 30 | 722 ~ 787MB |
| 60 | 755 ~ 762MB，**不再随次数增长** |

**bug 2：切"连续渲染"后画面不动**

`framePolicy` 原本是 `GLCanvas` 里 `remember` 的 key 之一，改策略会重建 surface。
桌面端表现为重建线程；安卓 `TextureView` 模式下更严重 —— `AndroidView` 的 factory 只跑一次，
新 surface 永远拿不到 listener，等于根本没起线程，画面停在最后一帧。

两处修：
1. `framePolicy` 从 `remember` key 里拿出来，改成 surface 上的可变字段，运行中热切换；
2. **只改字段不够** —— 线程可能正阻塞在 `lock.wait()` 上，必须顺带 `requestRender()` 唤醒它，
   否则表现为"开关打开了但一帧都不出"。

实测：桌面 `#1650 943x514 · 57.6 fps`（关→开可重复）；安卓 `#450 1860x914 draw 0.08
rb 0.0 present 1.42 total 1.5ms · 58.7 fps`。

### 13.9 安卓 TextureView 模式稳定性（60 秒连续渲染）

| 指标 | 结果 |
|---|---|
| PSS | 123 ~ 131 MB（锯齿，无累积） |
| RSS | 188 ~ 195 MB |
| 帧率 | 58.7 fps |
| readback | **0.00 ms**（零拷贝生效） |
| present | 1.42 ms（`eglSwapBuffers`） |
| 进程 | 存活，无异常日志 |

这正是 13.4 结论的正面印证：同一台设备上，`TextureView` 模式的回读成本是 0，
而 `ComposeImage` 模式要按 0.68 GB/s 搬像素。


### 13.10 连续渲染被钉在 60fps（已修）

**现象**：120Hz 安卓设备上，`Continuous` 模式只有 ~60fps。

**根因**：`periodNanos()` 在 `maxFps = 0` 时直接返回硬编码的 `NANOS_60HZ`。
文档写的是"跟随显示器刷新率"，但从来没实现过 —— 属于文档先行、实现没跟上。

**修法**

- 新增 `@Composable expect fun rememberDisplayRefreshRateHz()`：
  - 桌面：AWT `defaultScreenDevice.displayMode.refreshRate`。
  - 安卓：取 `display.supportedModes` 里的**最高刷新率**，而不是 `display.refreshRate`。
    后者返回当前模式，120Hz 屏在自适应/省电策略下空闲态常常仍报 60，
    会让"能跑多快跑多快"的语义失效。实际出帧节奏仍被 SurfaceFlinger 的缓冲队列限流到屏幕真实率。
- `GLRenderScope` 新增 `displayRefreshRateHz`，demo HUD 会显示 `屏 XXHz`，便于使用方自查。

**已验证**（本机只有 60Hz 环境）：

| 环境 | HUD | 结果 |
|---|---|---|
| Windows + 60Hz 显示器 | `屏 60Hz · 57.9 fps` | 正常 |
| MuMu 模拟器（仅 60Hz 模式） | `屏 60Hz · 57.6 fps` | 正常 |

**120Hz 未验证**：本机没有 120Hz 设备，需要在使用方的手机上复验。

> 如果手机上一路显示 `屏 120Hz` 但 fps 仍约 60，说明瓶颈不在本库的帧调度，而在系统侧
> —— 需要 App 主动申请高刷（`Window.setFrameRate(120f)` 或 `preferredDisplayModeId`），
> 这属于 Activity 层的事，库不该替使用方改窗口属性。


### 13.11 收尾：后端对照、重组次数、帧循环分配

**后端对照（D3D12 vs OpenGL）**，同一台机器、943×513 画布、连续渲染：

| 后端 | draw | readback | present | total | fps |
|---|---|---|---|---|---|
| D3D12（默认） | 0.01 | 3.77 | 0.05 | **3.84 ms** | 57.8 |
| OpenGL | 0.01 | 4.68 | 0.12 | 4.82 ms | 58.3 |

差异在噪声量级（总帧时差 ~1ms，帧率一样）。OpenGL 后端下回读略慢，推测是 Skia 与我们
抢同一个 GL 驱动。**结论与 3.6 一致：不需要为了性能切后端。**

**Compose 侧重组次数 = 0**：HUD 里那个"重组 N 次"计数器统计的是 `App()` 自身的重组。
`Continuous` 跑满 2190 帧后计数为 **2**（初始组合 1 次 + 打开开关 1 次），
连续渲染期间**一次都没有**。机制上是因为发布帧只写 `mutableStateOf`，
而这个 state 只在 `drawBehind` 里读取 → 只触发重绘，不进入重组。

> 为了让这个数字有意义，demo 里把 HUD 拆成了独立的 `HudText` 组合，让它自己订阅 state，
> 否则 HUD 每 0.5 秒更新一次就会带着整个 `App` 重组，计数器会一直被污染。

**帧循环内不再有堆分配**：`awaitFrame()` 原本每帧 `intArrayOf(w, h)`，已改为复用字段。
现在每帧还剩的对象只有两个，都是设计上要的：
`FrameStats`（对外快照，值语义）和 `ImageBitmap` 包装（几十字节）。
真正的大头是 13.7 说的那次原生拷贝，已经单独处理。


---

## 14. M3 实装记录

### 14.1 MSAA（三条路径全覆盖）

| 路径 | 做法 |
|---|---|
| 桌面 FBO | 颜色/深度挂多重采样 renderbuffer，每帧末尾 `glBlitFramebuffer` 到单采样纹理再回读 |
| 安卓离屏（`ComposeImage`） | 同上，用 `glRenderbufferStorageMultisample` + `glBlitFramebuffer` |
| 安卓窗口（`TextureView`） | 窗口表面本身不支持多重采样，先渲染进 MSAA FBO，再 resolve 并 blit 到默认帧缓冲 |

- 采样数按 `GL_MAX_SAMPLES` 收敛，驱动给不了就降级，**多重采样 FBO 不完整时自动退回单采样**，不会直接报错。
- 验证方法：统计画面区域内的**唯一颜色数**。硬边缘只有「前景色 + 背景色」两种；
  开 MSAA 后边缘会出现混合色。

| 环境 | 关 | 开 |
|---|---|---|
| 桌面 943×513 | 79 色 | 132 色，出现混合色 `(207,204,141)` |
| 实体机 Mali-G925 | 2 色 | **5 色**（2 主色 + 3 个混合层，正好是 4x 应产生的形态） |

代价（实体机，1188×2116）：`present` 0.6ms → **2.1ms**（多一次 resolve + blit），
fps 140.9 → 139.0。窗口路径开 MSAA 时不再是严格零拷贝，但多的那次是 GPU 内部搬运。

### 14.2 `config` 变更后 TextureView 不再刷新（bug，已修）

和 13.8 的 `framePolicy` 是**同源问题**：`config` 变化会让 `remember` 重建 surface，
而 `AndroidView` 的 factory 只跑一次，新 surface 拿不到 `SurfaceTextureListener`，等于没起线程。

修法：`attach()` 做成幂等；`AndroidView` 增加 `update` 回调重新接管；若此时 `SurfaceTexture`
已就绪（不会再有 `onSurfaceTextureAvailable`），主动起线程。

验证：开 MSAA 后帧号从 #570 继续增长，未卡死。

### 14.3 `androidGlesVersion` 生效

EGL 按配置请求 `EGL_CONTEXT_MAJOR/MINOR_VERSION_KHR`，失败逐级回退 3.2 → 3.1 → 3.0，
每一档再试一次不带 minor 的老写法（少数驱动不认 minor 属性）。实际版本仍以 `GLInfo` 上报为准。

### 14.4 生命周期门控

安卓侧观察 `Lifecycle`，非 `STARTED` 时置 `paused`，渲染线程不再出帧（保留上下文与资源）。
`LifecycleOwner` 从 `LocalContext` 逐层向上找，不额外引入 `lifecycle-compose` 依赖。

验证：退到后台后瞬时 CPU **0.0%**（`top -b -n 2` 取第二次采样；`dumpsys cpuinfo` 那个数字是
进程启动以来的累计均值，会误导）。

### 14.5 demo 重构

- 渲染场景抽成 `DemoScene` 接口，**桌面用 LWJGL、安卓用 GLES20 各实现一遍**旋转三角形。
  这不是重复代码，是刻意为之：证明库不抽象 GL。
- 新增 4x MSAA 开关，正好用来做上面 14.1 的对比。
- 帧统计增加 `屏 XXHz`（见 13.10）。

### 14.6 M3 后仍然遗留

| 项 | 状态 |
|---|---|
| 拖拽 resize 的内存峰值（13.8） | 仍偏高，用户同意暂不处理。方向：不依赖 GC 的位图淘汰队列 |
| 安卓 `ComposeImage` 模式的稳定性复测 | 本轮只测了 `TextureView`（优先项） |
| `GLConfig.debugContext` | 桌面已生效（GLFW 调试上下文）；安卓无对应概念 |


---

## 15. 页面切换后画面异常（真实 bug，已定位并修复）

### 15.1 现象

把 GLCanvas 嵌进带页面切换的程序：

- 第一次切到该页面：渲染正常
- 切走再切回：**画面出现四分之一区域异常**（以中心为原点看是第一象限那块）
- 只渲染清屏的 onDraw：正常
- 裸 GLCanvas（不画东西）：正常
- 把 `onSurfaceDestroyed` 的内容删掉：依旧异常

### 15.2 根因：GL 对象 id 不能跨上下文

**每次挂载 GLCanvas 都会新建一个 GL 上下文**（桌面新建 GLFW 窗口，安卓新建 EGL 上下文）。
program / VBO / VAO / texture 这类对象 id 只在所属上下文里有效。

使用方的代码把 GL 资源存在**顶层全局变量**里，靠一个 `initialized` 标志判断是否已创建：

```kotlin
var initialized = false
var instancedVao = 0
...
onSurfaceDestroyed = {
    glDeleteBuffers(...); glDeleteVertexArrays(...)
    initialized = false        // ← 只重置了 VAO/VBO
},
onDraw = {
    if (!initialized) { /* 建 VAO/VBO */ }
    glUseProgram(Shader.VANILLA.program)   // ← program 从没重建过！
}
```

`Shader.VANILLA.program` 是全局单例，只在第一个上下文里创建过。第二次挂载时：

```
created 4.6
resized 983 x 525
GLCanvas#program=3      ← 只出现一次，第二次挂载没有重建
```

第二次挂载沿用了上一个已销毁上下文的 program id，`glUseProgram` 拿到一个不存在的 program，
驱动**不报错**，只是静默地画出错内容。删除 `onSurfaceDestroyed` 也没用——那样 VAO/VBO 也一起失效，
错法不同而已。

复现见 `desktopApp/PageSwitchRepro.kt`（同时也是正确写法的参考实现）。

### 15.3 修法

**使用方侧**（必须改）：

- 所有 GL 资源在 `onSurfaceCreated` 里建 —— 这个回调**每次挂载都会调用**
- 所有 GL 资源在 `onSurfaceDestroyed` 里删，并把自己的字段清零
- `onDraw` 只负责画，不碰资源创建

**库侧改进**：`GLConfig.debugContext` 之前只请求了调试上下文，**没有注册调试回调**，
所以这类错误完全静默。现在会注册 `glDebugMessageCallback`，把驱动错误转到 `GLLog`：

```
[GL] source=0x8246 type=0x824c severity=0x9146: Error has been generated.
     GL error GL_INVALID_VALUE in UseProgram: (ID: 1001309929) Generic error
[GL] ... GL_INVALID_OPERATION in UseProgram: (ID: 1001309929) Generic error
```

有了这个，这类问题从"画面看着不对但不知道为什么"变成"日志直接点名 UseProgram"。

### 15.4 为什么库里不代劳

库不可能知道使用方把 program 存在哪个变量里，也无法替他们重建。唯一能做的是：
把回调的语义讲清楚（`onSurfaceCreated` 每次挂载都调）、
把排查手段给足（调试回调）、把正确写法示范出来（`PageSwitchRepro.kt`）。


---

## 16. 常驻上下文：着色器只编译一次

### 16.1 需求

页面切换频繁，每次进入都要重新编译着色器、重新初始化资源，第一次进入卡好几秒。
希望**只在第一次**做这些事。

### 16.2 障碍：GL 对象 id 不能跨上下文

program / VBO / VAO / texture 的 id 只在所属上下文有效。
所以"上下文常驻"是前提 —— 否则资源必然失效（也就是第 15 章那个 bug）。

### 16.3 桌面端的关键坑：WGL 的 DC 像素格式

第一版实现是"每次挂载创建一个上下文，用完不销毁，下次挂载复用"。失败在**跨线程迁移**：

```
[GLFW] error=0x10008   ← GLFW_FORMAT_UNAVAILABLE
[GLFW] "WGL: Failed to make context current"
```

`0x10008` 不是"上下文忙"，是**格式不匹配**：WGL 的 `wglMakeCurrent(hdc, hglrc)` 要求
DC 的像素格式与创建上下文时一致。GLFW 每个线程都重新 `GetDC(hwnd)`，拿到的 DC
像素格式与创建上下文的那份不同 → 绑定失败。

隔离测试（`desktopApp/ContextMoveTest.kt`）证实了这一点：**同一个 DC** 时上下文
可以自由跨线程迁移；换线程重新 `GetDC` 就失败。

所以桌面端的最终结构是：**会话自己持有一条常驻线程和上下文，所有画布的渲染都跑在那条线程上**，
上下文永远不跨线程。

### 16.4 安卓端：EGL 没这个坑

EGL 允许把上下文在释放之后绑定到另一条线程（没有 DC 的概念），
所以安卓端保持"每条画布一条渲染线程"，只把上下文常驻化即可。
同时存在多个画布时，第二个拿一个**共享对象**的子上下文（`eglCreateContext` 的 shareContext）。

### 16.5 最终结构

| | 桌面 | 安卓 |
|---|---|---|
| 上下文归属 | `GlfwSession`：一条常驻线程 + 一个 GLFW 窗口 | `EglSession`：EGLDisplay + 常驻 EGLContext |
| 渲染线程 | 会话线程（画布的循环跑在上面） | 每条画布一条线程，共享常驻上下文 |
| 并存画布 | 同 key 只能一个在渲染（后来的排队） | 同 key 第二个拿共享子上下文 |

回调语义随之调整（**这是一次行为变更**）：

| 回调 | 旧语义 | 新语义 |
|---|---|---|
| `onSurfaceCreated` | 每次挂载 | **上下文创建时**（每个会话一次） |
| `onSurfaceResized` | 尺寸变化时 | 不变（每次挂载都会触发） |
| `onSurfaceDestroyed` | 每次卸载 | **上下文销毁时**（正常运行时不触发） |

想主动销毁上下文：`ComposeGL.invalidateContext(sessionKey)` ——
上下文被丢弃，`onSurfaceDestroyed` 触发，下次挂载新建上下文并重新触发 `onSurfaceCreated`。

### 16.6 实测

桌面（943×514，自动切页两轮）：

```
[917ms]  onSurfaceCreated（会编译着色器）      ← 只有一次
[2330ms] 切走
[4331ms] 切回（第 2 次挂载）
[4336ms] 第一帧 draw                          ← 5ms 出帧，没有重新编译
[7336ms] 切回（第 3 次挂载）→ 7ms 出帧
```

安卓（模拟器，TextureView 模式）：`重组 5 次 · 上下文 1 次`，画面正常。

demo 里加了「挂载画布」开关和 HUD 上的「上下文 N 次」计数，可以直接观察这个行为。

---

## 17. GL 门面完整化（M4）

### 17.1 需求与形态

使用方的实例化渲染器（用 GPU 画百万级 2D 矩形）需要在**桌面与安卓上写同一份渲染代码**，
逐条 `GL46.xxx` / `GLES32.xxx` 分支写两遍不可维护。因此要一套统一调用面。

**形态：顶层 `expect fun`，不是 `expect object GL`。**

```kotlin
// GL.kt（commonMain）
expect fun glClear(mask: Int)

// GL.jvm.kt（jvmMain）
actual fun glClear(mask: Int) = GL46.glClear(mask)

// GL.android.kt（androidMain）
actual fun glClear(mask: Int) = GLES32.glClear(mask)
```

为什么不用 object：`object` 在这里没有任何收益——没有状态、没有成员、没有初始化顺序问题，
`actual object GL { ... }` 只是把函数多套一层名字（`GL.glClear`），
而且一旦以后想拆文件或只导入一部分，object 会挡路。顶层函数 `import mai_onsyn.compose_gl.gl.*` 后就是裸 `glClear`。

### 17.2 收录原则

**只收 GL 4.6 core ∩ GLES 3.2 的交集**，且只收实例化 2D 渲染真正会用到的东西。当前 **125 个函数 + 163 个常量**：

| 分类 | 函数 |
|---|---|
| 状态机 | `glEnable/glDisable/glIsEnabled`、`glDepthFunc/glDepthMask`、`glColorMask`、`glCullFace/glFrontFace`、`glLineWidth`、`glPolygonOffset`、`glPixelStorei`、`glFlush/glFinish`、`glGetError/glGetString/glGetInteger(v)/glGetFloat(v)` |
| 清屏 / 视口 | `glClear`、`glClearColor`、`glClearDepth`、`glClearStencil`、`glViewport`、`glScissor` |
| 混合 | `glBlendFunc(Separate)`、`glBlendEquation(Separate)`、`glBlendColor` |
| Buffer | `glGenBuffer(s)`、`glBindBuffer`、`glBufferData`（空/`Float/Int/Short/Byte` 数组）、`glBufferSubData`（同上）、`glDeleteBuffer(s)` |
| VAO / 属性 | `glGenVertexArray(s)`、`glBindVertexArray`、`glEnable/glDisableVertexAttribArray`、`glVertexAttribPointer/IPointer`、**`glVertexAttribDivisor`**、`glVertexAttrib2f/3f/4f`、`glBindAttribLocation` |
| 绘制 | `glDrawArrays`、`glDrawElements`、`glDrawArraysInstanced`、`glDrawElementsInstanced` |
| Shader / Program | `glCreateShader`、`glShaderSource`、`glCompileShader`、`glGetShaderiv`、`glGetShaderInfoLog`、`glCreateProgram`、`glAttach/glDetach`、`glLinkProgram`、`glValidateProgram`、`glUseProgram`、`glGetProgramiv`、`glGetProgramInfoLog`、`glGetUniformLocation`、`glGetAttribLocation` |
| Uniform | `glUniform1f/2f/3f/4f`、`glUniform1i/2i/3i/4i`、`glUniform1fv/2fv/3fv/4fv`、`glUniform1iv`、`glUniformMatrix2/3/4fv` |
| 纹理 | `glGenTexture(s)`、`glBindTexture`、`glActiveTexture`、`glTexParameteri/f`、`glTexImage2D`、`glTexSubImage2D`、`glGenerateMipmap`、`glDeleteTexture(s)` |
| FBO / RBO | `glGenFramebuffer(s)`、`glBindFramebuffer`、`glCheckFramebufferStatus`、`glFramebufferTexture2D`、`glFramebufferRenderbuffer`、`glGenRenderbuffer(s)`、`glRenderbufferStorage(Multisample)`、`glBlitFramebuffer`、`glDrawBuffers`、`glReadBuffer` |
| 回读 | `glReadPixels` |

**刻意不收**（收了就是骗人）：

| 类别 | 例子 | 原因 |
|---|---|---|
| 桌面专有 | `glPolygonMode`、`glPointSize`、`glBindFragDataLocation`、`glDrawBuffer`、SSBO、计算着色器 | GLES 3.2 没有 |
| 安卓专有 | `glShaderBinary` | 桌面没有 |
| 固定管线残留 | `glBegin`、`glMatrixMode`、`glColor*` | core profile 里已经删了 |
| Buffer 映射 | `glMapBufferRange` / `glUnmapBuffer` | 返回值是 `java.nio.ByteBuffer`，**commonMain 里没有这个类型**，跨不过去。写实例数据用 `glBufferSubData`，路径更短 |
| 像素类型的 tex 重载 | `glTexImage2D(..., IntArray?)` | 与 `ByteArray?` 形成重载歧义，`pixels = null` 每次都要写类型标注。只保留字节约 |

### 17.3 两端差异（都是签名逼出来的，不是设计选择）

| 项 | 桌面 | 安卓 |
|---|---|---|
| 批量 `glGen*` / `glGet*` | `IntBuffer` | `int[] + offset` |
| 字节偏移 | `Long` 参数 | `Int` 参数（`Long.toInt()`，单缓冲不可能超 2GB） |
| 深度清屏 | `glClearDepth(double)` | `glClearDepthf(float)` |
| 单值 `glGet*` 便捷重载 | 有（`glGetInteger`） | 无，统一走 `glGetIntegerv(int[], offset)` |

### 17.4 实现要点：两处必须记住的坑

**坑 1：`glGen*` 会把 buffer 的 position 前移。**

LWJGL 的 `glGenBuffers(n, IntBuffer)` 写入后 `position += n`。回读结果前必须 `position(0)`，
否则 `buf.get(out, 0, n)` 从末尾开始读，拿到的是上一次的残留（或者 `BufferUnderflowException`）。

**坑 2：`glReadPixels` 必须把数据拷回调用方的数组。**

第一版写成 `GL46.glReadPixels(..., Scratch.bytes(pixels))` —— 数据落在临时直接缓冲里，
调用方的 `ByteArray` 永远是原样。表现是"清屏成红色，回读出来全 0"。
这个 bug 靠编译和 code review 都看不出来，是**冒烟测试**抓到的（见 17.5）。
现在实现是：数组 → 直接缓冲 → `glReadPixels` → `position(0)` → `get(pixels)`。

**临时缓冲策略**：每个线程一套可增长的堆外缓冲（`ThreadLocal`），
`Byte` / `Float` / `Int` / `Short` 各一块，只增不减。这样每帧上传实例数据不会产生堆分配（NFR-2）。
用 `ThreadLocal` 是因为桌面端存在「会话线程 + 离屏线程」两类线程都可能碰 GL。

### 17.5 验证：`GLFacadeSmokeTest`

编译通过只能证明签名对。`composegl/src/jvmTest/.../GLFacadeSmokeTest.kt` 用一个隐藏 GLFW 窗口
真跑一遍 **实例化矩形渲染的完整链路**，然后回读像素做断言：

```
FBO(RGBA8) 建好并检查 COMPLETE
→ 编译/链接 shader（实例化顶点着色器）
→ 顶点 VBO（单位四边形）+ 实例 VBO（glBufferData 分配 + glBufferSubData 整块覆盖）
→ VAO + glVertexAttribDivisor(1, 1)
→ glDrawArraysInstanced × N
→ glReadPixels 断言：有非零像素、中心像素 ≈ (128, 0, 0, 128)  ← 预乘 alpha 也一起验了
→ glDrawElements / glDrawElementsInstanced / 批量 glGen*+glDelete* / 纹理分配+subImage
→ 收尾后 glCheckError 必须为 NO_ERROR
```

跑法：`./gradlew :composegl:jvmTest`（本机实测通过，GL 3.3 core / Intel UHD）。

**安卓侧只做了编译验证**（当时没有连接设备）。两端实现几乎逐行对应，差异集中在
`int[]+offset`、`Long→Int`、`glBufferSubData` 多一个 size 参数这三处；
真机复验时的最小清单：实例化绘制出图、`glReadPixels` 内容非零、`glTexSubImage2D` 不报 INVALID_VALUE。

### 17.6 桌面视觉走查

`./gradlew :desktopApp:run` 起窗口后确认：GL 区域（半透明清屏 + 三角形）、上下层 Compose 文字与按钮
全部正常，无 fallback、无黑块。截图存 `build/shots/facade_window.png`。

`shared/App.kt` 里的清屏与混合调用点已从 `GL.glXxx` 改为顶层 `glXxx`
（三角形场景 `TriangleScene` 仍用原生 LWJGL 绑定）——两条路可以混用，这正是"门面可选"的实证。

### 17.7 顺带清掉的历史包袱

`composegl/build.gradle.kts` 里原本为了压 `expect object GL` 的 Beta 警告而加的
`-Xexpect-actual-classes`（以及那段注释）已经删除——仓库里不再有任何 `expect class` / `expect object`。
