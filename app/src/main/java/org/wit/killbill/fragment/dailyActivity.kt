package org.wit.killbill.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.wit.killbill.activity.PageMainActivity
import org.wit.killbill.adapter.NotifyAdapter
import org.wit.killbill.adapter.NotifyAdapterListener
import org.wit.killbill.databinding.DailyStatusBinding
import org.wit.killbill.main.MainApp
import org.wit.killbill.models.NotifyModel
import java.util.Calendar

class DailyFragment : Fragment(), NotifyAdapterListener {
    companion object {
        // 添加 newInstance 方法
        fun newInstance(): DailyFragment {
            return DailyFragment()
        }
    }
    private val handler = Handler(Looper.getMainLooper())
    private val refreshInterval = 1000L // 1秒
    private var _binding: DailyStatusBinding? = null
    private val binding get() = _binding!!
    private lateinit var app: MainApp
    private var position: Int = 0


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DailyStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        app = requireActivity().application as MainApp

//        startAutoRefresh()
        // 设置特定的时间，筛选全部账单中符合时间规定
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentDay = calendar.get(Calendar.DATE)

        val currentList = mutableListOf<NotifyModel>()
        for (notifyModel in app.notifyNotifyModels.findAll()) {
            val dateTimeParts = notifyModel.time.split(" ")
            if (dateTimeParts.isNotEmpty()) {
                val dateParts = dateTimeParts[0].split("-")
                if (dateParts.size >= 3) {
                    val modelYear = dateParts[0].toInt()
                    val modelMonth = dateParts[1].toInt()
                    val modelDay = dateParts[2].toInt()

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

        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvBillList.layoutManager = layoutManager
        binding.rvBillList.adapter = NotifyAdapter(currentList, this)
    }

//    private val refreshRunnable = object : Runnable {
//        override fun run() {
//            // 获取最新数据并更新 Adapter
//            updateRecyclerView()
//            // 再次延迟执行（实现循环）
//            handler.postDelayed(this, refreshInterval)
//        }
//    }
//
//    private fun startAutoRefresh() {
//        handler.postDelayed(refreshRunnable, refreshInterval)
//    }



//    private fun updateRecyclerView() {
//        try {
//            binding.rvBillList.adapter?.notifyItemRangeChanged(0, app.notifyNotifyModels.findAll().size)
//        } catch (e: Exception) {
//            // 处理异常
//        }
//    }


    override fun onCardClick(notify: NotifyModel, pos: Int) {
        val launchIntentCard = Intent(requireContext(), PageMainActivity::class.java)
        launchIntentCard.putExtra("Notify_edit", notify)
        position = pos
        getClickResult.launch(launchIntentCard)
    }

    private val getClickResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            binding.rvBillList.adapter?.notifyItemRangeChanged(0, app.notifyNotifyModels.findAll().size)
        }
        if (it.resultCode == 99) {
            binding.rvBillList.adapter?.notifyItemRemoved(position)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        stopAutoRefresh()
        _binding = null
    }
}