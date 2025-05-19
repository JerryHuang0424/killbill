package org.wit.killbill.models

import timber.log.Timber


var lastID =0L

internal fun getId():Long{
    return lastID++
}
class NotifyMemStore :NotifyStore{
    private val notifyList = ArrayList<NotifyModel>()

    override fun findAll(): List<NotifyModel>{
        return notifyList
    }

    override fun createByMenu(notify: NotifyModel) {
        notify.id = getId()
        notifyList.add(notify)
        logAll()
    }

    override fun createByListener(notify: NotifyModel) {
        notifyList.add(NotifyModel())
        logAll()
    }

    override fun update(notify: NotifyModel) {
        val foundNotify: NotifyModel? = notifyList.find{ p -> p.id ==notify.id}
        if(foundNotify!= null){
            foundNotify.packageName = notify.packageName
            foundNotify.time = notify.title
            foundNotify.context = notify.context
            foundNotify.time = notify.time
            logAll()
        }
    }
    private fun logAll() {
        notifyList.forEach{ Timber.i("$it") }
    }
}

