package mai_onsyn.open_rhythm.bridge

import co.touchlab.kermit.Logger
import java.io.File


private const val SETTINGS_FILE_NAME = "settings.properties"

fun settingsFile(): File {
    val portable = portableSettingsFile()
    val userConfig = File(userConfigDirectory(), SETTINGS_FILE_NAME)

    val chosen = when {
        portable == null -> File(System.getProperty("user.dir"), SETTINGS_FILE_NAME)

        isMacOs() -> portable.takeIf { it.isFile && isWritableLocation(it) } ?: userConfig

        isWritableLocation(portable) -> portable

        else -> userConfig
    }

    if (portable != null && chosen != portable) {
        Logger.w {
            "Cannot store settings next to the application (${portable.absolutePath} is not usable), " +
                    "using ${chosen.absolutePath} instead"
        }
    }
    return chosen
}

private fun portableSettingsFile(): File? {
    val launcherDir = System.getProperty("jpackage.app-path")?.let(::File)?.parentFile ?: return null
    val appDir = if (isMacOs()) {
        launcherDir.parentFile?.parentFile?.parentFile ?: launcherDir
    } else {
        launcherDir
    }
    return File(appDir, SETTINGS_FILE_NAME)
}

private fun isWritableLocation(file: File): Boolean {
    val dir = file.parentFile ?: return false
    if (!dir.isDirectory && !dir.mkdirs()) return false
    return dir.canWrite()
}

private fun isMacOs(): Boolean =
    System.getProperty("os.name").lowercase().let { it.contains("mac") || it.contains("darwin") }

private fun userConfigDirectory(): File {
    val home = File(System.getProperty("user.home"))
    val osName = System.getProperty("os.name").lowercase()
    return when {
        isMacOs() -> File(home, "Library/Application Support/OpenRhythm")

        osName.contains("win") ->
            File(System.getenv("APPDATA") ?: File(home, "AppData/Roaming").path, "OpenRhythm")

        else ->
            File(System.getenv("XDG_CONFIG_HOME") ?: File(home, ".config").path, "OpenRhythm")
    }
}