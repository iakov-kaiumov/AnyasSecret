package edu.phystech.iag.kaiumov.anyassecret

import android.animation.ValueAnimator
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.plattysoft.leonids.ParticleSystem
import edu.phystech.iag.kaiumov.anyassecret.notification.AlarmStarter
import github.hotstu.lib.wavedrawable.WaveDrawable
import kotlinx.android.synthetic.main.activity_night.*

// Waves - https://github.com/hotstu/WaveDrawable

class NightActivity : AppCompatActivity() {


    private enum class DayState { NOON, NIGHT, UNDEFINED }
    private var dayState = DayState.NIGHT

    private lateinit var sunRiseAnimation : Animation
    private lateinit var sunFallAnimation : Animation
    private lateinit var bgMediaPlayer: MediaPlayer

    private val waveAnimator = ValueAnimator.ofFloat(0f, 1f)

    private var step = 0
    private var isGaming = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_night)

        AlarmStarter.start(this)

        sunRiseAnimation = AnimationUtils.loadAnimation(this, R.anim.sun_rise)
        sunFallAnimation = AnimationUtils.loadAnimation(this, R.anim.sun_fall)

        waveViewDay.background = createWaveDrawable(R.color.colorWaveNoon)
        waveViewNight.background = createWaveDrawable(R.color.colorWaveNight)

        bgMediaPlayer = MediaPlayer.create(this, R.raw.waves_background_sound)
        bgMediaPlayer.setVolume(0.2f, 0.2f)
        bgMediaPlayer.isLooping = true

        isGaming = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
            MainActivity.IS_GAMING_KEY,
            true
        )

        button.setOnClickListener {
            when (dayState) {
                DayState.NIGHT -> sunrise()
                DayState.NOON -> sunset()
                DayState.UNDEFINED -> Unit
            }
        }

        if (isGaming) {
            val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
            editor.putBoolean(MainActivity.IS_GAMING_KEY, false)
            editor.apply()

            typedTextView.setTypedText(R.string.game_over1)
            typedTextView.setOnCharacterTypedListener { _, index ->
                if (index == typedTextView.text.length - 1) {
                    onTypeFinish()
                }
            }
            createFirework()
        }
    }

    override fun onStart() {
        super.onStart()
        stars.onStart()
        starsWhite.onStart()
        waveAnimator.start()
        bgMediaPlayer.start()
    }

    override fun onStop() {
        stars.onStop()
        starsWhite.onStop()
        waveAnimator.cancel()
        bgMediaPlayer.pause()
        super.onStop()
    }

    private fun onTypeFinish() {
        when (step) {
            0 -> {
                typedTextView.visibility = View.INVISIBLE
                sunrise()
            }
            1 -> {
                typedTextView.visibility = View.INVISIBLE
            }
        }
        step += 1
    }

    private fun sunrise() {
        dayState = DayState.UNDEFINED
        sunView.visibility = View.VISIBLE
        sunView.startAnimation(sunRiseAnimation)
        // nightSkyView.startAnimation(fadingOutAnimation)
        stars.animate().alphaBy(1f).alpha(0f)
            .setInterpolator(AccelerateDecelerateInterpolator()).setDuration(10000).start()
        starsWhite.animate().alphaBy(1f).alpha(0f)
            .setInterpolator(AccelerateDecelerateInterpolator()).setDuration(10000).start()

        nightSkyView.animate().alphaBy(1f).alpha(0f)
            .setInterpolator(AccelerateDecelerateInterpolator()).setDuration(7000).start()
        sunsetSkyView.animate().alphaBy(1f).alpha(0f)
            .setInterpolator(AccelerateDecelerateInterpolator()).setDuration(10000).start()

        waveViewNight.animate().alphaBy(1f).alpha(0f)
            .setInterpolator(AccelerateDecelerateInterpolator()).setDuration(6000).start()

        val mediaPlayer = MediaPlayer.create(this, R.raw.sun_rise)
        mediaPlayer.start()

        Handler().postDelayed({ onSunRaised() }, 10000)
    }

    private fun onSunRaised() {
        dayState = DayState.NOON
        button.setText(R.string.sunset)
        if (isGaming) {
            typedTextView.visibility = View.VISIBLE
            typedTextView.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            typedTextView.setTypedText(R.string.game_over2)
        }
    }

    private fun sunset() {
        dayState = DayState.UNDEFINED
        sunView.startAnimation(sunFallAnimation)

        nightSkyView.animate().alphaBy(0f).alpha(1f)
            .setInterpolator(AccelerateDecelerateInterpolator()).setDuration(13000).start()
        sunsetSkyView.animate().alphaBy(0f).alpha(1f)
            .setInterpolator(AccelerateDecelerateInterpolator()).setDuration(6000).start()

        stars.animate().alphaBy(0f).alpha(1f)
            .setInterpolator(AccelerateDecelerateInterpolator()).setDuration(10000).start()
        starsWhite.animate().alphaBy(0f).alpha(1f)
            .setInterpolator(AccelerateDecelerateInterpolator()).setDuration(10000).start()

        waveViewNight.animate().alphaBy(0f).alpha(1f)
            .setInterpolator(AccelerateDecelerateInterpolator()).setDuration(6000).start()

        Handler().postDelayed({ onSunSet() }, 13000)
    }

    private fun onSunSet() {
        dayState = DayState.NIGHT
        button.setText(R.string.sunrise)
    }

    private fun createWaveDrawable(colorId: Int) : WaveDrawable {
        val waveDrawable = WaveDrawable()
        waveDrawable.setWaveSpeed(0.01f)
        waveDrawable.setColor(ContextCompat.getColor(this, colorId))
        waveAnimator.repeatMode = ValueAnimator.REVERSE
        waveAnimator.duration = 2000
        waveAnimator.addUpdateListener { animation ->
            waveDrawable.progress = animation.animatedFraction / 100 + 0.5f
        }
        waveAnimator.repeatCount = ValueAnimator.INFINITE
        return waveDrawable
    }

    private fun createFirework() {
        val ps = ParticleSystem(this, 50, R.drawable.star_pink, 600)
        ps.setScaleRange(0.7f, 1.3f)
        ps.setSpeedRange(0.1f, 0.25f)
        ps.setRotationSpeedRange(90f, 180f)
        ps.setFadeOut(200, AccelerateInterpolator())
        ps.oneShot(typedTextView, 70)

        val ps2 = ParticleSystem(this, 50, R.drawable.star_white, 600)
        ps2.setScaleRange(0.7f, 1.3f)
        ps2.setSpeedRange(0.1f, 0.25f)
        ps2.setRotationSpeedRange(90f, 180f)
        ps2.setFadeOut(200, AccelerateInterpolator())
        ps2.oneShot(typedTextView, 70)
    }
}
