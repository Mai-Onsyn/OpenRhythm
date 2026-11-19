# Open Rhythm 🎹

[中文](README.zh.md) | [English](README.md)

![Screenshot](http://cdn.jsdelivr.net/gh/Mai-Onsyn/ExternalLinkCDN/images/github-readme/OpenRhythm/screenshot.png?v=20260807)

## 🤔 What Is This?

Open Rhythm is a MIDI visualization & piano practice tool built on Compose Multiplatform — a Synthesia-style experience that's open source.

## 🌍 Supported Platforms

- **JVM Desktop** (Windows / Linux / macOS)
- **Android** (phones / tablets)

**iOS** – Planned, but not implemented yet (…I don't own an Apple device 😔)

## ✨ Design Philosophy

- Aim for the same experience as Synthesia, but **fully yours** — every single corner of the interface can be DIY'd!
- Made for ordinary people: simple and easy to use.
- One tool that merges piano practice, multi-instrument MIDI playback, Black MIDI playback, and special-effect piano (the legendary "giant pancake" 🥞).

## 💪 What It Can Do

- Parses MIDI files, auto-splits tracks, and detects instruments & events; parsed results are cached so subsequent loads are faster. Black MIDI is never loaded automatically.
- Per-track configuration: instrument, color, volume, and overall enable/disable.
- Multiple MIDI input devices + a single output device (with SF2 soundfont loading for the Gervill synth on JVM). Your computer keyboard works as a virtual MIDI input with fully customizable key mapping.
- An output-device-independent player system: play at any speed multiplier, start from anywhere in the track.
- Follow-Along Practice mode just like Synthesia's — optionally practice a single track — plus the standard Free Play mode.
- Theme / primary color / waterfall background image / octave lines / bar lines / note corner radius & shadows / keyboard height & pitch range, and so on — all adjustable, with live previews on the settings pages.
- Switchable rendering modes per scenario: Compose Canvas (regular practice & playback), CPU static-frame caching (millions of notes), OpenGL instancing (tens of millions of notes / special effects).

## ⚠️ Attentions

- **The desktop build is hefty, and so is its memory.** Desktop has to bundle the JVM and Skia, which makes the package hefty; Compose's native layer recycles memory fairly aggressively and late, so overall usage runs on the higher side — mainly Skia's GPU-accelerated rendering caches. Switching to CPU rendering roughly halves memory consumption, but at the cost of noticeably worse performance.
- **Attempt Black MIDI with due respect for your RAM.** When the JVM GC can't keep up, loading and playing Black MIDI can momentarily claim several GB — or even tens of GB — of memory. Devices with 16 GB or less are, for now, not recommended for playing Black MIDI. On Android the JVM memory limit bites harder, so larger Black MIDI files may fail to load at all (straight to OOM and a polite exit 👋). I plan to rewrite this part in JNI later to fix it.
- **The Linux / macOS builds come with caveats.** As the developer, I only have Windows and Android devices. The Linux and macOS builds are produced in virtual machines, so I can only build x64 versions. If you're on an ARM-based macOS/Linux device, please run the x64 build on Apple Silicon, or build an ARM version yourself. Building in virtual machines has limitations, so I cannot guarantee that unknown bugs won't show up on Linux and macOS (for example, my macOS virtual machine has no audio output device, so I cannot open the Gervill synthesizer; in theory, it should work on a physical machine).
- **OpenGL instanced rendering has only been verified on Windows and Android.** Linux support may come later; macOS and iOS will not be supported.

## 🗺️ Roadmap

- Offline MIDI video rendering.
- Importable custom waterfall shaders (planned: Kotlin scripting + OpenGL) for your own special effects.
- Rewrite MIDI parsing and note instance data upload in JNI to reduce memory usage.

The project is still iterating. If you run into problems, please open an issue with your system and device details — PRs are welcome too. And if it happens to be useful to you, go ahead and drop a star ⭐

## 🚀 Quick Start (Build & Run)

Just fire it up from the project root:

```bash
# Launch
./gradlew run

# Build Windows / macOS / Linux executable programs
./gradlew createReleaseDistributable   # Universal portable program
./gradlew packageReleaseDmg            # macOS installer
./gradlew packageReleaseMsi            # Windows installer
./gradlew packageReleaseDeb            # Linux installer
```

For the Android package, please use the **Generate App Bundles or APKs** tool from Android Studio or IDEA.

---

**Download**: [Releases](https://github.com/Mai-Onsyn/OpenRhythm/releases)

**License**: GPL-v3.0 — see the [LICENSE](https://github.com/Mai-Onsyn/OpenRhythm/blob/master/LICENSE.txt) file.

🔗 **Project Home**: [GitHub](https://github.com/Mai-Onsyn/OpenRhythm)
