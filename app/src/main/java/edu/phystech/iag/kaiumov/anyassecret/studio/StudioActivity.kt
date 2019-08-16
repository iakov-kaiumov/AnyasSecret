package edu.phystech.iag.kaiumov.anyassecret.studio

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.google.android.material.navigation.NavigationView
import edu.phystech.iag.kaiumov.anyassecret.R
import kotlinx.android.synthetic.main.activity_studio.*
import kotlinx.android.synthetic.main.activity_studio_app_bar.*
import kotlinx.android.synthetic.main.activity_studio_content.*
import org.jbox2d.common.Vec2
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

// https://github.com/sjwall/MaterialTapTargetPrompt/blob/master/sample/src/main/java/uk/co/samuelwall/materialtaptargetprompt/sample/KotlinActivity.kt

class StudioActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var boxGame: Box2DEngine
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var playStopButton: MenuItem
    private lateinit var deleteButton: MenuItem
    private lateinit var debugButton: MenuItem

    companion object {
        private const val LOOP_DELAY = 10L

        private const val YASHA_APPEAR_DELAY = 2000L

        private const val YASHA_DISAPPEAR_DELAY = 3000L

        private const val BUTTONS_TO_ADD = 6
    }

    private val gameHandler = Handler()
    private val gameLoop = object : Runnable {
        override fun run() {
            boxGame.step()
            boxGame.draw()
            gameHandler.postDelayed(this, LOOP_DELAY)
        }
    }

    private var isAllowButtonAdd = false
    private var buttonAdded = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_studio)
        title = ""
        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.go_back, R.string.go_deep)
        drawerLayout.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        navView.setNavigationItemSelectedListener(this)

        boxGame = Box2DEngine(this, root, windowManager.defaultDisplay)
        boxGame.reset()
        gameHandler.postDelayed(gameLoop, LOOP_DELAY)

        startActivityForResult(Intent(this, StudioIntroActivity::class.java), 0)

        root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && isAllowButtonAdd) {
                buttonAdded += 1
                boxGame.onTouch(event.x, event.y)
                if (buttonAdded == BUTTONS_TO_ADD) {
                    showRunTip()
                }
            }
            return@setOnTouchListener true
        }
    }

    override fun onBackPressed() = Unit

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        showWidgetPanelTip()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        isAllowButtonAdd = when (item.itemId) {
            R.id.widgetButton -> true
            else -> false
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.studio_app_bar_menu, menu)
        menu ?: return false
        playStopButton = menu.findItem(R.id.run_button)!!
        playStopButton.title = "play"
        deleteButton = menu.findItem(R.id.delete_button)!!
        debugButton = menu.findItem(R.id.debug_button)!!
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        when (item!!.itemId) {
            R.id.run_button -> {
                if (playStopButton.title == "play")
                    startLoading()
                else
                    onStopButtonPress()
            }
            R.id.delete_button -> boxGame.reset()
            R.id.debug_button -> {
                startActivity(Intent(this, DebugActivity::class.java))
                finish()
            }
            R.id.build_button -> {
                Toast.makeText(this, "Wtf? Are you a stupid one?", Toast.LENGTH_SHORT).show()
            }
            R.id.save_button -> {
                Toast.makeText(this, "Wtf? Who use ic_save button in 2019? " +
                        "Everything saves automatically, dear..", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startLoading() {
        isAllowButtonAdd = false
        playStopButton.setIcon(R.drawable.ic_stop)
        playStopButton.isEnabled = false
        playStopButton.title = "onStopButtonPress"
        progressPanel.visibility = View.VISIBLE
        boxGame.save()
        val strings = resources.getStringArray(R.array.loading_phrases)
        var index = 0
        val handler = Handler()
        val r = object : Runnable {
            override fun run() {
                if (index == strings.size) {
                    finishLoading()
                } else {
                    runOnUiThread { textView.text = strings[index] }
                    index += 1
                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.postDelayed(r, 100)
    }

    private fun finishLoading() {
        progressPanel.visibility = View.INVISIBLE
        playStopButton.isEnabled = true
        boxGame.setGravity(Vec2(0f, -9.81f))
        val handler = Handler()
        val yashaDisappear = Runnable {
            yashaView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.yasha_disappear))
            MaterialTapTargetPrompt.Builder(this)
                .setTarget(findViewById<View>(R.id.run_button))
                .setPrimaryText(getString(R.string.stop_tip_title))
                .setSecondaryText(getString(R.string.stop_tip))
                .setAnimationInterpolator(LinearOutSlowInInterpolator())
                .setFocalPadding(R.dimen.dp40)
                .setIcon(R.drawable.ic_stop)
                .setCaptureTouchEventOnFocal(true)
                .setCaptureTouchEventOutsidePrompt(true)
                .show()
        }
        val yashaAppear = Runnable {
            yashaView.visibility = View.VISIBLE
            yashaView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.yasha_appear))
            Toast.makeText(this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show()
            handler.postDelayed(yashaDisappear, YASHA_DISAPPEAR_DELAY)
        }
        handler.postDelayed(yashaAppear, YASHA_APPEAR_DELAY)
    }

    private fun onStopButtonPress() {
        isAllowButtonAdd = true
        playStopButton.setIcon(R.drawable.ic_play_arrow)
        playStopButton.title = "play"
        boxGame.setGravity(Vec2(0f, 0f))
        boxGame.load()

        debugButton.isEnabled = true

        MaterialTapTargetPrompt.Builder(this)
            .setTarget(findViewById<View>(R.id.debug_button))
            .setPrimaryText(getString(R.string.debug_tip_title))
            .setSecondaryText(getString(R.string.debug_tip))
            .setAnimationInterpolator(LinearOutSlowInInterpolator())
            .setFocalPadding(R.dimen.dp40)
            .setIcon(R.drawable.ic_debug)
            .setCaptureTouchEventOnFocal(true)
            .setCaptureTouchEventOutsidePrompt(true)
            .show()
    }

    private fun showWidgetPanelTip() {
        MaterialTapTargetPrompt.Builder(this)
            .setTarget(toolbar.getChildAt(0))
            .setPrimaryText(R.string.widget_tip_title)
            .setSecondaryText(R.string.widget_tip)
            .setIcon(R.drawable.ic_menu)
            .setAnimationInterpolator(FastOutSlowInInterpolator())
            .setCaptureTouchEventOnFocal(true)
            .setCaptureTouchEventOutsidePrompt(true)
            .show()
    }

    private fun showRunTip() {
        playStopButton.isEnabled = true
        MaterialTapTargetPrompt.Builder(this)
            .setTarget(findViewById<View>(R.id.run_button))
            .setPrimaryText(getString(R.string.run_tip_title))
            .setSecondaryText(getString(R.string.run_tip))
            .setAnimationInterpolator(LinearOutSlowInInterpolator())
            .setFocalPadding(R.dimen.dp40)
            .setCaptureTouchEventOnFocal(true)
            .setCaptureTouchEventOutsidePrompt(true)
            .setIcon(R.drawable.ic_play_arrow)
            .show()
    }
}
