package com.technocom.digitalslate.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.technocom.digitalslate.R
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreen : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
    }

    override fun onStart() {
        super.onStart()
        val path = "android.resource://" + packageName + "/" + R.raw.splesh
        splash_video.setVideoURI(Uri.parse(path))
        splash_video.setOnPreparedListener { mp -> splash_video.start();mp.setVolume(0f, 0f) }
        splash_video.setOnCompletionListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    override fun onStop() {
        splash_video.stopPlayback()
        super.onStop()
    }
}
