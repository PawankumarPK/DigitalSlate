package com.technocom.digitalslate.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.technocom.digitalslate.utils.Preferences

/*
Created by Pawan kumar
 */


abstract class BaseActivity : AppCompatActivity() {
    lateinit var pref: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pref = Preferences().getInstance(this)
    }

    fun toastLong(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
