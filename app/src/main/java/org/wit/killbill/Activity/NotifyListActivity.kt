package org.wit.killbill.Activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.wit.killbill.Activity.NotifyListActivity.Companion.REQUEST_CODE
import org.wit.killbill.Main.MainApp
import org.wit.killbill.NotifyServer.NotifyListener
import org.wit.killbill.NotifyServer.NotifyService
import org.wit.killbill.databinding.ActivityListMainBinding
import org.wit.killbill.databinding.CardBinding
import org.wit.killbill.models.NotifyModel
import org.wit.killbill.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import timber.log.Timber

class NotifyListActivity : AppCompatActivity(), NotifyListener{
    lateinit var app: MainApp
    private lateinit var binding: ActivityListMainBinding
    private lateinit var notifyService: NotifyService
    var notifyModel = NotifyModel()

    companion object {
        private const val REQUEST_CODE = 9527
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.title = title
        setSupportActionBar(binding.toolbar)

        app = application as MainApp

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = NotifyAdapter(app.notifyNotifyModels)

        //把NotifyListMain页面注册通知监听
        NotifyHelper.getInstance().setNotifyListener(this)

    }

    override fun onReceiveMessage(sbn: StatusBarNotification?) {
        // 1. 空安全检查

        val notification = sbn?.notification ?:return
        // 2. 获取消息内容（使用安全调用和空合并操作符）
        notifyModel.packageName = sbn.packageName?.toString()?:""
        val contextOri = notification.tickerText?.toString() ?: ""
        val parts= contextOri.split(":")
        notifyModel.title = parts.getOrElse(0) { "" }  // 第一个元素或空字符串
        notifyModel.context = parts.getOrElse(1) { "" } // 第二个元素或空字符串

        // 3. 格式化时间（添加时区处理）
        notifyModel.time =
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINESE).format(Date(sbn.getPostTime()))

        if(notifyModel.context.isNotEmpty()){
            app.notifyNotifyModels.add(notifyModel.copy())
            for (i in app.notifyNotifyModels.indices) {
                Timber.i("NotifyModels[$i]:${this.app.notifyNotifyModels[i]}")
            }
            val intent = Intent(this, PageMainActivity::class.java).apply {
                putExtra("NOTIFICATION_DATA", notifyModel) // 传递整个通知对象
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
        }

//        (binding.recyclerView.adapter)?.notifyItemRangeChanged(0, app.notifyNotifyModels.size)
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
        return super.onOptionsItemSelected(item)
    }

    private val getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){
            (binding.recyclerView.adapter)?.notifyItemRangeChanged(0,app.notifyNotifyModels.size)
        }
    }

    /**
     * 请求通知监听权限
     * @param view 触发视图
     */
    //用户点击按钮触发 requestPermission(), binding with the button in the layout:
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





class NotifyAdapter constructor(private var notifies: List<NotifyModel>) :
    RecyclerView.Adapter<NotifyAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val notify = notifies[holder.adapterPosition]
        holder.bind(notify)
    }

    override fun getItemCount(): Int = notifies.size

    class MainHolder(private val binding : CardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notify: NotifyModel) {
            binding.tvTitle.text = notify.title
            binding.tvPackageName.text = notify.packageName
            binding.tvContent.text = notify.context
            binding.tvTime.text = notify.time
        }
    }
}