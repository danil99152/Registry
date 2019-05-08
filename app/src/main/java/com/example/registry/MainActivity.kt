package com.example.registry

import android.app.Activity
import android.os.Bundle
import android.widget.Button


class MainActivity : Activity() {

    val camera = Camera()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnCapturePicture = findViewById<Button>(R.id.btnCapturePicture)

        btnCapturePicture.setOnClickListener {
            // capture picture
            camera.captureImage()
        }
        // Checking camera availability
//        if (!camera.isDeviceSupportCamera) {
//            Toast.makeText(
//                applicationContext,
//                "Sorry! Your device doesn't support camera",
//                Toast.LENGTH_LONG
//            ).show()
//            // will close the app if the device does't have camera
//            finish()
//        }
    }
}