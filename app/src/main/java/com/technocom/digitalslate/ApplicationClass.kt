package com.technocom.digitalslate

import android.app.Application
import com.google.android.gms.ads.MobileAds

/*
Created by Pawan kumar
 */


class ApplicationClass : Application() {

    override fun onCreate() {
        MobileAds.initialize(this, "ca-app-pub-9110061456871098~3901330535")
        super.onCreate()
    }

}