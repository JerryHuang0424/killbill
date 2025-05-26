package org.wit.killbill.activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import org.wit.killbill.R
import org.wit.killbill.adapter.NotifyAdapter
import org.wit.killbill.adapter.NotifyAdapterListener
import org.wit.killbill.backGroundService.BackGroundService
import org.wit.killbill.databinding.ActivityListMainBinding
import org.wit.killbill.main.MainApp
import org.wit.killbill.helper.messageHelper
import org.wit.killbill.models.NotifyHelper
import org.wit.killbill.models.NotifyModel
import org.wit.killbill.notifyServer.NotifyListener
import org.wit.killbill.notifyServer.NotifyService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class NotifyListActivity : AppCompatActivity(), NotifyListener, NotifyAdapterListener{
    lateinit var app: MainApp
    private lateinit var binding: ActivityListMainBinding
    private lateinit var notifyService: NotifyService
    private var notifyModel = NotifyModel()
    private val mshelper: messageHelper = messageHelper()
    private var position: Int = 0

    companion object {
        private const val REQUEST_CODE = 9527
    }



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.title = title
        setSupportActionBar(binding.toolbar)

        app = application as MainApp

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = NotifyAdapter(app.notifyNotifyModels.findAll(), this)


        //把NotifyListMain页面注册通知监听
//        NotifyHelper.getInstance().setNotifyListener(this)

        val intent = Intent(this, BackGroundService::class.java)
        startForegroundService(intent)
    }


    override fun onReceiveMessage(sbn: StatusBarNotification?) {
        // 1. 空安全检查

        val notification = sbn?.notification ?:return
        // 2. 获取消息内容（使用安全调用和空合并操作符）
        val packageName = sbn.packageName?.toString()?:""
        val contextOri = notification.tickerText?.toString() ?: ""
        val parts= contextOri.split(":")
        val Source = parts.getOrElse(0) { "" }  // 第一个元素或空字符串
        val money_message = mshelper.dealMessage(parts.getOrElse(1){""})
        val amount = money_message?.toDoubleOrNull() ?: 0.0
        // 步骤2：四舍五入到小数点后两位
        val roundedAmount = "%.2f".format(amount).toDouble()
        notifyModel.amount = roundedAmount
        notifyModel.context = "测试接收消息"


        // 3. 格式化时间（添加时区处理）
        notifyModel.time =
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINESE).format(Date(sbn.postTime))

        if(notifyModel.context.isNotEmpty() && mshelper.checkTargetPackageName(packageName) && mshelper.checkPaymentTitle(Source)){
            val intent = Intent(this, PageMainActivity::class.java)
            intent.putExtra("NOTIFICATION_DATA", notifyModel) // 传递整个通知对象
            getResult.launch(intent)
        }
//        if(notifyModel.context.isNotEmpty()){
//            val intent = Intent(this, PageMainActivity::class.java)
//            intent.putExtra("NOTIFICATION_DATA", notifyModel) // 传递整个通知对象
//            getResult.launch(intent)
//        }
    }

    override fun onCardClick(notify: NotifyModel, pos: Int) {
        val launchIntentCard = Intent(this, PageMainActivity::class.java)
        launchIntentCard.putExtra("Notify_edit", notify)
        position = pos
        getClickResult.launch(launchIntentCard)
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        menuInflater.inflate(R.menu.menu_setting, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.item_add -> {
                val launchIntent = Intent(this, PageMainActivity::class.java)
                getResult.launch(launchIntent)
            }
        }
        when(item.itemId){
            R.id.item_setting -> {
                requestPermission()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){
            (binding.recyclerView.adapter)?.notifyItemRangeChanged(0,app.notifyNotifyModels.findAll().size)
        }
    }

    private val getClickResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode ==Activity.RESULT_OK){
            (binding.recyclerView.adapter)?.notifyItemRangeChanged(0,app.notifyNotifyModels.findAll().size)
        }
        if(it.resultCode ==99){
                (binding.recyclerView.adapter)?.notifyItemRemoved(position)
        }
    }

    /**
     * 请求通知监听权限
     */
    //用户点击按钮触发 requestPermission(), binding with the button in the layout:
    private fun requestPermission() {
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

