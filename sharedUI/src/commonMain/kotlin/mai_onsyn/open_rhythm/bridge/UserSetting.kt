package mai_onsyn.open_rhythm.bridge

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.russhwolf.settings.Settings
import com.russhwolf.settings.boolean
import com.russhwolf.settings.float
import com.russhwolf.settings.int
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class UserSetting(
    st: Settings
) {
    var KeyboardAutoAspect by st.boolean("KeyBoardAutoAspect", true)
    var KeyboardAspectRatio by st.float("KeyBoardAspectRatio", 8f)
    var keyboardUserInteractionDisplayColor by st.color("keyboardUserInteractionDisplayColor", Color(138, 226, 52))
}

fun Settings.color(
    key: String? = null,
    defaultValue: Color
): ReadWriteProperty<Any?, Color> {
    val intDelegate = this.int(key, defaultValue.toArgb())

    return object : ReadWriteProperty<Any?, Color> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Color {
            return Color(intDelegate.getValue(thisRef, property))
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Color) {
            intDelegate.setValue(thisRef, property, value.toArgb())
        }
    }
}