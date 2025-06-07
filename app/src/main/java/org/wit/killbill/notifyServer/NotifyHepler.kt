package org.wit.killbill.models

import android.service.notification.StatusBarNotification
import org.wit.killbill.notifyServer.NotifyListener


class NotifyHelper {

    companion object {
        @Volatile
        private var instance: NotifyHelper? = null

        // Registration notification monitoring
        fun getInstance(): NotifyHelper {
            return instance ?: synchronized(this) {
                instance ?: NotifyHelper().also { instance = it }
            }
        }
    }

    private var notifyListener: NotifyListener? = null

    /**
     * receive messages
     * @param sbn status bar notification
     */
    fun onReceive(sbn: StatusBarNotification?) {
        if (notifyListener != null) {
            notifyListener!!.onReceiveMessage(sbn)
        }
    }

    /**
     * Set callback method
     *
     * @param notifyListener Notify monitoring
     */
    fun setNotifyListener(notifyListener: NotifyListener) {
        this.notifyListener = notifyListener
    }
}