package org.wit.killbill.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.permissionx.guolindev.PermissionX
import org.wit.killbill.R
import org.wit.killbill.adapter.NotifyAdapter
import org.wit.killbill.adapter.NotifyAdapterListener
import org.wit.killbill.backGroundService.BackGroundService
import org.wit.killbill.databinding.DailyStatusBinding
import org.wit.killbill.main.MainApp
import org.wit.killbill.models.NotifyModel
import java.util.Calendar

class dailyActivity: AppCompatActivity(), NotifyAdapterListener {
    lateinit var app : MainApp
    private lateinit var binding: DailyStatusBinding
    private var position: Int = 0
    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshInterval = 1000L // 1秒 = 1000毫秒

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DailyStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        app = application as MainApp

        //每秒执行一次界面刷新
        startAutoRefresh()

        val bottomNav = findViewById<BottomNavigationView?>(R.id.bottom_navigation)
        bottomNav.setOnNavigationItemSelectedListener(navListener)
        binding.bottomNavigation.selectedItemId = R.id.navigation_stats

        //设置特定的时间，筛选全部账单中符合时间规定
        //把时间设置为本年本月，筛选本月创建的账单
        //获取当前的年份和月份
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1  // 月份从0开始
        val currentDay = calendar.get(Calendar.DATE)

        val currentList = mutableListOf<NotifyModel>()
        for (notifyModel in app.notifyNotifyModels.findAll()) {
                // 先按空格分割日期和时间部分
                val dateTimeParts = notifyModel.time.split(" ")
                if (dateTimeParts.isNotEmpty()) {
                    // 只处理日期部分 (如 "2023-07-25")
                    val dateParts = dateTimeParts[0].split("-")
                    if (dateParts.size >= 3) {  // 确保有年、月、日三部分
                        val modelYear = dateParts[0].toInt()
                        val modelMonth = dateParts[1].toInt()
                        val modelDay = dateParts[2].toInt()

                        // 检查是否匹配当天
                        if (modelYear == currentYear &&
                            modelMonth == currentMonth &&
                            modelDay == currentDay) {
                            currentList.add(notifyModel)
                        }
                    }
                }
        }

        binding.tvDate.text = "${currentYear}年${currentMonth}月${currentDay}日"

        binding.tvTotalAmount.text = currentList.sumOf { it.amount }.toString()
        val layoutManager = LinearLayoutManager(this)
        binding.rvBillList.layoutManager = layoutManager
        binding.rvBillList.adapter = NotifyAdapter(currentList, this)
    }

    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val targetActivity = when (item.itemId) {
            R.id.navigation_today -> NotifyListActivity::class.java
            R.id.navigation_stats -> statisticActivity::class.java
//            R.id.navigation_settings -> dailyActivity::class.java
            else -> null
        }

        targetActivity?.let {
            if (this::class.java != it) {  // 检查当前Activity是否已经是目标Activity
                startActivity(Intent(this, it))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        } ?: false

        true  // 总是返回true表示处理了点击事件
    }

    private fun startAutoRefresh() {
        refreshHandler.postDelayed(refreshRunnable, refreshInterval)
    }

    private val refreshRunnable = object : Runnable {
        override fun run() {
            // 获取最新数据并更新 Adapter
            (binding.rvBillList.adapter)?.notifyItemRangeChanged(0,app.notifyNotifyModels.findAll().size)
            // 再次延迟执行（实现循环）
            refreshHandler.postDelayed(this, refreshInterval)
        }
    }

    override fun onCardClick(notify: NotifyModel, pos: Int) {
        val launchIntentCard = Intent(this, PageMainActivity::class.java)
        launchIntentCard.putExtra("Notify_edit", notify)
        position = pos
        getClickResult.launch(launchIntentCard)
    }

    private val getClickResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == RESULT_OK){
            (binding.rvBillList.adapter)?.notifyItemRangeChanged(0,app.notifyNotifyModels.findAll().size)
        }
        if(it.resultCode ==99){
            (binding.rvBillList.adapter)?.notifyItemRemoved(position)
        }
    }
}