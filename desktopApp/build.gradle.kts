import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":sharedUI"))
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "OpenRhythm"
            packageVersion = "1.0.0"

            linux {
                iconFile.set(project.file("appIcons/OpenRhythm.png"))
            }
            windows {
                iconFile.set(project.file("appIcons/OpenRhythm.ico"))
            }
            macOS {
                iconFile.set(project.file("appIcons/OpenRhythm.icns"))
                bundleID = "mai-onsyn.open-rhythm.desktopApp"
            }
        }

        jvmArgs(
            "-Dfile.encoding=UTF-8",
            "-XX:+UseZGC",
            "-Xms512m",
            "-XX:MaxRAMPercentage=80.0",
            "--enable-native-access=ALL-UNNAMED",
//            "-Xlog:gc"
        )

        buildTypes.release.proguard {
            isEnabled = !(org.gradle.internal.os.OperatingSystem.current().isMacOsX)
            version = "7.9.1"
            configurationFiles.from("proguard.txt")
            obfuscate = false  // 混淆
            optimize = true    // 优化
        }
    }
}

kotlin {
    sourceSets {
        getByName("test") {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

tasks.register("cleanPackagedDir") {
    description = "Simplify the size of JVM target files packaged through the createReleaseDistributable task"

    val fontDir = project.rootProject.file("desktopApp/build/compose/binaries/main-release/app/OpenRhythm/runtime/lib/fonts")
    doLast {
        if (fontDir.exists()) {
            fontDir.listFiles()?.forEach { file ->
                if (!(file ?: return@forEach).exists()) return@forEach

                val ext = file.extension
                if (ext == "ttf" || ext == "otf") file.delete()
            }
        }
    }
}

tasks.configureEach {
    when (name) {
        "createReleaseDistributable" -> finalizedBy("cleanPackagedDir")
    }
}