package com.yaya.apod.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.yaya.apod.api.ApiSuccessResponse
import com.yaya.apod.databinding.FragmentHomeBinding
import com.yaya.apod.ui.adapters.PictureAdapter
import com.yaya.apod.viewmodels.PicturesViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var binding: FragmentHomeBinding? = null
    private val viewModel: PicturesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding!!.listView.layoutManager = LinearLayoutManager(activity)
        val adapter = PictureAdapter()
        binding!!.listView.adapter = adapter
        subscribeUi(adapter)

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView = binding!!.listView
        recyclerView.addItemDecoration(VerticalSpaceItemDecoration(20))
    }

    private fun subscribeUi(adapter: PictureAdapter) {
        viewModel.content.observe(viewLifecycleOwner) {
            adapter.submitData(((it as ApiSuccessResponse).body))
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}