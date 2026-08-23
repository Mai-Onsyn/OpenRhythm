package mai_onsyn.open_rhythm.core.settings

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.toArgb
import com.russhwolf.settings.Settings
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

inline fun <reified T> Settings.observable(
    key: String,
    default: T
): MutableState<T> {
    @Suppress("UNCHECKED_CAST")
    val initialValue = when (default) {
        is Int -> getInt(key, default)
        is Long -> getLong(key, default)
        is String -> getString(key, default)
        is Boolean -> getBoolean(key, default)
        is Float -> getFloat(key, default)
        is Double -> getDouble(key, default)
        is Color -> unpackColor(getLong(key, packColor(default)))
        else -> error("Unsupported type: ${default!!::class}")
    } as T

    val state = mutableStateOf(initialValue)

    return object : MutableState<T> by state {
        override var value: T
            get() = state.value
            set(newValue) {
                state.value = newValue
                when (newValue) {
                    is Int -> putInt(key, newValue)
                    is Long -> putLong(key, newValue)
                    is String -> putString(key, newValue)
                    is Boolean -> putBoolean(key, newValue)
                    is Float -> putFloat(key, newValue)
                    is Double -> putDouble(key, newValue)
                    is Color -> putLong(key, packColor(newValue))
                    else -> error("Unsupported type for setting: ${newValue!!::class}")
                }
            }
    }
}

fun packColor(color: Color): Long {
    return if (color.isUnspecified) {
        1L shl 32
    } else {
        color.toArgb().toLong() and 0xFFFFFFFFL
    }
}

fun unpackColor(packed: Long): Color {
    return if (packed shr 32 != 0L) {
        Color.Unspecified
    } else {
        Color(packed.toInt())
    }
}

class ColorSerializer: KSerializer<Color> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: Color) = encoder.encodeLong(value.value.toLong())
    override fun deserialize(decoder: Decoder): Color = Color(decoder.decodeLong().toULong())
}