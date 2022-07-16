package com.delycomps.rintisa.cache

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap




class SharedPrefsCache(context: Context) {

    private val pref: SharedPreferences = context.getSharedPreferences("MyPref", 0)

    fun getToken(): String{
        return pref.getString("token", "")!!
    }

    fun removeToken() {
        val editor: SharedPreferences.Editor = pref.edit()
        editor.putString("token", "")
        editor.apply() // commit changes
    }

    fun get(key: String, type: String): Any? {
        return when (type) {
            "int" -> pref.getInt(key, 0)
            "bool" -> pref.getBoolean(key, false)
            "float" -> pref.getFloat(key, 0.toFloat())
            "string" -> pref.getString(key, "")
            else -> {
                null
            }
        }
    }

    fun set(key: String, value: Any?, type: String) {
        val editor: SharedPreferences.Editor = pref.edit()
        when (type) {
            "int" -> editor.putInt(key, if (value != null) value as Int else 0)
            "bool" -> editor.putBoolean(key, if (value != null) value as Boolean else false)
            "float" -> editor.putFloat(key, if (value != null) value as Float else 0.toFloat())
            "string" -> editor.putString(key, if (value != null) value as String else "")
        }
        editor.apply() // commit changes
    }

    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap? {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }


}