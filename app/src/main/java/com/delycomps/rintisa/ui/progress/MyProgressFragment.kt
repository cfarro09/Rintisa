package com.delycomps.rintisa.ui.progress

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.rintisa.R
import com.delycomps.rintisa.adapter.AdapterVisit2
import com.delycomps.rintisa.cache.SharedPrefsCache
import com.delycomps.rintisa.databinding.FragmentMyprogressBinding

class MyProgressFragment : Fragment() {

    private var _binding: FragmentMyprogressBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel = ViewModelProvider(this).get(ProgressViewModel::class.java)

        _binding = FragmentMyprogressBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val rv: RecyclerView = binding.myprogressRv
        val myProgressTotal: TextView = binding.myprogressTotal
        val myProgressVisited: TextView = binding.myprogressVisited
        val myProgressInitiates: TextView = binding.myprogressInitiates
        rv.layoutManager = LinearLayoutManager(rv.context)

        val builderLoading: AlertDialog.Builder = AlertDialog.Builder(rv.context)
        builderLoading.setCancelable(false) // if you want user to wait for some process to finish,
        builderLoading.setView(R.layout.layout_loading_dialog)
        val dialogLoading: AlertDialog = builderLoading.create()

        viewModel.getVisit2(SharedPrefsCache(rv.context).getToken())

        viewModel.listVisit2.observe(viewLifecycleOwner) {
            myProgressTotal.text = "${it.count()}"
            myProgressVisited.text = "${it.filter { x -> x.status == "VISITADO" }.toList().count()}"
            myProgressInitiates.text = "${it.filter { x -> x.status != "VISITADO" }.toList().count()}"
            rv.adapter = AdapterVisit2(it)
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            if (it.loading) {
                dialogLoading.show()
            } else if (!it.loading) {
                dialogLoading.dismiss()
            }
            if (!it.loading && !it.success && (it.result) != "") {
                Toast.makeText(rv.context, it.result, Toast.LENGTH_SHORT).show()
            }
        }
//        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}