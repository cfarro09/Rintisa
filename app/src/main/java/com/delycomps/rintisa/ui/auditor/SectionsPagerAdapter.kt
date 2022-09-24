package com.delycomps.rintisa.ui.auditor

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.delycomps.rintisa.AuditorClientFragment
import com.delycomps.rintisa.AuditorUserFragment

import com.delycomps.rintisa.R
import com.delycomps.rintisa.ui.supervisor.InformationMerchant

private val TAB_TITLES = arrayOf(
    R.string.tab_bins,
)

class SectionsPagerAdapter(private val context: Context, fm: FragmentManager, private val type: String) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return when (position) {
            0 -> if (type == "AUDITOR") BinsFragment() else if (type == "CLIENT") AuditorClientFragment() else AuditorUserFragment()
            else -> InformationMerchant()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return if (type == "AUDITOR") "TACHOS" else if (type == "CLIENT") "PUNTO DE VENTA" else "USUARIO"
    }

    override fun getCount(): Int {
        return 1
    }
}