package com.yaya.apod.ui.adapters

import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
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
            Picasso.get()
                .load(imageUrl)
                .fit()
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.ic_error)
                .into(imageView)
        }
    }

    @BindingAdapter(value = ["videoUrl", "type"], requireAll = true)
    @JvmStatic
    fun bindVideoFromUrl(webView: WebView, videoUrl: String?, type: String?) {
        if (type != MediaType.VIDEO.type) {
            return
        }
        val videoFrame =
            "<html><body><iframe width=\"100%\" height=\"100%\" src=\"${videoUrl}\" frameborder=\"0\" allowfullscreen></iframe></body></html>"
        webView.settings.javaScriptEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                val htmlData =
                    "<html><body><div align=\"center\" >This is the description for the load fail : +description+\nThe failed url is : +failingUrl+\n</div></body>";

                if (view != null) {
                    view.loadUrl("about:blank")
                    view.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null);
                    view.invalidate();
                };
            }
        }
        webView.loadData(videoFrame, "text/html", "utf-8")

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
