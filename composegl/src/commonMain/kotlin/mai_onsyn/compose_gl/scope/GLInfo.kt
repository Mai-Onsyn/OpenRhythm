package mai_onsyn.compose_gl.scope

/** 上下文信息，报告的是**实际**拿到的版本，不是请求的版本。 */
data class GLInfo(
    val major: Int,
    val minor: Int,
    val versionString: String,
    val rendererString: String,
    val vendorString: String,
) {
    val versionLabel: String get() = "$major.$minor"
}
