//package com.yaya.apod.ui.fragments
//
//import android.content.Context
//import android.content.SharedPreferences
//import android.os.Bundle
//import android.view.*
//import androidx.appcompat.content.res.AppCompatResources
//import androidx.databinding.DataBindingComponent
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.ViewModel
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.yaya.apod.DefaultConfig
//import com.yaya.apod.R
//import dagger.hilt.android.AndroidEntryPoint
//
//
//@AndroidEntryPoint
//abstract class BaseFragment : Fragment() {
//    lateinit var viewModel: ViewModel
////    lateinit var adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
//    val layoutTypeSharedKey = "isGridLayoutManager"
//    lateinit var sharedPreferences: SharedPreferences
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        sharedPreferences = requireContext().getSharedPreferences(
//            DefaultConfig.APP_SHARED_PREF_NAME,
//            Context.MODE_PRIVATE
//        )
//        setHasOptionsMenu(true)
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
//        inflater.inflate(R.menu.home_menu, menu)
//    }
//
//    override fun onPrepareOptionsMenu(menu: Menu) {
//        super.onPrepareOptionsMenu(menu)
//        val isGridLayoutManager = sharedPreferences.getBoolean(layoutTypeSharedKey, false)
//        menu.findItem(R.id.grid_item).icon = if (isGridLayoutManager) {
//            AppCompatResources.getDrawable(requireContext(), R.drawable.ic_grid_on)
//        } else {
//            AppCompatResources.getDrawable(requireContext(), R.drawable.ic_grid_off)
//        }
//    }
//
//    abstract fun setRecyclerViewLayoutManager(isGridLayoutManager: Boolean)
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        binding = null
//    }
//
//}