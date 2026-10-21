import io.kmpbits.splash.ExitAnimation
import io.kmpbits.splash.SplashColor
import io.kmpbits.splash.SplashLogo
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlinx.serialization)
    id("com.mikepenz.aboutlibraries.plugin")
    id("io.github.kmpbits.splash")
}

kotlin {
    android {
        namespace = "mai_onsyn.open_rhythm"
        compileSdk = 36
        minSdk = 24
        androidResources.enable = true
        compilerOptions { jvmTarget = JvmTarget.JVM_17 }
    }

    jvm {
        compilerOptions { jvmTarget = JvmTarget.JVM_17 }
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(libs.compose.runtime)
            api(libs.compose.ui)
            api(libs.compose.foundation)
            api(libs.compose.resources)
            api(libs.compose.ui.tooling.preview)
            api(libs.compose.material3)
            api(libs.kermit)
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.serialization.json)
            api(libs.multiplatformSettings)
            api(libs.kotlinx.datetime)

            api(libs.ktmidi)
            api(libs.kotlinx.io.core)
            implementation(libs.coil.compose)

            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs.compose)

            implementation(libs.navigation.compose)
            implementation(libs.ui.backhandler)

            implementation(libs.material.kolor)
            implementation(libs.aboutlibraries.compose.m3)
            implementation(libs.reorderable)
//            implementation("dev.chrisbanes.haze:haze-blur:2.0.0-alpha05")

            implementation(libs.localina)
        }

        commonTest.dependencies {
            api(kotlin("test"))
            api(libs.compose.ui.test)
            api(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.ktx)
            implementation(libs.kotlinx.coroutines.android)
            api("io.github.kmpbits:splash-runtime:1.4.2")
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
//            api("dev.atsushieno:ktmidi-jvm-desktop:0.12.0")
        }

        // JVM + Android 共享源集
        val jvmAndroidMain = maybeCreate("jvmAndroidMain")
        jvmAndroidMain.dependencies {
            implementation(libs.compose.gl)
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("jvmAndroid") {
                withJvm()
                withCompilations { it.target.platformType == KotlinPlatformType.androidJvm }
            }
        }
    }

    targets
        .withType<KotlinNativeTarget>()
        .matching { it.konanTarget.family.isAppleFamily }
        .configureEach {
            binaries {
                framework {
                    baseName = "SharedUI"
                    isStatic = true
                }
            }
        }
}

dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}

aboutLibraries {
    export {
        outputFile = project.file("src/commonMain/composeResources/files/aboutlibraries.json")
    }
}

splashScreen {
    backgroundColor = SplashColor.hex("#fdf7ff")
    backgroundColorNight = SplashColor.hex("#141218")
    exitAnimation = ExitAnimation.FadeOut(300)
    logo = SplashLogo.resource("foreground-512x.png")
    iosProjectPath = "iosApp/iosApp"
    androidAppPath = "androidApp"
}
