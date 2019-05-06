package com.example.registry

import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.PictureResult
import android.graphics.PointF
import com.otaliastudios.cameraview.CameraException
import com.otaliastudios.cameraview.CameraOptions



interface CameraListener : camera.addCameraListener {

//    override fun onPictureTaken(result: PictureResult) {
//        // Picture was taken!
//        // If planning to show a Bitmap, we will take care of
//        // EXIF rotation and background threading for you...
//        result.toBitmap(maxWidth, maxHeight, callback)
//
//        // If planning to save a file on a background thread,
//        // just use toFile. Ensure you have permissions.
//        result.toFile(file, callback)
//
//        // Access the raw data if needed.
//        val data = result.data
//    }

    override fun onCameraOpened(options: CameraOptions) {}

    override fun onCameraClosed() {}

    override fun onCameraError(error: CameraException) {}

    override fun onPictureTaken(result: PictureResult) {}

    override fun onVideoTaken(result: VideoResult) {}

    override fun onOrientationChanged(orientation: Int) {}

    override fun onFocusStart(point: PointF) {}

    override fun onFocusEnd(successful: Boolean, point: PointF) {}

    override fun onZoomChanged(newValue: Float, bounds: FloatArray, fingers: Array<PointF>) {}

    override fun onExposureCorrectionChanged(newValue: Float, bounds: FloatArray, fingers: Array<PointF>) {}
}
