package com.delycomps.rintisa

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.ortiz.touchview.TouchImageView
import java.io.*
import java.util.*


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