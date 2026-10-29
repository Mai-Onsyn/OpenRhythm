package mai_onsyn.compose_gl.error

/**
 * 平台无关的日志门面。
 *
 * 默认输出到平台日志（桌面 `System.err`、安卓 `Log.w`）。
 * 接入自己的日志系统（Kermit、println、写文件……）时设置 [handler] 即可：
 *
 * ```kotlin
 * GLLog.handler = { tag, message, cause -> Kermit.w { "GL[$tag] $message" } }
 * ```
 */
object GLLog {

    /**
     * 自定义日志出口。为 null 时走平台默认实现。
     *
     * 注意：回调可能在**渲染线程**上被调用，实现里别做重活。
     */
    var handler: ((tag: String, message: String, cause: Throwable?) -> Unit)? = null

    fun warn(tag: String, message: String, cause: Throwable? = null) {
        val custom = handler
        if (custom != null) {
            custom(tag, message, cause)
        } else {
            platformWarn(tag, message, cause)
        }
    }
}

internal expect fun platformWarn(tag: String, message: String, cause: Throwable?)
