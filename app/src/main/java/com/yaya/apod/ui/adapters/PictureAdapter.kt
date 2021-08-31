package com.yaya.apod.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.yaya.apod.R
import com.yaya.apod.api.MediaType
import com.yaya.apod.data.model.Apod
import com.yaya.apod.databinding.HomeListItemBinding
import com.yaya.apod.util.CustomClickListener
import com.yaya.apod.util.Util
import dagger.hilt.android.scopes.ActivityScoped


@ActivityScoped
class PictureAdapter : PagingDataAdapter<Apod, PictureAdapter.ViewHolder>(ApodDiffCallback()) {
    private var data: MutableList<Apod> = mutableListOf()
    private lateinit var delegate: ItemChangeDelegate

    interface ItemChangeDelegate {
        fun itemChanged(item: Apod)
    }

    fun setDelegate(delegate: ItemChangeDelegate) {
        this.delegate = delegate
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        lateinit var view: ViewBinding
        view = HomeListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)


    }

    override fun getItemCount(): Int {
        return data.size
    }


    inner class ViewHolder(val binding: HomeListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        lateinit var item: Apod

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

        private fun toggleFavorite() {
            item.favorite = !item.favorite
            binding.favoriteImgView.setFavorite(item.favorite)
            binding.favoriteImgView.startAnimation()
            binding.isFavorite = item.favorite
            binding.invalidateAll()
            delegate.itemChanged(item)
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
                    Picasso.get()
                        .load(item.hdUrl)
                        .fit()
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.ic_error)
                        .into(imageImgView, object : Callback {
                            override fun onSuccess() {
                                isLoading = false
                            }

                            override fun onError(e: Exception?) {
                                isLoading = false
                            }
                        })

                } else {
                    val frameVideo =
                        "<html><body><iframe width=\"100%\" height=\"100%\" src=\"${item.url}\" frameborder=\"0\" allowfullscreen></iframe></body></html>"
                    webView.settings.javaScriptEnabled = true
                    webView.webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                            view.loadUrl(url)
                            return true
                        }

                        override fun onPageFinished(view: WebView, url: String) {
                            super.onPageFinished(view, url)
                            binding.isLoading = false
                        }
                    }
                    webView.loadData(frameVideo, "text/html", "utf-8")
                }
            }
        }
    }

    fun submitData(data: MutableList<Apod>) {
//        this.data.clear()
        this.data = data
        notifyDataSetChanged()
    }

    fun addData(data: Apod) {
        this.data.add(0, data)
        notifyItemInserted(0)
    }

    private class ApodDiffCallback() : DiffUtil.ItemCallback<Apod>() {
        override fun areItemsTheSame(oldItem: Apod, newItem: Apod): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Apod, newItem: Apod): Boolean {
            return oldItem == newItem
        }

    }
}