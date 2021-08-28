package com.yaya.apod.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.squareup.picasso.Picasso
import com.yaya.apod.api.ApodResponse
import com.yaya.apod.databinding.ImageListItemBinding
import com.yaya.apod.databinding.VideoListItemBinding


class PictureAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var data: MutableList<ApodResponse> = mutableListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        lateinit var view: ViewBinding

        if (viewType == 1) {
            view = ImageListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        } else if (viewType == 2) {
            view = VideoListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        (holder as ViewHolder).bind(item)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        val item = data[position]
        return if (item.mediaType == "image") {
            1
        } else {
            2
        }

    }

    class ViewHolder(private val binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
//                navigateToPlant(plant, it)
            }
        }

        fun bind(item: ApodResponse) {
            binding.apply {
                if (this is ImageListItemBinding) {
                    Picasso.get()
                        .load(item.hdUrl)
                        .fit()
                        .centerCrop()
                        .into(this.imageImgView)

                    this.titleTxtView.text = item.title
                } else {
                    val frameVideo =
                        "<html><body><iframe width=\"100%\" height=\"100%\" src=\"${item.url}\" frameborder=\"0\" allowfullscreen></iframe></body></html>"
                    val videoListItemBinding = this as VideoListItemBinding
                    val webView = videoListItemBinding.webView
                    videoListItemBinding.titleTxtView.text = item.title
                    val webSettings: WebSettings = webView.settings
                    webSettings.javaScriptEnabled = true
                    webView.webChromeClient = WebChromeClient()
                    webView.loadData(frameVideo, "text/html", "utf-8")

                }
            }
        }
    }

    fun submitData(data: MutableList<ApodResponse>) {
        this.data.clear()
        this.data = data
        notifyDataSetChanged()
    }
}