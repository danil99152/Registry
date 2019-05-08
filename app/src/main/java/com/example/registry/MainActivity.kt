package com.example.registry

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (null == savedInstanceState) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, Camera2BasicFragment.newInstance())
                .commit()
        }

      //  imageView = findViewById<View>(R.id.image_view) as ImageView
    }
}