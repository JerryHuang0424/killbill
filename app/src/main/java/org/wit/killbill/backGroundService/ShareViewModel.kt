package org.wit.killbill.backGroundService

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    // 使用一个 Boolean 类型的 LiveData 来通知刷新
    private val _refreshEvent = MutableLiveData<Boolean>()
    val refreshEvent: LiveData<Boolean> get() = _refreshEvent

    // 触发刷新事件
    fun triggerRefresh() {
        _refreshEvent.value = true
    }
}