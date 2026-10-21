import java.util.Properties

rootProject.name = "OpenRhythm"

pluginManagement {
    repositories {
        google {
            content { 
              	includeGroupByRegex("com\\.android.*")
              	includeGroupByRegex("com\\.google.*")
              	includeGroupByRegex("androidx.*")
              	includeGroupByRegex("android.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            content { 
              	includeGroupByRegex("com\\.android.*")
              	includeGroupByRegex("com\\.google.*")
              	includeGroupByRegex("androidx.*")
              	includeGroupByRegex("android.*")
            }
        }
        mavenCentral()
        maven("https://nexus.silenium.dev/repository/maven-releases/") {
            name = "silenium-dev-releases"
        }
    }
}
include(":sharedUI")
include(":desktopApp")

val localProperties = Properties().apply {
    file("local.properties").takeIf { it.exists() }?.inputStream()?.use { fis ->
        this.load(fis)
    }
}

val disableAndroid = localProperties.getProperty("disable.android", "false").toBoolean()

if (!disableAndroid) include(":androidApp")