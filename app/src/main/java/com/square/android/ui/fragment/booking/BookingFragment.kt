package com.square.android.ui.fragment.booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.square.android.R
import com.square.android.data.pojo.Place
import com.square.android.extensions.toOrdinalString
import com.square.android.presentation.presenter.booking.BookingPresenter
import com.square.android.presentation.view.booking.BookingView
import com.square.android.ui.dialogs.DatePickDialog
import com.square.android.ui.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_booking.*
import kotlinx.android.synthetic.main.item_interval.view.*
import java.util.*

class BookingFragment : BaseFragment(), BookingView {
    @InjectPresenter
    lateinit var presenter: BookingPresenter

    override fun showDate(calendar: Calendar) {
        val title = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())

        val day = calendar.get(Calendar.DAY_OF_MONTH).toOrdinalString()
        val month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
        val subtitle = getString(R.string.book_subtitle, month, day)

        bookingDay.text = title
        bookingDate.text = subtitle
    }

    override fun showProgress() {
        bookControls.visibility = View.GONE
        bookProgress.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        bookControls.visibility = View.VISIBLE
        bookProgress.visibility = View.GONE
    }

    override fun showIntervals(data: List<Place.Interval>) {
        bookingIntervalList.removeAllViews()

        val inflater = layoutInflater

        data.forEachIndexed { index, interval ->
            val view = inflater.inflate(R.layout.item_interval, bookingIntervalList, false)

            view.bookingInterval.text = getString(R.string.time_range, interval.start, interval.end)
            view.bookingSpots.text = getString(R.string.spot_format, interval.spots)

            updateInterval(view, false)

            view.setOnClickListener { itemClicked(index) }

            bookingIntervalList.addView(view)
        }
    }

    override fun setSelectedItem(previousPosition: Int?, currentPosition: Int) {
        updateList(previousPosition, currentPosition)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_booking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bookingChooseDate.setOnClickListener { showDatePicker() }
        bookingDay.setOnClickListener { showDatePicker() }
        bookingDate.setOnClickListener { showDatePicker() }

        bookingBook.setOnClickListener { presenter.bookClicked() }
    }

    private fun showDatePicker() {
        DatePickDialog(context!!, DatePickDialog.minDateToday())
                .show { calendar: Calendar ->
                    presenter.dateSelected(calendar)
                }
    }

    private fun updateList(previousPosition: Int?, currentPosition: Int) {
        previousPosition?.let {
            val previous = bookingIntervalList.getChildAt(it)
            updateInterval(previous, false)
        }

        val current = bookingIntervalList.getChildAt(currentPosition)
        updateInterval(current, true)
    }

    private fun itemClicked(position: Int) {
        presenter.itemClicked(position)
    }

    private fun updateInterval(view: View, isActive: Boolean) {
        view.bookingInterval.isActivated = isActive
        view.bookingSpots.isActivated = isActive
        view.bookingRadio.isChecked = isActive
        view.bookingContainer.isActivated = isActive

        view.bookingSpots.isChecked = isActive
        view.bookingInterval.isChecked = isActive
    }
}
