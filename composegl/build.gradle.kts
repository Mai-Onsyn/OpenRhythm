import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

kotlin {
    android {
        namespace = "mai_onsyn.open_rhythm.composegl"
        compileSdk = 36
        minSdk = 24
        compilerOptions { jvmTarget = JvmTarget.JVM_17 }
    }

    jvm {
        compilerOptions { jvmTarget = JvmTarget.JVM_17 }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.compose.runtime)
            api(libs.compose.ui)
            api(libs.compose.foundation)
            api(libs.compose.resources)
            api(libs.kotlinx.coroutines.core)
        }

        jvmMain.dependencies {
            api("org.lwjgl:lwjgl:${libs.versions.lwjgl.get()}")
            api("org.lwjgl:lwjgl-glfw:${libs.versions.lwjgl.get()}")
            api("org.lwjgl:lwjgl-opengl:${libs.versions.lwjgl.get()}")
            runtimeOnly("org.lwjgl:lwjgl:${libs.versions.lwjgl.get()}:natives-windows")
            runtimeOnly("org.lwjgl:lwjgl-glfw:${libs.versions.lwjgl.get()}:natives-windows")
            runtimeOnly("org.lwjgl:lwjgl-opengl:${libs.versions.lwjgl.get()}:natives-windows")
        }

        commonTest.dependencies {
            api(kotlin("test"))
        }
    }
}
