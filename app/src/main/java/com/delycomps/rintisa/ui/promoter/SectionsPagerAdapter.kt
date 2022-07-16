package com.delycomps.rintisa.ui.promoter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.delycomps.rintisa.R

private val TAB_TITLES = arrayOf(
    R.string.tab_text_1,
    R.string.tab_text_stock,
    R.string.tab_text_sales,
)

class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return when (position) {
            0 -> InfoPromoterFragment()
            1 -> StockFragment()
            2 -> SalesFragment()
            else -> InfoPromoterFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return 3
    }
}