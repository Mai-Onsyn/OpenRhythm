package mai_onsyn.compose_gl.error

/** GL 初始化或渲染过程中的错误。 */
class GLException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
