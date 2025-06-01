package org.wit.killbill.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.data.PieEntry
import org.wit.killbill.databinding.StatisticBinding
import org.wit.killbill.main.MainApp
import org.wit.killbill.models.NotifyModel
import java.util.Calendar
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.wit.killbill.R
import org.wit.killbill.adapter.NotifyAdapter
import org.wit.killbill.adapter.NotifyAdapterListener

class statisticActivity: AppCompatActivity(),NotifyAdapterListener {
    private lateinit var binding: StatisticBinding
    lateinit var app: MainApp
    private var position: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StatisticBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as MainApp

        val bottomNav = findViewById<BottomNavigationView?>(R.id.bottom_navigation)
        bottomNav.setOnNavigationItemSelectedListener(navListener)
        binding.bottomNavigation.selectedItemId = R.id.navigation_stats


        //设置特定的时间，筛选全部账单中符合时间规定
        //把时间设置为本年本月，筛选本月创建的账单
        //获取当前的年份和月份
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1  // Calendar.MONTH 返回 0-11

        val pieChartList = mutableListOf<PieEntry>()
        val currentList = mutableListOf<NotifyModel>()

        for (notifyModel in app.notifyNotifyModels.findAll()) {
            // 假设 notifyModel.time 是 "yyyy-MM" 格式（例如 "2023-09"）
            val dateParts = notifyModel.time.split("-")
            if (dateParts.size >= 2) {
                val modelYear = dateParts[0].toInt()
                val modelMonth = dateParts[1].toInt()

                // 检查年份和月份是否都匹配
                if (modelYear == currentYear && modelMonth == currentMonth) {
                    pieChartList.add(
                        PieEntry(
                            notifyModel.amount.toFloat(),
                            if (notifyModel.type.isBlank()) "其他" else notifyModel.type
                        )
                    )
                    currentList.add(notifyModel)
                }
            }
        }

        val mergedEntries = pieChartList
            .groupBy { it.label } // 按type分组
            .map { (type, entries) ->
                PieEntry(
                    entries.sumOf { it.value.toDouble() }.toFloat(), // 合并amount
                    type
                )
            }

        //设置饼图的数据
        setupPieChart(mergedEntries)
        setupLegend(mergedEntries)

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = NotifyAdapter(currentList, this)


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

    override fun onCardClick(notify: NotifyModel, pos: Int) {
        val launchIntentCard = Intent(this, PageMainActivity::class.java)
        launchIntentCard.putExtra("Notify_edit", notify)
        position = pos
        getClickResult.launch(launchIntentCard)
    }

    private val getClickResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == RESULT_OK){
            (binding.recyclerView.adapter)?.notifyItemRangeChanged(0,app.notifyNotifyModels.findAll().size)
        }
        if(it.resultCode ==99){
            (binding.recyclerView.adapter)?.notifyItemRemoved(position)
        }
    }

    private fun setupPieChart(entries: List<PieEntry>) {
        val pieChart = binding.pieChart

        // 检查数据是否为空
        if (entries.isEmpty()) {
            // 隐藏饼图，显示提示文本
            pieChart.visibility = View.GONE
            binding.emptyText.visibility = View.VISIBLE
            binding.emptyText.text = "本月暂无消费记录"
            return
        }

        // 正常显示饼图（原有逻辑）
        pieChart.visibility = View.VISIBLE
        binding.emptyText.visibility = View.GONE

        val colors = listOf(
            "#FF6384".toColorInt(),
            "#36A2EB".toColorInt(),
            "#FFCE56".toColorInt(),
            "#4BC0C0".toColorInt(),
            "#9966FF".toColorInt()
        )

        val dataSet = PieDataSet(entries, "").apply {
            setColors(colors)
            valueTextSize = 12f
            valueTextColor = Color.WHITE
            yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
        }

        val pieData = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(pieChart))
        }

        pieChart.apply {
            data = pieData
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 40f
            transparentCircleRadius = 45f
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            animateY(1000, Easing.EaseInOutQuad)
            legend.isEnabled = false
            invalidate()
        }
    }

    private fun setupLegend(entries: List<PieEntry>) {
        val legendContainer = binding.legendContainer
        val colors = listOf(
            "#FF6384".toColorInt(),
            "#36A2EB".toColorInt(),
            "#FFCE56".toColorInt(),
            "#4BC0C0".toColorInt(),
            "#9966FF".toColorInt()
        )

        legendContainer.removeAllViews()

        entries.forEachIndexed { index, entry ->
            val legendItem = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 8)
                }

                text = "${entry.label}: ${entry.value}"

                compoundDrawablePadding = 16
                gravity = Gravity.CENTER_VERTICAL
            }

            legendContainer.addView(legendItem)
        }
    }

//    private fun createColorDot(color: Int) = resources.getDrawable(R.drawable.color_dot).apply {
//        setTint(color)
//    }

}