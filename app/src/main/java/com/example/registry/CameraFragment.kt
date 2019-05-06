package com.example.registry

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View

internal class CameraFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val camera = findViewById(R.id.camera)
        camera.setLifecycleOwner(viewLifecycleOwner)
    }
}