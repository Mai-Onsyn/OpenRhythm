package mai_onsyn.compose_gl.error

internal actual fun platformWarn(tag: String, message: String, cause: Throwable?) {
    System.err.println("[$tag] $message")
    cause?.printStackTrace()
}
