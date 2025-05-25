package org.wit.killbill.backGroundService


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import org.wit.killbill.R
import org.wit.killbill.activity.PageMainActivity
import org.wit.killbill.helper.messageHelper
import org.wit.killbill.models.NotifyHelper
import org.wit.killbill.models.NotifyModel
import org.wit.killbill.notifyServer.NotifyListener
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackGroundService : Service(), NotifyListener {

    private var isForeground = false
    private var notifyModel = NotifyModel()
    private val mshelper: messageHelper = messageHelper()



    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Timber.i("Background server created")
        NotifyHelper.getInstance().setNotifyListener(this)
        Timber.i("Notify Listening start...")
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "killbill_channel",
                "Background Service",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)

            val notification = NotificationCompat.Builder(this, "killbill_channel")
                .setContentTitle("KillBill Service")
                .setContentText("Running in background")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build()

            startForeground(1, notification) // 启动前台服务
        }
        return START_STICKY
    }
    override fun onReceiveMessage(sbn: StatusBarNotification?) {
        // 1. 空安全检查
        val notification = sbn?.notification ?:return
        // 2. 获取消息内容（使用安全调用和空合并操作符）
        val packageName = sbn.packageName?.toString()?:""
        val contextOri = notification.tickerText?.toString() ?: ""
        val parts= contextOri.split(":")
        val money_message = mshelper.dealMessage(parts.getOrElse(1){""})
        // 步骤1：转换为浮点数（若输入是整数如"158"，会自动补.0）
        val amount = money_message?.toDoubleOrNull() ?: 0.0
        // 步骤2：四舍五入到小数点后两位
        val roundedAmount = "%.2f".format(amount).toDouble()
        notifyModel.amount  = roundedAmount
         notifyModel.context = parts.getOrElse(1) { "" } // 第二个元素或空字符串

        // 3. 格式化时间（添加时区处理）
        notifyModel.time =
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINESE).format(Date(sbn.postTime))

//        if(notifyModel.context.isNotEmpty() && mshelper.checkTargetPackageName(packageName) && mshelper.checkPaymentTitle(notifyModel.title)){
//            val intent = Intent(this, PageMainActivity::class.java)
//            intent.putExtra("NOTIFICATION_DATA", notifyModel) // 传递整个通知对象
//        }
        if(notifyModel.context.isNotEmpty()){
            val intent = Intent(this, PageMainActivity::class.java)
            intent.putExtra("NOTIFICATION_DATA", notifyModel) // 传递整个通知对象
//            getResult.launch(intent)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        Timber.i("Background service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO()
    }

}
