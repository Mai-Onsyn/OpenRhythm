package mai_onsyn.open_rhythm.bridge

expect object PlatformInfos {
    val platform: Platform
    val rendererBackend: String
}

enum class Platform {
    Windows, macOS, Linux, Android, iOS, Unknown
}