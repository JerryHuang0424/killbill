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
     * Call when notification is published
     * @param sbn Status bar notification object
     */
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        NotifyHelper.getInstance().onReceive(sbn)
    }

    /**
     * Call when the listening service disconnects
     */
    override fun onListenerDisconnected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Request to rebind service
            requestRebind(ComponentName(this, NotificationListenerService::class.java))
        }
    }

    /**
     * Do you want to enable notification listening service
     * Is @return Boolean enabled
     */
    fun isNLServiceEnabled(): Boolean {
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(this)
        return packageNames.contains(packageName)
    }

    /**
     * Switch notification listener service
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