package org.wit.killbill.Activity

import NotifyHelper
import android.content.Intent
import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.snackbar.Snackbar
import com.llw.easyutil.Easy
import org.wit.killbill.Main.MainApp
import org.wit.killbill.NotifyServer.NotifyListener
import org.wit.killbill.NotifyServer.NotifyService
import org.wit.killbill.R
import org.wit.killbill.databinding.ActivityMainBinding
import org.wit.killbill.models.NotifyModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PageMainActivity : AppCompatActivity(){
    companion object {
        private const val REQUEST_CODE = 9527
    }
    private lateinit var textView: TextView
    private lateinit var notifyService: NotifyService
    private lateinit var binding: ActivityMainBinding
    var notifyModel = NotifyModel()
    lateinit var app : MainApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Plant a log timber tree, to show collect all information when app execute.
        Timber.plant(Timber.DebugTree())
        Timber.i("Placemark Activity started..")

        app = application as MainApp

        binding.btnSubmit.setOnClickListener() {
            notifyModel.packageName = binding.etPackageName.text.toString()
            notifyModel.title = binding.etTitle.text.toString()
            notifyModel.context = binding.etContent.text.toString()
            notifyModel.time = binding.etTime.text.toString()
            if(notifyModel.title.isNotEmpty()){
                app.notifyNotifyModels.add(notifyModel)
                Timber.i("add Button Pressed: ${notifyModel}")
                for (i in app.notifyNotifyModels.indices){
                    Timber.i("Notify[$i]:${this.app.notifyNotifyModels[i]}")
                }
                setResult(RESULT_OK)
                finish()
            }
            else{
                Snackbar.make(it,"Please Enter a title", Snackbar.LENGTH_LONG).show()
            }
        }

    }


//    //收到通知时触发 onReceiveMessage()
//    override fun onReceiveMessage(sbn: StatusBarNotification?) {
//        // 1. 空安全检查
//        val notification = sbn?.notification ?: return
//
//        // 2. 获取消息内容（使用安全调用和空合并操作符）
//        notifyModel.packageName = sbn.packageName?.toString()?:""
////        notifyModel.title = notification.tickerText?.toString() ?: ""
////        notifyModel.context = " "
//        val contextOri = notification.tickerText?.toString() ?: ""
//        val parts= contextOri.split(":")
//        println("split: ${parts[0]}, ${parts[1]}")
//        notifyModel.title = parts[0]
//        notifyModel.context = parts[1]
//
//            // 3. 格式化时间（添加时区处理）
//        notifyModel.time =
//            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINESE).format(Date(sbn.getPostTime()))
//
//        if(notifyModel.title.isNotEmpty()){
//            app.notifyNotifyModels.add(notifyModel.copy())
//        }
//
//        // 4. 更安全的UI更新（使用runOnUiThread）
//        runOnUiThread {
//            textView.text = """
//            应用包名：${sbn.packageName}
//            消息标题：${notifyModel.title}
//            消息内容：${notifyModel.context}
//            消息时间：${notifyModel.time}
//          """.trimIndent()
//        }
//    }


}