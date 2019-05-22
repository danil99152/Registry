package com.example.registry.facedetection

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.registry.R
import com.example.registry.Upload
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.otaliastudios.cameraview.*
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_face_detection.*
import kotlinx.android.synthetic.main.content_face_detection.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@Suppress("DEPRECATION", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class FaceDetectionActivity : AppCompatActivity(), FrameProcessor {

    private var cameraFacing: Facing = Facing.FRONT

    private val imageView by lazy { findViewById<ImageView>(R.id.face_detection_image_view)!! }

    private val bottomSheetButton by lazy { findViewById<FrameLayout>(R.id.bottom_sheet_button)!! }
    private val bottomSheetRecyclerView by lazy { findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.bottom_sheet_recycler_view)!! }
    private val bottomSheetBehavior by lazy { BottomSheetBehavior.from(findViewById(R.id.bottom_sheet)!!) }

    private val faceDetectionModels = ArrayList<FaceDetectionModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_detection)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        cameraView.sessionType = SessionType.PICTURE
        cameraView.facing = cameraFacing
        cameraView.setLifecycleOwner(this)
        cameraView.addFrameProcessor(this)
        face_detection_camera_toggle_button.setOnClickListener {
            cameraFacing = if (cameraFacing == Facing.FRONT) Facing.BACK else Facing.FRONT
            cameraView.facing = cameraFacing
        }

        cameraView.addCameraListener(object: CameraListener() {
            override fun onPictureTaken(jpeg: ByteArray?) {
                analyzeImage(BitmapFactory.decodeByteArray(jpeg, 0, jpeg?.size ?: 0))
            }
        })

        bottomSheetButton.setOnClickListener {
            cameraView.capturePicture()
        }

        bottomSheetRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        bottomSheetRecyclerView.adapter = FaceDetectionAdapter(this, faceDetectionModels)
    }

    @SuppressLint("SimpleDateFormat")
    private fun storeImage(imageData: Bitmap) {
        // get path to external storage (SD card)
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_DCIM + "/FaceDetected/")
        // create storage directories, if they don't exist
        storageDir.mkdirs()
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val filename = "FaceBy$timeStamp"
        try {
            val file = File.createTempFile(
                filename, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
            )
            val fileURI = file.toURI()
            val fileOutputStream = FileOutputStream(file)
            val bos = BufferedOutputStream(fileOutputStream)
            imageData.compress(Bitmap.CompressFormat.PNG, 100, bos)
            bos.flush()
            bos.close()
            MediaScannerConnection.scanFile(
                this,
                arrayOf(file.path),
                arrayOf("image/jpeg"), null
            )
            Upload().uploadFile(fileURI)
            Toast.makeText(this, "Вы авторизовались", Toast.LENGTH_SHORT).show()
            hideProgress()
        } catch (e: FileNotFoundException) {
            Log.w("TAG", "Error saving image file: " + e.message)
        } catch (e: IOException) {
            Log.w("TAG", "Error saving image file: " + e.message)
        }
        return
    }

    override fun process(frame: Frame) {

//        val width = frame.size.width
//        val height = frame.size.height
//
//        val metadata = FirebaseVisionImageMetadata.Builder()
//            .setWidth(width)
//            .setHeight(height)
//            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
//            .setRotation(if (cameraFacing == Facing.FRONT) FirebaseVisionImageMetadata.ROTATION_270 else FirebaseVisionImageMetadata.ROTATION_90)
//            .build()
//
//        val firebaseVisionImage = FirebaseVisionImage.fromByteArray(frame.data, metadata)
//        val options = FirebaseVisionFaceDetectorOptions.Builder()
//            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
//            .build()
//        val faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(options)
//        faceDetector.detectInImage(firebaseVisionImage)
//            .addOnSuccessListener {
//                face_detection_camera_image_view.setImageBitmap(null)
//
//                val bitmap = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888)
//                val canvas = Canvas(bitmap)
//                val dotPaint = Paint()
//                dotPaint.color = Color.RED
//                dotPaint.style = Paint.Style.FILL
//                dotPaint.strokeWidth = 4F
//                val linePaint = Paint()
//                linePaint.color = Color.GREEN
//                linePaint.style = Paint.Style.STROKE
//                linePaint.strokeWidth = 2F
//
//                for (face in it) {
//
//                    val faceContours = face.getContour(FirebaseVisionFaceContour.FACE).points
//                    for ((i, contour) in faceContours.withIndex()) {
//                        if (i != faceContours.lastIndex)
//                            canvas.drawLine(contour.x, contour.y, faceContours[i + 1].x, faceContours[i + 1].y, linePaint)
//                        else
//                            canvas.drawLine(contour.x, contour.y, faceContours[0].x, faceContours[0].y, linePaint)
//                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
//                    }
//
//                    val leftEyebrowTopContours = face.getContour(FirebaseVisionFaceContour.LEFT_EYEBROW_TOP).points
//                    for ((i, contour) in leftEyebrowTopContours.withIndex()) {
//                        if (i != leftEyebrowTopContours.lastIndex)
//                            canvas.drawLine(contour.x, contour.y, leftEyebrowTopContours[i + 1].x, leftEyebrowTopContours[i + 1].y, linePaint)
//                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
//                    }
//
//                    val leftEyebrowBottomContours = face.getContour(FirebaseVisionFaceContour.LEFT_EYEBROW_BOTTOM).points
//                    for ((i, contour) in leftEyebrowBottomContours.withIndex()) {
//                        if (i != leftEyebrowBottomContours.lastIndex)
//                            canvas.drawLine(contour.x, contour.y, leftEyebrowBottomContours[i + 1].x, leftEyebrowBottomContours[i + 1].y, linePaint)
//                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
//                    }
//
//                    val rightEyebrowTopContours = face.getContour(FirebaseVisionFaceContour.RIGHT_EYEBROW_TOP).points
//                    for ((i, contour) in rightEyebrowTopContours.withIndex()) {
//                        if (i != rightEyebrowTopContours.lastIndex)
//                            canvas.drawLine(contour.x, contour.y, rightEyebrowTopContours[i + 1].x, rightEyebrowTopContours[i + 1].y, linePaint)
//                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
//                    }
//
//                    val rightEyebrowBottomContours = face.getContour(FirebaseVisionFaceContour.RIGHT_EYEBROW_BOTTOM).points
//                    for ((i, contour) in rightEyebrowBottomContours.withIndex()) {
//                        if (i != rightEyebrowBottomContours.lastIndex)
//                            canvas.drawLine(contour.x, contour.y, rightEyebrowBottomContours[i + 1].x, rightEyebrowBottomContours[i + 1].y, linePaint)
//                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
//                    }
//
//                    val leftEyeContours = face.getContour(FirebaseVisionFaceContour.LEFT_EYE).points
//                    for ((i, contour) in leftEyeContours.withIndex()) {
//                        if (i != leftEyeContours.lastIndex)
//                            canvas.drawLine(contour.x, contour.y, leftEyeContours[i + 1].x, leftEyeContours[i + 1].y, linePaint)
//                        else
//                            canvas.drawLine(contour.x, contour.y, leftEyeContours[0].x, leftEyeContours[0].y, linePaint)
//                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
//                    }
//
//                    val rightEyeContours = face.getContour(FirebaseVisionFaceContour.RIGHT_EYE).points
//                    for ((i, contour) in rightEyeContours.withIndex()) {
//                        if (i != rightEyeContours.lastIndex)
//                            canvas.drawLine(contour.x, contour.y, rightEyeContours[i + 1].x, rightEyeContours[i + 1].y, linePaint)
//                        else
//                            canvas.drawLine(contour.x, contour.y, rightEyeContours[0].x, rightEyeContours[0].y, linePaint)
//                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
//                    }
//
//                    val upperLipTopContours = face.getContour(FirebaseVisionFaceContour.UPPER_LIP_TOP).points
//                    for ((i, contour) in upperLipTopContours.withIndex()) {
//                        if (i != upperLipTopContours.lastIndex)
//                            canvas.drawLine(contour.x, contour.y, upperLipTopContours[i + 1].x, upperLipTopContours[i + 1].y, linePaint)
//                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
//                    }
//
//                    val upperLipBottomContours = face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).points
//                    for ((i, contour) in upperLipBottomContours.withIndex()) {
//                        if (i != upperLipBottomContours.lastIndex)
//                            canvas.drawLine(contour.x, contour.y, upperLipBottomContours[i + 1].x, upperLipBottomContours[i + 1].y, linePaint)
//                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
//                    }
//
//                    val lowerLipTopContours = face.getContour(FirebaseVisionFaceContour.LOWER_LIP_TOP).points
//                    for ((i, contour) in lowerLipTopContours.withIndex()) {
//                        if (i != lowerLipTopContours.lastIndex)
//                            canvas.drawLine(contour.x, contour.y, lowerLipTopContours[i + 1].x, lowerLipTopContours[i + 1].y, linePaint)
//                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
//                    }
//
//                    val lowerLipBottomContours = face.getContour(FirebaseVisionFaceContour.LOWER_LIP_BOTTOM).points
//                    for ((i, contour) in lowerLipBottomContours.withIndex()) {
//                        if (i != lowerLipBottomContours.lastIndex)
//                            canvas.drawLine(contour.x, contour.y, lowerLipBottomContours[i + 1].x, lowerLipBottomContours[i + 1].y, linePaint)
//                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
//                    }
//
//                    val noseBridgeContours = face.getContour(FirebaseVisionFaceContour.NOSE_BRIDGE).points
//                    for ((i, contour) in noseBridgeContours.withIndex()) {
//                        if (i != noseBridgeContours.lastIndex)
//                            canvas.drawLine(contour.x, contour.y, noseBridgeContours[i + 1].x, noseBridgeContours[i + 1].y, linePaint)
//                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
//                    }
//
//                    val noseBottomContours = face.getContour(FirebaseVisionFaceContour.NOSE_BOTTOM).points
//                    for ((i, contour) in noseBottomContours.withIndex()) {
//                        if (i != noseBottomContours.lastIndex)
//                            canvas.drawLine(contour.x, contour.y, noseBottomContours[i + 1].x, noseBottomContours[i + 1].y, linePaint)
//                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
//                    }
//
//
//                    if (cameraFacing == Facing.FRONT) {
//                        val matrix = Matrix()
//                        matrix.preScale(-1F, 1F)
//                        val flippedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
//                        face_detection_camera_image_view.setImageBitmap(flippedBitmap)
//                    } else {
//                        face_detection_camera_image_view.setImageBitmap(bitmap)
//                    }
//                }
//            }
//            .addOnFailureListener {
//                face_detection_camera_image_view.setImageBitmap(null)
//            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)

            if (resultCode == Activity.RESULT_OK) {
                val imageUri = result.uri
                analyzeImage(MediaStore.Images.Media.getBitmap(contentResolver, imageUri))
                face_detection_camera_container.visibility = View.GONE
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "There was some error : ${result.error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun analyzeImage(image: Bitmap?) {
        if (image == null) {
            Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
            return
        }
        imageView.setImageBitmap(null)
        faceDetectionModels.clear()
        bottomSheetRecyclerView.adapter?.notifyDataSetChanged()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        showProgress()

        val firebaseVisionImage = FirebaseVisionImage.fromBitmap(image)
        val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .build()
        val faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(options)
        faceDetector.detectInImage(firebaseVisionImage)
            .addOnSuccessListener {
                val mutableImage = image.copy(Bitmap.Config.ARGB_8888, true)

                detectFaces(it, mutableImage)

                imageView.setImageBitmap(mutableImage)
                hideProgress()
                bottomSheetRecyclerView.adapter?.notifyDataSetChanged()
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            .addOnFailureListener {
                Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
                hideProgress()
            }
        return
    }

    private fun detectFaces(faces: List<FirebaseVisionFace>?, image: Bitmap?) {
        if (faces == null || image == null) {
            Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
            return
        } else if (faces.size == 1) storeImage(image)
        else if (faces.size != 1) {
            Toast.makeText(this, "Не видно вашего лица или несколько лиц", Toast.LENGTH_SHORT).show()
            hideProgress()
        }
    }

    private fun showProgress() {
        findViewById<View>(R.id.bottom_sheet_button_image).visibility = View.GONE
        findViewById<View>(R.id.bottom_sheet_button_progress).visibility = View.VISIBLE
    }

    private fun hideProgress() {
        findViewById<View>(R.id.bottom_sheet_button_image).visibility = View.VISIBLE
        findViewById<View>(R.id.bottom_sheet_button_progress).visibility = View.GONE
    }

}
