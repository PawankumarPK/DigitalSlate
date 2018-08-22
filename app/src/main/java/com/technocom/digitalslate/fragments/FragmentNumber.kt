package com.technocom.digitalslate.fragments

import android.Manifest
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
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
import android.support.v4.content.FileProvider
import android.support.v7.widget.LinearLayoutManager
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.technocom.digitalslate.R
import com.technocom.digitalslate.activities.HomeActivity
import com.technocom.digitalslate.adapter.RecyclerViewAdapter
import com.technocom.digitalslate.utils.PaintView
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.dialog_slate_board.*
import kotlinx.android.synthetic.main.fragment_number.*
import kotlinx.android.synthetic.main.savefile_dialog.*
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FragmentNumber : BaseFragment() {

    private val displayRectangle = Rect()
    private var width = 0
    private lateinit var dialog: Dialog
    private lateinit var path: File
    private var pencilColor = Color.WHITE
    private var eraserColor = Color.BLACK
    private var position = 0
    private var alphabets: ArrayList<String> = ArrayList()
    private lateinit var adapter: RecyclerViewAdapter
    private lateinit var mInterstitialAd: InterstitialAd
    private val PERMISSIONS_MULTIPLE_REQUEST = 123
    private lateinit var metrics: DisplayMetrics

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_number, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            checkPermission()
        homeActivity.requestedOrientation = HomeActivity.screenMode
        homeActivity.mToolbar.visibility = GONE
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

        countingList();dialogClick(true)
        paintView.init(metrics)
        pencil.setOnClickListener { changeBackground();pencil.setBackgroundColor(Color.GRAY); paintView.changeColorAndText(pencilColor, 25) }
        eraser.setOnClickListener { paintView.clear() }
        save.setOnClickListener { saveBitmap();changeBackground();save.setBackgroundColor(Color.GRAY); paintView.changeColorAndText(Color.TRANSPARENT, 25) }
        mColor.setOnClickListener { openDialog(false) }
        mSlateBoard.setOnClickListener { slateBoard() }
        mNext.setOnClickListener { nextButton() }
        mPrevious.setOnClickListener { previousButton() }
        mHome.setOnClickListener { homeButton() }
        mOrientation.setOnClickListener { orientation(0) }
        mOrientation2.setOnClickListener { orientation(1) }
        share.setOnClickListener { share();changeBackground();share.setBackgroundColor(Color.GRAY); paintView.changeColorAndText(Color.TRANSPARENT, 25) }
        // mArrow.setOnCheckedChangeListener { _, isChecked -> hideBottomBar(isChecked) }

        if (!pref.isPremium)
            InterstitialAdd()
    }

    private fun pencilConfig() {
        if (pencil.isSelected)
            paintView.changeColorAndText(Color.WHITE, 25)
        else
            paintView.changeColorAndText(Color.TRANSPARENT, 25)

    }

    private fun changeBackground() {

        pencil.setBackgroundColor(Color.TRANSPARENT)
        eraser.setBackgroundColor(Color.TRANSPARENT)
        save.setBackgroundColor(Color.TRANSPARENT)
        mColor.setBackgroundColor(Color.TRANSPARENT)
        share.setBackgroundColor(Color.TRANSPARENT)

    }

    private fun saveBitmap() {
        dialog.setContentView(R.layout.savefile_dialog)
        dialog.setTitle("Save & Share")
        dialog.mView.setOnClickListener { validationYes();dialog.dismiss() }
        dialog.mCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun share() {
        dialog.setContentView(R.layout.dialog_share)
        dialog.setTitle("Share")
        dialog.mView.setOnClickListener { validationYes(); onShareClick();dialog.dismiss() }
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
        //  onShareClick()
        // openDialog(paintView.file)
    }


    //click on save dialog
    /*  private fun openDialog(paintFile: File) {
          dialog.setContentView(R.layout.savefile_dialog)
          dialog.setCancelable(false)
          dialog.setCanceledOnTouchOutside(false)

          dialog.mCancel.setOnClickListener {
              dialog.dismiss()
          }
          dialog.mView.setOnClickListener {
              openPaint(paintFile)
              dialog.dismiss()
          }
          dialog.show()
      }*/

    private fun openPaint(file: File) {
        val target = Intent(Intent.ACTION_VIEW)
        // target.setDataAndType(Uri.fromFile(file), "image/*")

        val apkURI = FileProvider.getUriForFile(homeActivity, homeActivity.applicationContext.packageName + ".provider", file)
        target.setDataAndType(apkURI, "image/*")
        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val intent = Intent.createChooser(target, "Open File")
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            toastLong("No Application Found to open paint")
        }
    }

    /* private fun getDialog(): Dialog {
         if (!::dialog.isInitialized) {
             val dialog = Dialog(homeActivity, R.style.AppTheme)
             dialog.setCanceledOnTouchOutside(false)
             dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
             homeActivity.dialog = dialog
         }
         return homeActivity.dialog
     }*/

    private fun openDialog(supportsAlpha: Boolean) {
        val dialog = AmbilWarnaDialog(homeActivity, pencilColor, supportsAlpha, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                paintView.changeColorAndText(color, PaintView.BRUSH_SIZE)
                this@FragmentNumber.pencilColor = color
                // displayColor()
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

        /* val pm = packageManager
         val sendIntent = Intent(Intent.ACTION_SEND)
         sendIntent.type = "text/plain"*/

        // val openInChooser = Intent . createChooser (emailIntent, resources.getString(R.string.share_chooser_text));

        /*   val share = Intent(Intent.ACTION_SEND)
           val resInfo = pm.queryIntentActivities(sendIntent, 0)
           val intentList = ArrayList<LabeledIntent>()
           for (i in 0..resInfo.size) {
               // Extract the label, append it, and repackage it in a LabeledIntent
               val ri = resInfo[i]
               val packageName = ri.activityInfo.packageName
               if (packageName.contains("twitter") || packageName.contains("facebook") || packageName.contains("mms") || packageName.contains("android.gm")) {
                   val intent = Intent()
                   intent.component = ComponentName(packageName, ri.activityInfo.name)
                   intent.action = Intent.ACTION_SEND
                   intent.type = "text/plain"
                   when {
                       packageName.contains("twitter") -> intent.putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.share_twitter))
                       packageName.contains("facebook") -> intent.putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.share_facebook))
                       packageName.contains("mms") -> intent.putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.share_sms))
                       packageName.contains("android.gm") -> { // If Gmail shows up twice, try removing homeActivity else-if clause and the reference to "android.gm" above
                           intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(resources.getString(R.string.share_email_gmail)))
                           intent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.share_email_subject))
                           intent.type = "message/rfc822"
                       }
                   }*/

        //  intentList.add(LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon))
        /*  val extraIntents = intentList.toArray(arrayOfNulls<LabeledIntent>(intentList.size))

          share.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents)
          startActivity(share)*/
        // startActivity(Intent.createChooser(share, "Share Image"))

    }

    /* private fun hideBottomBar(boolean: Boolean) {
         if (boolean) {
             paintOptions.visibility = View.GONE
             val lp = mArrow.layoutParams as RelativeLayout.LayoutParams
             lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,10)
             mArrow.layoutParams = lp
         } else {
             paintOptions.visibility = View.VISIBLE
             val layoutParams =  RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
             layoutParams.addRule(RelativeLayout.ABOVE, R.id.paintOptions)
             layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
             mArrow.layoutParams = layoutParams

         }

     }*/


    private fun nextButton() {
        position++
        mRecyclerView.smoothScrollToPosition(position)
        //  mRecyclerView.adapter.notifyItemChanged(position)
        paintView.clear()
        showHide(position)

    }

    private fun previousButton() {
        position--
        mRecyclerView.smoothScrollToPosition(position)
        paintView.clear()
        showHide(position)
    }

    private fun showHide(pos: Int) {
        when (pos) {
            0 -> {
                mPrevious.visibility = View.INVISIBLE
                mPrevious.isClickable = true
                mNext.visibility = View.VISIBLE
                mNext.isClickable = true
            }
            9 -> {
                mNext.visibility = View.INVISIBLE
                mNext.isClickable = true
                mPrevious.visibility = View.VISIBLE
                mPrevious.isClickable = true

            }
            8 -> {
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

    /*  private fun alphabetsList() {
          val list = resources.getStringArray(R.array.Alphabets)
          alphabets.clear()
          for (i in 0..list.lastIndex) {
              alphabets.add(list[i])
          }
          adapter.notifyDataSetChanged()
      }*/

    /*  interface SmallAlphabets {
          fun alphabetsSmall(bool: Boolean)
      }*/

    private fun countingList() {
        val countlist = resources.getStringArray(R.array.counting)
        for (i in 0..countlist.lastIndex) {
            alphabets.add(countlist[i])
        }
    }

    private fun dialogClick(boolean: Boolean) {
        position = 0
        if (boolean)
            RecyclerViewAdapter.myTypeface = Typeface.createFromAsset(homeActivity.assets, "DOTLRG__.TTF")
        else
            RecyclerViewAdapter.myTypeface = Typeface.createFromAsset(homeActivity.assets, "Dotted.ttf")
        mRecyclerView.scrollToPosition(position)
        adapter.notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(homeActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)

            if (ActivityCompat.shouldShowRequestPermissionRationale(homeActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Snackbar.make(homeActivity.findViewById(android.R.id.content),
                        "Please Grant Permissions to access your phone media",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE"
                ) {
                    requestPermissions(
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_MULTIPLE_REQUEST)
                }.show()
            } else {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_MULTIPLE_REQUEST)
            }
    }

    private fun slateBoard() {
        dialog.setContentView(R.layout.dialog_slate_board)
        dialog.setTitle("Choose Slate ")

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

    private fun InterstitialAdd() {
        mInterstitialAd = InterstitialAd(homeActivity)
        mInterstitialAd.adUnitId = "ca-app-pub-9110061456871098/4154296185"
        mInterstitialAd.loadAd(AdRequest.Builder().build())
    }

}
