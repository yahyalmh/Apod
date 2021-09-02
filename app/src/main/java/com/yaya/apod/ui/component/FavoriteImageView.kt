package com.yaya.apod.ui.component

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped

@ActivityScoped
@AndroidEntryPoint
class FavoriteImageView @JvmOverloads constructor(
    @ActivityContext context: Context,
    attrs: AttributeSet?
) :
    AppCompatImageView(context, attrs) {
    var isFavorite: Boolean = false

    @JvmName("setFavorite1")
    fun setFavorite(state: Boolean) {
        isFavorite = state
    }

    fun startAnimation() {
        val scaleDownX =
            ObjectAnimator.ofFloat(this, "scaleX", 0.5f, 0.7f, 1f)
                .apply { duration = 250 }
        val scaleDownY =
            ObjectAnimator.ofFloat(this, "scaleY", 0.5f, 0.7f, 1f)
                .apply { duration = 250 }

        val scaleUpX =
            ObjectAnimator.ofFloat(this, "scaleX", 1.5f, 1.7f, 1f)
                .apply { duration = 250 }
        val scaleUpY =
            ObjectAnimator.ofFloat(this, "scaleY", 1.5f, 1.7f, 1f)
                .apply { duration = 250 }

        if (isFavorite) {
            AnimatorSet().apply {
                playTogether(scaleUpX, scaleUpY)
                start()
            }
        } else {
            AnimatorSet().apply {
                playTogether(scaleDownX, scaleDownY)
                start()
            }
        }
    }

}