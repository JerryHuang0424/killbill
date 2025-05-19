package org.wit.killbill.backGroundService

import android.content.Intent
import android.os.IBinder
import android.app.Service

class BackGroundService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let{

        }
        // 执行后台任务
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清理资源
    }
}