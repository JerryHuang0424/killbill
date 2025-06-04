package org.wit.killbill.notifyServer

import android.service.notification.StatusBarNotification

interface NotifyListener {
    /**
     * 接收到通知栏消息
     * @param type
     */
    fun onReceiveMessage (sbn: StatusBarNotification?)

}