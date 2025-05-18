package org.wit.killbill.Activity

import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.wit.killbill.Main.MainApp
import org.wit.killbill.NotifyServer.NotifyListener
import org.wit.killbill.NotifyServer.NotifyService
import org.wit.killbill.databinding.ActivityListMainBinding
import org.wit.killbill.databinding.CardBinding
import org.wit.killbill.models.NotifyModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import timber.log.Timber

class NotifyListActivity : AppCompatActivity(), NotifyListener{
    lateinit var app: MainApp
    private lateinit var binding: ActivityListMainBinding
    private lateinit var notifyService: NotifyService
    var notifyModel = NotifyModel()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        println("split: ${parts[0]}, ${parts[1]}")
        notifyModel.title = parts[0]
        notifyModel.context = parts[1]

        // 3. 格式化时间（添加时区处理）
        notifyModel.time =
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINESE).format(Date(sbn.getPostTime()))

        if(notifyModel.title.isNotEmpty()){
            app.notifyNotifyModels.add(notifyModel.copy())
            for (i in app.notifyNotifyModels.indices) {
                Timber.i("NotifyModels[$i]:${this.app.notifyNotifyModels[i]}")
            }
        }

        (binding.recyclerView.adapter)?.notifyItemRangeChanged(0, app.notifyNotifyModels.size)
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