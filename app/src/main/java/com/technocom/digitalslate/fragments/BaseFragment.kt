package com.technocom.digitalslate.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.widget.Toast
import com.technocom.digitalslate.activities.HomeActivity
import com.technocom.digitalslate.utils.Preferences

/*
Created by Pawan kumar
 */


abstract class BaseFragment : Fragment() {
    lateinit var pref: Preferences
    lateinit var homeActivity: HomeActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeActivity = activity as HomeActivity
        pref = Preferences().getInstance(homeActivity)
    }

    fun toastLong(message: String) {
        Toast.makeText(homeActivity, message, Toast.LENGTH_LONG).show()
    }
}