package edu.phystech.iag.kaiumov.anyassecret.studio

import android.content.Context
import android.util.Size
import android.view.Display
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.World
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.PI

// https://github.com/HaydnTrigg/JBox2D-Demo/blob/master/app/src/main/java/com/haydntrigg/android/MainActivity.java

class Box2DEngine(private val context: Context, private val rootLayout: ConstraintLayout,
                  display: Display) {

    companion object {
        private const val DT = 1 / 100f
        private const val VELOCITY_ITERATIONS = 16
        private const val POSITION_ITERATIONS = 8
        private const val worldScale = 0.01f
        private const val buttonScale = 0.90f
        private const val BUTTON_WIDTH = 300
        private const val BUTTON_HEIGHT = 150
    }

    private val positions = ArrayList<Pair<Float, Float>>()
    private val world = World(Vec2(0.0f, 0.0f))
    private var accumulator = 0f
    private var lastTicks = -1L
    private val displayWidth = display.width
    private val displayHeight = display.height

    fun setGravity(g: Vec2) {
        world.gravity = g
    }

    fun save() {
        positions.clear()
        var body = world.bodyList
        while (body != null) {
            if (body.type == BodyType.DYNAMIC) {
                val button = body.userData as Button
                positions.add(Pair(button.translationX + button.width / 2,
                    button.translationY + button.height / 2))
            }
            body = body.next
        }
    }

    fun load() {
        reset()
        for (pos in positions) {
            createBoxWithButton(pos.first.toInt(), pos.second.toInt(),
                BUTTON_WIDTH, BUTTON_HEIGHT)
        }
    }

    fun reset() {
        var body = world.bodyList
        while (body != null) {
            val button = body.userData as Button
            rootLayout.removeView(button)
            world.destroyBody(body)
            body = body.next
        }
        val height = 300
        createFloor(displayWidth / 2, displayHeight, displayWidth, height)
    }

    fun step() {
        // destroying bodies
        var body = world.bodyList
        while (body != null) {
            if (body.userData != null) {
                val button = body.userData as Button
                if (button.translationY > displayHeight * 2 ||
                    button.translationX + button.width < -100 ||
                    button.translationX > displayWidth * 2) {
                    world.destroyBody(body)
                }
            }
            body = body.next
        }

        val nowticks = Calendar.getInstance().time.time
        if (lastTicks == -1L)
            lastTicks = nowticks
        accumulator += (nowticks - lastTicks) / 1000.0f
        lastTicks = nowticks
        while (accumulator > DT) {
            world.step(DT, VELOCITY_ITERATIONS, POSITION_ITERATIONS)
            accumulator -= DT
        }
    }

    fun draw() {
        if (world.bodyCount == 0)
            return
        var body = world.bodyList
        for (i in 0 until world.bodyCount) {
            val button = body.userData as Button
            val size = button.tag as Size
            button.translationX = body.position.x / worldScale - size.width / 2
            button.translationY = displayHeight - body.position.y / worldScale - size.height / 2
            button.rotation = (-body.angle * 180 / PI).toFloat()
            body = body.next
        }
    }

    fun onTouch(x: Float, y: Float) {
        createBoxWithButton(x.toInt(), y.toInt(), BUTTON_WIDTH, BUTTON_HEIGHT)
    }

    private fun createFloor(x: Int, y: Int, width: Int, height: Int) {
        val bodyDef = BodyDef()

        bodyDef.position = Vec2(x * worldScale, (displayHeight - y) * worldScale)
        bodyDef.angle = 0.0f
        bodyDef.linearVelocity = Vec2(0.0f, 0.0f)
        bodyDef.angularVelocity = 0.0f
        bodyDef.fixedRotation = false
        bodyDef.active = true
        bodyDef.bullet = false
        bodyDef.allowSleep = false
        bodyDef.gravityScale = 1.0f
        bodyDef.linearDamping = 0.0f
        bodyDef.angularDamping = 0.0f
        bodyDef.userData = addButton(x, y, width, height, "")
        bodyDef.type = BodyType.STATIC

        val shape = PolygonShape()

        shape.setAsBox(width * worldScale * buttonScale / 2,
            height * worldScale * buttonScale / 2)
        val fixtureDef = FixtureDef()
        fixtureDef.shape = shape
        fixtureDef.userData = null
        fixtureDef.friction = 0.35f
        fixtureDef.restitution = 0.05f
        fixtureDef.density = 0.75f
        fixtureDef.isSensor = false

        val body = world.createBody(bodyDef)
        body.createFixture(fixtureDef)
    }

    private fun createBoxWithButton(x: Int, y: Int, width: Int, height: Int) {
        val bodyDef = BodyDef()

        bodyDef.position = Vec2(x * worldScale, (displayHeight - y) * worldScale)
        bodyDef.angle = 0.0f
        bodyDef.linearVelocity = Vec2(0.0f, 0.0f)
        bodyDef.angularVelocity = 0.0f
        bodyDef.fixedRotation = false
        bodyDef.active = true
        bodyDef.bullet = false
        bodyDef.allowSleep = false
        bodyDef.gravityScale = 1.0f
        bodyDef.linearDamping = 0.0f
        bodyDef.angularDamping = 0.0f
        bodyDef.userData = addButton(x, y, width, height, "Push me!")
        bodyDef.type = BodyType.DYNAMIC

        val shape = PolygonShape()

        shape.setAsBox(width * worldScale * buttonScale / 2,
            height * worldScale * buttonScale / 2)
        val fixtureDef = FixtureDef()
        fixtureDef.shape = shape
        fixtureDef.userData = null
        fixtureDef.friction = 0.35f
        fixtureDef.restitution = 0.05f
        fixtureDef.density = 0.75f
        fixtureDef.isSensor = false

        val body = world.createBody(bodyDef)
        body.createFixture(fixtureDef)
    }

    private fun addButton(x: Int, y: Int, width: Int, height: Int, text: String) : Button {
        val button = Button(context)
        button.text = text
        val lp = ConstraintLayout.LayoutParams(width, height)
        rootLayout.addView(button, lp)
        button.x = (x - width / 2).toFloat()
        button.y = (y - height / 2).toFloat()
        button.tag = Size(width, height)
        return button
    }
}