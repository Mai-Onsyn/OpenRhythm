package mai_onsyn.open_rhythm.bridge

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.russhwolf.settings.Settings
import com.russhwolf.settings.int
import com.russhwolf.settings.string
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import mai_onsyn.open_rhythm.ui.pages.library.UILibraryFolder
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class UserSetting(
    st: Settings
) {
    // =====General Appearance=====
    var DarkMode by st.observable("DarkMode", 2)  // 0 = light; 1 = dark; 2 = system default
    var PrimarySeedColor by st.observable("PrimarySeedColor", Color(0xFF485F84))
    var UserSpecifiedPrimarySeedColor by st.observable("UserSpecifiedPrimarySeedColor", Color(0xFFCD20ED))

    // =====General Interaction=====
    var DoubleClickToPlayPause by st.observable("DoubleClickToPlayPause", false)
    var DoubleFingerTapToPlayPause by st.observable("DoubleFingerTapToPlayPause", true)

    // =====Keyboard Appearance=====
    var KeyboardAutoAspect by st.observable("KeyBoardAutoAspect", true)
    var KeyboardAspectRatio by st.observable("KeyBoardAspectRatio", 8f)
    var KeyboardUserInteractionDisplayColor by st.observable("KeyboardUserInteractionDisplayColor", Color(138, 226, 52))

    // =====User Data=====
    val libraryFolderList by st.list("LibraryFolderList", mutableListOf(), UILibraryFolder.serializer())
}

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
        is Color -> Color(getInt(key, default.toArgb()))
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
                    is Color -> putInt(key, newValue.toArgb())
                    else -> error("Unsupported type for setting: ${newValue!!::class}")
                }
            }
    }
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

inline fun <reified T> Settings.list(
    key: String,
    defaultValue: List<T>,
    serializer: KSerializer<T>
): ReadWriteProperty<Any?, MutableList<T>> {

    val json = Json

    val delegate = string(
        key,
        json.encodeToString(
            ListSerializer(serializer),
            defaultValue
        )
    )

    var version by mutableStateOf(0)

    return object : ReadWriteProperty<Any?, MutableList<T>> {

        override fun getValue(
            thisRef: Any?,
            property: KProperty<*>
        ): MutableList<T> {
            val value = json.decodeFromString(
                ListSerializer(serializer),
                delegate.getValue(thisRef, property)
            )

            version++
            return SerializableList(
                value.toMutableList()
            ) {
                delegate.setValue(
                    thisRef,
                    property,
                    json.encodeToString(
                        ListSerializer(serializer),
                        it
                    )
                )
            }
        }


        override fun setValue(
            thisRef: Any?,
            property: KProperty<*>,
            value: MutableList<T>
        ) {

            delegate.setValue(
                thisRef,
                property,
                json.encodeToString(
                    ListSerializer(serializer),
                    value
                )
            )
            version++
        }
    }
}

class SerializableList<T>(
    private val list: MutableList<T>,
    private val onChanged: (List<T>) -> Unit
) : MutableList<T> by list {


    private fun changed() {
        onChanged(this)
    }


    override fun add(element: T): Boolean {
        val result = list.add(element)
        if (result) changed()
        return result
    }


    override fun add(index: Int, element: T) {
        list.add(index, element)
        changed()
    }


    override fun addAll(elements: Collection<T>): Boolean {
        val result = list.addAll(elements)
        if (result) changed()
        return result
    }


    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        val result = list.addAll(index, elements)
        if (result) changed()
        return result
    }


    override fun remove(element: T): Boolean {
        val result = list.remove(element)
        if (result) changed()
        return result
    }


    override fun removeAt(index: Int): T {
        val result = list.removeAt(index)
        changed()
        return result
    }


    override fun removeAll(elements: Collection<T>): Boolean {
        val result = list.removeAll(elements)
        if (result) changed()
        return result
    }


    override fun clear() {
        list.clear()
        changed()
    }


    override fun retainAll(elements: Collection<T>): Boolean {
        val result = list.retainAll(elements)
        if (result) changed()
        return result
    }


    override fun set(index: Int, element: T): T {
        val result = list.set(index, element)
        changed()
        return result
    }
}