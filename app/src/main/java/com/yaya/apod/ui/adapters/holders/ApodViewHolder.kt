package com.yaya.apod.ui.adapters.holders

import android.annotation.SuppressLint
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.yaya.apod.R
import com.yaya.apod.api.MediaType
import com.yaya.apod.data.model.Apod
import com.yaya.apod.databinding.HomeListItemBinding
import com.yaya.apod.util.CustomClickListener
import com.yaya.apod.util.DateUtil

class ApodViewHolder(private val binding: HomeListItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private lateinit var delegate: ItemDelegate
    lateinit var item: Apod

    interface ItemDelegate {
        fun itemChanged(apod: Apod)
        fun itemClicked(item: Apod)
    }

    fun setDelegate(delegate: ItemDelegate) {
        this.delegate = delegate
    }


    fun bind(item: Apod) {
        if (this::item.isInitialized && item.id == this.item.id) {
            return
        }

        this.item = item
        binding.apply {
            isNew = item.date == DateUtil.todayDate()
            titleTxtView.text = item.title
            isFavorite = item.favorite
            type = item.mediaType

            isLoading = true
            if (item.mediaType == MediaType.IMAGE.type) {
                fetchImage(item.url)
            } else {
                fetchVideo(item.url)
            }
        }
    }

    private fun fetchImage(url: String) {
        Picasso.get()
            .load(url)
            .fit()
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.ic_error)
            .into(binding.imageImgView, object : Callback {
                override fun onSuccess() {
                    binding.isLoading = false
                }

                override fun onError(e: Exception?) {
                    binding.isLoading = false
                }
            })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun fetchVideo(url: String) {
        val videoFrame =
            "<html><body><iframe width=\"100%\" height=\"100%\" src=\"${url}\" frameborder=\"0\" allowfullscreen></iframe></body></html>"
        binding.webView.settings.javaScriptEnabled = true

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                binding.isLoading = false
            }
        }
        binding.webView.loadData(videoFrame, "text/html", "utf-8")
    }

    private fun toggleFavorite() {
        item.favorite = !item.favorite
        binding.favoriteImgView.setFavorite(item.favorite)
        binding.favoriteImgView.startAnimation()
        binding.isFavorite = item.favorite
        delegate.itemChanged(item)
    }

    init {
        binding.favoriteImgView.setOnClickListener {
            toggleFavorite()
        }

        binding.root.setOnClickListener(object : CustomClickListener() {
            override fun onDoubleClick(v: View) {
                toggleFavorite()
            }

            override fun onSingleClick(v: View) {
                navigateToApodDetails(item)
            }
        })
    }

    private fun navigateToApodDetails(apod: Apod) {
        delegate.itemClicked(apod)
    }
}