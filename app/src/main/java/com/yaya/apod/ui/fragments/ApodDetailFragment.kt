package com.yaya.apod.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.yaya.apod.R
import com.yaya.apod.data.model.Apod
import com.yaya.apod.databinding.FragmentApodDetailBinding
import com.yaya.apod.viewmodels.ApodDetailViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ApodDetailFragment : Fragment() {
    public interface Callback {
        fun addToFavorite(apod: Apod)
    }

    private val apodDetailViewModel: ApodDetailViewModel by viewModels()
    private lateinit var binding: FragmentApodDetailBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<FragmentApodDetailBinding>(
            inflater,
            R.layout.fragment_apod_detail,
            container,
            false
        ).apply {
            fab.imageTintList =
                AppCompatResources.getColorStateList(requireContext(), R.color.favorite_color);
            viewModel = apodDetailViewModel
            lifecycleOwner = viewLifecycleOwner
            callback = object : Callback {
                override fun addToFavorite(apod: Apod) {
                    apod.favorite = !apod.favorite
                    apodDetailViewModel.addApodToFavorite(apod)
                    val snackMessageStringId = if (apod.favorite) {
                        R.string.added_to_favorite
                    } else {
                        R.string.removed_from_favorite
                    }
                    Snackbar.make(root, snackMessageStringId, Snackbar.LENGTH_LONG).show()
                }
            }
            var isToolbarShown = false
            // scroll change listener begins at Y = 0 when image is fully collapsed
            apodDetailScrollview.setOnScrollChangeListener(
                NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->

                    // User scrolled past image to height of toolbar and the title text is
                    // underneath the toolbar, so the toolbar should be shown.
                    Log.v("TAGgg", "${scrollY}, ${toolbar.height}")
                    val shouldShowToolbar = scrollY > toolbar.height

                    // The new state of the toolbar differs from the previous state; update
                    // appbar and toolbar attributes.
                    if (isToolbarShown != shouldShowToolbar) {
                        isToolbarShown = shouldShowToolbar

                        // Use shadow animator to add elevation if toolbar is shown
                        appbar.isActivated = shouldShowToolbar

                        // Show the plant name if toolbar is shown
                        toolbarLayout.isTitleEnabled = shouldShowToolbar
                    }
                }
            )
            toolbar.setNavigationOnClickListener { view ->
                view.findNavController().navigateUp()
            }

            toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_wallpaper -> {
//                        setWall()
                        true
                    }
                    else -> false
                }
            }
        }

        setHasOptionsMenu(true)
        return binding.root
    }
}