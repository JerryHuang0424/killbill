package org.wit.killbill.activity

import android.app.Service
import android.content.Intent
import android.os.IBinder

class NotifyActivity: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }
}