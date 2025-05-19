package org.wit.killbill.models

interface NotifyStore{
    fun findAll(): List<NotifyModel>
    fun createByMenu(notify: NotifyModel)
    fun createByListener(notify: NotifyModel)
    fun update(notify: NotifyModel)
}