package org.wit.killbill.activity

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.FileProvider
import org.wit.killbill.BuildConfig
import com.google.android.material.snackbar.Snackbar
import org.wit.killbill.main.MainApp
import org.wit.killbill.R
import org.wit.killbill.databinding.ActivityMainBinding
import org.wit.killbill.models.NotifyModel
import org.wit.killbill.helper.messageHelper
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class PageMainActivity : AppCompatActivity(){
    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_GALLERY = 2
    }
    private lateinit var binding: ActivityMainBinding
    private var notifyModel = NotifyModel()
    lateinit var app : MainApp
    private var edit = false
    private val mshelper: messageHelper = messageHelper()
    private lateinit var gridLayout: GridLayout
    private var selectedButton: Button? = null
    private lateinit var photoUri: Uri
    private lateinit var photoFile: File


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

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINESE).apply {
            timeZone = TimeZone.getDefault()  // 使用设备当前时区
        }
        val currentTime = Calendar.getInstance().time
        binding.etTime.setText(sdf.format(currentTime))



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

        binding.btnCamera.setOnClickListener() {
            takePicture()
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
//                setResult(RESULT_OK)
                finish()
            }
        }

    }

    private fun takePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (takePictureIntent.resolveActivity(packageManager) != null) {
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                Timber.e("Error creating image file")
                return
            }

            photoUri = FileProvider.getUriForFile(
                this,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                photoFile
            )

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // ⭐ 把图片 Uri 存进模型
            notifyModel.imageUri = photoUri.toString()

            // （可选）立刻显示在 ImageView
            binding.imageView.setImageURI(photoUri)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile():File{
        val timeStamp = android.icu.text.SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(
            Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName,".jpg", storageDir)
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
