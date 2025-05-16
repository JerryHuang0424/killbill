package org.wit.killbill.NotifyServer

import android.util.Log
import org.wit.killbill.NotifyServer.NotifyService.Companion.Alipay
import org.wit.killbill.NotifyServer.NotifyService.Companion.HONOR_MMS
import org.wit.killbill.NotifyServer.NotifyService.Companion.QQ
import org.wit.killbill.NotifyServer.NotifyService.Companion.TAG
import org.wit.killbill.NotifyServer.NotifyService.Companion.WX
import NotifyHelper
import android.service.notification.StatusBarNotification

interface NotifyListener {
    /**
     * 接收到通知栏消息
     * @param type
     */
    fun onReceiveMessage (sbn: StatusBarNotification?)

}