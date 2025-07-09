package org.wit.killbill.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import org.wit.killbill.R
import org.wit.killbill.activity.PageMainActivity
import org.wit.killbill.adapter.NotifyAdapter
import org.wit.killbill.adapter.NotifyAdapterListener
import org.wit.killbill.databinding.StatisticBinding
import org.wit.killbill.main.MainApp
import org.wit.killbill.models.NotifyModel
import java.util.Calendar

class StatisticFragment : Fragment(), NotifyAdapterListener {
    companion object {
        // add newInstance() method
        fun newInstance(): StatisticFragment {
            return StatisticFragment()
        }
    }

    private var _binding: StatisticBinding? = null
    private val binding get() = _binding!!
    private lateinit var app: MainApp
    private var position: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = StatisticBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        app = requireActivity().application as MainApp

        // Set a specific time to filter all bills
        // that meet the time requirements
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1

        val pieChartList = mutableListOf<PieEntry>()
        val currentList = mutableListOf<NotifyModel>()

        for (notifyModel in app.notifyNotifyModels.findAll()) {
            val dateParts = notifyModel.time.split("-")
            if (dateParts.size >= 2) {
                val modelYear = dateParts[0].toInt()
                val modelMonth = dateParts[1].toInt()

                if (modelYear == currentYear && modelMonth == currentMonth) {
                    pieChartList.add(
                        PieEntry(
                            notifyModel.amount.toFloat(),
                            if (notifyModel.type.isBlank()) R.string.Else else notifyModel.type
                        )
                    )
                    currentList.add(notifyModel)
                }
            }
        }

//        _binding?.titleText?.setText("Consumption statistics for ${currentMonth} month")
        val title = getString(R.string.consumption_statistics, currentMonth)
        _binding?.titleText?.text = title


        val mergedEntries = pieChartList
            .groupBy { it.label }
            .map { (type, entries) ->
                PieEntry(
                    entries.sumOf { it.value.toDouble() }.toFloat(),
                    type
                )
            }

        // Set up pie charts and data
        setupPieChart(mergedEntries)
        setupLegend(mergedEntries)

        // set RecyclerView
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = NotifyAdapter(currentList, this)
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
                binding.recyclerView.adapter?.notifyItemRangeChanged(
                    0,
                    app.notifyNotifyModels.findAll().size
                )
            }
            if (it.resultCode == 99) {
                binding.recyclerView.adapter?.notifyItemRemoved(position)
            }
        }

    private fun setupPieChart(entries: List<PieEntry>) {
        val pieChart = binding.pieChart

        if (entries.isEmpty()) {
            pieChart.visibility = View.GONE
            binding.emptyText.visibility = View.VISIBLE
            binding.emptyText.text = "No Consumption Records for this Month"
            return
        }

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
            val legendItem = TextView(requireContext()).apply {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}