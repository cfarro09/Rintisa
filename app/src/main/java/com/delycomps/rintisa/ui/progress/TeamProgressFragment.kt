package com.delycomps.rintisa.ui.progress

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delycomps.rintisa.R
import com.delycomps.rintisa.adapter.AdapterPointsale
import com.delycomps.rintisa.adapter.AdapterUserSup
import com.delycomps.rintisa.adapter.AdapterVisit2
import com.delycomps.rintisa.cache.SharedPrefsCache
import com.delycomps.rintisa.databinding.FragmentTeamprogressBinding
import com.delycomps.rintisa.model.PointSale
import com.delycomps.rintisa.model.UserSup
import com.google.gson.Gson
import kotlin.math.roundToInt

class TeamProgressFragment : Fragment() {

    private var _binding: FragmentTeamprogressBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel = ViewModelProvider(this).get(ProgressViewModel::class.java)

        _binding = FragmentTeamprogressBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val rv: RecyclerView = binding.teamprogressRv
        rv.layoutManager = LinearLayoutManager(rv.context)

        val efectivity: TextView = binding.teamprogressEfectivity
        val totalT: TextView = binding.teamprogressTotal
        val visited: TextView = binding.teamprogressVisited
        val waiting: TextView = binding.teamprogressWaiting
        val initiates: TextView = binding.teamprogressInitiates

        val builderLoading: AlertDialog.Builder = AlertDialog.Builder(rv.context)
        builderLoading.setCancelable(false) // if you want user to wait for some process to finish,
        builderLoading.setView(R.layout.layout_loading_dialog)
        val dialogLoading: AlertDialog = builderLoading.create()

        val builderResumeUser: AlertDialog.Builder = AlertDialog.Builder(rv.context)
        val inflater1 = this.layoutInflater
        val dialogResumeView = inflater1.inflate(R.layout.layout_resume_user, null)
        builderResumeUser.setView(dialogResumeView)
        val dialogResume = builderResumeUser.create()
        val rvResumeVisit: RecyclerView = dialogResumeView.findViewById(R.id.dialog_team_rv_visit)
        rvResumeVisit.layoutManager = LinearLayoutManager(rv.context)
        val dialogEfectivity: TextView = dialogResumeView.findViewById(R.id.dialog_team_efectivity)
        val dialogVisitedSuccess: TextView = dialogResumeView.findViewById(R.id.dialog_team_visited_success)
        val dialogVisitedFail: TextView = dialogResumeView.findViewById(R.id.dialog_team_visited_fail)
        val dialogVisitInitiated: TextView = dialogResumeView.findViewById(R.id.dialog_team_initiates)
        val dialogTotalVisit: TextView = dialogResumeView.findViewById(R.id.dialog_team_visited)
        val dialogTotalWaiting: TextView = dialogResumeView.findViewById(R.id.dialog_team_waiting)
        val dialogUser: TextView = dialogResumeView.findViewById(R.id.dialog_team_user)

        viewModel.getUserSup(SharedPrefsCache(rv.context).getToken())

        viewModel.listUserSup.observe(viewLifecycleOwner) {
            val finishVisit = it.fold(0) { acc, item -> item.finishVisit + acc }
            val finishSuccessVisit = it.fold(0) { acc, item -> item.finishSuccessVisit + acc }
            val finishFailVisit = finishVisit - finishSuccessVisit
            val initiatedVisit = it.fold(0) { acc, item -> item.initiatedVisit + acc }
            val withoutVisit = it.fold(0) { acc, item -> item.withoutVisit + acc }
            val totalValue = finishVisit + initiatedVisit + withoutVisit
            val efect = if  ((totalValue)  > 0) (finishVisit * 100) / totalValue else 0.0

            visited.text = finishVisit.toString()
            initiates.text = initiatedVisit.toString()
            totalT.text = totalValue.toString()
            waiting.text = withoutVisit.toString()
            efectivity.text = "${efect}%"
            Log.d("aaaa", Gson().toJson(it))
            rv.adapter = AdapterUserSup(it, object : AdapterUserSup.ListAdapterListener {

                override fun clickItem(user: UserSup) {
                    val efectivity = ((user.finishVisit.toDouble() / (user.finishVisit.toDouble() + user.initiatedVisit.toDouble() + user.withoutVisit.toDouble())) * 100)

                    dialogEfectivity.text = "${efectivity.roundToInt()}%"
                    dialogVisitedSuccess.text = user.finishSuccessVisit.toString()
                    dialogVisitedFail.text = (user.finishVisit - user.finishSuccessVisit).toString()
                    dialogTotalVisit.text = (user.finishVisit).toString()
                    dialogVisitInitiated.text = user.initiatedVisit.toString()
                    dialogTotalWaiting.text = user.withoutVisit.toString()
                    dialogUser.text = user.User

                    rvResumeVisit.adapter = AdapterPointsale(listOf(), object : AdapterPointsale.ListAdapterListener {
                        override fun onClickAtDetailPointSale(pointSale1: PointSale, position: Int) {
                        }
                    })

                    dialogResume.show()
                    viewModel.getPointSale(user.userId, SharedPrefsCache(rv.context).getToken())
                }
            })
        }
        viewModel.listPointSale.observe(viewLifecycleOwner) {
            rvResumeVisit.adapter = AdapterPointsale(it.map { x ->
                x.wasSaveOnBD = true
                x
            }, object : AdapterPointsale.ListAdapterListener {
                override fun onClickAtDetailPointSale(pointSale1: PointSale, position: Int) {

                }
            })
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