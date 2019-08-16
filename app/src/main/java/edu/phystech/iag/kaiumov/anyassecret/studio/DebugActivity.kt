package edu.phystech.iag.kaiumov.anyassecret.studio

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import edu.phystech.iag.kaiumov.anyassecret.R
import edu.phystech.iag.kaiumov.anyassecret.TextGameActivity
import kotlinx.android.synthetic.main.activity_debug.*
import kotlin.random.Random


class DebugActivity : AppCompatActivity() {

    companion object {
        private const val YASHA_APPEAR_DELAY = 2000L
        private const val YASHA_DISAPPEAR_DELAY = 3000L
    }

    private lateinit var bgMusicPlayer : MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        bgMusicPlayer = MediaPlayer.create(this, R.raw.matrix_music)
        bgMusicPlayer.isLooping = true
        bgMusicPlayer.start()

        val lines = getString(R.string.java_stack_trace).split("\n")
        showStackTrace(lines) { finishJavaStackTrace() }
    }

    override fun onDestroy() {
        bgMusicPlayer.stop()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.studio_app_bar_menu, menu)
        menu ?: return false
        val playStopButton = menu.findItem(R.id.run_button)!!
        playStopButton.title = "ic_stop"
        playStopButton.setIcon(R.drawable.ic_stop)
        return true
    }

    override fun onBackPressed() = Unit

    private fun showStackTrace(lines: List<String>, onFinish: () -> Unit) {
        var index = 0
        val handler = Handler()
        val r = object : Runnable {
            override fun run() {
                if (index == lines.size) {
                    onFinish()
                } else {
                    textView.append(lines[index])
                    textView.append("\n \n")
                    scrollView.smoothScrollTo(0, textView.bottom)
                    index += 1
                    handler.postDelayed(this, Random.nextLong(50, 200))
                }
            }
        }
        textView.text = ""
        handler.postDelayed(r, 100)
    }

    private fun finishJavaStackTrace() {
        deepButton.visibility = View.VISIBLE
        deepButton.text = getString(R.string.go_deep)
        deepButton.setOnClickListener {
            val lines = getString(R.string.assembler_stack_trace).split(";")
            showStackTrace(lines) { finishAssemblerStackTrace() }
            deepButton.visibility = View.INVISIBLE
        }

        val handler = Handler()
        val yashaDisappear = Runnable {
            yashaView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.yasha_disappear))
        }
        val yashaAppear = Runnable {
            yashaView.visibility = View.VISIBLE
            yashaView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.yasha_appear))
            handler.postDelayed(yashaDisappear, YASHA_DISAPPEAR_DELAY)
        }
        handler.postDelayed(yashaAppear, YASHA_APPEAR_DELAY)
    }

    private fun finishAssemblerStackTrace() {
        deepButton.visibility = View.VISIBLE
        deepButton.text = getString(R.string.go_deeper)
        deepButton.setOnClickListener {
            imageView1.visibility = View.VISIBLE
            imageView2.visibility = View.VISIBLE
            Glide.with(this).load(R.raw.deep_code).into(imageView1)
            Glide.with(this).load(R.raw.noise_gif).into(imageView2)
            deepButton.text = getString(R.string.go_back)

            val mediaPlayer = MediaPlayer.create(this, R.raw.fade_in_noise)
            mediaPlayer.start()

            imageView2.animate()
                .alphaBy(0f)
                .alpha(1f)
                .setDuration(6000)
                .withEndAction {
                    imageView2.setImageResource(android.R.color.black)
                    deepButton.visibility = View.INVISIBLE
                    mediaPlayer.stop()
                    bgMusicPlayer.stop()
                    Handler().postDelayed(
                        {
                            startActivity(Intent(applicationContext, TextGameActivity::class.java))
                            finish()
                        },
                        6000
                    )
                }
                .start()

            deepButton.setOnClickListener(null)
        }
    }
}
