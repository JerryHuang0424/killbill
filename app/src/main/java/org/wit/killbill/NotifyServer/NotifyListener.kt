package org.wit.killbill.NotifyServer


interface NotifyListener {
    /**
     * 接收到通知栏消息
     * @param type
     */
    fun onReceiveMessage(type: Int)

    /**
     * 移除掉通知栏消息
     * @param type
     */
    fun onRemovedMessage(type: Int)
}