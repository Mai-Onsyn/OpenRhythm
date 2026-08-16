package mai_onsyn.open_rhythm.bridge

import co.touchlab.kermit.Logger
import dev.atsushieno.ktmidi.MidiOutput
import java.io.File
import javax.sound.midi.MidiSystem
import javax.sound.midi.Synthesizer

fun loadSoundbankToKtmidiOutput(output: MidiOutput, sf2File: File): Boolean {
    try {
        // 从 JvmMidiOutput 中反射获取 private 字段 'port'
        val portField = output.javaClass.getDeclaredField("port").apply {
            isAccessible = true
        }
        val port = portField.get(output) ?: return false

        // 从 JvmMidiReceiverPortDetails 中反射获取字段 'device'
        val deviceField = port.javaClass.getDeclaredField("device").apply {
            isAccessible = true
        }
        val device = deviceField.get(port) as? Synthesizer ?: return false

        // 确保设备已打开
        if (!device.isOpen) {
            device.open()
        }

        // 读取 SF2 并加载到该 Synthesizer 实例中
        val soundbank = MidiSystem.getSoundbank(sf2File)

        // 卸载默认音色库
        device.defaultSoundbank?.let { defaultSb ->
            device.unloadAllInstruments(defaultSb)
        }

        val success = device.loadAllInstruments(soundbank)
        Logger.i { "SF2 loaded: $success, internal instruments count: ${device.loadedInstruments.size}" }
        return success

    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}