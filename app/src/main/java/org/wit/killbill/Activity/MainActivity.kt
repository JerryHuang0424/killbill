package org.wit.killbill.Activity

import NotifyHelper
import android.content.Intent
import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.wit.killbill.NotifyServer.NotifyListener
import org.wit.killbill.NotifyServer.NotifyService
import org.wit.killbill.R
import java.util.Locale
import java.util.Date
import java.text.SimpleDateFormat
import java.util.TimeZone

class MainActivity : AppCompatActivity(), NotifyListener {
    companion object {
        private const val REQUEST_CODE = 9527
    }

    private lateinit var textView: TextView

    private lateinit var notifyService: NotifyService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textView)
        NotifyHelper.getInstance().setNotifyListener(this)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }



    override fun onReceiveMessage(sbn: StatusBarNotification?) {
        // 1. 空安全检查
        val notification = sbn?.notification ?: return

        // 2. 获取消息内容（使用安全调用和空合并操作符）
        val msgContent = notification.tickerText?.toString() ?: ""

        // 3. 格式化时间（添加时区处理）
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE).apply {
            timeZone = TimeZone.getDefault() // 使用系统默认时区
        }.format(Date(sbn.postTime))

        // 4. 更安全的UI更新（使用runOnUiThread）
        runOnUiThread {
            textView.text = """
            应用包名：${sbn.packageName}
            消息内容：$msgContent
            消息时间：$time
          """.trimIndent()
        }
    }
    /**
     * 请求通知监听权限
     * @param view 触发视图
     */
    fun requestPermission(view: View) {
        // 方法1：直接跳转通知监听权限设置页（推荐）
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)

        // 方法2：检查并提示用户手动开启（可选）
        if (!NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)) {
            Toast.makeText(this, "请找到本应用并开启通知监听权限", Toast.LENGTH_LONG).show()
        }
    }

    private fun showMsg(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (notifyService.isNLServiceEnabled()) {
                showMsg("通知服务已开启")
                notifyService.toggleNotificationListenerService(true)
            } else {
                showMsg("通知服务未开启")
                notifyService.toggleNotificationListenerService(false)
            }
        }
    }

}