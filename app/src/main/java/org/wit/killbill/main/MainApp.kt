package org.wit.killbill.main

import android.app.Application
import org.wit.killbill.models.NotifyJSONStroeJSONStore
import org.wit.killbill.models.NotifyMemStore
import org.wit.killbill.models.NotifyModel
import org.wit.killbill.models.NotifyStore
import timber.log.Timber


class MainApp : Application() {

    lateinit var notifyNotifyModels: NotifyStore

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Timber.i("Notify started")
        notifyNotifyModels = NotifyJSONStroeJSONStore(applicationContext)

    }

}