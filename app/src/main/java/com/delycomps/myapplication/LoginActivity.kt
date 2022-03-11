package com.delycomps.myapplication

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.delycomps.myapplication.api.Repository
import com.delycomps.myapplication.cache.SharedPrefsCache
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        val builderLoading: AlertDialog.Builder = AlertDialog.Builder(this)
        builderLoading.setCancelable(false) // if you want user to wait for some process to finish,
        builderLoading.setView(R.layout.layout_loading_dialog)
        val dialogLoading: AlertDialog = builderLoading.create()

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
                        Toast.makeText(this, "Bienvenido $username (${result?.role?.uppercase() ?: ""})", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
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