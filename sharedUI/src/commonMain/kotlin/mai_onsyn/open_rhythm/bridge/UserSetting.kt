package mai_onsyn.open_rhythm.bridge

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.russhwolf.settings.Settings
import com.russhwolf.settings.boolean
import com.russhwolf.settings.float
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
    var KeyboardAutoAspect by st.boolean("KeyBoardAutoAspect", true)
    var KeyboardAspectRatio by st.float("KeyBoardAspectRatio", 8f)
    var KeyboardUserInteractionDisplayColor by st.color("KeyboardUserInteractionDisplayColor", Color(138, 226, 52))
    var AlwaysFocusMidiRegion by st.boolean("AlwaysFocusMidiRegion", true)

    val libraryFolderList by st.list("LibraryFolderList", mutableListOf(), UILibraryFolder.serializer())
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