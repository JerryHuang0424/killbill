package org.wit.killbill.main

import android.app.Application
import org.wit.killbill.models.NotifyMemStore
import org.wit.killbill.models.NotifyModel
import timber.log.Timber


class MainApp : Application() {

    val notifyNotifyModels = NotifyMemStore()

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Timber.i("Notify started")
        notifyNotifyModels.createByMenu(NotifyModel(1,"huohuo", "yes", "2025-05-18 16:20"))

    }

}