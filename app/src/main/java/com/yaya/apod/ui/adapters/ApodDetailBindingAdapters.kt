package com.yaya.apod.ui.adapters

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import com.yaya.apod.R
import com.yaya.apod.api.MediaType

object BindingAdapters {

    @BindingAdapter(value = ["imageUrl", "type"], requireAll = true)
    @JvmStatic
    fun bindImageFromUrl(imageView: ImageView, imageUrl: String?, type: String?) {
        if (type.isNullOrEmpty() || type != MediaType.IMAGE.type) {
            return
        }
        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get().load(imageUrl).fit().placeholder(R.drawable.placeholder_image)
                .error(R.drawable.ic_error).fit().into(imageView)
        }
    }

    @BindingAdapter("isFabGone")
    @JvmStatic
    fun bindIsFabGone(view: FloatingActionButton, isGone: Boolean?) {
        if (isGone == null || isGone) {
            view.hide()
        } else {
            view.show()
        }
    }

    @BindingAdapter("isFavorite")
    @JvmStatic
    fun bindIsFavorite(floatingActionButton: FloatingActionButton, isFavorite: Boolean?) {
        if (isFavorite == null) {
            return
        }
        if (isFavorite) {
            floatingActionButton.setImageResource(R.drawable.ic_favorite)
        } else {
            floatingActionButton.setImageResource(R.drawable.ic_not_favorite)
        }
    }
}
