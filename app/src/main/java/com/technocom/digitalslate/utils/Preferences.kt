package com.technocom.digitalslate.utils

import android.content.Context
import android.content.SharedPreferences
import com.technocom.digitalslate.R

/**
 * Created by Pawan on 3/5/2018.
 */

class Preferences {

    private lateinit var sharedPreferences: SharedPreferences
    private var instance: Preferences? = null


    var isPremium: Boolean
        get() = sharedPreferences.getBoolean(ISPREMIUM, false)
        set(value) = sharedPreferences.edit().putBoolean(ISPREMIUM, value).apply()


    private fun initialize(context: Context): Preferences {
        this.sharedPreferences = context.getSharedPreferences(context.resources.getString(R.string.app_name), Context.MODE_PRIVATE)
        return this
    }

    fun getInstance(context: Context): Preferences {
        if (instance == null)
            instance = initialize(context)
        return instance!!
    }

    companion object {
        private const val ISPREMIUM = "ISPREMIUM"

    }


}
