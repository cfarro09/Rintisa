package com.delycomps.rintisa.ui.merchant

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import com.delycomps.rintisa.R

private val TAB_TITLES = arrayOf(
    R.string.tab_text_1,
    R.string.tab_text_2,
    R.string.tab_text_3,
    R.string.tab_text_availability
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, private val fm: FragmentManager, private val showSurvey: Boolean, private val showAvailability: Boolean) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return when (position) {
            0 -> InformationFragment()
            1 -> MaterialsFragment()
            2 -> if (showAvailability && showSurvey) PriceFragment() else if (showAvailability) AvailabilityFragment() else PriceFragment()
            3 -> AvailabilityFragment()
            else -> InformationFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(arrayOf(
            R.string.tab_text_1,
            R.string.tab_text_2,
            if (showAvailability && showSurvey) R.string.tab_text_3 else if (showAvailability) R.string.tab_text_availability else R.string.tab_text_3,
            R.string.tab_text_availability
        )[position])
    }

    override fun getCount(): Int {
        return 2 + (if (showSurvey) 1 else 0) + (if (showAvailability) 1 else 0)
    }
}