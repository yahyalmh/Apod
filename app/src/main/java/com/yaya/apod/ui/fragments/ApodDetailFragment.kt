package com.yaya.apod.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.drawable.Drawable
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.FileProvider
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.yaya.apod.BuildConfig
import com.yaya.apod.DefaultConfig
import com.yaya.apod.R
import com.yaya.apod.api.MediaType
import com.yaya.apod.data.model.Apod
import com.yaya.apod.databinding.FragmentApodDetailBinding
import com.yaya.apod.ui.component.OptionalDialog
import com.yaya.apod.util.Constants
import com.yaya.apod.viewmodels.ApodDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.Normalizer
import java.util.*


@AndroidEntryPoint
class ApodDetailFragment : Fragment(), Target {
    interface Callback {
        fun addToFavorite(apod: Apod)
    }

    private var askAboutStorage: Boolean = true
    private lateinit var sharedPreferences: SharedPreferences
    private var activityResultLauncher: ActivityResultLauncher<Array<String>>
    private val tmpFileName = "temp_file.jpg"
    private var imageBitmap: Bitmap? = null
    private val apodDetailViewModel: ApodDetailViewModel by viewModels()
    private lateinit var binding: FragmentApodDetailBinding

    private lateinit var apod: Apod
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBackPress()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        sharedPreferences = requireContext().getSharedPreferences(
            DefaultConfig.APP_SHARED_PREF_NAME, Context.MODE_PRIVATE
        )
        askAboutStorage = sharedPreferences.getBoolean(Constants.ASK_ABOUT_STORAGE_HARED_KEY, true)
        binding = DataBindingUtil.inflate<FragmentApodDetailBinding>(
            inflater, R.layout.fragment_apod_detail, container, false
        ).apply {
            fab.imageTintList =
                AppCompatResources.getColorStateList(requireContext(), R.color.favorite_color)
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
            apodDetailScrollview.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->

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
            })
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            whenStarted {
                apod = withContext(Dispatchers.IO) {
                    apodDetailViewModel.getApods()
                }
            }
            initToolbar()
            if (apod.mediaType == MediaType.IMAGE.type) {
                Picasso.get().load(apod.url).into(this@ApodDetailFragment)
            } else {
                loadWebView()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebView() {
        binding.loading.visibility = View.VISIBLE
        binding.webView.addJavascriptInterface(
            SimpleWebJavascriptInterface(requireActivity()), "Android"
        )
        binding.webView.settings.javaScriptEnabled = true
        val videoFrame =
            "<html><body><iframe width=\"100%\" height=\"100%\" src=\"${apod.url}\" frameborder=\"0\" allowfullscreen></iframe></body></html>"
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                binding.loading.visibility = View.INVISIBLE
                view.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                binding.loading.visibility = View.INVISIBLE
            }

            override fun onReceivedHttpError(
                view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?
            ) {
                setErrorWebView()
            }

            override fun onReceivedSslError(
                view: WebView?, handler: SslErrorHandler?, error: SslError?
            ) {
                setErrorWebView()
            }

            override fun onReceivedError(
                view: WebView?, request: WebResourceRequest?, error: WebResourceError?
            ) {
                binding.loading.visibility = View.INVISIBLE
                setErrorWebView()
            }
        }
        binding.webView.loadData(videoFrame, "text/html", "utf-8")
    }

    fun setErrorWebView() {
        val defaultErrorPagePath = "file:///android_asset/html/default_error_page.html"
        binding.webView.loadUrl(defaultErrorPagePath)
        binding.webView.invalidate()
    }

    inner class SimpleWebJavascriptInterface(private val activity: Activity) {
        @JavascriptInterface
        fun reloadWebPage() {
            activity.runOnUiThread {
                Log.e("TAGgg", "uiThread")
                loadWebView()
            }
        }
    }

    private fun initToolbar() {
        binding.toolbar.setNavigationOnClickListener { view ->
            view.findNavController().navigateUp()
        }

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_wallpaper -> {
                    showSetWallpaperDialog()
                    true
                }
                R.id.action_share -> {
                    shareContent()
                    true
                }
                R.id.action_download -> {
                    downloadContent()
                    true
                }
                else -> false
            }
        }
        if (apod.mediaType == MediaType.VIDEO.type) {
            binding.toolbar.menu.findItem(R.id.action_wallpaper).isVisible = false
        }
        setHasOptionsMenu(true)
    }

    private fun downloadContent() {
        val showCheckPermission =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && requireContext().checkSelfPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED

        if (showCheckPermission) {
            val shouldShowRationale =
                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (!askAboutStorage && !shouldShowRationale) {
                showPermissionErrorAlert()
            } else {
                showRationalPermissionAlert()
            }
        } else {
            saveContentToPath()
        }
    }

    private fun showRationalPermissionAlert() {
        val dialog = OptionalDialog.Builder(requireContext()).setIcon(R.drawable.ic_storage)
            .setHint(getString(R.string.permission_request)).setSecondOption(getString(R.string.ok),
                object : OptionalDialog.OptionalDialogClickListener {
                    override fun onClick(dialog: OptionalDialog) {
                        dialog.dismiss()
                        activityResultLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        )
                    }
                }).setFirstOption(
                getString(R.string.cancel),
                object : OptionalDialog.OptionalDialogClickListener {
                    override fun onClick(dialog: OptionalDialog) {
                        dialog.dismiss()
                    }
                })
        dialog.show()
    }

    private fun saveContentToPath() {
        val reservedChars = "[|\\<\":>+\\[]/']"
        var fileName = if ( apod.title.length > 20) {
            apod.title.substring(0, 20).lowercase(Locale.getDefault())
        } else {
            apod.title.lowercase(Locale.getDefault())
        }
        fileName = Normalizer.normalize(fileName, Normalizer.Form.NFD)
        fileName = fileName.replace(Regex("[^a-zA-Z0-9]"), " ")

        if (apod.mediaType == MediaType.IMAGE.type) {
            val pictureDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val appDir = File(pictureDir, Constants.APP_EXTERNAL_DIR_NAME)
            if (!appDir.exists()) {
                appDir.mkdirs()
            }

            val file = File(appDir, "${fileName}.jpg")
            if (imageBitmap != null) {
                val wasSuccess =
                    imageBitmap!!.compress(CompressFormat.PNG, 100, FileOutputStream(file))
                if (wasSuccess) {
                    OptionalDialog.Builder(requireContext())
                        .setMessage(getString(R.string.success_download))
                        .setHint(String.format(getString(R.string.open_file), appDir.absolutePath))
                        .setIcon(R.drawable.ic_download).setSecondOption(getString(R.string.file),
                            object : OptionalDialog.OptionalDialogClickListener {
                                override fun onClick(dialog: OptionalDialog) {
                                    val intent = Intent()
                                    intent.action = Intent.ACTION_VIEW
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    val uri = if (Build.VERSION.SDK_INT >= 24) {
                                        FileProvider.getUriForFile(
                                            activity!!,
                                            BuildConfig.APPLICATION_ID + ".provider",
                                            file
                                        )
                                    } else {
                                        Uri.fromFile(file)
                                    }
                                    intent.setDataAndType(uri, "image/*")
                                    startActivity(intent)
                                    dialog.dismiss()
                                }
                            }).setFirstOption(getString(R.string.cancel),
                            object : OptionalDialog.OptionalDialogClickListener {
                                override fun onClick(dialog: OptionalDialog) {
                                    dialog.dismiss()
                                }
                            }).show()

                } else {
                    showSnackBar(R.string.download_failed)
                }
            } else {
                showSnackBar(R.string.try_again)
            }
        } else {
            val movieDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            val appDir = File(movieDir, Constants.APP_EXTERNAL_DIR_NAME)
            if (!appDir.exists()) {
                appDir.mkdirs()
            }
            val file = File(appDir, "${fileName}.mp4")
            showSnackBar(R.string.unable_download_video)
//            startDownload(apod.url, Uri.fromFile(file))
        }

    }

    private fun startDownload(url: String, dest: Uri) {
        val uri = Uri.parse(url)
        val mgr = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//        registerReceiver(onComplete,
//            new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
//        registerReceiver(onNotificationClick,
//            new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));

        val lastDownload = mgr.enqueue(
            DownloadManager.Request(uri).setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
            ).setAllowedOverRoaming(false)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setTitle("Demo").setDescription("Something useful. No, really.")
                .setDestinationUri(dest)
        )

    }

    private fun shareContent() {
        if (apod.mediaType == MediaType.IMAGE.type && imageBitmap != null) {
            val tmpFile = File(requireContext().cacheDir, tmpFileName)
            imageBitmap!!.compress(CompressFormat.PNG, 100, FileOutputStream(tmpFile))

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "image/jpeg"
            if (Build.VERSION.SDK_INT >= 24) {
                try {
                    intent.putExtra(
                        Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                            requireContext(), BuildConfig.APPLICATION_ID + ".provider", tmpFile
                        )
                    )
                    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                } catch (ignore: java.lang.Exception) {
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tmpFile))
                }
            } else {
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tmpFile))
            }
            startActivity(Intent.createChooser(intent, "Share Image"))
        } else {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_TEXT, apod.url)
            shareIntent.type = "text/plain"
            startActivity(Intent.createChooser(shareIntent, "Share"))
//               .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        }
    }

    private fun showSetWallpaperDialog() {
        val dialog = OptionalDialog.Builder(requireContext()).setIcon(R.drawable.ic_wallpaper)
            .withCancelBtn().setMessage(getString(R.string.set_wall_question)).setCancelable(false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dialog.setHint(getString(R.string.set_wall_hint_N))
                .setFirstOption(getString(R.string.set_home_screen),
                    object : OptionalDialog.OptionalDialogClickListener {
                        override fun onClick(dialog: OptionalDialog) {
                            dialog.dismiss()
                            binding.progressOverlay.show()
                            setWallpaper(WallpaperManager.FLAG_SYSTEM)
                        }
                    }).setSecondOption(getString(R.string.set_lock_screen),
                    object : OptionalDialog.OptionalDialogClickListener {
                        override fun onClick(dialog: OptionalDialog) {
                            dialog.dismiss()
                            binding.progressOverlay.show()
                            setWallpaper(WallpaperManager.FLAG_LOCK)
                        }
                    }).setThirdOption(getString(R.string.set_both),
                    object : OptionalDialog.OptionalDialogClickListener {
                        override fun onClick(dialog: OptionalDialog) {
                            dialog.dismiss()
                            binding.progressOverlay.show()
                            setWallpaper(null)
                        }
                    })
        } else {
            dialog.setHint(getString(R.string.set_wall_hint))
                .setSecondOption(getString(R.string.set_wallpaper),
                    object : OptionalDialog.OptionalDialogClickListener {
                        override fun onClick(dialog: OptionalDialog) {
                            dialog.dismiss()
                            binding.progressOverlay.show()
                            setWallpaper(null)
                        }
                    }).setFirstOption(getString(R.string.cancel),
                    object : OptionalDialog.OptionalDialogClickListener {
                        override fun onClick(dialog: OptionalDialog) {
                            dialog.dismiss()
                        }
                    })
        }

        dialog.show()
    }

    private fun setWallpaper(flag: Int?) {
        if (!binding.progressOverlay.isCanceled) {
            val wallpaperManager = WallpaperManager.getInstance(requireContext())
            if (imageBitmap == null) {
                showSnackBar(R.string.download_image_failed)
                return
            }
            val result: Int = if (flag != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                wallpaperManager.setBitmap(imageBitmap, null, false, flag)
            } else {
                try {
                    // test this
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(
                            imageBitmap, null, false, WallpaperManager.FLAG_LOCK
                        )
                        wallpaperManager.setBitmap(
                            imageBitmap, null, false, WallpaperManager.FLAG_SYSTEM
                        )
                    }

                    wallpaperManager.setBitmap(imageBitmap)
                    1
                } catch (e: IOException) {
                    0
                }
            }
            if (result > 0) {
                showSnackBar(R.string.set_wall_success)
            } else {
                showSnackBar(R.string.set_wall_failed)
            }
        }
        binding.progressOverlay.hide()
    }

    private fun setBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.progressOverlay.isShowed) {
                        binding.progressOverlay.showStopDialog()
                    } else {
                        // if you want onBackPressed() to be called as normal afterwards
                        if (isEnabled) {
                            isEnabled = false
                            requireActivity().onBackPressed()
                        }
                    }
                }
            })
    }

    init {
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            askAboutStorage = result[Manifest.permission.READ_EXTERNAL_STORAGE]!!
            sharedPreferences.edit()
                .putBoolean(Constants.ASK_ABOUT_STORAGE_HARED_KEY, askAboutStorage).apply()

            var allAreGranted = true
            for (b in result.values) {
                allAreGranted = allAreGranted && b
            }

            if (allAreGranted) {
                saveContentToPath()
            }
        }
    }

    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
        if (!binding.progressOverlay.isCanceled) {
            if (bitmap == null) {
                showSnackBar(R.string.download_image_failed)
                return
            }
            imageBitmap = bitmap
//            binding.detailImage.setImageBitmap(imageBitmap)
        }
    }

    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
        showSnackBar(R.string.download_image_failed)
    }

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

    private fun showSnackBar(resId: Int) {
        binding.progressOverlay.hide()
        Snackbar.make(
            binding.root, resId, Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        val tmpFile = File(requireContext().cacheDir, tmpFileName)
        if (tmpFile.exists()) {
            tmpFile.delete()
        }
    }

    private fun showPermissionErrorAlert() {
        val dialog = OptionalDialog.Builder(requireContext()).setIcon(R.drawable.ic_storage)
            .setHint(getString(R.string.permission_setting_request))
            .setSecondOption(getString(R.string.setting),
                object : OptionalDialog.OptionalDialogClickListener {
                    override fun onClick(dialog: OptionalDialog) {
                        dialog.dismiss()
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.parse("package:" + requireContext().packageName)
                            startActivity(intent)
                        } catch (e: Exception) {
                            Log.e("TAG", e.message!!)
                        }
                    }
                }).setFirstOption(
                getString(R.string.cancel),
                object : OptionalDialog.OptionalDialogClickListener {
                    override fun onClick(dialog: OptionalDialog) {
                        dialog.dismiss()
                    }
                })

        dialog.show()
    }
}