package com.delycomps.myapplication.cache

import android.content.Context
import android.content.SharedPreferences

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
}