package com.yaya.apod.ui.component

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.content.res.AppCompatResources
import com.yaya.apod.R
import com.yaya.apod.util.AndroidUtils
import com.yaya.apod.util.LayoutHelper
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ActivityContext


@AndroidEntryPoint
class Loading(@ActivityContext context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {
    var isCanceled = false
    var isShowed = false

    init {
        val progressbar = ProgressBar(context)
        progressbar.isIndeterminate = true
        val shape = GradientDrawable()
        shape.cornerRadius = AndroidUtils.dp(context, 15f).toFloat()
        shape.shape = GradientDrawable.RECTANGLE
        shape.color = AppCompatResources.getColorStateList(context, R.color.black)
        progressbar.background = shape

        addView(
            progressbar,
            LayoutHelper.createFrame(context, 72f, 72f, Gravity.CENTER)
        )
        foregroundGravity = Gravity.CENTER

        setOnClickListener {
            showStopDialog()
        }
    }

    fun showStopDialog() {
        val dialog = OptionalDialog.Builder(context)
            .setCancelable(false)
            .setIcon(R.drawable.ic_stop)
            .setMessage(context.getString(R.string.stop_loading))
            .setFirstOption(
                context.getString(R.string.stop),
                object : OptionalDialog.OptionalDialogClickListener {
                    override fun onClick(dialog: OptionalDialog) {
                        dialog.dismiss()
                        hide()
                    }
                }
            )
            .setSecondOption(
                context.getString(R.string.wait_more),
                object : OptionalDialog.OptionalDialogClickListener {
                    override fun onClick(dialog: OptionalDialog) {
                        dialog.dismiss()
                    }
                }
            )
        dialog.show()
    }

    fun show() {
        isCanceled = false
        visibility = View.VISIBLE
        isShowed = true
        invalidate()
    }

    fun hide() {
        visibility = View.INVISIBLE
        isCanceled = true
        isShowed = false
    }
}