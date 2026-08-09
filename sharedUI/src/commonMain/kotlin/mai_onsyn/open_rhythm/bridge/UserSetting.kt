package mai_onsyn.open_rhythm.bridge

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.toArgb
import com.russhwolf.settings.Settings
import com.russhwolf.settings.string
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import mai_onsyn.open_rhythm.ui.pages.library.UILibraryFolder
import mai_onsyn.open_rhythm.ui.pages.setting.categories.key_map.KeyMidiMapping
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class UserSetting(
    st: Settings
) {
    // =====General Appearance=====
    var DarkMode                        by st.observable("DarkMode",                        2)  // 0 = light; 1 = dark; 2 = system default
    var PrimarySeedColor                by st.observable("PrimarySeedColor",                Color(0xFF485F84))
    var UserSpecifiedPrimarySeedColor   by st.observable("UserSpecifiedPrimarySeedColor",   Color(0xFFCD20ED))

    // =====General Interaction=====
    var DoubleClickToPlayPause      by st.observable("DoubleClickToPlayPause",      false)
    var DoubleFingerTapToPlayPause  by st.observable("DoubleFingerTapToPlayPause",  true)

    // =====MIDI Input=====
    val enabledMidiInputDeviceList  by st.list("EnabledMidiInputDeviceList",    mutableListOf("Virtual Keyboard"), String.serializer())
    var EnableInputMidiNoteEvent    by st.observable("EnableInputMidiNoteEvent",    true)
    var EnableInputMidiCCEvent      by st.observable("EnableInputMidiCCEvent",      true)
    var EnableInputMidiPCEvent      by st.observable("EnableInputMidiPCEvent",      true)
    var EnableInputMidiPBEvent      by st.observable("EnableInputMidiPCBEvent",     true)
    var EnableInputOtherMidiEvent   by st.observable("EnableInputOtherMidiEvent",   true)

    // =====MIDI Output=====
    var SelectedOutputDeviceName    by st.observable("SelectedOutputDeviceName",    "")
    var EnableOutputMidiNoteEvent   by st.observable("EnableOutputMidiNoteEvent",   true)
    var EnableOutputMidiCCEvent     by st.observable("EnableOutputMidiCCEvent",     true)
    var EnableOutputMidiPCEvent     by st.observable("EnableOutputMidiPCBEvent",    true)
    var EnableOutputMidiPBEvent     by st.observable("EnableOutputMidiPCBEvent",    true)
    var EnableOutputOtherMidiEvent  by st.observable("EnableOutputOtherMidiEvent",  true)
    var GervillSF2Path              by st.observable("GervillSF2Path",              "")

    // =====MIDI File=====
    var UseParserV1 by st.observable("UseParserV1", false)

    // =====Key Mapping=====
    val userKeyMappings by st.list("UserKeyMappings", KeyMidiMapping.default(), KeyMidiMapping.serializer())

    // =====Waterfall Appearance=====
    var WaterfallBackgroundColor        by st.observable("WaterfallBackgroundColor",        Color.Unspecified)
    var CustomWaterfallBackgroundColor  by st.observable("CustomWaterfallBackgroundColor",  Color(48, 48, 48))
    var BackgroundImageDir              by st.observable("BackgroundImageDir",              "")
    var BackgroundImageOpacity          by st.observable("BackgroundImageOpacity",          0.3f)
    var BackgroundImageBlurDp           by st.observable("BackgroundImageBlurDp",           0f)
    var OriginalBackgroundImageSize     by st.observable("OriginalBackgroundImageSize",     false)
    var DrawOctaveLines                 by st.observable("DrawOctaveLines",                 true)
    var DrawSectionLines                by st.observable("DrawSectionLines",                true)

    // =====Note Appearance=====
    var NoteRoundConerPercent   by st.observable("NoteRoundConerPercent",   0.5f)
    var QuarterNoteDpHeight     by st.observable("QuarterNoteDpHeight",     120f)
    var DrawPitchLabels         by st.observable("DrawPitchLabels",         false)
    var DrawNoteShadow          by st.observable("DrawNoteShadow",          true)

    // =====Keyboard Appearance=====
    var KeyboardAutoAspect              by st.observable("KeyBoardAutoAspect",          true)
    var KeyboardAspectRatio             by st.observable("KeyBoardAspectRatio",         8f)
    var KeyboardInteractionColor        by st.observable("KeyboardInteractionColor",    Color(138, 226, 52))
    var EnableKeyboardDragArea          by st.observable("EnableKeyboardDragArea",      true)
    var KeyboardDragAreaColor           by st.observable("KeyboardDragAreaColor",       Color.Unspecified)
    var CustomKeyboardDragAreaColor     by st.observable("CustomKeyboardDragAreaColor", Color(0xFF404040))
    var DrawRedSplitLine                by st.observable("DrawRedSplitLine",            true)
    var KeyboardShadowColor             by st.observable("KeyboardShadowColor",         Color.Black)
    var WhiteKeyColor                   by st.observable("WhiteKeyColor",               Color.White)
    var BlackKeyColor                   by st.observable("BlackKeyColor",               Color.Black)
    var OverlayLabelsMode               by st.observable("OverlayLabelsMode",           0)  // 0=无 1=仅Cx音符 2=仅白键 3=全部
    var MinPitch                        by st.observable("MinPitch",                    21)
    var MaxPitch                        by st.observable("MaxPitch",                    108)

    // =====Log=====
    var LogLevel        by st.observable("LogLevel",        2)  // 2=Info
    var MaxLogCount     by st.observable("MaxLogCount",     5000)

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