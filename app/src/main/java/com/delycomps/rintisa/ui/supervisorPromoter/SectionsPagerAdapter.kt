package com.delycomps.rintisa.ui.supervisorPromoter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import com.delycomps.rintisa.R
import com.delycomps.rintisa.ui.supervisor.InformationMerchant

private val TAB_TITLES = arrayOf(
    R.string.tab_text_1,
    R.string.review,
    R.string.status_pdv
)

class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return when (position) {
            0 -> InformationMerchant()
            1 -> ReviewFragment()
            2 -> StatusFragment()
            else -> InformationMerchant()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return 3
    }
}