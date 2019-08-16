package edu.phystech.iag.kaiumov.anyassecret

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.phystech.iag.kaiumov.anyassecret.settings.SettingsActivity
import kotlinx.android.synthetic.main.activity_black.*
import java.util.*

class BlackActivity : AppCompatActivity() {

    private lateinit var bgMediaPlayer : MediaPlayer
    private val timer = Timer()
    private var currentProgress = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_black)

        bgMediaPlayer = MediaPlayer.create(this, R.raw.scary_music)
        bgMediaPlayer.isLooping = true
        bgMediaPlayer.start()

        typedTextView.setTypedText(R.string.game_lost_text)
        typedTextView.setOnCharacterTypedListener { _, index ->
            if (index == typedTextView.text.length - 1) {
                onTypeFinish()
            }
        }
    }

    override fun onDestroy() {
        bgMediaPlayer.stop()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.black_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.settings_item -> {
                startActivityForResult(Intent(this, SettingsActivity::class.java), 0)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            timer.cancel()
            bgMediaPlayer.stop()
            val mediaPlayer = MediaPlayer.create(this, R.raw.impossible_sound)
            mediaPlayer.setOnCompletionListener {
                startActivity(Intent(this, NightActivity::class.java))
                finish()
            }
            mediaPlayer.start()
        }
    }

    override fun onBackPressed() = Unit

    private fun onTypeFinish() {
        typedTextView.visibility = View.INVISIBLE
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    currentProgress += 1
                    progressBar.progress = currentProgress
                    textView.text = getString(R.string.current_progress) + " " + currentProgress.toString() + "%"
                    if (currentProgress == 40) {
                        Toast.makeText(applicationContext, R.string.do_it, Toast.LENGTH_SHORT).show()
                    }
                    if (currentProgress == 100) {
                        finish()
                    }
                }
            }
        }, 0, 800)
    }


}
