package com.yaya.apod.ui.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yaya.apod.DefaultConfig
import com.yaya.apod.R
import com.yaya.apod.data.model.Apod
import com.yaya.apod.data.repo.Status
import com.yaya.apod.databinding.FragmentHomeBinding
import com.yaya.apod.ui.adapters.MediaAdapter
import com.yaya.apod.ui.adapters.MediaLoadStateAdapter
import com.yaya.apod.ui.adapters.holders.ApodViewHolder
import com.yaya.apod.ui.component.VerticalSpaceItemDecoration
import com.yaya.apod.util.AndroidUtils
import com.yaya.apod.viewmodels.ApodViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged


@AndroidEntryPoint
class HomeFragment : Fragment(), ApodViewHolder.ItemChangeDelegate {
    private var binding: FragmentHomeBinding? = null
    private val viewModel: ApodViewModel by viewModels()
    private lateinit var adapter: MediaAdapter
    private val layoutTypeSharedKey = "isGridLayoutManager"
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.home_menu, menu)
    }


    private fun setRecyclerViewLayoutManager(isGridLayoutManager: Boolean) {
        binding!!.listView.layoutManager = if (isGridLayoutManager) {
            GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        } else {
            LinearLayoutManager(activity)
        }
        binding!!.listView.adapter = adapter.withLoadStateHeaderAndFooter(
            header = MediaLoadStateAdapter(adapter),
            footer = MediaLoadStateAdapter(adapter)
        )
        binding!!.needUpKey = false
        sharedPreferences.edit().putBoolean(layoutTypeSharedKey, isGridLayoutManager).apply()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences(
            DefaultConfig.APP_SHARED_PREF_NAME,
            Context.MODE_PRIVATE
        )

        initAdapter()

        initRecyclerView()

        initSwipeToRefresh()

        initArrowUpKey()
//        subscribeUi(adapter)

        return binding!!.root
    }

    private fun initArrowUpKey() {
        binding!!.arrowUp.setOnClickListener {

            binding!!.listView.smoothScrollToPosition(0)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter.addLoadStateListener {
            if (adapter.itemCount >= 1) {
                binding!!.isError = false
            }
        }

        lifecycleScope.launchWhenResumed {
            adapter.loadStateFlow.distinctUntilChanged { old, new ->
                (old.refresh is LoadState.Error && new.refresh is LoadState.Error && ((old.refresh as LoadState.Error).error == (new.refresh as LoadState.Error).error))
            }.collectLatest { loadStates ->
                when (loadStates.mediator?.refresh) {
                    is LoadState.Loading -> {
                        binding!!.swipeRefresh.isRefreshing = true
                        binding!!.isLoading = true
                        binding!!.isError = false
                    }
                    is LoadState.Error -> {
                        binding!!.swipeRefresh.isRefreshing = false
                        binding!!.isError = true
                        binding!!.isLoading = false
                        binding!!.errorTxtView.text =
                            (loadStates.mediator?.refresh as LoadState.Error).error.message
                        AlertDialog.Builder(context)
                            .setTitle(getString(R.string.app_name))
                            .setMessage((loadStates.mediator?.refresh as LoadState.Error).error.message)
                            .setPositiveButton(
                                getString(R.string.OK)
                            ) { dialog, _ -> dialog.dismiss() }.create().show()
                    }
                    is LoadState.NotLoading -> {
                        binding!!.isLoading = false
                        binding!!.isError = false
                        binding!!.swipeRefresh.isRefreshing = false
                    }
                }

                binding!!.invalidateAll()
            }
        }
    }

    private fun initAdapter() {
        adapter = MediaAdapter(this)
        lifecycleScope.launchWhenCreated {
            viewModel.apods().collectLatest {
                adapter.submitData(it)
            }
        }
    }

    private fun initRecyclerView() {
        binding!!.listView.addItemDecoration(
            VerticalSpaceItemDecoration(
                AndroidUtils.dp(
                    requireActivity().applicationContext,
                    5f
                )
            )
        )

        setRecyclerViewLayoutManager(sharedPreferences.getBoolean(layoutTypeSharedKey, false))
        binding!!.listView.adapter = adapter.withLoadStateHeaderAndFooter(
            header = MediaLoadStateAdapter(adapter),
            footer = MediaLoadStateAdapter(adapter)
        )
        binding!!.listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val currentIndex = (binding!!.listView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                binding!!.needUpKey = (binding!!.listView.layoutManager is GridLayoutManager && currentIndex > 3)
                        || (binding!!.listView.layoutManager is LinearLayoutManager && currentIndex > 1)
            }
        })
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

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val isGridLayoutManager = sharedPreferences.getBoolean(layoutTypeSharedKey, false)
        menu.findItem(R.id.grid_item).icon = if (isGridLayoutManager) {
            AppCompatResources.getDrawable(requireContext(), R.drawable.ic_grid_on)
        } else {
            AppCompatResources.getDrawable(requireContext(), R.drawable.ic_grid_off)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if (id == R.id.grid_item) {

            if (binding!!.listView.layoutManager is GridLayoutManager) {
                item.icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_grid_off)
                setRecyclerViewLayoutManager(false)
            } else {
                item.icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_grid_on)
                setRecyclerViewLayoutManager(true)
            }
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun itemChanged(item: Apod) {
        viewModel.updateApod(item)
    }
}