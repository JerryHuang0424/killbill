package org.wit.killbill.notifyServer

import android.service.notification.StatusBarNotification

interface NotifyListener {
    /**
     * Received notification bar message
     * @param type
     */
    fun onReceiveMessage (sbn: StatusBarNotification?)

}