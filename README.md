# Open Rhythm 🎹

[中文](README.zh.md) | [English](README.md)

![Screenshot](http://cdn.jsdelivr.net/gh/Mai-Onsyn/ExternalLinkCDN/images/github-readme/OpenRhythm/screenshot.png?v=20260807)

## 🤔 What Is This?

Open Rhythm is a piano visualization & MIDI practice tool — a Synthesia-style experience that's open source. It plays standard MIDI 1 (`.mid`) files with a flowing waterfall visualization, lets you jam freely, and offers a follow-along practice mode.



## 🌍 Cross-Platform

Built on Compose Multiplatform. Currently supported:

- 💻 **JVM Desktop** (Windows / Linux / macOS)
- 📱 **Android** (phones / tablets)

📱 **iOS** – 🚨 Planned, but not implemented yet (…I don't own an Apple device 😔)



## ✨ Highlights

### MIDI Engine

- Parses MIDI 1 files, auto-splits tracks, and detects instruments & events
- Per-track configuration: instrument, color, volume, and overall enable/disable
- Caches parsed MIDI files for faster subsequent loads
- Multiple MIDI input devices
- Single MIDI output device (with SF2 soundfont loading for the Gervill synth on JVM)
- Computer keyboard works as a virtual MIDI input with fully customizable key mapping
- Independently filter the send/receive of Note, CC, PC, and PB events

### Playback & Interaction

- A pure-Kotlin MIDI player/event sender driven by coroutine timing + linear tick interpolation
- Playback at any speed multiplier
- Start from anywhere in the track, regardless of MIDI state
- Drag the waterfall to scrub the playback position precisely

### Practice Modes

- **Follow-Along Practice**: once you hit play, notes wait until you press the right key to continue (optionally practice a single track)
- **Free Play**: no falling waterfall — hit a key and a note floats upward, perfect for improvising
- Virtual keyboard and optional note-name labels on falling notes

### UI Customization

- UI adapts to landscape & portrait
- Light / Dark / System theme with a fully customizable primary color
- Waterfall background: follow-theme, solid color, or custom image (with opacity & blur)
- Octave & bar lines with customizable color and thickness
- Adjustable note height, corner radius, shadows, and name labels
- Virtual keyboard: key colors, drag-to-resize height, shadows, pitch-range display, and auto aspect ratio
- Status bar can be completely hidden during playback
- Settings pages (waterfall / keyboard) show live previews when the window is wide enough

### Extras

- Built-in logging system with adjustable level and log-file export
- All settings persist across sessions, with one-click reset



## ⚠️ Known Limitations

### Performance

Being JVM-based and relying on Compose Canvas, the waterfall can visibly drop frames on very large MIDI files (>500KB, 3000+ notes on screen).

On Android, FileKit implementation and JVM quirks make MIDI loading & parsing 15×+ slower than desktop. Although parallelism has been employed to improve efficiency, loading speed is still severely bottlenecked by Android I/O performance.

### Size & Memory

Compose Multiplatform leans on the JVM, so Desktop needs to bundle the JVM and Skia — which makes the package hefty.

JVM memory (ZGC) is well managed, but Compose's native layer holds onto memory fairly aggressively and recycles it late, so overall memory usage runs on the higher side. This is mainly caused by Skia GPU-accelerated rendering caches. Switching to CPU rendering can reduce memory consumption by half, but at the cost of poor performance.

### Platform Limitations

As the developer, I only have Windows and Android devices. The Linux and macOS builds are produced in virtual machines, so I can only build x64 versions. If you are using an ARM-based macOS/Linux device, please run the x64 build on Apple Silicon (macOS), or build an ARM version yourself. In addition, building in virtual machines has limitations, so I cannot guarantee that unknown bugs will not occur on Linux and macOS. (For example, my macOS virtual machine has no audio output device, so I cannot open the Gervill synthesizer; in theory, it should work on a physical machine.)

As for iOS devices, I currently have no way to support them at all.

## 🗺️ Roadmap

- 3D shadows on the virtual keyboard
- MIDI video rendering
- An external scripting language (kotlite planned) to control waterfall / virtual-keyboard rendering



## 🚀 Quick Start (Build & Run)

Just fire it up from the project root:

```bash
# Launch
./gradlew run

# Build Windows / macOS / Linux executable programs
./gradlew createReleaseDistributable # Universal portable program
./gradlew pakcageReleaseDmg # macOS installer
./gradlew pakcageReleaseMsi # Windows installer
./gradlew pakcageReleaseDeb # Linux installer

# Build Android apk
# Please use the Generate App Bundles or APKs tool from Android Studio or IDEA
```

## 📄 License

GPL‑v3.0 — see the [LICENSE](https://github.com/Mai-Onsyn/OpenRhythm/blob/master/LICENSE.txt) file.



## 🔗 Project Home

[GitHub](https://github.com/Mai-Onsyn/OpenRhythm)
