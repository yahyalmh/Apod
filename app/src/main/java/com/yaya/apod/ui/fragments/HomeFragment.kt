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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yaya.apod.DefaultConfig
import com.yaya.apod.R
import com.yaya.apod.data.model.Apod
import com.yaya.apod.databinding.FragmentHomeBinding
import com.yaya.apod.ui.adapters.MediaAdapter
import com.yaya.apod.ui.adapters.MediaLoadStateAdapter
import com.yaya.apod.ui.adapters.holders.ApodViewHolder
import com.yaya.apod.ui.component.OptionalDialog
import com.yaya.apod.ui.component.VerticalSpaceItemDecoration
import com.yaya.apod.util.AndroidUtils
import com.yaya.apod.util.Constants
import com.yaya.apod.viewmodels.ApodViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.apods().collectLatest {
                    adapter.submitData(it)
                }
            }
        }

        adapter.addLoadStateListener {
            if (adapter.itemCount >= 1) {
                binding!!.isError = false
            }
        }

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

    override fun onResume() {
        super.onResume()
        (binding!!.listView.layoutManager!! as LinearLayoutManager).scrollToPositionWithOffset(
            lastFirstVisiblePosition, 0
        )
    }

    private fun setErrorState(loadStates: CombinedLoadStates) {
        binding!!.swipeRefresh.isRefreshing = false
        if (binding!!.listView.adapter!!.itemCount <= 0){
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

//        viewModel.apods().map { apods ->
//            apods.filter { apod -> apod.mediaType == MediaType.VIDEO.type }
//        }
    }

    private fun initRecyclerView() {
        binding!!.listView.addItemDecoration(
            VerticalSpaceItemDecoration(
                AndroidUtils.dp(
                    requireActivity().applicationContext, 5f
                )
            )
        )

        setRecyclerViewLayoutManager(
            sharedPreferences.getBoolean(
                Constants.LAYOUT_TYPE_SHARED_KEY, false
            )
        )
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

    private fun setArrowUpKey() {
        binding!!.needUpKey =
            (binding!!.listView.layoutManager is GridLayoutManager && lastFirstVisiblePosition > 3) || (binding!!.listView.layoutManager is LinearLayoutManager && lastFirstVisiblePosition > 1)
    }

    private fun setRecyclerViewLayoutManager(isGridLayoutManager: Boolean) {
        binding!!.listView.layoutManager = if (isGridLayoutManager) {
            GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        } else {
            LinearLayoutManager(activity)
        }
        binding!!.listView.adapter = adapter.withLoadStateHeaderAndFooter(
            header = MediaLoadStateAdapter(adapter), footer = MediaLoadStateAdapter(adapter)
        )
        sharedPreferences.edit().putBoolean(Constants.LAYOUT_TYPE_SHARED_KEY, isGridLayoutManager)
            .apply()
        (binding!!.listView.layoutManager!! as LinearLayoutManager).scrollToPositionWithOffset(
            lastFirstVisiblePosition, 0
        )
        setArrowUpKey()
    }

    private fun initSwipeToRefresh() {
        binding!!.swipeRefresh.setOnRefreshListener { adapter.refresh() }
    }

    private fun initArrowUpKey() {
        binding!!.arrowUp.setOnClickListener {
            binding!!.listView.smoothScrollToPosition(0)
        }
    }

    private fun initToolbarMenu() {
        binding!!.toolbar.inflateMenu(R.menu.home_menu)
        updateToolbar()
        binding!!.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.grid_item -> {
                    if (binding!!.listView.layoutManager is GridLayoutManager) {
                        setRecyclerViewLayoutManager(false)
                    } else {
                        setRecyclerViewLayoutManager(true)
                    }
                    updateToolbar()
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    private fun updateToolbar() {
        val menu = binding!!.toolbar.menu
        val isGridLayoutManager =
            sharedPreferences.getBoolean(Constants.LAYOUT_TYPE_SHARED_KEY, false)

        menu.findItem(R.id.grid_item).icon = if (isGridLayoutManager) {
            AppCompatResources.getDrawable(requireContext(), R.drawable.ic_grid_on)
        } else {
            AppCompatResources.getDrawable(requireContext(), R.drawable.ic_grid_off)
        }
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

/*    private fun subscribeUi(adapter: MediaAdapter) {
        viewModel.contents.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    binding!!.isLoading = true
                    binding!!.isError = false
                }
                Status.SUCCESS -> {
                    adapter.submitData(it.data!!)
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

    }*/
}