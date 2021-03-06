package com.delycomps.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.delycomps.myapplication.cache.SharedPrefsCache
import android.os.Handler

class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIMEOUT:Long = 500 // 1 sec

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val token: String = SharedPrefsCache(this).getToken()

        Handler().postDelayed({
            if (token != "") {
                val role = SharedPrefsCache(this).get("type", "string")
                startActivity(Intent(this, if (role == "SUPERVISOR" || role == "SUPERVISOR RINTI") SupervisorActivity::class.java else if (role == "COMERCIAL RINTI") AuditorActivity::class.java else MainActivity::class.java))
                finish()
            }
            else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }, SPLASH_TIMEOUT)

    }
}