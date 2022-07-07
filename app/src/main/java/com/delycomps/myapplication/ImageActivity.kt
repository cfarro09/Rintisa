package com.delycomps.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.delycomps.myapplication.adapter.AdapterCustomer
import com.delycomps.myapplication.adapter.AdapterPointsale
import com.delycomps.myapplication.adapter.AdapterQuestions
import com.delycomps.myapplication.cache.SharedPrefsCache
import com.delycomps.myapplication.model.Customer
import com.delycomps.myapplication.model.DataAuditor
import com.delycomps.myapplication.model.DataSupervisor
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.gson.Gson
import com.ortiz.touchview.TouchImageView
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        val imageview = findViewById<TouchImageView>(R.id.activity_image_overlay)

        val urlImage: String? = intent?.getStringExtra(Constants.URL_IMAGE)

        if ((urlImage ?: "") != "") {
            Glide.with(this)
                .asBitmap()
                .load(urlImage)
                .into(object : CustomTarget<Bitmap>(){
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        imageview.setImageBitmap(resource)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {

                    }
                })
        }
    }
}