package org.wit.killbill.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class NotifyModel(var id:Long = 0,var amount: BigDecimal = BigDecimal("0.00"), var context: String = "", var type: String = "", var time: String = ""):Parcelable
