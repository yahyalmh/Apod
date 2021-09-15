package com.yaya.apod.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.View
import javax.inject.Singleton
import kotlin.math.ceil


@Singleton
class AndroidUtils {

    companion object {
        fun dp(context: Context, value: Float): Int {
            val density = context.resources.displayMetrics.density;

            return if (value == 0f) {
                0
            } else ceil(density * value).toInt()
        }

        @Suppress("DEPRECATION")
        fun isInternetAvailable(context: Context): Boolean {
            try {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                var netInfo = connectivityManager.activeNetworkInfo
                if (netInfo != null && (netInfo.isConnectedOrConnecting || netInfo.isAvailable)) {
                    return true
                }
                netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                if (netInfo != null && netInfo.isConnectedOrConnecting) {
                    return true
                } else {
                    netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                    if (netInfo != null && netInfo.isConnectedOrConnecting) {
                        return true
                    }
                }
            } catch (e: Exception) {
                return true
            }
            return false
        }

        fun animateView(view: View, toVisibility: Int, toAlpha: Float, duration: Int) {
            val show = toVisibility == View.VISIBLE
            if (show) {
                view.alpha = 0f
            }
            view.visibility = View.VISIBLE
            view.animate()
                .setDuration(duration.toLong())
                .alpha(if (show) toAlpha else 0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = toVisibility
                    }
                })
        }

    }
}