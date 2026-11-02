package mai_onsyn.open_rhythm.ui.modules.midi_flow.waterfalls.gpu.pool

class InstanceSlot(
    var vboPtr: Int,
    var chunk: NoteChunk?,
    var uploaded: Boolean
) {
    // 创建vbo
    fun initialize() {

    }

    // 复用已有vbo
    fun upload() {
        TODO()
    }

    // 完全删除vbo 删除后这个变量必须再次initialize
    fun destroy() {

    }

    override fun toString(): String {
        return "InstanceSlot($chunk, uploaded=$uploaded, vbo=$vboPtr)"
    }
}