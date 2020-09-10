package com.woocommerce.android.ui.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.woocommerce.android.R
import com.woocommerce.android.analytics.AnalyticsTracker
import com.woocommerce.android.ui.login.UnifiedLoginTracker.Click
import com.woocommerce.android.ui.login.UnifiedLoginTracker.Flow
import com.woocommerce.android.ui.login.UnifiedLoginTracker.Step
import com.woocommerce.android.ui.login.UnifiedLoginTracker.Step.PROLOGUE
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_login_prologue.*
import javax.inject.Inject

class LoginPrologueFragment : androidx.fragment.app.Fragment() {
    companion object {
        const val TAG = "login-prologue-fragment"

        fun newInstance(): LoginPrologueFragment {
            return LoginPrologueFragment()
        }
    }

    interface PrologueFinishedListener {
        fun onPrimaryButtonClicked()
        fun onSecondaryButtonClicked()
    }

    @Inject lateinit var unifiedLoginTracker: UnifiedLoginTracker
    private var prologueFinishedListener: PrologueFinishedListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login_prologue, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            unifiedLoginTracker.track(Flow.PROLOGUE, PROLOGUE)
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)

        if (activity is PrologueFinishedListener) {
            prologueFinishedListener = activity as PrologueFinishedListener
        }
    }

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
        unifiedLoginTracker.setFlowAndStep(Flow.PROLOGUE, Step.PROLOGUE)
    }

    override fun onDetach() {
        super.onDetach()
        prologueFinishedListener = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        button_login_store.setOnClickListener {
            // Login with site address
            unifiedLoginTracker.trackClick(Click.LOGIN_WITH_SITE_ADDRESS)
            prologueFinishedListener?.onPrimaryButtonClicked()
        }

        button_login_wpcom.setOnClickListener {
            // Login with WordPress.com account
            unifiedLoginTracker.trackClick(Click.CONTINUE_WITH_WORDPRESS_COM)
            prologueFinishedListener?.onSecondaryButtonClicked()
        }
    }
}
