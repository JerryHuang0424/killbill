package org.wit.killbill.models

import android.service.notification.StatusBarNotification
import org.wit.killbill.notifyServer.NotifyListener


class NotifyHelper {

    companion object {
        @Volatile
        private var instance: NotifyHelper? = null
//
//        const val N_MESSAGE = 0
//        const val N_ZFB = 1
//        const val N_QQ = 2
//        const val N_WX = 3

        //注册通知监听
        fun getInstance(): NotifyHelper {
            return instance ?: synchronized(this) {
                instance ?: NotifyHelper().also { instance = it }
            }
        }
    }

    private var notifyListener: NotifyListener? = null

    /**
     * 收到消息
     * @param sbn 状态栏通知
     */
    fun onReceive(sbn: StatusBarNotification?) {
        if (notifyListener != null) {
            notifyListener!!.onReceiveMessage(sbn)
        }
    }

    /**
     * 移除消息
     * @param sbn 状态栏通知
     */
//    fun onRemoved(sbn: StatusBarNotification?) {
//        if (notifyListener != null) {
//            notifyListener!!.onRemovedMessage(sbn)
//        }
//    }

    /**
     * 设置回调方法
     *
     * @param notifyListener 通知监听
     */
    fun setNotifyListener(notifyListener: NotifyListener) {
        this.notifyListener = notifyListener
    }
}