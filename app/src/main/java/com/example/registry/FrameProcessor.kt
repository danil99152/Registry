package com.example.registry

import android.support.v4.view.ViewCompat.getRotation
import android.support.annotation.WorkerThread
import com.otaliastudios.cameraview.Frame


internal class FrameProcessor  : cameraView.addFrameProcessor(){
    @WorkerThread
    override fun process(frame: Frame) {
        val data = frame.getData()
        val rotation = frame.getRotation()
        val time = frame.getTime()
        val size = frame.getSize()
        val format = frame.getFormat()
    }
}