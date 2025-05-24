package org.wit.killbill.models

import android.content.Context
import android.net.Uri
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import org.wit.killbill.helpers.*
import timber.log.Timber
import java.lang.reflect.Type
import java.util.*

const val JSON_FILE = "notification.json"
val gsonBuilder: Gson = GsonBuilder().setPrettyPrinting()
    .create()
val listType: Type = object : TypeToken<ArrayList<NotifyModel>>() {}.type

fun generateRandomId(): Long {
    return Random().nextLong()
}

class NotifyJSONStroeJSONStore(private val context: Context) : NotifyStore {

    var notifies = mutableListOf<NotifyModel>()

    init {
        if (exists(context, JSON_FILE)) {
            deserialize()
        }
    }

    override fun findAll(): MutableList<NotifyModel> {
        logAll()
        return notifies
    }

    override fun createByMenu(notify: NotifyModel) {
        notify.id = generateRandomId()
        notifies.add(0,notify)
        serialize()
    }

    override fun createByListener(notify: NotifyModel) {
        notify.id = getId()
        notifies.add(0, notify)
        logAll()    }


    override fun update(notify: NotifyModel) {
        val foundNotify: NotifyModel? = notifies.find{ p -> p.id ==notify.id}
        if(foundNotify!= null){
            foundNotify.amount = notify.amount
            foundNotify.context = notify.context
            foundNotify.time = notify.time
            foundNotify.type = notify.type
            logAll()
        }
    }

    override fun delete(notify: NotifyModel) {
        notifies.remove(notify)
        serialize()
    }

    private fun serialize() {
        val jsonString = gsonBuilder.toJson(notifies, listType)
        write(context, JSON_FILE, jsonString)
    }

    private fun deserialize() {
        val jsonString = read(context, JSON_FILE)
        notifies = gsonBuilder.fromJson(jsonString, listType)
    }

    private fun logAll() {
        notifies.forEach { Timber.i("$it") }
    }
}

