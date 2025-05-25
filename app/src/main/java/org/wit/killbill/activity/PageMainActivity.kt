package org.wit.killbill.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import org.wit.killbill.main.MainApp
import org.wit.killbill.R
import org.wit.killbill.databinding.ActivityMainBinding
import org.wit.killbill.models.NotifyModel
import org.wit.killbill.helper.messageHelper
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode


class PageMainActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMainBinding
    private var notifyModel = NotifyModel()
    lateinit var app : MainApp
    private var edit = false
    private val mshelper: messageHelper = messageHelper()
    private lateinit var gridLayout: GridLayout
    private var selectedButton: Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gridLayout = findViewById(R.id.gridLayout)

        binding.toolbar2.title
        setSupportActionBar(binding.toolbar2)

        //Plant a log timber tree, to show collect all information when app execute.
        Timber.plant(Timber.DebugTree())
        Timber.i("Notify card Activity started..")

        app = application as MainApp

        val categories = arrayOf("餐饮", "日用", "交通", "学习", "教育", "医疗", "娱乐", "购物")

        setupButtons(categories)



        if(intent.hasExtra("Notify_edit")){
            edit = true
            notifyModel = intent.extras?.getParcelable("Notify_edit")!!
            binding.etTitle.setText(notifyModel.amount.toString())
            binding.etContent.setText(notifyModel.context)
            binding.etTime.setText(notifyModel.time)
            binding.btnSubmit.setText(R.string.save_card)
        }

        if(intent.hasExtra("NOTIFICATION_DATA")){
            notifyModel = intent.extras?.getParcelable("NOTIFICATION_DATA")!!
            binding.etTitle.setText(notifyModel.amount.toString())
            binding.etContent.setText(mshelper.dealMessage(notifyModel.context))
//            binding.etTime.setText(notifyModel.time)

        }

        binding.btnDelete.setOnClickListener(){
            setResult(99)
            app.notifyNotifyModels.delete(notifyModel)
            finish()
        }

        binding.btnSubmit.setOnClickListener() {
            var money_amount = binding.etTitle.text.toString()
            // 步骤1：转换为浮点数（若输入是整数如"158"，会自动补.0）
            val amount = money_amount.toDoubleOrNull() ?: 0.0
            // 步骤2：四舍五入到小数点后两位
            val roundedAmount = "%.2f".format(amount).toDouble()
            notifyModel.amount  = roundedAmount
            notifyModel.context = binding.etContent.text.toString()
            notifyModel.time = binding.etTime.text.toString()
            if(binding.etTitle.text?.isEmpty() ?: true){
                Snackbar.make(it,"Please Enter a title", Snackbar.LENGTH_LONG).show()

            }
            else{
                if(edit){
                    app.notifyNotifyModels.update(notifyModel)

                }else{
                    app.notifyNotifyModels.createByMenu(notifyModel)
                }
                setResult(RESULT_OK)
                finish()
            }
        }

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.cancel, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.item_cancel -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupButtons(categories: Array<String>) {
        gridLayout.removeAllViews()

        categories.forEach { category ->
            val button = Button(this).apply {
                text = category
                setBackgroundResource(R.drawable.button_state_selector)  // 使用选择器而不是直接设置颜色
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                }

                setOnClickListener {
                    updateSelectedButton(this)
                    notifyModel.type = category
                }
            }
            gridLayout.addView(button)
        }
    }

    private fun updateSelectedButton(button: Button) {
        selectedButton?.isSelected = false
        button.isSelected = true
        selectedButton = button
    }

    // dp转px扩展函数
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
