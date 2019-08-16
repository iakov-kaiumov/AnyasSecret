package edu.phystech.iag.kaiumov.anyassecret

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_heart.*
import net.kibotu.heartrateometer.HeartRateOmeter
import net.kibotu.kalmanrx.jama.Matrix
import net.kibotu.kalmanrx.jkalman.JKalman

class HeartActivity : AppCompatActivity() {

    private var subscription: CompositeDisposable? = null
    private var step = 0

    private var score = 1000
    private var prevScore = 1000
    private var initialRate = 70
    private var averageCount = 1
    private var isGaming = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heart)

        val path = "android.resource://" + packageName + "/" + R.raw.penguins_video
        videoView.setVideoURI(Uri.parse(path))

        typedTextView.setTypedText(R.string.heart_intro_tip1)
        typedTextView.setOnCharacterTypedListener { _, index ->
            if (index == typedTextView.text.length - 1) {
                onTypeFinish()
            }
        }
    }

    override fun onBackPressed() = Unit

    override fun onResume() {
        super.onResume()
        dispose()
        subscription = CompositeDisposable()
    }

    override fun onPause() {
        dispose()
        super.onPause()
    }

    private fun onTypeFinish() {
        when (step) {
            0 -> startMeasuring()
            1 -> startVideo()
            2 -> Handler().postDelayed({
                    startActivity(Intent(this, BlackActivity::class.java))
                    finish()
                }, 2000)
        }
        step += 1
    }

    private fun startVideo() {
        isGaming = true
        typedTextView.visibility = View.INVISIBLE
        scoreTextView.visibility = View.VISIBLE
        videoView.visibility = View.VISIBLE
        videoView.setOnCompletionListener { onVideoFinish() }
        videoView.start()
    }

    private fun onVideoFinish() {
        videoView.visibility = View.GONE
        dispose()
        typedTextView.visibility = View.VISIBLE
        typedTextView.setTypedText(R.string.heart_intro_end)
    }

    private fun startMeasuring() {
        val kalman = JKalman(2, 1)

        // measurement [x]
        val m = Matrix(1, 1)

        // transitions for x, dx
        val tr = arrayOf(doubleArrayOf(1.0, 0.0), doubleArrayOf(0.0, 1.0))
        kalman.transition_matrix = Matrix(tr)

        // 1s somewhere?
        kalman.error_cov_post = kalman.error_cov_post.identity()


        val bpmUpdates = HeartRateOmeter()
            .withAverageAfterSeconds(3)
            .setFingerDetectionListener(this::onFingerChange)
            .bpmUpdates(preview)
            .subscribe({

                if (it.value == 0)
                    return@subscribe

                m.set(0, 0, it.value.toDouble())

                // state [x, dx]
                val s = kalman.Predict()

                // corrected state [x, dx]
                val c = kalman.Correct(m)

                val bpm = it.copy(value = c.get(0, 0).toInt())
                onBpm(bpm)
            }, Throwable::printStackTrace)

        subscription?.add(bpmUpdates)

    }

    private fun onBpm(bpm: HeartRateOmeter.Bpm) {
        bpmTextView.text = getString(R.string.heart_bmp) + " " + bpm.value.toString()

        if (isGaming) {
            score += initialRate - bpm.value
            scoreTextView.text = getString(R.string.score_label) + " " + score
            if (prevScore <= score) {
                scoreTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            } else {
                scoreTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            }
            prevScore = score
        } else {
            // Setting Up initial rate
            initialRate = (initialRate * averageCount + bpm.value) / (averageCount + 1)
            averageCount += 1
            if (averageCount == 12) {
                continueButton.visibility = View.VISIBLE
                continueButton.setOnClickListener {
                    typedTextView.setTypedText(R.string.heart_intro_tip2)
                    continueButton.visibility = View.GONE
                }
            }
        }
    }

    private fun onFingerChange(fingerDetected: Boolean) {
        if (fingerDetected) {
            fingerTextView.text = ""
        } else {
            fingerTextView.setText(R.string.heart_finger_not_detected)
        }
    }

    private fun dispose() {
        if (subscription?.isDisposed == false)
            subscription?.dispose()
    }
}
