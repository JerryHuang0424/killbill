package org.wit.killbill.backGroundService

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.service.notification.StatusBarNotification
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import org.wit.killbill.R
import org.wit.killbill.activity.NotifyListActivity
import org.wit.killbill.activity.PageMainActivity
import org.wit.killbill.helper.messageHelper
import org.wit.killbill.models.NotifyHelper
import org.wit.killbill.models.NotifyModel
import org.wit.killbill.notifyServer.NotifyListener
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class BackGroundService : Service(), NotifyListener {

    private var isForeground = false
    private var notifyModel = NotifyModel()
    private val mshelper: messageHelper = messageHelper()
    private lateinit var notificationManager: NotificationManager
    private val NOTIFICATION_ID = 101
    private val CHANNEL_ID = "killbill_auto_accounting_channel"

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Timber.i("Background server created")
        NotifyHelper.getInstance().setNotifyListener(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        Timber.i("Notify Listening start...")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start as foreground service with custom notification
        startForegroundServiceWithCustomNotification()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Auto Accounting Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when KillBill is automatically recording transactions"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundServiceWithCustomNotification() {
        // Create custom notification view
        val contentView = RemoteViews(packageName, R.layout.notification)

        // Set any dynamic content if needed
        contentView.setTextViewText(R.id.notification_status, "Monitoring transactions...")

        // Create pending intent for when notification is tapped
        val notificationIntent = Intent(this, NotifyListActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setCustomContentView(contentView)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        // Start foreground service
        startForeground(NOTIFICATION_ID, notification)
        Timber.i("Foreground service started...")
        isForeground = true
    }

    override fun onReceiveMessage(sbn: StatusBarNotification?) {
        val notification = sbn?.notification ?: return
        val packageName = sbn.packageName?.toString() ?: ""
        val contextOri = notification.tickerText?.toString() ?: ""
        val parts = contextOri.split(":")
        val money_message = mshelper.dealMessage(parts.getOrElse(1) { "" })

        // Process amount
        val amount = money_message?.toDoubleOrNull() ?: 0.0
        val roundedAmount = "%.2f".format(amount).toDouble()
        notifyModel.amount = roundedAmount
        notifyModel.context = parts.getOrElse(1) { "" }

        // Format time
        notifyModel.time = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINESE)
            .format(Date(sbn.postTime))

        // Update notification with transaction info
        updateNotificationWithTransaction(roundedAmount)

        if (notifyModel.context.isNotEmpty()) {
            val intent = Intent(this, PageMainActivity::class.java)
            intent.putExtra("NOTIFICATION_DATA", notifyModel)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun updateNotificationWithTransaction(amount: Double) {
        val contentView = RemoteViews(packageName, R.layout.notification)

        // Update notification content
        contentView.setTextViewText(
            R.id.notification_status,
            "Recorded transaction: ¥${"%.2f".format(amount)}"
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setCustomContentView(contentView)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        notificationManager.cancel(NOTIFICATION_ID)
        Timber.i("Background service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Not a bound service
    }
}