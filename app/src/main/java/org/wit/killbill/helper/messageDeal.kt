package org.wit.killbill.helper

interface messageDeal {
    fun checkTargetPackageName(packageName: String): Boolean
    fun checkPaymentTitle(title: String): Boolean
    fun dealMessage(message: String): String?
}