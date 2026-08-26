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

- UI adapts to landscape & portrait (track-edit page isn't portrait-ready yet)
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

On Android, FileKit implementation and JVM quirks make MIDI loading & parsing 15×+ slower than desktop (30 multi-track MIDIs average 5–10s, vs. under half a second on desktop).

### Size & Memory

Compose Multiplatform leans on the JVM, so Desktop needs to bundle the JVM and Skia — which makes the package hefty.

JVM memory (ZGC) is well managed, but Compose's native layer holds onto memory fairly aggressively and recycles it late, so overall memory usage runs on the higher side.



## 🗺️ Roadmap

- Multi-threaded MIDI loading to ease the Android bottleneck
- 3D shadows on the virtual keyboard
- An external scripting language (kotlite planned) to control waterfall / virtual-keyboard rendering



## 🚀 Quick Start (Build & Run)

Just fire it up from the project root:

```bash
./gradlew build run
```



## 📄 License

GPL‑v3.0 — see the [LICENSE](https://github.com/Mai-Onsyn/OpenRhythm/blob/master/LICENSE.txt) file.



## 🔗 Project Home

[GitHub](https://github.com/Mai-Onsyn/OpenRhythm)
