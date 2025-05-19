package org.wit.killbill.notifyServer

import android.content.ComponentName
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import android.content.pm.PackageManager
import org.wit.killbill.models.NotifyHelper

class NotifyService : NotificationListenerService() {

    companion object {
        public const val TAG = "NotifyService"

        // 定义常用应用包名常量
        public const val QQ = "com.tencent.mobileqq"          // QQ
        public const val WX = "com.tencent.mm"                // 微信
        public const val HONOR_MMS= "com.hihonor.mms";        //荣耀短信
        public const val Alipay="com.eg.android.AlipayGphone" //支付宝

    }

    /**
     * 当通知发布时调用
     * @param sbn 状态栏通知对象
     */
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        NotifyHelper.getInstance().onReceive(sbn)
    }

    /**
     * 当通知被移除时调用
     * @param sbn 状态栏通知对象
     */
//    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
//        sbn?.let { notification ->
//            when (notification.packageName) {
//                HONOR_MMS -> Log.d(TAG, "移除短信")
//                QQ -> Log.d(TAG, "移除QQ消息")
//                WX -> Log.d(TAG, "移除微信消息")
//                Alipay -> Log.d(TAG, "移除支付宝消息")
//            }
//        }
//    }

    /**
     * 当监听服务断开连接时调用
     */
    override fun onListenerDisconnected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 请求重新绑定服务
            requestRebind(ComponentName(this, NotificationListenerService::class.java))
        }
    }

    /**
     * 是否启用通知监听服务
     * @return Boolean 是否启用
     */
    fun isNLServiceEnabled(): Boolean {
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(this)
        return packageNames.contains(packageName)
    }

    /**
     * 切换通知监听器服务
     */
    fun toggleNotificationListenerService(enable: Boolean) {
        val pm = packageManager
        pm.setComponentEnabledSetting(
            ComponentName(applicationContext, NotifyService::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )

        pm.setComponentEnabledSetting(
            ComponentName(applicationContext, NotifyService::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}