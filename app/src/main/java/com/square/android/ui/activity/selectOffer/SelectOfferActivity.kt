package com.square.android.ui.activity.selectOffer


import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.square.android.R
import com.square.android.SCREENS
import com.square.android.androidx.navigator.AppNavigator
import com.square.android.data.pojo.OfferInfo
import com.square.android.data.pojo.PlaceInfo
import com.square.android.data.pojo.UserInfo
import com.square.android.presentation.presenter.selectOffer.SelectOfferPresenter
import com.square.android.presentation.view.selectOffer.SelectOfferView
import com.square.android.ui.activity.BaseActivity
import com.square.android.ui.activity.review.EXTRA_OFFER_ID
import com.square.android.ui.activity.review.EXTRA_REDEMPTION_ID
import com.square.android.ui.activity.review.ReviewActivity
import com.square.android.ui.activity.review.ReviewExtras
import com.square.android.ui.fragment.offer.OfferAdapter
import kotlinx.android.synthetic.main.activity_select_offer.*
import org.jetbrains.anko.intentFor
import ru.terrakok.cicerone.Navigator

const val OFFER_EXTRA_ID = "CLAIMED_OFFER_EXTRA_ID"

class SelectOfferActivity : BaseActivity(), SelectOfferView, OfferAdapter.Handler {
    private var adapter: OfferAdapter? = null

    private var dialog: OfferDialog? = null

    @InjectPresenter
    lateinit var presenter: SelectOfferPresenter

    @ProvidePresenter
    fun providePresenter() = SelectOfferPresenter(getRedemptionId())

    override fun provideNavigator(): Navigator = SelectOfferNavigator(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_offer)

        selectOfferList.setHasFixedSize(true)

        selectOfferBack.setOnClickListener { presenter.backClicked() }

        selectOfferSubmit.setOnClickListener { presenter.submitClicked() }
    }

    override fun showOfferDialog(offer: OfferInfo, userInfo: UserInfo, place: PlaceInfo) {
        dialog = OfferDialog(this)

        dialog!!.show(offer, place, userInfo) {
            presenter.dialogSubmitClicked(offer.id)
        }
    }

    override fun showData(data: List<OfferInfo>) {
        adapter = OfferAdapter(data, this)

        selectOfferList.adapter = adapter
    }

    override fun setSelectedItem(position: Int) {
        adapter?.setSelectedItem(position)

        selectOfferSubmit.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        selectOfferProgress.visibility = View.INVISIBLE
        selectOfferList.visibility = View.VISIBLE
    }

    override fun itemClicked(position: Int) {
        presenter.itemClicked(position)
    }

    override fun showProgress() {
        selectOfferProgress.visibility = View.VISIBLE
        selectOfferList.visibility = View.INVISIBLE
    }

    private class SelectOfferNavigator(activity: FragmentActivity) : AppNavigator(activity, R.id.selectOfferContainer) {
        override fun createActivityIntent(context: Context, screenKey: String, data: Any?) =
                when (screenKey) {
                    SCREENS.REVIEW -> {
                        val extras = data as ReviewExtras
                        context.intentFor<ReviewActivity>(EXTRA_OFFER_ID to extras.offerId,
                                EXTRA_REDEMPTION_ID to extras.redemptionId)
                    }
                    else -> null
                }

        override fun createFragment(screenKey: String, data: Any?): Fragment? = null

    }

    private fun getRedemptionId() = intent.getLongExtra(OFFER_EXTRA_ID, 0)
}
