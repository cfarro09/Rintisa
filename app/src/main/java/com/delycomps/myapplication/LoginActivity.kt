package com.delycomps.myapplication

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.delycomps.myapplication.api.Repository
import com.delycomps.myapplication.cache.SharedPrefsCache
import com.delycomps.myapplication.model.DataMerchant
import com.delycomps.myapplication.model.DataPromoter
import com.delycomps.myapplication.model.DataSupervisor
import com.delycomps.myapplication.ui.merchant.MerchantViewModel
import com.delycomps.myapplication.ui.promoter.PromoterViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson

class LoginActivity : AppCompatActivity() {

    private lateinit var merchantViewModel: MerchantViewModel
    private lateinit var promoterViewModel: PromoterViewModel
    private lateinit var supervisorViewModel: SupervisorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        merchantViewModel = ViewModelProvider(this).get(MerchantViewModel::class.java)
        promoterViewModel = ViewModelProvider(this).get(PromoterViewModel::class.java)
        supervisorViewModel = ViewModelProvider(this).get(SupervisorViewModel::class.java)

        val builderLoading: AlertDialog.Builder = AlertDialog.Builder(this)
        builderLoading.setCancelable(false) // if you want user to wait for some process to finish,
        builderLoading.setView(R.layout.layout_loading_dialog)
        val dialogLoading: AlertDialog = builderLoading.create()

        merchantViewModel.dataProducts.observe(this) {
            val data = DataMerchant(merchantViewModel.dataBrands.value!!, merchantViewModel.dataMaterials.value!!, merchantViewModel.dataProducts.value!!)
            SharedPrefsCache(this).set("data-merchant", Gson().toJson(data), "string")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        promoterViewModel.dataStocks.observe(this) {
            val data = DataPromoter(promoterViewModel.dataMerchandise.value!!, promoterViewModel.dataBrandSale.value!!, promoterViewModel.dataStocks.value!!, emptyList(), emptyList())
            SharedPrefsCache(this).set("data-promoter", Gson().toJson(data), "string")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        supervisorViewModel.dataMarket.observe(this) {
            val data = DataSupervisor(it, supervisorViewModel.dataQuestion.value ?: emptyList())
            SharedPrefsCache(this).set("data-supervisor", Gson().toJson(data), "string")
            startActivity(Intent(this, SupervisorActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.login_access).setOnClickListener {
            val username = findViewById<EditText>(R.id.login_username).text.toString()
            val password = findViewById<EditText>(R.id.login_password).text.toString()

            if (username != "" && password != "") {
                dialogLoading.show()

                Repository().login(username, password) { isSuccess, result, message ->
                    dialogLoading.dismiss()
                    if (isSuccess) {
                        SharedPrefsCache(this).set("type", result?.role?.uppercase() ?: "", "string")
                        SharedPrefsCache(this).set("token", result?.token, "string")
                        SharedPrefsCache(this).set("fullname", result?.fullname, "string")
                        Toast.makeText(this, "Bienvenido $username (${result?.role?.uppercase() ?: ""})", Toast.LENGTH_SHORT).show()

                        when {
                            result?.role?.uppercase() == "MERCADERISTA" -> {
                                merchantViewModel.getMainMulti(SharedPrefsCache(this).getToken())
                            }
                            result?.role?.uppercase() == "IMPULSADOR" -> {
                                promoterViewModel.getMainMultiInitial(SharedPrefsCache(this).getToken())
                            }
                            else -> {
                                supervisorViewModel.getMainMultiInitial(SharedPrefsCache(this).getToken())
                            }
                        }
                    } else {
                        Snackbar.make(findViewById<EditText>(R.id.login_username), message ?: "Usuario incorrecto", Snackbar.LENGTH_LONG).setBackgroundTint(resources.getColor(
                            R.color.colorSecondary
                        )).show()
                    }
                }
            }
        }
    }
}