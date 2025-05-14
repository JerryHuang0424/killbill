package org.wit.killbill.NotifyServer

import android.content.ComponentName
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotifyService : NotificationListenerService() {

    companion object {
        private const val TAG = "NotifyService"

        // 定义常用应用包名常量
        private const val QQ = "com.tencent.mobileqq"          // QQ
        private const val WX = "com.tencent.mm"                // 微信

    }

    /**
     * 当通知发布时调用
     * @param sbn 状态栏通知对象
     */
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let { notification ->
            when (notification.packageName) {
                MESSAGES, MMS, HONOR_MMS -> Log.d(TAG, "收到短信")
                QQ -> Log.d(TAG, "收到QQ消息")
                WX -> Log.d(TAG, "收到微信消息")
                IN_CALL -> Log.d(TAG, "收到来电")
                else -> Unit // 不做任何操作
            }
        }
    }

    /**
     * 当通知被移除时调用
     * @param sbn 状态栏通知对象
     */
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn?.let { notification ->
            when (notification.packageName) {
                QQ -> Log.d(TAG, "移除QQ消息")
                WX -> Log.d(TAG, "移除微信消息")
                else -> Unit // 不做任何操作
            }
        }
    }

    /**
     * 当监听服务断开连接时调用
     */
    override fun onListenerDisconnected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 请求重新绑定服务
            requestRebind(ComponentName(this, NotificationListenerService::class.java))
        }
    }
}