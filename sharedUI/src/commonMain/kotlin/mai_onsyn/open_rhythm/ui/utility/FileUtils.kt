package mai_onsyn.open_rhythm.ui.utility

import kotlin.math.roundToLong

private val FILE_SIZE_UNITS = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB")

/**
 * 将字节数格式化为可读文件大小文本
 * - 1024 进制
 * - 保留 2 位小数（四舍五入），尾随 0 省略
 */
fun Long.toFileSize(): String {
    if (this < 0L) {
        return if (this == Long.MIN_VALUE) "-8EB" else "-" + (-this).toFileSize()
    }
    if (this < 1024L) return "${this}B"

    var value = this.toDouble()
    var unitIndex = 0
    while (value >= 1024.0 && unitIndex < FILE_SIZE_UNITS.lastIndex) {
        value /= 1024.0
        unitIndex++
    }

    val scaled   = (value * 100.0).roundToLong()
    val intPart  = scaled / 100L
    val fracPart = scaled % 100L

    return buildString {
        append(intPart)
        if (fracPart != 0L) {
            append('.')
            if (fracPart < 10L) append('0')
            append(fracPart)
        }
        append(FILE_SIZE_UNITS[unitIndex])
    }
}