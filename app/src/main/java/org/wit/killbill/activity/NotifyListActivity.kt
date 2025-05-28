package org.wit.killbill.activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.permissionx.guolindev.PermissionX
import org.wit.killbill.R
import org.wit.killbill.adapter.NotifyAdapter
import org.wit.killbill.adapter.NotifyAdapterListener
import org.wit.killbill.backGroundService.BackGroundService
import org.wit.killbill.databinding.ActivityListMainBinding
import org.wit.killbill.main.MainApp
//import org.wit.killbill.helper.messageHelper
import org.wit.killbill.models.NotifyModel
import org.wit.killbill.notifyServer.NotifyService
import java.util.Calendar


class NotifyListActivity : AppCompatActivity(), NotifyAdapterListener{
    lateinit var app: MainApp
    private lateinit var binding: ActivityListMainBinding
    private lateinit var notifyService: NotifyService
//    private var notifyModel = NotifyModel()
//    private val mshelper: messageHelper = messageHelper()
    private var position: Int = 0
    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshInterval = 1000L // 1秒 = 1000毫秒

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

        //设置特定的时间，筛选全部账单中符合时间规定
        val tempList = app.notifyNotifyModels.findAll()
        //把时间设置为本年本月，筛选本月创建的账单
        // 获取当前的年份和月份
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1  // Calendar.MONTH 返回 0-11

        val currentYearMonthList = mutableListOf<NotifyModel>()

        for (notifyModel in tempList) {
            // 假设 notifyModel.time 是 "yyyy-MM" 格式（例如 "2023-09"）
            val dateParts = notifyModel.time.split("-")
            if (dateParts.size >= 2) {
                val modelYear = dateParts[0].toInt()
                val modelMonth = dateParts[1].toInt()

                // 检查年份和月份是否都匹配
                if (modelYear == currentYear && modelMonth == currentMonth) {
                    currentYearMonthList.add(notifyModel)
                }
            }
        }

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = NotifyAdapter(app.notifyNotifyModels.findAll(), this)
        //把Adapter里面的NotifyModel改为筛选过的
//        binding.recyclerView.adapter = NotifyAdapter(currentMonthList, this)

        //每秒执行一次界面刷新
        startAutoRefresh()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && !PermissionX.isGranted(this, android.Manifest.permission.POST_NOTIFICATIONS)) {
            PermissionX.init(this)
                .permissions(android.Manifest.permission.POST_NOTIFICATIONS)
                .request { allGranted, _, _ ->
                if (allGranted) {
                    val intent = Intent(this, BackGroundService::class.java)
                    startForegroundService(intent)
                }
            }
        } else {
            val intent = Intent(this, BackGroundService::class.java)
            startForegroundService(intent)
        }

    }

    override fun onCardClick(notify: NotifyModel, pos: Int) {
        val launchIntentCard = Intent(this, PageMainActivity::class.java)
        launchIntentCard.putExtra("Notify_edit", notify)
        position = pos
        getClickResult.launch(launchIntentCard)
    }

    private val refreshRunnable = object : Runnable {
        override fun run() {
            // 获取最新数据并更新 Adapter
            (binding.recyclerView.adapter)?.notifyItemRangeChanged(0,app.notifyNotifyModels.findAll().size)
            // 再次延迟执行（实现循环）
            refreshHandler.postDelayed(this, refreshInterval)
        }
    }

    private fun startAutoRefresh() {
        refreshHandler.postDelayed(refreshRunnable, refreshInterval)
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
                getResult.launch(launchIntent)            }
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

