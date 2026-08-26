# Open Rhythm 🎹

[English](README.md) | [中文](README.zh.md)

![Screenshot](http://cdn.jsdelivr.net/gh/Mai-Onsyn/ExternalLinkCDN/images/github-readme/OpenRhythm/screenshot.png?v=20260807)

## 🤔这是干什么的？

Open Rhythm 是一款钢琴可视化与 MIDI 练习软件，类似 Synthesia。支持标准 MIDI 1 格式（.mid）文件的播放、瀑布流可视化、自由演奏，以及跟随练习模式。

---

## 🌍 跨平台支持

基于 Compose Multiplatform，目前支持：

- 💻 **JVM 桌面** (Windows / Linux / macOS)
- 📱 **Android** (手机 / 平板)

📱 **iOS** – 🚨 计划支持，尚未实现 (我没有苹果设备😔)

---

## ✨主要特性

### MIDI相关

- 解析 MIDI 1 文件，自动拆分轨道，识别乐器与事件
- MIDI文件轨道配置：涵盖乐器、颜色、音量和整体是否启用
- 对已解析的 MIDI 文件进行缓存，加快二次加载
- 多MIDI输入设备连接
- 单MIDI输出设备连接 (对于JVM平台的Gervill合成器，支持加载SF2音色库)
- 电脑键盘可作为虚拟 MIDI 输入，并自定义键位映射
- 可单独过滤MIDI音符、CC、PC、PB事件的接收与发

### 播放与交互

- 基于kotlin协程等待与tick线性插值的纯kotlin控制的MIDI播放器/事件发送器
- 任意倍速播放
- 可从任意位置开始播放，不受 MIDI 状态影响
- 瀑布流可拖拽以精确调整播放进度

### 练习模式

- **跟随练习**：进入播放页后，所有音符会等待用户按下正确的键才继续（可指定练习单条轨道）
- **自由演奏**：无向下瀑布流，按下一个键会升起一个向上飘动的音符，适合即兴演奏
- 虚拟键盘、下落的音符可显示音名

### 界面定制

- UI动态适配横屏与竖屏 (轨道编辑页面暂未适配竖屏)
- 亮色 / 暗色 / 跟随系统主题，任意自定义主色
- 瀑布流背景：跟随主题、纯色或自定义图片（支持透明度、模糊）
- 显示八度线、小节线，可自定义颜色和粗细
- 音符高度、圆角、阴影、音名标签均可调节
- 虚拟键盘：键颜色、拖拽调整高度、阴影、显示音程范围，并支持自动宽高比
- 播放时状态栏可完全隐藏
- 设置页面（瀑布流 / 键盘）在窗口足够宽时显示实时预览

### 其他
- 内置日志系统，可调节日志级别并导出日志文件
- 全部设置持久化，支持一键重置

---

## ⚠️ 已知不足

### 性能问题

基于 JVM 且依赖 Compose Canvas，在播放大型 MIDI（>500KB，同屏 3000+ 音符）时，瀑布流会有明显掉帧。

Android 平台受 FileKit 实现和 JVM 差异影响，MIDI 文件的加载与解析速度比桌面慢 15 倍以上（30 个多轨 MIDI 平均 5~10 秒，桌面端用不到半秒）。

### 体积与内存

Compose Multiplatform 依赖 JVM，在 Desktop 需要捆绑 JVM 和 Skia，体积较大。

虽然 JVM 内存（ZGC）控制良好，但 Compose 的 Native 层内存占用较高且回收不及时，整体内存消耗偏高。

---

## 🗺️未来计划

- 多线程 MIDI 文件加载，以缓解安卓端加载慢的问题
- 虚拟键盘立体阴影
- 使用外置脚本语言 (计划 kotlite) 控制 MIDI 瀑布流 / 虚拟键盘的渲染

---

## 🚀快速开始 (构建 & 运行)

在项目根目录：

```bash
./gradlew build run
```

---

## 📄 许可证
GPL‑v3.0，详见 [LICENSE](https://github.com/Mai-Onsyn/OpenRhythm/blob/master/LICENSE.txt) 文件。

---

## 🔗 项目主页

[Github](https://github.com/Mai-Onsyn/OpenRhythm)