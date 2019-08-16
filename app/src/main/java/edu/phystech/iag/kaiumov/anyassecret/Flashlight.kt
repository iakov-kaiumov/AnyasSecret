package edu.phystech.iag.kaiumov.anyassecret

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Handler

object Flashlight {

    fun turnOn(context: Context) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
        val cameraId = cameraManager!!.cameraIdList[0]
        cameraManager.setTorchMode(cameraId, true)

    }

    fun turnOff(context: Context) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
        val cameraId = cameraManager!!.cameraIdList[0]
        cameraManager.setTorchMode(cameraId, false)
    }

    fun blink(context: Context, template: Array<Long>) {
        var index = 0
        val handler = Handler()
        val r = object : Runnable {
            override fun run() {
                index += 1
                if (index == template.size) {
                    turnOff(context)
                    return
                }
                if (index % 2 == 0) {
                    turnOn(context)
                } else {
                    turnOff(context)
                }
                handler.postDelayed(this, template[index])
            }
        }
        handler.postDelayed(r, template[index])

    }

}
