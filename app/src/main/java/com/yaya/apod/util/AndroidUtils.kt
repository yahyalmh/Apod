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
            var result = false
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                connectivityManager?.run {
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                        ?.run {
                            result = when {
                                hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                                hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                                else -> false
                            }
                        }
                }
            } else {
                connectivityManager?.run {
                    connectivityManager.activeNetworkInfo?.run {
                        if (type == ConnectivityManager.TYPE_WIFI) {
                            result = true
                        } else if (type == ConnectivityManager.TYPE_MOBILE) {
                            result = true
                        }
                    }
                }
            }
            return result
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