package com.technocom.digitalslate.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import com.technocom.digitalslate.BuildConfig
import com.technocom.digitalslate.R
import com.technocom.digitalslate.util.IabHelper
import com.technocom.digitalslate.utils.Preferences
import kotlinx.android.synthetic.main.activity_about_us.*

class AboutUs : BaseActivity() {

    var mIsPremium = false
    private lateinit var mHelper: IabHelper
    private val TAG = "InAppPurchase"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)


        mClose.setOnClickListener { finish() }
        version.text = "v.${BuildConfig.VERSION_NAME}"
        mBuyVersion.setOnClickListener { buyPro() }

        rateApp.setOnRatingBarChangeListener { _, _, _ -> ratingListener() }

        mShare.setOnClickListener {
            val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            val shareBody = "https://play.google.com/store/apps/details?id=$packageName"
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(sharingIntent, "Share using"))
        }
        val base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAu+oqx2e1/PuZj+m44V3VESbhyMuuu1SV88K1aVshQBqjHbZexTtIeyzWnerztUp6fFMzDL4aYeuKo4xCZMqGee+eWjLkGf31vET+ZOPvWbgxGCj5/xK1/GJLZ7H8B6utPVqgdWusK30rOVTCjA6Iw9GevbFWwHgrPzs+N42PzqAOMkL0Y/DPVZBJBUKamgYGRLrLAYiKVJ73d6ONTQToIqqFqg/TmO0T7ixsO6jrKbTzI7/lZqtJbylkHQqrLnQy4m1QWGlJi1yPYtD+wSFKCC03tonAtN9CWTXEfymdU1JT7QizFK07+ShwPYIEGmrTQu1jLlFfTPSqIBNF49sR+wIDAQAB"
        mHelper = IabHelper(this, base64EncodedPublicKey)
        mHelper.startSetup { result ->
            if (!result.isSuccess) {
                Log.d(TAG, "In-app Billing setup failed: $result")
            } else {
                Log.d(TAG, "In-app Billing is set up OK")
                mHelper.queryInventoryAsync(mGotInventoryListener)
            }
        }

    }

    private fun ratingListener() {
        var uri: Uri
        try {
            uri = Uri.parse("market://details?id=${packageName}")
            val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
            startActivity(myAppLinkToMarket)
        } catch (e: ActivityNotFoundException) {
            uri = Uri.parse("https://play.google.com/store/apps/details?id=${packageName}")
            val goMarket = Intent(Intent.ACTION_VIEW, uri)
            startActivity(goMarket)
        }

    }

    private fun buyPro() {
        if (pref.isPremium)
            showDialog()
        else {
            try {
                mHelper.launchPurchaseFlow(this, packageName, 10001,
                        mPurchaseFinishedListener, "mypurchasetoken")
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    private var mPurchaseFinishedListener: IabHelper.OnIabPurchaseFinishedListener = IabHelper.OnIabPurchaseFinishedListener { result, purchase ->
        if (result.isFailure) {
            Log.d(TAG, "mPurchaseFinishedListenerisFailure")
        } else {
            Log.d(TAG, "mPurchaseFinishedListenerisFailureElse")
        }
    }
    private val mGotInventoryListener = IabHelper.QueryInventoryFinishedListener { result, inventory ->
        if (result.isFailure) {
            Log.d(TAG, "isFailure")
        } else {
            mIsPremium = inventory.hasPurchase(packageName)
            pref.isPremium = (mIsPremium)
        }
    }

    //Dialog for buy pro version
    private fun showDialog() {
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("Alert")
        alertDialog.setMessage("You are already using 'Pro Version'.")
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK")
        { dialog, _ ->
            dialog.dismiss()
        }
        alertDialog.show()
    }

}
