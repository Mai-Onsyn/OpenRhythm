# ComposeGL 桌面端渲染路线实测记录

> 记录时间：2026-11-06
> 内容：把「GL 画面放进 Compose」这件事上**实际尝试过**的每一条路线、做法、实测数据，以及它为什么走不通。

---

## 0. 问题与约束

| # | 要求 | 说明 |
|---|---|---|
| R1 | Compose 内容能叠在 GL 画面上 | 用户的状态栏悬浮在 GL 区域之上，不能挪走 |
| R2 | 不能有 GC 卡顿 | 生产环境每 240ms 一次 80–100ms 停顿，不可用 |
| R3 | 帧率/延迟尽量好 | 次要 |

约束：生产环境**必须用 ZGC**；目标硬件是 **Intel UHD 核显**。

---

## 1. 结论速览

| # | 路线 | 叠加 | 无卡顿 | 结果 |
|---|---|---|---|---|
| A | ComposeImage：回读 + 每帧新建 Skia Image（原方案） | ✅ | ❌ | 能用，但 ZGC 下必须靠 `System.gc()` 兜底，卡死 |
| B | 常驻 GL 上下文（着色器只编译一次） | ✅ | — | **已落地**，与呈递路径无关 |
| C | NativeSurface：SwingPanel + AWTGLCanvas | ❌ | ✅ | 画布在 Compose 互操作层里显示不出来 |
| D | 零拷贝：GL 纹理共享给 Skia | ✅ | ✅ | **已打通并落地**（`SharedTexture` 模式，见第 11 章） |
| E | D3D 共享纹理（原生库，silenium 路线） | ✅ | ✅ | 目标硬件缺扩展，未实施 |
| F | ANGLE（GLES→D3D11→共享） | ✅ | ✅ | 未实施（成本最高，需引入 ANGLE + 原生库） |
| G | 分配驱动 GC（替代 `System.gc()`） | ✅ | 🟡 | **当前生产方案**，有界但内存偏高 |

---

## 2. 路线 A：ComposeImage（回读 + Skia 合成）

### 2.1 做法

```
GL 渲染到离屏 FBO
  → glReadPixels 到 Skia 位图内存（零 CPU 拷贝，直接写进位图）
  → Image.makeRaster(info, data, rowBytes) 每帧新建一个 Skia Image
  → Compose 绘制这个 ImageBitmap
```

### 2.2 结果：叠加成立，但每帧产生一整帧原生内存

关掉兜底 GC 后观察 RSS：

| 配置 | RSS 增长 |
|---|---|
| 默认（D3D 后端） | **+120 MB/s** |
| `-Dskiko.renderApi=SOFTWARE` | **+109 MB/s** |

每帧约 2MB（正好 943×514×4），两种后端一致 → 与 GPU 纹理无关。

**归因实验**（决定性的那次）：在 `FrameReader.read()` 里跳过 `Image.makeRaster`，只保留 `glReadPixels`：

| 实验 | RSS |
|---|---|
| 完整发布路径 | +120 MB/s |
| 跳过 Image 创建 | **≈ 0（0.5MB/5s）** |

→ 垃圾 100% 来自「每帧新建 Skia Image 交给 Compose」这一步，回读与 GL 渲染本身不漏。

### 2.3 为什么走不通

Image 是原生对象，释放挂在 RefCnt 的 Cleaner 上，也就是 JVM GC。但堆里只有一个几百字节的壳，
堆压力几乎为零 → GC 不主动跑 → 原生内存一路涨到 OOM。

于是只能人为触发 GC：

```kotlin
// 累计到预算就 System.gc()
GC(80) Pause Full (System.gc()) 12M->11M(34M) 20.263ms   ← 每 610ms 一次（G1，943×514）
```

堆只有 12M→11M，GC 几乎回收不到 JVM 对象，纯粹为了让 Cleaner 跑起来。生产画布更大，单次 **80–100ms**。

### 2.4 在这一层试过、但都不行的改法

| 改法 | 画面 | 效果 | 为什么不行 |
|---|---|---|---|
| 复用同一 Image + `Bitmap.notifyPixelsChanged()` + 帧计数触发重绘 | ❌ 冻结 | — | Skia 给 Image 缓存了纹理；Skiko 的 `Image` 没暴露像素失效接口 |
| 复用 Bitmap + 每帧 `asComposeImageBitmap()` | ✅ | GC 次数 **81 → 81（完全相同）** | 包装内部仍是整帧拷贝 + 新纹理 |
| 强制软件 Skiko 后端 | ✅ | 109MB/s，停顿反而 **44ms** | 换后端不解决分配，只让光栅化更慢 |
| `Image.close()` 只保留最近 3 帧 | ✅ | RSS **依旧 +120MB/s** | Compose 的 `ImageBitmap` 包装器自己持有原生引用，只有它被 GC 才释放 |
| `-XX:+ExplicitGCInvokesConcurrent` | ✅ | **G1**：20ms → 1.4ms + 2ms；**ZGC：无效** | ZGC 的 `System.gc()` 是同步等整个周期跑完，不是 STW 停顿，参数治不了 |

冻结实验的量化证据：两帧差异仅 **1589 像素**（全是 HUD 文字），画布内容零变化。

---

## 3. 路线 B：常驻 GL 上下文（已落地）

### 3.1 做法

按 `sessionKey` 常驻 GL 上下文：画布卸载不销毁，下次挂载复用。

### 3.2 结果

上下文每会话只建 1 次；重新挂载 **5ms 出帧**（之前每次挂载都要重编着色器，卡好几秒）。
（当时的实现把 `onSurfaceCreated` 也绑在上下文上，后来改成两层生命周期：画布级 `onCanvasCreated` 每次挂载都调。）
安卓实测 `重组 5 次 · 上下文 1 次`。

### 3.3 桌面端踩的坑

第一版让上下文跟着渲染线程走，报 `GLFW_FORMAT_UNAVAILABLE (0x10008)`：
WGL 的 `wglMakeCurrent` 要求 DC 的像素格式与创建上下文时一致，而 GLFW 每条线程重新 `GetDC`
拿到的格式不同。隔离测试证明**同一个 DC 时上下文可以自由跨线程迁移**。

最终做法：**会话自持一条常驻线程 + 上下文，所有渲染都跑在那条线程上**，上下文永不跨线程。
安卓 EGL 没有这个概念，保持每条画布一条线程即可。

---

## 4. 路线 C：NativeSurface（SwingPanel + AWTGLCanvas）

### 4.1 做法

用 AWT 原生画布直接承载 GL（`org.lwjglx:lwjgl3-awt`），绕开 Skia → 零回读、零每帧分配。
代价是重量级组件，Compose 内容不能叠在上面（违反 R1，先验证可行性）。

### 4.2 先解决了三个问题

| 问题 | 解决方式 |
|---|---|
| `NoClassDefFoundError: org/lwjgl/system/jawt/JAWT` | `lwjgl3-awt` 的 POM 对 lwjgl core 用了未展开 classifier。**只能排除 core**，整组排除会连带干掉 `lwjgl-jawt` |
| `IllegalArgumentException: Invalid OpenGL version` | `GLUtil.validVersionGL` 只认到 **4.5**，默认 4.6 被拒 → 夹到 4.5 |
| `initGL` 从不被调用 | **`AWTGLCanvas` 没有重写 `paint()`**，AWT 的绘制流程不触发 GL，必须显式调 `render()` |

修完后 `initGL 4.5.0 - Intel UHD`、`paintGL` 帧号递增，一切正常。

### 4.3 为什么走不通

画布在 Compose 的 `SwingInteropViewGroup` 里**显示不出来**：

| 配置 | 结果 |
|---|---|
| 默认 | 画布被 Skia 层完全盖住，**全白** |
| `-Dcompose.interop.blending=true` | 画布变透明，**GL 输出仍不可见** |
| `-Dskiko.renderApi=OPENGL` | 仍然白屏 |

排除项：组件 `isShowing=true`、尺寸 755×410 正确、`paintGL` 在跑、**连给画布设红色背景都看不到**。

→ 不是 GL 的问题，是 Compose 的互操作层不合成 heavyweight(Canvas) 子组件。

### 4.4 与 karmakrafts/compose-gl 的对比

它用的是**完全一样**的结构：

```kotlin
internal class GLCanvasComponent : AWTGLCanvas(GLData().apply { ... })
SwingPanel(modifier = modifier, factory = { GLCanvasManager.create(...) })
```

| | karmakrafts | 本项目 |
|---|---|---|
| Compose Multiplatform | **1.7.3** | **1.11.1** |
| lwjgl / lwjgl3-awt | 3.3.6 / 0.2.3 | 3.3.6 / 0.2.3 |

依赖完全一致，只差 Compose 版本。所以「参考它的思路」帮不上忙——思路已经照做了，
卡点在 Compose 版本上，而降到 1.7.3 对现有项目不现实。

---

## 5. 路线 D：零拷贝（GL 纹理共享给 Skia）

### 5.1 参考实现 silenium-dev/compose-gl 的做法

1. 反射掏 Skiko 私有字段：`redrawer.device`（D3D12 设备）、`contextHandler`（DirectContext）
2. 原生 C++ 在 Skiko 的 D3D12 设备上建纹理，`CreateSharedHandle` 导出 NT 共享句柄
3. GL 侧用 `EXT_external_objects`（`glImportMemoryWin32HandleEXT`）把显存导入成 GL 纹理
4. Skia 用 `GrBackendTexture` 包同一块显存 → DrawScope 里当普通图片画

它的代价：自带原生库（链接**它自己编译的 Skia**）+ 反射私有字段 + 要求特定后端和驱动扩展。

### 5.2 前提探查：Skiko 到底是不是 GL 后端

**第一次探针（结论错误）**：在 `drawBehind` 里调 `GL11.glGetString`：

```
FATAL ERROR in native method: Thread[#42,AWT-EventQueue-0]: No context is current
```

据此误判为「Compose 1.11 的 Skiko 不再提供 GL 后端」。**这个判断是错的**——
绘制回调跑在 EDT，真正的 GPU 渲染在独立渲染线程上，EDT 上没有上下文说明不了任何事。

**第二次探针（反射 `SkiaLayer`）**：

```
getRenderApi()                          = OPENGL
getRedrawer$skiko()                     = org.jetbrains.skiko.redrawer.WindowsOpenGLRedrawer
redrawer.contextHandler                 = OpenGLContextHandler
redrawer.context (long)                 = 0x10000        ← HGLRC
contextHandler 里的 DirectContext 字段    = DirectContext(_ptr=..., refCount=2)
```

三样东西都拿得到：**GL 后端、HGLRC、Skiko 正在用的 DirectContext**。

### 5.3 事后加入 Share Group：失败

```
wglCreateContextAttribsARB(dc, skikoHglrc, attribs) → 0x0    WinError=170 (ERROR_BUSY)
wglCreateContextAttribsARB(dc, 0,          attribs) → 0x20002 WinError=0
wglShareLists(ours, skiko)                          → false  WinError=170
```

一开始怀疑像素格式不一致（WGL 要求共享双方格式相同，而 Skiko 的格式拿不到），
于是枚举设备全部像素格式逐个试：

```
设备支持的像素格式数量 = 101
（78..101 这 24 个 SetPixelFormat 报 WinError=1004，跳过）
所有像素格式均无法共享
```

**77 个有效格式全部失败** → 不是像素格式问题。再做对照实验定因：

| 实验 | `wglShareLists` |
|---|---|
| 同一 DC 上两个独立上下文 | `true` |
| 共享目标正在**本线程** current | `true` |
| 共享目标正在**另一条线程** current | **`false`，WinError=170** |

**根因：WGL 不允许与「正被其他线程 current」的上下文建立共享组。**
Skiko 的 GL 上下文常年 current 在它自己的渲染线程上，外部没有任何 API 能让它释放。

（silenium 不需要共享：它在 **Skiko 自己的绘制回调里**用 Skiko 的上下文跑 GL。
而 Compose 1.11 的绘制回调在 EDT、没有 current 上下文，那条路也断了。）

### 5.4 创建时加入 Share Group：成功

Skiko 的上下文创建在 native DLL 里：

```java
private static native long createContext(long device, long hwnd, boolean transparent);  // 没有 share 参数
```

但它是 Kotlin 文件门面类 `WindowsOpenGLRedrawerKt` 里的 7 个 `private static native`，
`WindowsOpenGLRedrawer` 只通过编译器生成的 `access$xxx` 调用。于是用 **classpath 影子类**顶掉它：

```kotlin
@file:JvmName("WindowsOpenGLRedrawerKt")
package org.jetbrains.skiko.redrawer
fun `access$createContext`(device: Long, hwnd: Long, transparent: Boolean): Long = ...
```

实测 classpath 上 `composegl-jvm.jar`（index 2）排在 `skiko-awt`（index 21）之前，影子类生效
（`Class.protectionDomain.codeSource` 已确认）。7 个方法的签名（javap 核对）：

```
getDevice(platformInfo) / createContext(device, hwnd, transparent) / makeCurrent(device, context)
swapBuffers(device) / deleteContext(context) / setSwapInterval(interval) / dwmFlush()
```

过程中踩到两个坑：

1. **`getDevice` 收到的不是 HWND**，是 JAWT 的 `platformInfo` 指针。直接 `GetDC` 返回 0
   → `IllegalStateException: Can't get device`。要按 `JAWTWin32DrawingSurfaceInfo` 的偏移取。
2. **JAWT 里那个 `hdc` 不能直接 `ChoosePixelFormat`**（WinError=6 ERROR_INVALID_HANDLE），
   要用解析出的 `hwnd` 自己 `GetDC(hwnd)`。

结果：

```
bootstrap ctx=0x20000
createContext dc=... share=0x20000 -> 0x10001  shared=true
```

**Skiko 的 GL 上下文从创建起就在我们的 Share Group 里，UI 渲染完全正常。**
前提是 `-Dskiko.renderApi=OPENGL`（默认 D3D12 redrawer 走不到这个类）。

### 5.5 把纹理交给 Skia：三个硬伤

**先修正 API 用法**（写错会直接崩进程）：

```kotlin
BackendTexture.makeGL(w, h, isMipmapped, textureId, textureTarget, textureFormat)
//                                           ^ GL 枚举        ^ GL 枚举
```

- 后两个参数是 **GL 枚举**，不是 `GrGLFormat` 编号。传 `1` 当 target → Skia `EXCEPTION_ILLEGAL_INSTRUCTION` 崩进程。
- `format` 必须是 **sized internal format**：`GL_RGBA8 (0x8058)` 可以，`GL_RGBA (0x1908)` 让 `MakeFromTexture` 返回 null。

改对后 `adoptTextureFrom` 成功，画面里出现由 GL 纹理直接填充的方块，**没有回读**——
零拷贝的「取」这一半成立。但接下来三条都堵死：

| # | 现象 | 证据 | 为什么 |
|---|---|---|---|
| 1 | **内容不刷新** | `glReadPixels` 确认纹理在蓝/橙交替；Compose 侧 `gen` 与 `draw` 计数都在涨；屏幕始终是初始帧 | Skia 在绘制时快照了纹理内容。每帧重新 `toComposeImageBitmap()` 包一层新对象（不产生原生分配）也无效 |
| 2 | **每帧 adopt 会崩** | `EXCEPTION_ACCESS_VIOLATION` @ `Managed._nInvokeFinalizer` ← `CleanableImpl.clean` ← skiko Cleaner 线程 | `adoptTextureFrom` 让 Skia 接管纹理所有权，释放时要删 GL 纹理，而 Cleaner 线程没有 current 上下文 |
| 3 | **这套 JNI 本来是给 Metal 写的** | skiko 源码注释：`SkImages::AdoptTextureFrom -> wrapBackendTexture -> GrMtlGpu::onWrapBackendTexture`，反复出现 "wraps a Metal GPU texture" | GL 分支在 0.144.6 上既不完整（返回 null、内容快照）也不安全（Cleaner 崩） |

### 5.6 结论

- **Share Group 打通是真实有效的**（影子类 + bootstrap 上下文，代码保留）；
- **第二阶段在 skiko 公开 API 上无解**。要往下走必须改 skiko 本身，补一个正确的 GL
  `GrBackendTexture` 绑定和明确的释放线程。

---

## 6. 路线 E：D3D 共享纹理（原生库）

### 6.1 做法

silenium 那条：在 Skiko 的 D3D 设备上建纹理 → 导出 NT 共享句柄 → GL 侧 `EXT_external_objects`
导入 → Skia 用 `GrBackendTexture` 包同一块显存。

### 6.2 为什么走不通

1. **驱动扩展**：需要 `WGL_NV_DX_interop2` 或 `EXT_external_objects`，基本是 NVIDIA 专有；
   目标机是 Intel UHD，不具备。
2. **自己编 Skia**：它的 `libcompose-gl.dll` 链接的是它自己编译的 Skia（`nix/skia-config.nix`、
   `subprojects.tpl/skia`），和 Skiko 自带的那份不是同一个二进制。复用它的 dll 等于把版本
   绑死在它的 Skia + Skiko + 反射字段名上。
3. **Skia C++ 符号没有导出**：Skiko 的 DLL 里只有 951 个 `Java_org_jetbrains_skia_*` JNI 入口，
   走 C++ 就必须自己编一份 Skia 并与 Skiko 的 ABI 对齐。

---

## 7. 路线 F：ANGLE（GLES → D3D11 → 与 D3D12 共享）

### 7.1 做法

引入 ANGLE，把 GL 渲染转到 D3D11，再与 Skiko 的 D3D 设备做纹理共享。

### 7.2 为什么没做

成本最高：需要引入 ANGLE 原生库 + D3D11/D3D12 共享 + 自己控制 Skiko 的 D3D 设备，
而且共享仍然绕不开「Skiko 的上下文/设备在它自己的线程上」这个约束。
在 E 已经因为驱动扩展被排除之后，这条路只剩「理论可行」这一个理由，投入产出比不成立。

---

## 8. 路线 G：分配驱动 GC（当前生产方案）

### 8.1 做法

既然原生内存只能靠 GC 归还，就让 GC **并发、持续**地跑，而不是 `System.gc()` 同步卡住：
每帧分配一块与帧同尺寸的 `ByteArray`（写进 `@Volatile` 字段防止被 JIT 优化掉）。

### 8.2 实测（ZGC / 943×514 / 60fps）

| 配置 | RSS | GC |
|---|---|---|
| 不处理 | **+120MB/s** 无限增长 | 无 |
| 泵 = 1/4 帧（29MB/s） | 仍 **+120MB/s**（太小，触发不动 GC） | 无 |
| 泵 = 1/2 帧 | 2.2–2.9GB 波动 | 40s 内 2 次 Proactive |
| **泵 = 1 帧 + `-Xmx768m`** | **1.36–1.52GB 稳定** | 40s 内 **32 次 Proactive，全并发** |
| 泵 = 1 帧，不限堆 | 2.1–2.5GB 波动 | 每 3.5s 一次 Proactive |

### 8.3 边界（它是折中，不是解法）

- 泵的大小**必须与每帧原生增量相当**，太小触发不动 GC，等于没做；
- ZGC **必须给 `-Xmx`**，否则堆会一直涨、回收不积极；
- 内存占用 ≈ 分配速率 × 回收间隔，速率 = `帧宽×帧高×4×fps`。1080p@60 就是 500MB/s，
  堆会撑到 GB 级 —— 这是它「能跑但不好看」的根源；
- 它没有解决每帧整帧分配这件事，只是把「同步卡住」换成了「并发回收 + 内存偏高」。

---

## 9. 排查过程中出现过的误判（记录以免重犯）

| 误判 | 实际情况 | 怎么纠正的 |
|---|---|---|
| 「Compose 1.11 的 Skiko 在 Windows 没有 GL 后端」 | 有，`WindowsOpenGLRedrawer`；只是绘制回调在 EDT、渲染在独立线程 | 反射 `SkiaLayer.getRenderApi()` |
| 「Skia 对纹理做了快照」（第一次） | 那次是 **Compose 根本没重绘**：后台线程写 Compose 状态没走 EDT、没触发 apply notifications | 加 `draw` 计数才排除；之后确认 UI 在重绘，快照结论才成立 |
| 「复用同一个 Image 可以省掉每帧分配」 | 画面冻结，Skia 缓存了纹理 | 两帧差异仅 1589 像素 |
| 「WGL 共享失败是像素格式不一致」 | 枚举 77 个格式全失败；真正原因是共享目标正被别的线程 current（ERROR_BUSY） | 三个对照实验 |
| 「`makeGL` 的 format 传 GrGLFormat 编号」 | 是 GL 枚举，且必须是 sized internal format；传错直接让进程崩 | 读 skiko 源码 + 逐步试 |

---

## 10. 2026-11-06 阶段性总结（已被第 11 章推翻，保留作历史）

当时认为：回读路径的每帧分配无法在 Compose 的 `ImageBitmap` 语义下确定性释放；
唯一的零拷贝出口（GL 纹理共享）在 skiko 0.144.6 上只完成了「上下文进同一个 Share Group」，
把纹理交给 Skia 这一步因为 API 本身是 Metal 导向的（内容快照 + Cleaner 崩溃）而不可用；
最终落地的是「用分配驱动 ZGC 并发回收」，代价是内存占用偏高。

**上面三条结论后来全部被证伪，见第 11 章。**

---

## 11. 零拷贝打通（2026-11-06 晚，最终结论）

第 5 章把「把纹理交给 Skia」判成死结，是三个独立的误判叠在一起。逐条推翻：

| 第 5 章的说法 | 实际 |
|---|---|
| 「JNI 是给 Metal 写的，GL 分支不完整」 | 错。`BackendTexture.makeGL` 注释明写 "from GL texture"，skiko 自己的 `OpenGLContextHandler` 每帧用 `Surface.makeFromBackendRenderTarget`；`Image.adoptTextureFrom` 在 GL 后端下可用 |
| 「Skia 快照了纹理内容」 | 真因是 **Compose 的 display list 按底层 `SkiaImage` 缓存**。V0–V2 里 render / Skiko 上下文 / Skia `drawImage` 三层像素都完美交替，屏幕方块 6/6 截图冻在首帧（`blue=10415`） |
| 「每帧 adopt 会在 Cleaner 线程崩」 | 只发生在无 current 上下文的后台线程。在 EDT + Skiko HGLRC current 时显式 `close()`，`Managed.close()` 会同步注销 Cleaner |

### 11.1 分级实测（`TextureProbe.kt`，像素统计来自 `build/PixelProbe.java`）

| 阶段 | render | Skiko 上下文 | Skia drawImage | 屏幕方块 |
|---|---|---|---|---|
| 第 5 章 baseline | 交替 | — | — | 冻结（初始蓝） |
| V0 adopt 挪到 EDT + V1 fence + V2 `resetGL`/`glTextureParametersModified` | 交替 | 交替 | 交替 | **仍冻结**，6/6 截图 `blue=10415` |
| **V3 每帧新建底层 SkiaImage** | 交替 | 交替 | 交替 | **交替**，3 张 `orange=10406` / 3 张 `blue=10415` |

**关键前提**：`MainUIDispatcher = SwingDispatcher`（EDT），且 `WindowsOpenGLRedrawer`
每帧末尾 `makeCurrent()` 后不释放上下文 —— 在 EDT 上反射拿 `device`(HDC)/`context`(HGLRC)
重新 `wglMakeCurrent` 是幂等且安全的。第一次探针报 "No context is current" 只是没调
`GL.createCapabilities()`（LWJGL capabilities 是 ThreadLocal），不是真没上下文。

### 11.2 又一个坑：`toComposeImageBitmap()` 会把零拷贝打回原形

接进 `GLCanvas` 后首版在 943×482 / 60fps 下 **67 秒 native 崩溃**
（`EXCEPTION_UNCAUGHT_CXX_EXCEPTION` @ `skiko-windows-x64.dll`）。崩溃栈指向
`SharedTextureHost.publishOnEdt()` → `Actuals_skikoExcludingWebKt.toBitmap(Image)`：

```kotlin
// Compose 1.11.1 desktop
fun Image.toComposeImageBitmap(): ImageBitmap = SkiaBackedImageBitmap(toBitmap())

internal actual fun Image.toBitmap(): Bitmap {
    val bitmap = Bitmap(); bitmap.allocPixels(...)
    Canvas(bitmap).drawImage(this, 0f, 0f)   // GPU → CPU 整帧光栅化
    ...
}
```

即 `toComposeImageBitmap()` 会**每帧整帧拷成 CPU 位图**（≈110 MB/s），这就是用户看到的
「显存没涨、内存线性增长」和 `std::bad_alloc` 垮掉 JVM 的主因。核显纹理走共享系统内存，
所以任务管理器里 GPU 专用显存不动、进程 RSS 涨。

**修正**：`FrameHost` 改成交 `org.jetbrains.skia.Image` 本身，绘制端用
`drawIntoCanvas { it.skiaCanvas.drawImageRect(image, src, dst, null) }` 绕开 `ImageBitmap`。

### 11.3 最终链路与实测

```
渲染线程（bootstrap 上下文，Share Group）
  → 每帧新 GL 纹理 + FBO → onDraw → glFenceSync + glFlush
  → tryPublish（EDT 忙则丢弃并删纹理，形成背压）
EDT（Skiko HGLRC current）
  → glWaitSync + glDeleteSync → BackendTexture.makeGL → adoptTextureFrom
  → 保留最近 3 帧，更早的显式 close（Skia 同步删纹理）
  → state.value = SkiaImage → Snapshot.sendApplyNotifications()
Compose draw 阶段 → drawIntoCanvas → skiaCanvas.drawImageRect（无 ImageBitmap、无回读）
```

| 指标 | 值 |
|---|---|
| 帧率（943×482 / 连续渲染 / Intel UHD） | 56–57 fps（屏 60Hz） |
| 帧耗时 | total 0.16ms（首版含 `toBitmap` 时 0.36ms） |
| 叠加 | 上下按钮/文本均可见、可点；重组 2 次；上下文 1 次 |
| 内存（连续 4 分钟） | WS 737.6 → 741.6 → 747.2 MB，PeakWS 恒 751.2MB，**稳定** |
| 崩溃 | 无（首版 67 秒崩，修正后 4 分 40 秒存活至手动停止） |
| 默认 `ComposeImage` 回归 | 通过（三角形、叠加、HUD 均正常） |

启动：`./gradlew :desktopApp:run`。`main()` 里已写死 `skiko.renderApi=OPENGL` 与
`SharedTexture` 模式，不依赖任何环境变量或 JVM 参数。

### 11.4 启动方式统一后的复核

用环境变量决定启动方式时，实跑得到过两种结果：「画面在动但内存线性增长」与
「画面在动且内存平稳」，差异在于参数是否真的传进 Compose Desktop fork 出来的 JVM。
改为 `main()` 里直接 `System.setProperty("skiko.renderApi", "OPENGL")` + 固定
`SharedTexture` 模式后，不带任何环境变量跑 `./gradlew :desktopApp:run`：

- `composegl-skiko-share.log` 尾部 `createContext ... shared=true`，影子类生效
- 帧号 #1890 → #9930，57.1–57.6 fps，画面持续在动
- 进程内存（WorkingSet / Commit / PeakWS，MB）：
  T0 `730.7 / 611.7 / 734.3` → T+2min `744.3 / 613.0 / 753.8` → T+6min `713.9 / 612.6 / 755.3`
- 连续 7 分 14 秒无崩溃、无新增 `hs_err` 日志。WS 在工作集回收后回落，Commit 恒定

同时修掉一处静默泄漏：`publishOnEdt()` 在 `resolveSkiko()` 失败（后端不对或 classpath
影子类没生效）时原先直接 return，渲染线程仍每帧新建纹理却无人 adopt，纹理持续累积。
现在失败累计 30 次后置 `unusable`（渲染线程丢弃并自删纹理），并通过 `onError`
让 `GLCanvas` 显示 fallback，不再静默涨内存。

### 11.5 画布卸载 / 重挂与窗口关闭复核

demo 的「挂载画布」开关此前从未接到 `GLCanvas.enabled`（历史 6 个版本都没有），
页面切换一直没被验证过。补上 `enabled = mounted` 后实测：

- 卸载：三角形消失、开关变灰、无报错，HUD「上下文 1 次」不变
- 重挂：三角形恢复并继续刷新，上下文仍为 1 次（着色器没重编译）
- 连续切换 80 次：#3360 帧、57.5 fps、重组 82 次、上下文 1 次、无崩溃
- 内存（常驻 host 版）：Commit T0 `615.0` → 40 次 `622.4` → 80 次 `628.9MB`，
  第二个 40 次只 +1.0MB；显式 `GC.run` 后 `630.4`、静置 30 秒 `629.0` → 预热后收敛
- WS `723.6 → 786.2MB`，PeakWS `797.5MB`；WS 在工作集回收后回落

期间两处实现修正：先给 `SharedTextureHost` 加 `dispose()`，挡住窗口关闭后 HGLRC
已销毁时 EDT 仍去 `wglMakeCurrent` 的报错；随后发现每次挂载重建 host 会留下
「已 publish 但没人 adopt」的纹理，改成按 `sessionKey` 常驻复用
（`SharedTextureHost.obtain`），`wglMakeCurrent` 失败改为静默并清空 handles 缓存。
两版正常关闭窗口都是 `BUILD SUCCESSFUL`、stderr 无输出。

