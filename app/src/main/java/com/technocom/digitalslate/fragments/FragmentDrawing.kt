package com.technocom.digitalslate.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.technocom.digitalslate.R
import com.technocom.digitalslate.activities.HomeActivity
import com.technocom.digitalslate.adapter.RecyclerViewAdapter
import com.technocom.digitalslate.utils.PaintView
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.dialog_drawing_background.*
import kotlinx.android.synthetic.main.dialog_eraser.*
import kotlinx.android.synthetic.main.dialog_pencil.*
import kotlinx.android.synthetic.main.dialog_share.*
import kotlinx.android.synthetic.main.fragment_drawing.*
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FragmentDrawing : BaseFragment() {

    private val displayRectangle = Rect()
    private var width = 0
    private lateinit var dialog: Dialog
    private lateinit var path: File
    private var pencilColor = Color.WHITE
    private var pencilSize = 25
    private var eraserSize = 25
    private var eraserColor = Color.BLACK
    private var alphabets: ArrayList<String> = ArrayList()
    private lateinit var adapter: RecyclerViewAdapter
    private lateinit var mInterstitialAd: InterstitialAd
    private val PERMISSIONS_MULTIPLE_REQUEST = 123
    private val dialogBackColor = Color.LTGRAY
    private var dialogBack = 5
    private var dialogEraserBackColor = "eraserColor"
    private var dialogBlackSlateEraser = "blackEraserColor"
    private var dialogPencilBack = 5
    private lateinit var metrics: DisplayMetrics

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_drawing, container, false)
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
        paintView.init(metrics)
        pencil.setOnClickListener { changePencil(pencilSize, dialogBack); pencilDialog();changeBackground();pencil.setBackgroundColor(Color.GRAY) }
        eraser.setOnClickListener { changeEraser(eraserSize, dialogEraserBackColor);eraserDialog();changeBackground();eraser.setBackgroundColor(Color.GRAY) }
        save.setOnClickListener { saveBitmap();changeBackground();save.setBackgroundColor(Color.GRAY) }
        mRefresh.setOnClickListener { paintView.clear();changeBackground();mRefresh.setBackgroundColor(Color.GRAY) }
        mColor.setOnClickListener { openDialog(false);changeBackground();mColor.setBackgroundColor(Color.GRAY) }
        mSlateBoard.setOnClickListener { slateBoard() }
        mOrientation.setOnClickListener { orientation(0) }
        mOrientation2.setOnClickListener { orientation(1) }
        share.setOnClickListener { share() }
        mArrow.setOnCheckedChangeListener { _, isChecked -> hideBottomBar(isChecked) }
        mHome.setOnClickListener { homeButton() }

        if (!pref.isPremium)
            InterstitialAdd()
    }

    private fun changeBackground() {

        pencil.setBackgroundColor(Color.TRANSPARENT)
        eraser.setBackgroundColor(Color.TRANSPARENT)
        save.setBackgroundColor(Color.TRANSPARENT)
        mColor.setBackgroundColor(Color.TRANSPARENT)
        mRefresh.setBackgroundColor(Color.TRANSPARENT)

    }

    private fun orientation(orientation: Int) {
        HomeActivity.screenMode = orientation
        homeActivity.requestedOrientation = HomeActivity.screenMode
    }

    private fun share() {
        dialog.setContentView(R.layout.dialog_share)
        dialog.setTitle("Share")
        dialog.mView.setOnClickListener { validationYes();onShareClick();dialog.dismiss() }
        dialog.mCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun slateBoard() {
        dialog.setContentView(R.layout.dialog_drawing_background)
        dialog.mBlack.setOnClickListener {
            paintView.setBoardColor(Color.BLACK)
            pencilColor = Color.WHITE
            eraserColor = Color.BLACK
            changePencil(pencilSize, dialogBack)
            dialog.dismiss()
            changeBackground()
            paintView.clear()
        }
        dialog.mWhite.setOnClickListener {
            paintView.setBoardColor(Color.WHITE)
            pencilColor = Color.BLACK
            eraserColor = Color.WHITE
            dialog.dismiss()
            dialogEraserBackColor = "two"
            dialogPencilBack = 1
            changePencil(pencilSize, dialogBack)
            changeBackground()
            paintView.clear()
        }
        dialog.show()
        WhiteSlateEraserBackground(dialogBlackSlateEraser)
        WhiteSlatePencilBackground(dialogPencilBack)
    }

    private fun WhiteSlateEraserBackground(blackeraseBack: String) {
        when (blackeraseBack) {
            "one" -> {
                dialog.smallEraser.setBackgroundColor(dialogBackColor)
            }
            "two" -> {
                dialog.mediumEraser.setBackgroundColor(dialogBackColor)
            }
        }
    }

    private fun WhiteSlatePencilBackground(blackPencilBack: Int) {
        when (blackPencilBack) {
            0 -> {
                dialog.thinPencil.setBackgroundColor(dialogBackColor)
            }
        }
    }

    private fun saveBitmap() {
        dialog.setContentView(R.layout.savefile_dialog)
        dialog.mView.setOnClickListener { validationYes();dialog.dismiss() }
        dialog.mCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
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

//    private fun openPaint(file: File) {
//        val target = Intent(Intent.ACTION_VIEW)
//        // target.setDataAndType(Uri.fromFile(file), "image/*")
//
//        val apkURI = FileProvider.getUriForFile(homeActivity, this.applicationContext.packageName + ".provider", file)
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

    @SuppressLint("InflateParams")
    private fun pencilDialog() {
        val layout = LayoutInflater.from(homeActivity).inflate(R.layout.dialog_pencil, null)
        layout.minimumWidth = width
        // val dialog = getDialog()
        dialog.setContentView(layout)
        dialog.setTitle("Choose Pencil")
        dialog.thinPencil.setOnClickListener { changePencil(10, 0) }
        dialog.smallPencil.setOnClickListener { changePencil(15, 1) }
        dialog.mediumPencil.setOnClickListener { changePencil(25, 2) }
        dialog.largePencil.setOnClickListener { changePencil(45, 3) }
        dialog.show()
        background(dialogBack)

    }

    private fun changePencil(pencilSize: Int, dialogBack: Int) {
        this.dialogBack = dialogBack
        this.pencilSize = pencilSize
        paintView.changeColorAndText(pencilColor, this.pencilSize)
        dialog.dismiss()
    }

    private fun background(dialogBackground: Int) {
        when (dialogBackground) {
            0 -> {
                dialog.thinPencil.setBackgroundColor(dialogBackColor)
            }
            1 -> {
                dialog.smallPencil.setBackgroundColor(dialogBackColor)
            }
            2 -> {
                dialog.mediumPencil.setBackgroundColor(dialogBackColor)
            }
            3 -> {
                dialog.largePencil.setBackgroundColor(dialogBackColor)
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun eraserDialog() {
        val layout = LayoutInflater.from(homeActivity).inflate(R.layout.dialog_eraser, null)
        layout.minimumWidth = width
        dialog.setContentView(layout)
        dialog.setTitle("Choose Eraser")
        dialog.smallEraser.setOnClickListener { changeEraser(35, "one") }
        dialog.mediumEraser.setOnClickListener { changeEraser(45, "two") }
        dialog.largeEraser.setOnClickListener { changeEraser(65, "three") }
        dialog.clearAll.setOnClickListener { paintView.clear();dialog.dismiss() }
        dialog.show()
        erserBackground(dialogEraserBackColor)
    }

    private fun changeEraser(eraserSize: Int, dialogEraserBackColor: String) {
        this.dialogEraserBackColor = dialogEraserBackColor
        this.eraserSize = eraserSize
        paintView.changeColorAndText(eraserColor, this.eraserSize)
        dialog.dismiss()
    }


    private fun erserBackground(eraseBack: String) {
        when (eraseBack) {
            "one" -> {
                dialog.smallEraser.setBackgroundColor(dialogBackColor)
            }
            "two" -> {
                dialog.mediumEraser.setBackgroundColor(dialogBackColor)
            }
            "three" -> {
                dialog.largeEraser.setBackgroundColor(dialogBackColor)
            }

        }
    }

    /* private fun getDialog(): Dialog {
         if (!::dialog.isInitialized) {
             val dialog = Dialog(this, R.style.AppTheme)
             dialog.setCanceledOnTouchOutside(false)
             dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
             this.dialog = dialog
         }
         return this.dialog
     }*/

    private fun openDialog(supportsAlpha: Boolean) {


        if (!paintView.isPencil) {
            toastLong("Eraser Selectable")
        } else {
            val dialog = AmbilWarnaDialog(homeActivity, pencilColor, supportsAlpha, object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                    paintView.changeColorAndText(color, PaintView.BRUSH_SIZE)
                    this@FragmentDrawing.pencilColor = color

                    // displayColor()
                }

                override fun onCancel(dialog: AmbilWarnaDialog) {
                    //  Toast.makeText(applicationContext, "cancel", Toast.LENGTH_SHORT).show()
                }
            })
            dialog.show()

        }
    }


    private fun onShareClick() {

        // val pm = packageManager
        /*  val sendIntent = Intent(Intent.ACTION_SEND)
          sendIntent.type = "text/plain"
  */
        // val openInChooser = Intent . createChooser (emailIntent, resources.getString(R.string.share_chooser_text));
/*

        val share = Intent(Intent.ACTION_SEND)
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
                    packageName.contains("android.gm") -> { // If Gmail shows up twice, try removing this else-if clause and the reference to "android.gm" above
                        intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(resources.getString(R.string.share_email_gmail)))
                        intent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.share_email_subject))
                        intent.type = "message/rfc822"
                    }
                }

                //  intentList.add(LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon))
            }
        }


        val extraIntents = intentList.toArray(arrayOfNulls<LabeledIntent>(intentList.size))

        share.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents)
        startActivity(share)
        // startActivity(Intent.createChooser(share, "Share Image"))
*/


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
            toastLong("Please,Save this image")
            return
        }
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse(file.path))
        startActivity(Intent.createChooser(share, "Share Image"))
    }

    private fun hideBottomBar(boolean: Boolean) {
        if (boolean)
            paintOptions.visibility = View.GONE
        else
            paintOptions.visibility = View.VISIBLE
    }

    /* private fun drawAlphabets(boolean: Boolean) {
         alphabetsList()
         alpha.alphabetsSmall(boolean)
         paintView.clear()
     }*/

    private fun homeButton() {
        fragmentManager!!.popBackStack()


    }

    /* private fun alphabetsList() {
         val list = resources.getStringArray(R.array.Alphabets)
         alphabets.clear()
         for (i in 0..list.lastIndex) {
             alphabets.add(list[i])
         }
         adapter.notifyDataSetChanged()
     }

     interface SmallAlphabets {
         fun alphabetsSmall(bool: Boolean)
     }*/


    /* private fun hindiVowel() {
         val hinVowlist = resources.getStringArray(R.array.hindivowel)
         alphabets.clear()
         visible()
         paintView.clear()
         for (i in 0..hinVowlist.lastIndex) {
             alphabets.add(hinVowlist[i])
         }
         alpha.alphabetsSmall(false)
     }

     private fun hindiConstant() {
         val hinConstList = resources.getStringArray(R.array.hindiconstant)
         alphabets.clear()
         visible()
         paintView.clear()
         for (i in 0..hinConstList.lastIndex) {
             alphabets.add(hinConstList[i])
         }
         alpha.alphabetsSmall(false)
     }
 */


    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(homeActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)

            if (ActivityCompat.shouldShowRequestPermissionRationale(homeActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Snackbar.make(view!!,
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


    private fun InterstitialAdd() {
        mInterstitialAd = InterstitialAd(homeActivity)
        mInterstitialAd.adUnitId = "ca-app-pub-9110061456871098/4154296185"
        mInterstitialAd.loadAd(AdRequest.Builder().build())
    }

}
