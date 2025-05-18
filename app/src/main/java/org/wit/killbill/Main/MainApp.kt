package org.wit.killbill.Main

import android.app.Application
import org.wit.killbill.models.NotifyModel
import timber.log.Timber


class MainApp : Application() {

    val notifyNotifyModels = ArrayList<NotifyModel>()

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Timber.i("Placemark started")
        notifyNotifyModels.add(NotifyModel("huohuo", "yes", "2025-05-18 16:20", "com.tencent.mobileqq"))

    }

}