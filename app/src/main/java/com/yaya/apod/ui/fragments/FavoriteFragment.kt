package com.yaya.apod.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.map
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yaya.apod.DefaultConfig
import com.yaya.apod.R
import com.yaya.apod.api.MediaType
import com.yaya.apod.data.model.Apod
import com.yaya.apod.databinding.FragmentFavoriteBinding
import com.yaya.apod.ui.adapters.FavoriteAdapter
import com.yaya.apod.ui.adapters.holders.ApodViewHolder
import com.yaya.apod.ui.component.SharedPreferenceBooleanLiveData
import com.yaya.apod.ui.component.VerticalSpaceItemDecoration
import com.yaya.apod.util.AndroidUtils
import com.yaya.apod.util.Constants
import com.yaya.apod.viewmodels.FavoriteViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FavoriteFragment : Fragment(), ApodViewHolder.ItemDelegate {
    private lateinit var sharedPreferences: SharedPreferences
    private var binding: FragmentFavoriteBinding? = null
    private lateinit var adapter: FavoriteAdapter
    private val viewModel: FavoriteViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences(
            DefaultConfig.APP_SHARED_PREF_NAME, Context.MODE_PRIVATE
        )
        setHasOptionsMenu(true)

        initAdapter()

        initRecyclerView()

        initArrowUpKey()

        initToolbarMenu()

        return binding!!.root
    }

    private fun initAdapter() {
        adapter = FavoriteAdapter(this)
        collectFavorite()
    }

    private fun collectFavorite() {
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            SharedPreferenceBooleanLiveData(
                sharedPreferences, Constants.VIDEO_SHOW_SHARED_KEY, true
            ).observe(viewLifecycleOwner) { isShowingVideo ->
                val videoItem = binding!!.toolbar.menu.findItem(R.id.video_item)
                if (isShowingVideo) {
                    videoItem.icon =
                        AppCompatResources.getDrawable(requireContext(), R.drawable.ic_video)
                    videoItem.title = getString(R.string.is_showing_video)
                } else {
                    videoItem.icon =
                        AppCompatResources.getDrawable(requireContext(), R.drawable.ic_video_off)
                    videoItem.title = getString(R.string.is_not_showing_video)
                }

                viewModel.favorites.map { apods ->
                    apods.filter { apod ->
                        if (isShowingVideo) {
                            true
                        } else {
                            apod.mediaType != MediaType.VIDEO.type
                        }
                    }
                }.observe(viewLifecycleOwner) {
                    binding!!.isFavorite = it.isEmpty()
                    adapter.submitData(it as MutableList<Apod>)
                }
            }
        }
    }

    private fun initRecyclerView() {
        binding!!.listView.addItemDecoration(
            VerticalSpaceItemDecoration(
                AndroidUtils.dp(
                    requireActivity().applicationContext, 5f
                )
            )
        )
        observeLayoutManagerType()

        binding!!.listView.adapter = adapter
        binding!!.listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val currentIndex =
                    (binding!!.listView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                binding!!.needUpKey =
                    (binding!!.listView.layoutManager is GridLayoutManager && currentIndex > 3) || (binding!!.listView.layoutManager is LinearLayoutManager && currentIndex > 1)
            }
        })
    }

    private fun observeLayoutManagerType() {
        val isGridLayoutManagerSharedLiveData = SharedPreferenceBooleanLiveData(
            sharedPreferences, Constants.LAYOUT_TYPE_SHARED_KEY, false
        )
        isGridLayoutManagerSharedLiveData.observe(viewLifecycleOwner) { isGridLayoutManager ->
            if (isGridLayoutManager) {
                binding!!.toolbar.menu.findItem(R.id.grid_item).icon =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.ic_grid_on)
                binding!!.listView.layoutManager =
                    GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
            } else {
                binding!!.listView.layoutManager = LinearLayoutManager(activity)
                binding!!.toolbar.menu.findItem(R.id.grid_item).icon =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.ic_grid_off)
            }
            binding!!.listView.adapter = adapter
            binding!!.needUpKey = false
        }
    }

    private fun initArrowUpKey() {
        binding!!.arrowUp.setOnClickListener {
            binding!!.listView.smoothScrollToPosition(0)
        }
    }

    private fun initToolbarMenu() {
        binding!!.toolbar.inflateMenu(R.menu.home_menu)
        binding!!.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.grid_item -> {
                    val isGridLayoutManager = binding!!.listView.layoutManager !is GridLayoutManager
                    sharedPreferences.edit()
                        .putBoolean(Constants.LAYOUT_TYPE_SHARED_KEY, isGridLayoutManager).apply()
                    true
                }
                R.id.video_item -> {
                    val isShowingVideo = it.title == getString(R.string.is_not_showing_video)
                    sharedPreferences.edit()
                        .putBoolean(Constants.VIDEO_SHOW_SHARED_KEY, isShowingVideo).apply()
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun itemChanged(apod: Apod) {
        viewModel.updateApod(apod)
    }

    override fun itemClicked(item: Apod) {
        val actionHomeToDetail =
            FavoriteFragmentDirections.actionFavoriteToDetail(item.id.toString())
        findNavController().navigate(actionHomeToDetail)
    }
}