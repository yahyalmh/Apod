package com.yaya.apod.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.squareup.picasso.Picasso
import com.yaya.apod.api.ApiResponse
import com.yaya.apod.api.ApiSuccessResponse
import com.yaya.apod.api.ApodResponse
import com.yaya.apod.databinding.HomeListItemBinding

class PictureAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var data: MutableList<ApiResponse<ApodResponse>> = mutableListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        lateinit var view: ViewBinding

        if (viewType == 1) {
            view = HomeListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        } else if (viewType == 2) {
            view = HomeListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = (data[position] as ApiSuccessResponse).body
        (holder as ViewHolder).bind(item)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        val item = (data[position] as ApiSuccessResponse).body
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
                Picasso.get()
                    .load(item.hdUrl)
                    .into((this as HomeListItemBinding).imageImgView)
                this.titleTxtView.text = item.title
            }
        }
    }

    fun submitData(data: MutableList<ApiResponse<ApodResponse>>) {
        this.data.clear()
        this.data = data
        notifyDataSetChanged()
    }
}