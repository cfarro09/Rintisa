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
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }, SPLASH_TIMEOUT)

    }
}