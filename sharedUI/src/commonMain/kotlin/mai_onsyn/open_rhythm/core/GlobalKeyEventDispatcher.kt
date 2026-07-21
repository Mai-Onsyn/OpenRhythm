package mai_onsyn.open_rhythm.core

import androidx.compose.ui.input.key.KeyEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class GlobalKeyEventDispatcher(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val capacity: Int = Channel.BUFFERED
) {
    // 事件通道
    private val eventChannel = Channel<KeyEvent>(capacity, BufferOverflow.DROP_OLDEST)

    // 处理器列表（顺序敏感）
    private val handlers = mutableListOf<suspend (KeyEvent) -> Boolean>()

    // 是否已经启动分发循环
    private var isDispatching = false

    /**
     * 注册一个事件处理器。
     * 处理器按注册顺序被调用，返回 true 表示消费事件，后续处理器将不再被调用。
     */
    fun registerHandler(handler: suspend (KeyEvent) -> Boolean) {
        handlers.add(handler)
        ensureDispatchingStarted()
    }

    /**
     * 移除处理器（按引用相等性）
     */
    fun unregisterHandler(handler: suspend (KeyEvent) -> Boolean) {
        handlers.remove(handler)
    }

    /**
     * 将原始键盘事件推入队列
     */
    fun push(event: KeyEvent) {
        eventChannel.trySend(event)
        ensureDispatchingStarted()
    }

    /**
     * 关闭分发器，停止接收新事件，释放资源
     */
    fun close() {
        eventChannel.close()
    }

    // 确保分发循环只启动一次
    private fun ensureDispatchingStarted() {
        if (!isDispatching) {
            isDispatching = true
            scope.launch {
                try {
                    for (event in eventChannel) {
                        dispatchEvent(event)
                    }
                } catch (_: Exception) {
                    // 通道关闭时退出
                } finally {
                    isDispatching = false
                }
            }
        }
    }

    // 核心分发逻辑：顺序调用处理器
    private suspend fun dispatchEvent(event: KeyEvent) {
        // 复制处理器列表，防止注册/注销在遍历过程中引发并发修改
        val snapshot = handlers.toList()
        for (handler in snapshot) {
            try {
                val consumed = handler(event)
                if (consumed) {
                    // 事件被消费，停止传递
                    break
                }
            } catch (e: Exception) {
                // 单个处理器异常不影响其他处理器，可根据需要记录日志
                // 继续下一个处理器
            }
        }
    }
}