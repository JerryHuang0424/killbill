package org.wit.killbill.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.wit.killbill.R
import org.wit.killbill.activity.PageMainActivity
import org.wit.killbill.adapter.NotifyAdapter
import org.wit.killbill.adapter.NotifyAdapterListener
import org.wit.killbill.databinding.DailyStatusBinding
import org.wit.killbill.main.MainApp
import org.wit.killbill.models.NotifyModel
import java.util.Calendar

class DailyFragment : Fragment(), NotifyAdapterListener {
    companion object {
        // add newInstance() method
        fun newInstance(): DailyFragment {
            return DailyFragment()
        }
    }

//    private val handler = Handler(Looper.getMainLooper())
//    private val refreshInterval = 1000L // 1s
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

        // Set a specific time to filter all bills that meet the time requirements
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
                        modelDay == currentDay
                    ) {
                        currentList.add(notifyModel)
                    }
                }
            }
        }

        binding.tvDate.text = getString(R.string.date_format, currentYear, currentMonth, currentDay)
        binding.tvTotalAmount.text = currentList.sumOf { it.amount }.toString()

        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvBillList.layoutManager = layoutManager
        binding.rvBillList.adapter = NotifyAdapter(currentList, this)
    }

    override fun onCardClick(notify: NotifyModel, pos: Int) {
        val launchIntentCard = Intent(requireContext(), PageMainActivity::class.java)
        launchIntentCard.putExtra("Notify_edit", notify)
        position = pos
        getClickResult.launch(launchIntentCard)
    }

    private val getClickResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                binding.rvBillList.adapter?.notifyItemRangeChanged(
                    0,
                    app.notifyNotifyModels.findAll().size
                )
            }
            if (it.resultCode == 99) {
                binding.rvBillList.adapter?.notifyItemRemoved(position)
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}