package org.wit.killbill.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import org.wit.killbill.main.MainApp
import org.wit.killbill.R
import org.wit.killbill.databinding.ActivityMainBinding
import org.wit.killbill.models.NotifyModel
import org.wit.killbill.messageDeal.messageHelper
import timber.log.Timber


class PageMainActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMainBinding
    private var notifyModel = NotifyModel()
    lateinit var app : MainApp
    private var edit = false
    private val mshelper: messageHelper = messageHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar2.title
        setSupportActionBar(binding.toolbar2)

        //Plant a log timber tree, to show collect all information when app execute.
        Timber.plant(Timber.DebugTree())
        Timber.i("Notify card Activity started..")

        app = application as MainApp

        if(intent.hasExtra("Notify_edit")){
            edit = true
            notifyModel = intent.extras?.getParcelable("Notify_edit")!!
            binding.etTitle.setText(notifyModel.title)
            binding.etContent.setText(notifyModel.context)
            binding.etTime.setText(notifyModel.time)
            binding.btnSubmit.setText(R.string.save_card)
        }

        if(intent.hasExtra("NOTIFICATION_DATA")){
            notifyModel = intent.extras?.getParcelable("NOTIFICATION_DATA")!!
            binding.etTitle.setText(notifyModel.title)
            binding.etContent.setText(mshelper.dealMessage(notifyModel.context))
            binding.etTime.setText(notifyModel.time)

        }

        binding.btnSubmit.setOnClickListener() {
            notifyModel.title = binding.etTitle.text.toString()
            notifyModel.context = binding.etContent.text.toString()
            notifyModel.time = binding.etTime.text.toString()
            if(notifyModel.title.isEmpty()){
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

}