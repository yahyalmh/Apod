package com.yaya.apod.util

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View

abstract class CustomClickListener : View.OnClickListener {
    private val delta: Long = DEFAULT_QUALIFICATION_SPAN
    private var deltaClick: Long
    private val handler = Handler(Looper.getMainLooper())

    override fun onClick(v: View) {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ onSingleClick(v) }, delta)
        if (SystemClock.elapsedRealtime() - deltaClick < delta) {
            handler.removeCallbacksAndMessages(null)
            onDoubleClick(v)
        }
        deltaClick = SystemClock.elapsedRealtime()
    }

    abstract fun onDoubleClick(v: View)
    abstract fun onSingleClick(v: View)

    companion object {
        private const val DEFAULT_QUALIFICATION_SPAN: Long = 200
    }

    init {
        deltaClick = 0
    }
}