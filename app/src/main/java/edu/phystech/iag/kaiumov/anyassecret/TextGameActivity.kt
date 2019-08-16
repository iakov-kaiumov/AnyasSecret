package edu.phystech.iag.kaiumov.anyassecret

import android.animation.ValueAnimator
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_textgame.*
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.random.Random

class TextGameActivity : AppCompatActivity() {

    private lateinit var mediaPlayer : MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_textgame)
        createButtons()
        parseGameFile()
        onStepChange(0)
        animateImageView()

        mediaPlayer = MediaPlayer.create(this, R.raw.noise_sound)
        mediaPlayer.isLooping = true
        mediaPlayer.start()

        typedTextView.setOnCharacterTypedListener { _, index ->
            if (index == typedTextView.text.length - 1) {
                onTypeFinish()
            }
        }
    }

    override fun onDestroy() {
        mediaPlayer.stop()
        super.onDestroy()
    }

    override fun onBackPressed() = Unit

    private fun animateImageView() {
        Glide.with(this).load(R.raw.noise_with_men).into(imageView)

        val handler = Handler()
        val r = object : Runnable {
            override fun run() {
                val animation = ValueAnimator.ofFloat(
                    imageView.alpha,
                    Random.nextInt(5, 40) / 100f
                )
                val duration = Random.nextLong(500, 2000)
                animation.duration = duration
                animation.addUpdateListener {
                    val value = it.animatedValue as Float
                    imageView.alpha = value
                    mediaPlayer.setVolume(value, value)
                }
                animation.start()
                handler.postDelayed(this, duration)
            }
        }
        handler.postDelayed(r, 100)
    }

    // region game

    companion object {
        private const val ACTION_INTIMIDATION = "ACTION_INTIMIDATION"
        private val BLINK_TEMPLATE = Array(30) { 500L }
    }

    private class Answer(val text: String, val nextStep: Int)
    private class GameStep(val text: String, val answers: ArrayList<Answer>, val action: () -> Unit = {})

    private var gameSteps = ArrayList<GameStep>()
    private var buttons = ArrayList<Button>()

    private fun intimidation() {
        Flashlight.blink(this, BLINK_TEMPLATE)
    }

    private fun createButtons() {
        for (i in 0 until 3) {
            val button = Button(this, null, android.R.attr.borderlessButtonStyle)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            button.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            buttonPanel.addView(button, lp)
            buttons.add(button)
        }
    }

    private fun onStepChange(nextStep: Int) {
        if (nextStep == -1) {
            startActivity(Intent(this, HeartActivity::class.java))
            finish()
        }
        val step = gameSteps[nextStep]
        typedTextView.text = step.text
        typedTextView.setTypedText(step.text)
        for (i in 0 until step.answers.size) {
            buttons[i].visibility = View.VISIBLE
            buttons[i].text = step.answers[i].text
            buttons[i].setOnClickListener {
                buttonPanel.visibility = View.INVISIBLE
                onStepChange(step.answers[i].nextStep)
            }
        }
        for (i in step.answers.size until 3) {
            buttons[i].visibility = View.GONE
        }
        step.action()
    }

    private fun onTypeFinish() {
        buttonPanel.visibility = View.VISIBLE
    }

    private fun parseGameFile() {
        val stream = InputStreamReader(resources.openRawResource(R.raw.textgame), "utf-8")
        val rawString = BufferedReader(stream).readText()
        val steps = rawString.split("\n\n")
        for (step in steps) {
            val lines = step.split("\n")
            val answers = ArrayList<Answer>()
            for (i in 2 until lines.size) {
                val split = lines[i].split("|")
                if (split.size != 2)
                    break
                answers.add(Answer(split[0], split[1].trim().toInt()))
            }
            val split = lines[1].split("|")
            val text = split[0]
            if (split.size == 2) {
                val intimidation : () -> Unit = { intimidation() }
                val emptyAction : () -> Unit = {}
                val action: () -> Unit = when (split[1]) {
                    ACTION_INTIMIDATION -> intimidation
                    else -> emptyAction
                }
                gameSteps.add(GameStep(text, answers, action))
            } else {
                gameSteps.add(GameStep(text, answers))
            }
        }
    }

    // region game end
}
