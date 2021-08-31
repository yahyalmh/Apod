package com.yaya.apod.ui.adapters

import android.annotation.SuppressLint
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
import com.yaya.apod.util.Util

class ApodViewHolder(private val binding: HomeListItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private lateinit var delegate: ItemChangeDelegate
    lateinit var item: Apod

    interface ItemChangeDelegate {
        fun itemChanged(item: Apod)
    }

    fun setDelegate(delegate: ItemChangeDelegate) {
        this.delegate = delegate
    }


    fun bind(item: Apod) {
        this.item = item
        binding.apply {
            isNew = item.date == Util.getTodayDate()
            isLoading = true
            titleTxtView.text = item.title
            isFavorite = item.favorite
            type = item.mediaType

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
        binding.invalidateAll()
        delegate.itemChanged(item)
    }

    init {
        binding.favoriteImgView.setOnClickListener {
            toggleFavorite()
        }

        binding.root.setOnClickListener(object : CustomClickListener() {
            override fun onDoubleClick() {
                toggleFavorite()
            }

            override fun onSingleClick() {
                //   navigateToPlant(plant, it)
            }

        })
    }
}