package org.wit.killbill.NotifyServer

object NotifyHelper {
        const val N_MESSAGE = 0
    const val N_CALL = 1
    const val N_QQ = 2
    const val N_WX = 3

private var notifyListener: NotifyListener? = null

/**
 * 收到消息
 * @param type 消息类型
 */
fun onReceive(type: Int) {
    notifyListener?.onReceiveMessage(type)
}

/**
 * 移除消息
 * @param type 消息类型
 */
fun onRemoved(type: Int) {
    notifyListener?.onRemovedMessage(type)
}

/**
 * 设置回调方法
 * @param notifyListener 通知监听
 */
fun setNotifyListener(notifyListener: NotifyListener) {
    this.notifyListener = notifyListener
}
}

interface NotifyListener {
    fun onReceiveMessage(type: Int)
    fun onRemovedMessage(type: Int)
}