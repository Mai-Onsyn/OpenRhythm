package mai_onsyn.open_rhythm.bridge

actual object PlatformInfos {
    actual val platform: Platform = System.getProperty("os.name").lowercase().let {
        if (it.contains("win")) Platform.Windows
        else if (it.contains("mac")) Platform.macOS
        else if (it.contains("linux")) Platform.Linux
        else Platform.Unknown
    }
    actual val rendererBackend: String = System.getProperty("skiko.renderApi") ?: ""
}