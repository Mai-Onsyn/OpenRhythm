# Open Rhythm 🎹

[English](README.md) | [中文](README.zh.md)

![Screenshot](http://cdn.jsdelivr.net/gh/Mai-Onsyn/ExternalLinkCDN/images/github-readme/OpenRhythm/screenshot.png?v=20260807)

## 🤔 这是什么
Open Rhythm 是一个基于 Compose Multiplatform 的 MIDI 可视化与钢琴练习工具。

## 🌍 平台支持

- **JVM 桌面**：Windows / Linux / macOS
- **Android**：手机 / 平板

**iOS** – 计划支持，尚未实现 (我没有苹果设备😔)

## ✨ 设计理念

- 追求与 Synthesia 同等的使用体验，但！**完全个性化**——界面上的每一个地方都能DIY！
- 面向普通人，简单易用
- 将钢琴练习、多乐器 MIDI 播放、Black MIDI 播放和特效钢琴（大饼）融为一体

## 💪 它能做什么

- 解析 MIDI 文件，自动拆分轨道、识别乐器与事件，解析结果会缓存以加快二次加载，不会自动加载 Black MIDI
- 逐轨配置乐器 / 颜色 / 音量 / 启用状态
- 多 MIDI 输入设备 + 单个输出设备（JVM 端 Gervill 可加载 SF2 音色库），电脑键盘可作为虚拟 MIDI 输入设备，且按键映射可自定义
- 输出设备无关的播放器系统，支持任意倍速、任意位置播放
- Synthesia 同款跟随练习模式，可只练某一轨，以及标配的自由演奏模式
- 主题 / 主色 / 背景图 / 八度线 / 小节线 / 音符圆角阴影 / 键盘高度与音程范围等等均可调节，设置页可实时预览
- 不同场景的渲染模式可切换：Compose Canvas（普通 midi 练习 / 播放用）；CPU 静态帧缓存（百万级音符）；OpenGL 实例化（千万级音符 / 特效）

## ⚠️ 使用前请留意

- **桌面端体积和内存偏高**。Desktop 需要捆绑 JVM 和 Skia，体积较大；Compose 的 Native 层内存回收不及时，整体内存占用偏高，主要来自 Skia GPU 加速渲染缓存。切到 CPU 渲染能降低约一倍内存，但性能会差不少。
- **Black MIDI 请量力而行**。JVM GC 不及时时，加载和播放 Black MIDI 可能瞬时占用几 GB 甚至几十 GB 内存；16G 及以下内存的设备暂时不建议用来播放 Black MIDI。Android 端还受 JVM 内存限制，较大的 Black MIDI 可能加载不了（会直接OOM闪退）。后续我会考虑用 JNI 重写这部分来解决。
- **Linux / macOS 构建有局限**。我目前只有 Windows 和 Android 实机，Linux 和 macOS 版本是通过虚拟机构建的，只能构建 x64 版本。如果你用的是 ARM 架构的 macOS/Linux，请在 Apple Silicon Mac 上运行 x64 版本，或自行构建 ARM 版本。虚拟机环境也可能有未知 bug，比如我的 macOS 虚拟机没有音频输出设备，打不开 Gervill 合成器；理论上实体机可以使用。
- **OpenGL 实例化渲染目前只在 Windows 和 Android 上验证过**，Linux 后续再考虑支持，macOS、iOS不会支持。

## 🗺 计划

- MIDI 离线视频渲染。
- 可导入的自定义瀑布流着色器（计划 kotlin scripting + OpenGL），以实现自定义特效。
- JNI 重写 MIDI 解析与音符实例数据上传以优化内存占用。

项目还在迭代中，遇到问题欢迎带上系统和设备信息提 issue，也欢迎 PR。如果它刚好对你有用，那就点个 star 吧 ⭐

## 🚀 构建 & 运行

在项目根目录：

```bash
# 启动
./gradlew run

# 构建 Windows / macOS / Linux 可执行程序
./gradlew createReleaseDistributable   # 通用绿色程序
./gradlew packageReleaseDmg            # macOS 安装包
./gradlew packageReleaseMsi            # Windows 安装包
./gradlew packageReleaseDeb            # Linux 安装包
```

Android 安装包请使用 Android Studio 或 IDEA 的 **Generate App Bundles or APKs** 工具构建。

---

**下载**：[Releases](https://github.com/Mai-Onsyn/OpenRhythm/releases)

**许可证**：GPL-v3.0，详见 [LICENSE](https://github.com/Mai-Onsyn/OpenRhythm/blob/master/LICENSE.txt)

🔗**项目主页**：[GitHub](https://github.com/Mai-Onsyn/OpenRhythm)