package org.wit.killbill.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotifyModel(var id:Long = 0, var title: String = "", var context: String = "", var time: String = ""):Parcelable
