package com.woocommerce.android.ui.refunds

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.woocommerce.android.R
import com.woocommerce.android.analytics.AnalyticsTracker
import com.woocommerce.android.ui.base.UIMessageResolver
import com.woocommerce.android.ui.main.navigateBackWithResult
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_refund_confirmation.*
import javax.inject.Inject

class RefundConfirmationFragment : DaggerFragment() {
    companion object {
        const val REFUND_SUCCESS_KEY = "refund-success-key"
    }
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var uiMessageResolver: UIMessageResolver

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        return inflater.inflate(R.layout.fragment_refund_confirmation, container, false)
    }

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViewModel()
    }

    private fun initializeViewModel() {
        ViewModelProviders.of(requireActivity(), viewModelFactory).get(IssueRefundViewModel::class.java).also {
            initializeViews(it)
            setupObservers(it)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupObservers(viewModel: IssueRefundViewModel) {
        viewModel.showSnackbarMessage.observe(this, Observer {
            uiMessageResolver.showSnack(it)
        })

        viewModel.formattedRefundAmount.observe(this, Observer {
            refundConfirmation_refundAmount.text = it
        })

        viewModel.isRefundButtonEnabled.observe(this, Observer {
            refundConfirmation_refundAmount.isEnabled = it
        })

        viewModel.previousRefunds.observe(this, Observer {
            refundConfirmation_previouslyRefunded.text = it
        })

        viewModel.exitAfterRefund.observe(this, Observer {
            val bundle = Bundle()
            bundle.putBoolean(REFUND_SUCCESS_KEY, it)

            requireActivity().navigateBackWithResult(bundle)
        })
    }

    private fun initializeViews(viewModel: IssueRefundViewModel) {
        refundConfirmation_btnRefund.setOnClickListener {
            viewModel.onRefundConfirmed(refundConfirmation_reason.text.toString())
        }
    }
}
