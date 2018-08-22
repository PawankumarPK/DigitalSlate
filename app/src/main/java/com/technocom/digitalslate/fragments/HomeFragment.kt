package com.technocom.digitalslate.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.technocom.digitalslate.R
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_home.*

/*
Created by Pawan kumar
 */

class HomeFragment : BaseFragment() {
    private lateinit var animation: Animation
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        animation = AnimationUtils.loadAnimation(homeActivity, R.anim.anim)
        mPractiseDrawing.setOnClickListener { blackBoard(FragmentDrawing(), it) }
        mPractiseAlphabets.setOnClickListener { blackBoard(FragmentAlphabets(), it) }
        mPractiseNumber.setOnClickListener { blackBoard(FragmentNumber(), it) }
    }


    override fun onStart() {
        super.onStart()
        homeActivity.mToolbar.visibility = VISIBLE
    }
    private fun blackBoard(fragment: Fragment, view: View) {
        view.startAnimation(animation)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                fragmentManager!!.beginTransaction().replace(R.id.fragmentContainer, fragment).addToBackStack(null).commit()
            }

            override fun onAnimationStart(animation: Animation?) {}
        })
    }

    override fun onStop() {
        super.onStop()
        homeActivity.mToolbar.visibility = GONE
    }
}