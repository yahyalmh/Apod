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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.filter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yaya.apod.DefaultConfig
import com.yaya.apod.R
import com.yaya.apod.api.MediaType
import com.yaya.apod.data.model.Apod
import com.yaya.apod.databinding.FragmentHomeBinding
import com.yaya.apod.ui.adapters.MediaAdapter
import com.yaya.apod.ui.adapters.MediaLoadStateAdapter
import com.yaya.apod.ui.adapters.holders.ApodViewHolder
import com.yaya.apod.ui.component.OptionalDialog
import com.yaya.apod.ui.component.SharedPreferenceBooleanLiveData
import com.yaya.apod.ui.component.VerticalSpaceItemDecoration
import com.yaya.apod.util.AndroidUtils
import com.yaya.apod.util.Constants
import com.yaya.apod.viewmodels.ApodViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@AndroidEntryPoint
class HomeFragment : Fragment(), ApodViewHolder.ItemDelegate {
    private var lastFirstVisiblePosition: Int = 0
    private var binding: FragmentHomeBinding? = null
    private val viewModel: ApodViewModel by viewModels()
    private lateinit var adapter: MediaAdapter
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences(
            DefaultConfig.APP_SHARED_PREF_NAME, Context.MODE_PRIVATE
        )

        initAdapter()

        initRecyclerView()

        initSwipeToRefresh()

        initArrowUpKey()

        initToolbarMenu()

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        collectApods()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.distinctUntilChanged { old, new ->
                    val res =
                        (old.refresh is LoadState.Error && new.refresh is LoadState.Error && ((old.refresh as LoadState.Error).error == (new.refresh as LoadState.Error).error))
                    res
                }.collectLatest { loadStates ->
                    when (loadStates.mediator?.refresh) {
                        is LoadState.Loading -> {
                            setLoadingState()
                        }
                        is LoadState.Error -> {
                            setErrorState(loadStates)
                        }
                        is LoadState.NotLoading -> {
                            setNotLoadingState()
                        }
                    }

                    binding!!.invalidateAll()
                }
            }
        }
    }

    private fun collectApods() {
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
                viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                    viewModel.apods().map { apods ->
                        apods.filter { apod ->
                            if (isShowingVideo) {
                                true
                            } else {
                                apod.mediaType != MediaType.VIDEO.type
                            }
                        }
                    }.collectLatest { adapter.submitData(it) }
                }
            }
        }
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
            binding!!.listView.adapter = adapter.withLoadStateHeaderAndFooter(
                header = MediaLoadStateAdapter(adapter), footer = MediaLoadStateAdapter(adapter)
            )
            binding!!.needUpKey = false
        }
    }

    private fun setErrorState(loadStates: CombinedLoadStates) {
        binding!!.swipeRefresh.isRefreshing = false
        if (binding!!.listView.adapter!!.itemCount <= 0) {
            binding!!.isError = true
        }
        binding!!.isLoading = false
        binding!!.errorTxtView.text =
            (loadStates.mediator?.refresh as LoadState.Error).error.message

        val dialog = OptionalDialog.Builder(requireContext()).setIcon(R.drawable.ic_error)
            .setHint((loadStates.mediator?.refresh as LoadState.Error).error.message!!)
            .setFirstOption(
                getString(R.string.ok),
                object : OptionalDialog.OptionalDialogClickListener {
                    override fun onClick(dialog: OptionalDialog) {
                        dialog.dismiss()
                    }
                })

        dialog.show()
    }

    private fun setNotLoadingState() {
        binding!!.isLoading = false
        binding!!.isError = false
        binding!!.swipeRefresh.isRefreshing = false
    }

    private fun setLoadingState() {
        binding!!.swipeRefresh.isRefreshing = true
        binding!!.isLoading = true
        binding!!.isError = false
    }

    private fun initAdapter() {
        adapter = MediaAdapter(this)
        //  restores the RecyclerView state only when the adapter is not empty (adapter.getItemCount() > 0)
        adapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        adapter.addLoadStateListener {
            if (adapter.itemCount >= 1) {
                binding!!.isError = false
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
        binding!!.listView.adapter = adapter.withLoadStateHeaderAndFooter(
            header = MediaLoadStateAdapter(adapter), footer = MediaLoadStateAdapter(adapter)
        )
        binding!!.listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                lastFirstVisiblePosition =
                    (binding!!.listView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                setArrowUpKey()
            }
        })
    }

    private fun initSwipeToRefresh() {
        binding!!.swipeRefresh.setOnRefreshListener { adapter.refresh() }
    }

    private fun initArrowUpKey() {
        binding!!.arrowUp.setOnClickListener {
            binding!!.listView.smoothScrollToPosition(0)
        }
    }

    private fun setArrowUpKey() {
        binding!!.needUpKey =
            (binding!!.listView.layoutManager is GridLayoutManager && lastFirstVisiblePosition > 3) || (binding!!.listView.layoutManager is LinearLayoutManager && lastFirstVisiblePosition > 1)
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

    override fun onResume() {
        super.onResume()
        (binding!!.listView.layoutManager!! as LinearLayoutManager).scrollToPositionWithOffset(
            lastFirstVisiblePosition, 0
        )
    }

    override fun onPause() {
        super.onPause()
        lastFirstVisiblePosition =
            (binding!!.listView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


    override fun itemChanged(apod: Apod) {
        viewModel.updateApod(apod)
    }

    override fun itemClicked(item: Apod) {
        val actionHomeToDetail = HomeFragmentDirections.actionHomeToDetail(item.id.toString())
        findNavController().navigate(actionHomeToDetail)
    }
}