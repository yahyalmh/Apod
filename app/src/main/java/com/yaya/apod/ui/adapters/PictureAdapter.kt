package com.yaya.apod.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import com.yaya.apod.data.model.Apod
import com.yaya.apod.databinding.HomeListItemBinding
import dagger.hilt.android.scopes.ActivityScoped


@ActivityScoped
class PictureAdapter(private val delegate: ApodViewHolder.ItemChangeDelegate) :
    PagingDataAdapter<Apod, ApodViewHolder>(ApodDiffCallback()) {
    private var data: MutableList<Apod> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApodViewHolder {
        lateinit var view: ViewBinding
        view = HomeListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        val apodViewHolder = ApodViewHolder(view)
        apodViewHolder.setDelegate(delegate)
        return apodViewHolder
    }

    override fun onBindViewHolder(holder: ApodViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)


    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun submitData(data: MutableList<Apod>) {
        this.data.clear()
        this.data = data
        notifyDataSetChanged()
    }

    fun insertData(data: Apod, index: Int) {
        this.data.add(index, data)
        notifyItemInserted(index)
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