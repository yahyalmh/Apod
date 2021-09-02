package com.yaya.apod.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.yaya.apod.data.model.Apod
import com.yaya.apod.data.repo.Status
import com.yaya.apod.databinding.FragmentHomeBinding
import com.yaya.apod.ui.adapters.MediaAdapter
import com.yaya.apod.ui.adapters.MediaLoadStateAdapter
import com.yaya.apod.ui.adapters.holders.ApodViewHolder
import com.yaya.apod.viewmodels.ApodViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest


@AndroidEntryPoint
class HomeFragment : Fragment(), ApodViewHolder.ItemChangeDelegate {
    private var binding: FragmentHomeBinding? = null
    private val viewModel: ApodViewModel by viewModels()
    private lateinit var adapter: MediaAdapter
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding!!.listView.addItemDecoration(VerticalSpaceItemDecoration(20))
        initAdapter()
        initSwipeToRefresh()
//        search(adapter)
//        subscribeUi(adapter)

        return binding!!.root
    }

    private fun initAdapter() {
        binding!!.listView.layoutManager = LinearLayoutManager(activity)
        adapter = MediaAdapter(this)
        binding!!.listView.adapter = adapter.withLoadStateHeaderAndFooter(
            header = MediaLoadStateAdapter(adapter),
            footer = MediaLoadStateAdapter(adapter)
        )

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { loadStates ->
                binding!!.swipeRefresh.isRefreshing =
                    loadStates.mediator?.refresh is LoadState.Loading
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.apods().collectLatest {
                adapter.submitData(it)
            }
        }
        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow
                // Use a state-machine to track LoadStates such that we only transition to
                // NotLoading from a RemoteMediator load if it was also presented to UI.
                .asLiveData()
                // Only emit when REFRESH changes, as we only want to react on loads replacing the
                // list.
                .distinctUntilChanged().observe(viewLifecycleOwner) { it.refresh }
//                // Only react to cases where REFRESH completes i.e., NotLoading.
//                .filter { it.refresh is LoadState.NotLoading }
            // Scroll to top is synchronous with UI updates, even if remote load was triggered.
//                .collect { binding.list.scrollToPosition(0) }
        }
    }

    private fun initSwipeToRefresh() {
        binding!!.swipeRefresh.setOnRefreshListener { adapter.refresh() }
    }

    private fun subscribeUi(adapter: MediaAdapter) {
        viewModel.contents.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    binding!!.isLoading = true
                    binding!!.isError = false
                }
                Status.SUCCESS -> {
                    adapter.submitDatas(it.data!!)
                    binding!!.isLoading = false
                    binding!!.isError = false
                }
                Status.ERROR -> {
                    binding!!.isError = true
                    binding!!.isLoading = false
                    binding!!.errorTxtView.text = it.message
                }
            }
            binding!!.invalidateAll()
        }


//        viewModel.todayContent.observe(viewLifecycleOwner) {
//            if (it.data != null) {
//                adapter.addData(it.data)
//            }
//        }
    }

    private fun search(adapter: MediaAdapter) {
//        viewModel.fetchContents().observe(viewLifecycleOwner) {
//            adapter.submitData(viewLifecycleOwner.lifecycle, it)
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun itemChanged(item: Apod) {
        viewModel.updateApod(item)
    }
}