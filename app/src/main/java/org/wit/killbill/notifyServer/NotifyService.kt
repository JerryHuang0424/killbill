package org.wit.killbill.notifyServer

import android.content.ComponentName
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import android.content.pm.PackageManager
import org.wit.killbill.models.NotifyHelper

class NotifyService : NotificationListenerService() {

    /**
     * 当通知发布时调用
     * @param sbn 状态栏通知对象
     */
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        NotifyHelper.getInstance().onReceive(sbn)
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