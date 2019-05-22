package com.example.registry

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(R.string.about)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        main_activity_recycler_view.layoutManager = LinearLayoutManager(this)
        main_activity_recycler_view.adapter = MainActivityAdapter(this)
    }
}
