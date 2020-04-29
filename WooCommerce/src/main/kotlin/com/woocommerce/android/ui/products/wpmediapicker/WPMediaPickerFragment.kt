package com.woocommerce.android.ui.products.wpmediapicker

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.woocommerce.android.R
import com.woocommerce.android.RequestCodes
import com.woocommerce.android.analytics.AnalyticsTracker
import com.woocommerce.android.extensions.navigateBackWithResult
import com.woocommerce.android.ui.base.BaseFragment
import com.woocommerce.android.ui.dialog.CustomDiscardDialog
import com.woocommerce.android.ui.main.MainActivity.Companion.BackPressListener
import com.woocommerce.android.ui.products.wpmediapicker.WPMediaLibraryGalleryView.OnWPMediaGalleryClickListener
import com.woocommerce.android.viewmodel.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_wpmedia_picker.*
import javax.inject.Inject

class WPMediaPickerFragment : BaseFragment(), OnWPMediaGalleryClickListener, BackPressListener {
    companion object {
        const val ARG_SELECTED_IMAGES = "selected_image_ids"
        private const val KEY_IS_CONFIRMING_DISCARD = "is_confirming_discard"
    }

    @Inject lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: WPMediaPickerViewModel by viewModels { viewModelFactory }

    private var isConfirmingDiscard = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_wpmedia_picker, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_done, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViewModel()

        if (savedInstanceState?.getBoolean(KEY_IS_CONFIRMING_DISCARD) == true) {
            confirmDiscard()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_IS_CONFIRMING_DISCARD, isConfirmingDiscard)
        super.onSaveInstanceState(outState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_done -> {
                navigateBackWithResult()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
    }

    private fun initializeViewModel() {
        setupObservers(viewModel)
        viewModel.start()
    }

    private fun setupObservers(viewModel: WPMediaPickerViewModel) {
        viewModel.mediaList.observe(viewLifecycleOwner, Observer {
            wpMediaGallery.showImages(it, this)
        })
    }

    override fun getFragmentTitle(): String {
        val count = wpMediaGallery.getSelectionCount()
        return if (count == 0) {
            getString(R.string.product_wpmedia_title)
        } else {
            String.format(getString(R.string.selection_count), count)
        }
    }

    private fun navigateBackWithResult() {
        if (wpMediaGallery.getSelectionCount() > 0) {
            val bundle = Bundle().also {
                it.putParcelableArrayList(ARG_SELECTED_IMAGES, wpMediaGallery.getSelectedImages())
            }
            requireActivity().navigateBackWithResult(
                    RequestCodes.WPMEDIA_LIBRARY_PICKER,
                    bundle,
                    R.id.nav_host_fragment_main,
                    R.id.productDetailFragment
            )
        } else {
            findNavController().navigateUp()
        }
    }

    /**
     * User has scrolled to the bottom of the gallery, fire a request to load more
     */
    override fun onRequestLoadMore() {
        viewModel.onLoadMoreRequested()
    }

    /**
     * User has selected/deselected in the gallery, update the title to show the selection count
     */
    override fun onSelectionCountChanged() {
        requireActivity().title = getFragmentTitle()
    }

    override fun onRequestAllowBackPress(): Boolean {
        if (wpMediaGallery.getSelectionCount() > 0) {
            confirmDiscard()
            return false
        } else {
            return true
        }
    }

    private fun confirmDiscard() {
        isConfirmingDiscard = true
        CustomDiscardDialog.showDiscardDialog(
                requireActivity(),
                posBtnAction = DialogInterface.OnClickListener { _, _ ->
                    isConfirmingDiscard = false
                    findNavController().navigateUp()
                },
                negBtnAction = DialogInterface.OnClickListener { _, _ ->
                    isConfirmingDiscard = false
                })
    }
}
