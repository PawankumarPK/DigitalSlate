package com.technocom.digitalslate.fragments

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.annotation.RequiresApi
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.technocom.digitalslate.R
import com.technocom.digitalslate.activities.HomeActivity
import com.technocom.digitalslate.adapter.RecyclerViewAdapter
import com.technocom.digitalslate.utils.PaintView
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.dialog_alphabets.*
import kotlinx.android.synthetic.main.dialog_slate_board.*
import kotlinx.android.synthetic.main.fragment_alphabets.*
import kotlinx.android.synthetic.main.savefile_dialog.*
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class FragmentAlphabets : BaseFragment() {

    private val displayRectangle = Rect()
    private var width = 0
    private lateinit var dialog: Dialog
    private lateinit var path: File
    private var color = Color.BLACK
    private var position = 0
    private var pencilColor = Color.WHITE
    private var alphabets: ArrayList<String> = ArrayList()
    private lateinit var adapter: RecyclerViewAdapter
    private lateinit var mInterstitialAd: InterstitialAd
    private val PERMISSIONSMULTIPLEREQUEST = 123
    private val alphaDialogBackground = Color.LTGRAY
    private var alphadialog = 3
    private lateinit var metrics: DisplayMetrics

    companion object {
        lateinit var alpha: SmallAlphabets
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_alphabets, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            checkPermission()

        homeActivity.requestedOrientation = HomeActivity.screenMode
        homeActivity.mToolbar.visibility = View.GONE

        dialog = Dialog(homeActivity)
        if (resources.displayMetrics.widthPixels > resources.displayMetrics.heightPixels)
            paintOptions.bringToFront()

        mRecyclerView.layoutManager = LinearLayoutManager(homeActivity, LinearLayoutManager.HORIZONTAL, false)
        adapter = RecyclerViewAdapter(homeActivity, alphabets)
        mRecyclerView.adapter = adapter

        path = File(Environment.getExternalStorageDirectory().path, "/" + resources.getString(R.string.app_name))
        metrics = DisplayMetrics()
        homeActivity.windowManager.defaultDisplay.getMetrics(metrics)
        homeActivity.window.decorView.getWindowVisibleDisplayFrame(displayRectangle)
        width = (displayRectangle.width() * 0.9f).toInt()

        alphabetsList();dialogClick(true)

        paintView.init(metrics)
        pencil.setOnClickListener { changeBackground();pencil.setBackgroundColor(Color.GRAY); paintView.changeColorAndText(pencilColor, 25) }
        eraser.setOnClickListener { paintView.clear() }
        save.setOnClickListener { saveBitmap();changeBackground();save.setBackgroundColor(Color.GRAY); paintView.changeColorAndText(Color.TRANSPARENT, 25) }
        practise.setOnClickListener { alphabetsDialog();changeBackground();practise.setBackgroundColor(Color.GRAY)}
        mColor.setOnClickListener { openDialog(false) }
        mSlateBoard.setOnClickListener { slateBoard() }
        mNext.setOnClickListener { nextButton() }
        mPrevious.setOnClickListener { previousButton() }
        mHome.setOnClickListener { homeButton() }
        mOrientation.setOnClickListener { orientation(0) }
        mOrientation2.setOnClickListener { orientation(1) }
        share.setOnClickListener { share();changeBackground();share.setBackgroundColor(Color.GRAY); paintView.changeColorAndText(Color.TRANSPARENT, 25) }

        //  mArrow.setOnCheckedChangeListener { _, isChecked -> hideBottomBar(isChecked) }

        if (!pref.isPremium)
            interstitialAd()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(homeActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)

            if (ActivityCompat.shouldShowRequestPermissionRationale(homeActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Snackbar.make(view!!, "Please Grant Permissions to access your phone media",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE"
                ) {
                    requestPermissions(
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONSMULTIPLEREQUEST)
                }.show()
            } else {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONSMULTIPLEREQUEST)
            }
    }

    private fun changeBackground() {
        pencil.setBackgroundColor(Color.TRANSPARENT)
        save.setBackgroundColor(Color.TRANSPARENT)
        mColor.setBackgroundColor(Color.TRANSPARENT)
        practise.setBackgroundColor(Color.TRANSPARENT)
        share.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun saveBitmap() {
        dialog.setContentView(R.layout.savefile_dialog)
        dialog.setTitle("Save")
        dialog.mView.setOnClickListener { validationYes();dialog.dismiss() }
        dialog.mCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()

    }

    private fun share() {
        dialog.setContentView(R.layout.dialog_share)
        dialog.setTitle("Share")
        dialog.mView.setOnClickListener { validationYes();onShareClick();dialog.dismiss() }
        dialog.mCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun orientation(orientation: Int) {
        HomeActivity.screenMode = orientation
        homeActivity.requestedOrientation = HomeActivity.screenMode
        position = 0
        mRecyclerView.scrollToPosition(position)
    }

    private fun validationYes() {

        if (!path.exists())
            path.mkdir()
        if (paintView.saveBitmap(path.path, "${System.currentTimeMillis()}") && paintView.file.exists())
            if (mInterstitialAd.isLoaded)
                mInterstitialAd.show()
        toastLong("Successful")
        // onShareClick()
        // openDialog(paintView.file)
    }


    private fun alphabetsDialog() {
        // val dialog = getDialog()
        dialog.setContentView(R.layout.dialog_alphabets)
        dialog.setTitle("Practise")
        dialog.mBigAlphabets.setOnClickListener { drawAlphabets(true);dialog.dismiss();dialogClick(true);alphadialog = 0 }
        dialog.mSmallAlphabets.setOnClickListener { drawAlphabets(false);dialog.dismiss();dialogClick(true);alphadialog = 1 }
        /*  dialog.mHindiVowel.setOnClickListener { hindiVowel();dialog.dismiss();dialogClick(false) }
          dialog.mHindiConstant.setOnClickListener { hindiConstant();dialog.dismiss();dialogClick(false) }*/
        dialog.show()
        alphabetBackground(alphadialog)
    }

    private fun alphabetBackground(dialogBack: Int) {
        when (dialogBack) {
            0 -> {
                dialog.mBigAlphabets.setBackgroundColor(alphaDialogBackground)
            }
            1 -> {
                dialog.mSmallAlphabets.setBackgroundColor(alphaDialogBackground)
            }
        }
    }

//    private fun openPaint(file: File) {
//        val target = Intent(Intent.ACTION_VIEW)
//        // target.setDataAndType(Uri.fromFile(file), "image/*")
//
//        val apkURI = FileProvider.getUriForFile(homeActivity, homeActivity.applicationContext.packageName + ".provider", file)
//        target.setDataAndType(apkURI, "image/*")
//        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//
//        val intent = Intent.createChooser(target, "Open File")
//        try {
//            startActivity(intent)
//        } catch (e: ActivityNotFoundException) {
//            toastLong("No Application Found to open paint")
//        }
//    }


    private fun openDialog(supportsAlpha: Boolean) {
        val dialog = AmbilWarnaDialog(homeActivity, color, supportsAlpha, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                paintView.changeColorAndText(color, PaintView.BRUSH_SIZE)
                this@FragmentAlphabets.color = color
            }

            override fun onCancel(dialog: AmbilWarnaDialog) {
                //  Toast.makeText(applicationContext, "cancel", Toast.LENGTH_SHORT).show()
            }
        })
        dialog.show()
    }

    private fun onShareClick() {
        val share = Intent(Intent.ACTION_SEND)
        share.type = "image/jpeg"
        val bytes = ByteArrayOutputStream()
        // icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val f = File(Environment.getExternalStorageDirectory().toString() + File.pathSeparator)
        try {
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val file = paintView.file
        if (file == null) {
            toastLong("Please,Save homeActivity image")
            return
        }
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse(file.path))
        startActivity(Intent.createChooser(share, "Share Image"))


    }

    private fun drawAlphabets(boolean: Boolean) {
        alphabetsList()
        alpha.alphabetsSmall(boolean)
        paintView.clear()
    }

    private fun nextButton() {
        position++
        mRecyclerView.scrollToPosition(position)
        showHide(position)
        mRecyclerView.adapter.notifyItemChanged(position)
        paintView.clear()
    }

    private fun previousButton() {
        position--
        mRecyclerView.scrollToPosition(position)
        showHide(position)
        mRecyclerView.adapter.notifyItemChanged(position)
        paintView.clear()

    }

    private fun showHide(pos: Int) {
        when (pos) {
            0 -> {
                mPrevious.visibility = View.INVISIBLE
                mPrevious.isClickable = true
                mNext.visibility = View.VISIBLE
                mNext.isClickable = true
            }
            25 -> {
                mNext.visibility = View.INVISIBLE
                mNext.isClickable = true
                mPrevious.visibility = View.VISIBLE
                mPrevious.isClickable = true
            }
            24 -> {
                mNext.visibility = View.VISIBLE
                mNext.isClickable = true
                mPrevious.visibility = View.VISIBLE
                mPrevious.isClickable = true

            }
        }
        if (pos > 0)
            mPrevious.visibility = View.VISIBLE

    }

    private fun homeButton() {
        fragmentManager!!.popBackStack()

    }

    private fun alphabetsList() {
        val list = resources.getStringArray(R.array.Alphabets)
        alphabets.clear()
        for (i in 0..list.lastIndex) {
            alphabets.add(list[i])
        }

    }

    interface SmallAlphabets {
        fun alphabetsSmall(bool: Boolean)
    }

    private fun dialogClick(boolean: Boolean) {
        position = 0
        if (boolean)
            RecyclerViewAdapter.myTypeface = Typeface.createFromAsset(homeActivity.assets, "DOTLRG__.TTF")
        else
            RecyclerViewAdapter.myTypeface = Typeface.createFromAsset(homeActivity.assets, "Dotted.ttf")
        adapter.notifyDataSetChanged()
        mPrevious.visibility = View.INVISIBLE
        mNext.visibility = View.VISIBLE
        mRecyclerView.scrollToPosition(position)

    }


    private fun slateBoard() {
        dialog.setContentView(R.layout.dialog_slate_board)
        dialog.setTitle("Choose Slate")
        dialog.mBlack.setOnClickListener {
            pencilColor = Color.WHITE
            paintView.changeColorAndText(Color.WHITE, 25)
            paintView.setBoardColor(Color.BLACK)
            dialog.dismiss()
            paintView.clear()
        }
        dialog.mWhite.setOnClickListener {
            pencilColor = Color.BLACK
            paintView.changeColorAndText(Color.BLACK, 25)
            paintView.setBoardColor(Color.WHITE)
            dialog.dismiss()
            paintView.clear()
        }
        dialog.mGreen.setOnClickListener {
            pencilColor = Color.WHITE
            paintView.changeColorAndText(Color.WHITE, 25)
            paintView.setBoardColor(Color.parseColor("#3E5B47"))
            dialog.dismiss()
            paintView.clear()
        }
        dialog.show()
    }

    private fun interstitialAd() {
        mInterstitialAd = InterstitialAd(homeActivity)
        mInterstitialAd.adUnitId = "ca-app-pub-9110061456871098/7756318281"
        mInterstitialAd.loadAd(AdRequest.Builder().build())
    }
}
