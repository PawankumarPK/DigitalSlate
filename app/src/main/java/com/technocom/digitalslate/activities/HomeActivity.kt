package com.technocom.digitalslate.activities

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import com.technocom.digitalslate.R
import com.technocom.digitalslate.fragments.HomeFragment
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity : BaseActivity() {

    private lateinit var player: MediaPlayer
    companion object {
        var screenMode = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        player = MediaPlayer.create(this, R.raw.sound)
        setSupportActionBar(mToolbar)
        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (fragment != null)
            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()
        else
            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, HomeFragment()).commit()
        mSoundCheckbox.setOnCheckedChangeListener { _, isChecked -> sound(isChecked) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_items, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mAboutUs -> startActivity(Intent(this, AboutUs::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    /*  override fun onBackPressed() {

          if (supportFragmentManager.backStackEntryCount == 0)
              exitApp(false)
          else
              super.onBackPressed()
      }

      private fun exitApp(exit: Boolean) {
          AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(resources.getString(R.string.exit))
                  .setMessage(resources.getString(R.string.Areyousureyouwanttoexit))
                  .setPositiveButton(resources.getString(R.string.yes)) { _, _ -> if (exit) finish() else super.onBackPressed() }.setNegativeButton(resources.getString(R.string.no), null).show()
      }
  */
    private fun sound(boolean: Boolean) {
        if (boolean) {
            player.start()
            player.isLooping = true
        }
        else
            player.pause()
    }

    override fun onDestroy() {
        player.stop()
        player.release()
        super.onDestroy()
    }

    override fun onStop() {
        player.pause()
        super.onStop()
    }

    override fun onStart() {
        mSoundCheckbox.performClick()
        super.onStart()
    }
}
