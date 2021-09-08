package com.yaya.apod.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yaya.apod.util.Constants
import com.yaya.apod.DefaultConfig
import com.yaya.apod.R
import com.yaya.apod.data.model.Apod
import com.yaya.apod.databinding.FragmentFavoriteBinding
import com.yaya.apod.ui.adapters.FavoriteAdapter
import com.yaya.apod.ui.adapters.holders.ApodViewHolder
import com.yaya.apod.ui.component.VerticalSpaceItemDecoration
import com.yaya.apod.util.AndroidUtils
import com.yaya.apod.viewmodels.FavoriteViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoriteFragment : Fragment(), ApodViewHolder.ItemChangeDelegate {
    private lateinit var sharedPreferences: SharedPreferences
    private var binding: FragmentFavoriteBinding? = null
    private lateinit var adapter: FavoriteAdapter
    private val viewModel: FavoriteViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences(
            DefaultConfig.APP_SHARED_PREF_NAME,
            Context.MODE_PRIVATE
        )
        setHasOptionsMenu(true)

        initAdapter()

        initRecyclerView()

        initArrowUpKey()

        return binding!!.root
    }

    private fun initAdapter() {
        adapter = FavoriteAdapter(this)
        lifecycleScope.launchWhenCreated {
            viewModel.favorites.observe(viewLifecycleOwner) {
                if (it.size <= 0) {
                    binding!!.isFavorite = true
                    binding!!.noFavoriteTextView.text = getString(R.string.no_favorite_error)
                } else {
                    binding!!.isFavorite = false
                }
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

        setRecyclerViewLayoutManager(
            sharedPreferences.getBoolean(
                Constants.LAYOUT_TYPE_SHARED_KEY,
                false
            )
        )
        binding!!.listView.adapter = adapter
        binding!!.listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val currentIndex =
                    (binding!!.listView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                binding!!.needUpKey =
                    (binding!!.listView.layoutManager is GridLayoutManager && currentIndex > 3)
                            || (binding!!.listView.layoutManager is LinearLayoutManager && currentIndex > 1)
            }
        })
    }

    private fun initArrowUpKey() {
        binding!!.arrowUp.setOnClickListener {
            binding!!.listView.smoothScrollToPosition(0)
        }
    }


    private fun setRecyclerViewLayoutManager(isGridLayoutManager: Boolean) {
        binding!!.listView.layoutManager = if (isGridLayoutManager) {
            GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        } else {
            LinearLayoutManager(activity)
        }
        binding!!.listView.adapter = adapter
        binding!!.needUpKey = false
        sharedPreferences.edit().putBoolean(Constants.LAYOUT_TYPE_SHARED_KEY, isGridLayoutManager)
            .apply()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.home_menu, menu)
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val isGridLayoutManager =
            sharedPreferences.getBoolean(Constants.LAYOUT_TYPE_SHARED_KEY, false)
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


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun itemChanged(item: Apod) {
        viewModel.updateApod(item)
    }
}