package mai_onsyn.open_rhythm.core.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.russhwolf.settings.Settings
import kotlinx.serialization.builtins.serializer
import mai_onsyn.open_rhythm.ui.pages.library.UILibraryFolder
import mai_onsyn.open_rhythm.ui.pages.setting.categories.key_map.KeyMidiMapping
import mai_onsyn.open_rhythm.ui.theme.TrackColorDefaults

class UserSetting(
    private val st: Settings
) {
    // =====General Appearance=====
    var DarkMode                        by st.observable("DarkMode",                        2)  // 0 = light; 1 = dark; 2 = system default
    var PrimarySeedColor                by st.observable("PrimarySeedColor",                Color(0xFF485F84))
    var UserSpecifiedPrimarySeedColor   by st.observable("UserSpecifiedPrimarySeedColor",   Color(0xFFCD20ED))
    var StatusBarShadow                 by st.observable("StatusBarShadow",                 true)
    var ShowMidiDeviceInfoInHome        by st.observable("ShowMidiDeviceInfoInHome",        true)

    // =====General Interaction=====
    var DoubleClickToPlayPause      by st.observable("DoubleClickToPlayPause",      false)
    var DoubleFingerTapToPlayPause  by st.observable("DoubleFingerTapToPlayPause",  true)
    var AutoStartPlayback           by st.observable("AutoStartPlayback",           true)
    var PlaybackStartDistance       by st.observable("PlaybackStartDistance",       4)
    var MobileScreenOrientation     by st.observable("MobileScreenOrientation",     2)  // 0=vertical; 1=horizontal; 2=any; 3=system

    // =====General Security=====
    var ShowFolderPathInLibrary     by st.observable("ShowFolderPathInLibrary",     true)

    // =====MIDI Input=====
    val enabledMidiInputDeviceList  by st.list("EnabledMidiInputDeviceList",    mutableListOf("Virtual Keyboard"), String.serializer())
    var EnableInputMidiNoteEvent    by st.observable("EnableInputMidiNoteEvent",    true)
    var EnableInputMidiCCEvent      by st.observable("EnableInputMidiCCEvent",      true)
    var EnableInputMidiPCEvent      by st.observable("EnableInputMidiPCEvent",      true)
    var EnableInputMidiPBEvent      by st.observable("EnableInputMidiPCBEvent",     true)

    // =====MIDI Output=====
    var SelectedOutputDeviceName    by st.observable("SelectedOutputDeviceName",    "")
    var EnableOutputMidiNoteEvent   by st.observable("EnableOutputMidiNoteEvent",   true)
    var EnableOutputMidiCCEvent     by st.observable("EnableOutputMidiCCEvent",     true)
    var EnableOutputMidiPCEvent     by st.observable("EnableOutputMidiPCBEvent",    true)
    var EnableOutputMidiPBEvent     by st.observable("EnableOutputMidiPCBEvent",    true)
    var GervillSF2Path              by st.observable("GervillSF2Path",              "")

    // =====MIDI Track=====
    val trackColors             by st.list("TrackColors",                   TrackColorDefaults.colors(), ColorSerializer())
    var MidiInteractionColor    by st.observable("MidiInteractionColor", Color(138, 226, 52))
    var MidiInteractionChannel  by st.observable("MidiInteractionChannel",  0)
    var DrumKitHiddenByDefault  by st.observable("DrumKitHiddenByDefault",  true)

    // =====MIDI File=====
    var UseParserV1 by st.observable("UseParserV1", false)

    // =====Key Mapping=====
    val userKeyMappings by st.list("UserKeyMappings", KeyMidiMapping.default(), KeyMidiMapping.Companion.serializer())

    // =====Waterfall Background=====
    var WaterfallBackgroundColor        by st.observable("WaterfallBackgroundColor",        Color.Unspecified)
    var CustomWaterfallBackgroundColor  by st.observable("CustomWaterfallBackgroundColor", Color(48, 48, 48))
    var BackgroundImageDir              by st.observable("BackgroundImageDir",              "")
    var BackgroundImageOpacity          by st.observable("BackgroundImageOpacity",          0.3f)
    var BackgroundImageBlurDp           by st.observable("BackgroundImageBlurDp",           0f)
    var ImageExpandToKeyboard           by st.observable("ImageExpandToKeyboard",           false)
    var OriginalBackgroundImageSize     by st.observable("OriginalBackgroundImageSize",     false)
    var DrawOctaveLines                 by st.observable("DrawOctaveLines",                 true)
    var OctaveLineColor                 by st.observable("OctaveLineColor",                 Color.LightGray)
    var OctaveLineThickness             by st.observable("OctaveLineThickness",             0.7f)
    var DrawSectionLines                by st.observable("DrawSectionLines",                true)
    var SectionLineColor                by st.observable("SectionLineColor",                Color.LightGray)
    var SectionLineThickness            by st.observable("SectionLineThickness",            0.7f)

    // =====Note Appearance=====
    var NoteRoundConerPercent   by st.observable("NoteRoundConerPercent",   0.5f)
    var QuarterNoteDpHeight     by st.observable("QuarterNoteDpHeight",     120f)
    var DrawPitchLabels         by st.observable("DrawPitchLabels",         false)
    var DrawNoteShadow          by st.observable("DrawNoteShadow",          true)
    var NoteOpacity             by st.observable("NoteOpacity",             1f)
    var OpacityAffectKeyboard   by st.observable("OpacityAffectKeyboard",   false)

    // =====Keyboard Appearance=====
    var KeyboardAutoAspect              by st.observable("KeyBoardAutoAspect",          true)
    var KeyboardAspectRatio             by st.observable("KeyBoardAspectRatio",         8f)
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

    // =====Overlay=====
    var ShowCurrentTick         by st.observable("ShowCurrentTick",             false)
    var ShowFps                 by st.observable("ShowFps",                     false)
    var ShowFrameTime           by st.observable("ShowFrameTime",               false)
    var ShowRenderingNoteCount  by st.observable("ShowRenderingNoteCount",      false)
    var ShowActiveNoteCount     by st.observable("ShowActiveNoteCount",         false)

    // =====User Data=====
    val libraryFolderList by st.list("LibraryFolderList", mutableListOf(), UILibraryFolder.serializer())
    val midiFileSettings by st.map("MidiFileSettings", mutableMapOf(), String.serializer(), MidiFileSettings.serializer())

    fun resetAllSettings() = st.clear()
    fun clearUserMidiFileSettings() = st.remove("MidiFileSettings")
    fun clearLibraries() = st.remove("LibraryFolderList")
}