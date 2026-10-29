package mai_onsyn.compose_gl.error

import android.util.Log

internal actual fun platformWarn(tag: String, message: String, cause: Throwable?) {
    if (cause != null) Log.w(tag, message, cause) else Log.w(tag, message)
}
