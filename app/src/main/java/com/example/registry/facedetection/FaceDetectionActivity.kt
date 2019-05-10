package com.example.registry.facedetection

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.media.FaceDetector
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import com.example.registry.R
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark
import com.otaliastudios.cameraview.Facing
import com.otaliastudios.cameraview.Frame
import com.otaliastudios.cameraview.FrameProcessor
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_face_detection.*
import kotlinx.android.synthetic.main.content_face_detection.*

class FaceDetectionActivity : AppCompatActivity(), FrameProcessor {

    private var cameraFacing: Facing = Facing.FRONT

    private val imageView by lazy { findViewById<ImageView>(R.id.face_detection_image_view)!! }

    private val bottomSheetButton by lazy { findViewById<FrameLayout>(R.id.bottom_sheet_button)!! }
    private val bottomSheetRecyclerView by lazy { findViewById<RecyclerView>(R.id.bottom_sheet_recycler_view)!! }
    private val bottomSheetBehavior by lazy { BottomSheetBehavior.from(findViewById(R.id.bottom_sheet)!!) }

    private val faceDetectionModels = ArrayList<FaceDetectionModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_detection)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        face_detection_camera_view.facing = cameraFacing
        face_detection_camera_view.setLifecycleOwner(this)
        face_detection_camera_view.addFrameProcessor(this)
        face_detection_camera_toggle_button.setOnClickListener {
            cameraFacing = if (cameraFacing == Facing.FRONT) Facing.BACK else Facing.FRONT
            face_detection_camera_view.facing = cameraFacing
        }

        bottomSheetButton.setOnClickListener {
            //TODO bag - вылетает приложение при нажатии на кнопку фото
            CropImage.activity().start(this)
        }

        bottomSheetRecyclerView.layoutManager = LinearLayoutManager(this)
        bottomSheetRecyclerView.adapter = FaceDetectionAdapter(this, faceDetectionModels)
    }

    override fun process(frame: Frame) {

        val width = frame.size.width
        val height = frame.size.height

        val metadata = FirebaseVisionImageMetadata.Builder()
            .setWidth(width)
            .setHeight(height)
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
            .setRotation(if (cameraFacing == Facing.FRONT) FirebaseVisionImageMetadata.ROTATION_270 else FirebaseVisionImageMetadata.ROTATION_90)
            .build()

        val firebaseVisionImage = FirebaseVisionImage.fromByteArray(frame.data, metadata)
        val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
            .build()
        val faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(options)
        faceDetector.detectInImage(firebaseVisionImage)
            .addOnSuccessListener {
                face_detection_camera_image_view.setImageBitmap(null)

                val bitmap = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                val dotPaint = Paint()
                dotPaint.color = Color.RED
                dotPaint.style = Paint.Style.FILL
                dotPaint.strokeWidth = 4F
                val linePaint = Paint()
                linePaint.color = Color.GREEN
                linePaint.style = Paint.Style.STROKE
                linePaint.strokeWidth = 2F

                for (face in it) {

                    val faceContours = face.getContour(FirebaseVisionFaceContour.FACE).points
                    for ((i, contour) in faceContours.withIndex()) {
                        if (i != faceContours.lastIndex)
                            canvas.drawLine(contour.x, contour.y, faceContours[i + 1].x, faceContours[i + 1].y, linePaint)
                        else
                            canvas.drawLine(contour.x, contour.y, faceContours[0].x, faceContours[0].y, linePaint)
                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
                    }

                    val leftEyebrowTopContours = face.getContour(FirebaseVisionFaceContour.LEFT_EYEBROW_TOP).points
                    for ((i, contour) in leftEyebrowTopContours.withIndex()) {
                        if (i != leftEyebrowTopContours.lastIndex)
                            canvas.drawLine(contour.x, contour.y, leftEyebrowTopContours[i + 1].x, leftEyebrowTopContours[i + 1].y, linePaint)
                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
                    }

                    val leftEyebrowBottomContours = face.getContour(FirebaseVisionFaceContour.LEFT_EYEBROW_BOTTOM).points
                    for ((i, contour) in leftEyebrowBottomContours.withIndex()) {
                        if (i != leftEyebrowBottomContours.lastIndex)
                            canvas.drawLine(contour.x, contour.y, leftEyebrowBottomContours[i + 1].x, leftEyebrowBottomContours[i + 1].y, linePaint)
                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
                    }

                    val rightEyebrowTopContours = face.getContour(FirebaseVisionFaceContour.RIGHT_EYEBROW_TOP).points
                    for ((i, contour) in rightEyebrowTopContours.withIndex()) {
                        if (i != rightEyebrowTopContours.lastIndex)
                            canvas.drawLine(contour.x, contour.y, rightEyebrowTopContours[i + 1].x, rightEyebrowTopContours[i + 1].y, linePaint)
                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
                    }

                    val rightEyebrowBottomContours = face.getContour(FirebaseVisionFaceContour.RIGHT_EYEBROW_BOTTOM).points
                    for ((i, contour) in rightEyebrowBottomContours.withIndex()) {
                        if (i != rightEyebrowBottomContours.lastIndex)
                            canvas.drawLine(contour.x, contour.y, rightEyebrowBottomContours[i + 1].x, rightEyebrowBottomContours[i + 1].y, linePaint)
                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
                    }

                    val leftEyeContours = face.getContour(FirebaseVisionFaceContour.LEFT_EYE).points
                    for ((i, contour) in leftEyeContours.withIndex()) {
                        if (i != leftEyeContours.lastIndex)
                            canvas.drawLine(contour.x, contour.y, leftEyeContours[i + 1].x, leftEyeContours[i + 1].y, linePaint)
                        else
                            canvas.drawLine(contour.x, contour.y, leftEyeContours[0].x, leftEyeContours[0].y, linePaint)
                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
                    }

                    val rightEyeContours = face.getContour(FirebaseVisionFaceContour.RIGHT_EYE).points
                    for ((i, contour) in rightEyeContours.withIndex()) {
                        if (i != rightEyeContours.lastIndex)
                            canvas.drawLine(contour.x, contour.y, rightEyeContours[i + 1].x, rightEyeContours[i + 1].y, linePaint)
                        else
                            canvas.drawLine(contour.x, contour.y, rightEyeContours[0].x, rightEyeContours[0].y, linePaint)
                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
                    }

                    val upperLipTopContours = face.getContour(FirebaseVisionFaceContour.UPPER_LIP_TOP).points
                    for ((i, contour) in upperLipTopContours.withIndex()) {
                        if (i != upperLipTopContours.lastIndex)
                            canvas.drawLine(contour.x, contour.y, upperLipTopContours[i + 1].x, upperLipTopContours[i + 1].y, linePaint)
                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
                    }

                    val upperLipBottomContours = face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).points
                    for ((i, contour) in upperLipBottomContours.withIndex()) {
                        if (i != upperLipBottomContours.lastIndex)
                            canvas.drawLine(contour.x, contour.y, upperLipBottomContours[i + 1].x, upperLipBottomContours[i + 1].y, linePaint)
                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
                    }

                    val lowerLipTopContours = face.getContour(FirebaseVisionFaceContour.LOWER_LIP_TOP).points
                    for ((i, contour) in lowerLipTopContours.withIndex()) {
                        if (i != lowerLipTopContours.lastIndex)
                            canvas.drawLine(contour.x, contour.y, lowerLipTopContours[i + 1].x, lowerLipTopContours[i + 1].y, linePaint)
                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
                    }

                    val lowerLipBottomContours = face.getContour(FirebaseVisionFaceContour.LOWER_LIP_BOTTOM).points
                    for ((i, contour) in lowerLipBottomContours.withIndex()) {
                        if (i != lowerLipBottomContours.lastIndex)
                            canvas.drawLine(contour.x, contour.y, lowerLipBottomContours[i + 1].x, lowerLipBottomContours[i + 1].y, linePaint)
                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
                    }

                    val noseBridgeContours = face.getContour(FirebaseVisionFaceContour.NOSE_BRIDGE).points
                    for ((i, contour) in noseBridgeContours.withIndex()) {
                        if (i != noseBridgeContours.lastIndex)
                            canvas.drawLine(contour.x, contour.y, noseBridgeContours[i + 1].x, noseBridgeContours[i + 1].y, linePaint)
                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
                    }

                    val noseBottomContours = face.getContour(FirebaseVisionFaceContour.NOSE_BOTTOM).points
                    for ((i, contour) in noseBottomContours.withIndex()) {
                        if (i != noseBottomContours.lastIndex)
                            canvas.drawLine(contour.x, contour.y, noseBottomContours[i + 1].x, noseBottomContours[i + 1].y, linePaint)
                        canvas.drawCircle(contour.x, contour.y, 4F, dotPaint)
                    }


                    if (cameraFacing == Facing.FRONT) {
                        val matrix = Matrix()
                        matrix.preScale(-1F, 1F)
                        val flippedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                        face_detection_camera_image_view.setImageBitmap(flippedBitmap)
                    } else {
                        face_detection_camera_image_view.setImageBitmap(bitmap)
                    }
//////////////////////////////////////////////////////////////////////////////////////////////TODO

                    val x0 = face.getContour(FirebaseVisionFaceContour.FACE).points[0].x
                    val x1 = face.getContour(FirebaseVisionFaceContour.FACE).points[1].x
                    val x2 = face.getContour(FirebaseVisionFaceContour.FACE).points[2].x
                    val x3 = face.getContour(FirebaseVisionFaceContour.FACE).points[3].x
                    val x4 = face.getContour(FirebaseVisionFaceContour.FACE).points[4].x
                    val x5 = face.getContour(FirebaseVisionFaceContour.FACE).points[5].x
                    val x6 = face.getContour(FirebaseVisionFaceContour.FACE).points[6].x
                    val x7 = face.getContour(FirebaseVisionFaceContour.FACE).points[7].x
                    val x8 = face.getContour(FirebaseVisionFaceContour.FACE).points[8].x
                    val x9 = face.getContour(FirebaseVisionFaceContour.FACE).points[9].x
                    val x10 = face.getContour(FirebaseVisionFaceContour.FACE).points[10].x
                    val x11 = face.getContour(FirebaseVisionFaceContour.FACE).points[11].x
                    val x12 = face.getContour(FirebaseVisionFaceContour.FACE).points[12].x
                    val x13 = face.getContour(FirebaseVisionFaceContour.FACE).points[13].x
                    val x14 = face.getContour(FirebaseVisionFaceContour.FACE).points[14].x
                    val x15 = face.getContour(FirebaseVisionFaceContour.FACE).points[15].x
                    val x16 = face.getContour(FirebaseVisionFaceContour.FACE).points[16].x
                    val x17 = face.getContour(FirebaseVisionFaceContour.FACE).points[17].x
                    val x18 = face.getContour(FirebaseVisionFaceContour.FACE).points[18].x
                    val x19 = face.getContour(FirebaseVisionFaceContour.FACE).points[19].x
                    val x20 = face.getContour(FirebaseVisionFaceContour.FACE).points[20].x
                    val x21 = face.getContour(FirebaseVisionFaceContour.FACE).points[21].x
                    val x22 = face.getContour(FirebaseVisionFaceContour.FACE).points[22].x
                    val x23 = face.getContour(FirebaseVisionFaceContour.FACE).points[23].x
                    val x24 = face.getContour(FirebaseVisionFaceContour.FACE).points[24].x
                    val x25 = face.getContour(FirebaseVisionFaceContour.FACE).points[25].x
                    val x26 = face.getContour(FirebaseVisionFaceContour.FACE).points[26].x
                    val x27 = face.getContour(FirebaseVisionFaceContour.FACE).points[27].x
                    val x28 = face.getContour(FirebaseVisionFaceContour.FACE).points[28].x
                    val x29 = face.getContour(FirebaseVisionFaceContour.FACE).points[29].x
                    val x30 = face.getContour(FirebaseVisionFaceContour.FACE).points[30].x
                    val x31 = face.getContour(FirebaseVisionFaceContour.FACE).points[31].x
                    val x32 = face.getContour(FirebaseVisionFaceContour.FACE).points[32].x
                    val x33 = face.getContour(FirebaseVisionFaceContour.FACE).points[33].x
                    val x34 = face.getContour(FirebaseVisionFaceContour.FACE).points[34].x
                    val x35 = face.getContour(FirebaseVisionFaceContour.FACE).points[35].x

                    val x130 = face.getContour(FirebaseVisionFaceContour.ALL_POINTS).points[130].x

                    val y0 = face.getContour(FirebaseVisionFaceContour.FACE).points[0].y
                    val y1 = face.getContour(FirebaseVisionFaceContour.FACE).points[1].y
                    val y2 = face.getContour(FirebaseVisionFaceContour.FACE).points[2].y
                    val y3 = face.getContour(FirebaseVisionFaceContour.FACE).points[3].y
                    val y4 = face.getContour(FirebaseVisionFaceContour.FACE).points[4].y
                    val y5 = face.getContour(FirebaseVisionFaceContour.FACE).points[5].y
                    val y6 = face.getContour(FirebaseVisionFaceContour.FACE).points[6].y
                    val y7 = face.getContour(FirebaseVisionFaceContour.FACE).points[7].y
                    val y8 = face.getContour(FirebaseVisionFaceContour.FACE).points[8].y
                    val y9 = face.getContour(FirebaseVisionFaceContour.FACE).points[9].y
                    val y10 = face.getContour(FirebaseVisionFaceContour.FACE).points[10].y
                    val y11 = face.getContour(FirebaseVisionFaceContour.FACE).points[11].y
                    val y12 = face.getContour(FirebaseVisionFaceContour.FACE).points[12].y
                    val y13 = face.getContour(FirebaseVisionFaceContour.FACE).points[13].y
                    val y14 = face.getContour(FirebaseVisionFaceContour.FACE).points[14].y
                    val y15 = face.getContour(FirebaseVisionFaceContour.FACE).points[15].y
                    val y16 = face.getContour(FirebaseVisionFaceContour.FACE).points[16].y
                    val y17 = face.getContour(FirebaseVisionFaceContour.FACE).points[17].y
                    val y18 = face.getContour(FirebaseVisionFaceContour.FACE).points[18].y
                    val y19 = face.getContour(FirebaseVisionFaceContour.FACE).points[19].y
                    val y20 = face.getContour(FirebaseVisionFaceContour.FACE).points[20].y
                    val y21 = face.getContour(FirebaseVisionFaceContour.FACE).points[21].y
                    val y22 = face.getContour(FirebaseVisionFaceContour.FACE).points[22].y
                    val y23 = face.getContour(FirebaseVisionFaceContour.FACE).points[23].y
                    val y24 = face.getContour(FirebaseVisionFaceContour.FACE).points[24].y
                    val y25 = face.getContour(FirebaseVisionFaceContour.FACE).points[25].y
                    val y26 = face.getContour(FirebaseVisionFaceContour.FACE).points[26].y
                    val y27 = face.getContour(FirebaseVisionFaceContour.FACE).points[27].y
                    val y28 = face.getContour(FirebaseVisionFaceContour.FACE).points[28].y
                    val y29 = face.getContour(FirebaseVisionFaceContour.FACE).points[29].y
                    val y30 = face.getContour(FirebaseVisionFaceContour.FACE).points[30].y
                    val y31 = face.getContour(FirebaseVisionFaceContour.FACE).points[31].y
                    val y32 = face.getContour(FirebaseVisionFaceContour.FACE).points[32].y
                    val y33 = face.getContour(FirebaseVisionFaceContour.FACE).points[33].y
                    val y34 = face.getContour(FirebaseVisionFaceContour.FACE).points[34].y
                    val y35 = face.getContour(FirebaseVisionFaceContour.FACE).points[35].y

                    val y130 = face.getContour(FirebaseVisionFaceContour.ALL_POINTS).points[130].y

                    val hash = ((x0/x1/x2/x3/x4/x5/x6/x7/x8/x9/x10/x11/x12/x13/x14/x15/x16/x17/x18/x19/x20/x21/x22/x23/x24/x25/x26/x27/x28/x29/x30/x31/x32/x33/x34/x35) + (y0/y1/y2/y3/y4/y5/y6/y7/y8/y9/y10/y11/y12/y13/y14/y15/y16/y17/y18/y19/y20/y21/y22/y23/y24/y25/y26/y27/y28/y29/y30/y31/y32/y33/y34/y35)).hashCode()

                    Log.d("test", "hash: $hash")

//////////////////////////////////////////////////////////////////////////////////////////////
                }
            }
            .addOnFailureListener {
                face_detection_camera_image_view.setImageBitmap(null)
            }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
    }

    private fun detectFaces(faces: List<FirebaseVisionFace>?, image: Bitmap?) {
        if (faces == null || image == null) {
            Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
            return
        }

        val canvas = Canvas(image)
        val facePaint = Paint()
        facePaint.color = Color.RED
        facePaint.style = Paint.Style.STROKE
        facePaint.strokeWidth = 8F
        val faceTextPaint = Paint()
        faceTextPaint.color = Color.RED
        faceTextPaint.textSize = 40F
        faceTextPaint.typeface = Typeface.DEFAULT_BOLD
        val landmarkPaint = Paint()
        landmarkPaint.color = Color.RED
        landmarkPaint.style = Paint.Style.FILL
        landmarkPaint.strokeWidth = 8F

        for ((index, face) in faces.withIndex()) {

            canvas.drawRect(face.boundingBox, facePaint)
            canvas.drawText("Face$index", (face.boundingBox.centerX() - face.boundingBox.width() / 2) + 8F, (face.boundingBox.centerY() + face.boundingBox.height() / 2) - 8F, faceTextPaint)

            if (face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE) != null) {
                val leftEye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE)!!
                canvas.drawCircle(leftEye.position.x, leftEye.position.y, 8F, landmarkPaint)
            }
            if (face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE) != null) {
                val rightEye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE)!!
                canvas.drawCircle(rightEye.position.x, rightEye.position.y, 8F, landmarkPaint)
            }
            if (face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE) != null) {
                val nose = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE)!!
                canvas.drawCircle(nose.position.x, nose.position.y, 8F, landmarkPaint)
            }
            if (face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR) != null) {
                val leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR)!!
                canvas.drawCircle(leftEar.position.x, leftEar.position.y, 8F, landmarkPaint)
            }
            if (face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EAR) != null) {
                val rightEar = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EAR)!!
                canvas.drawCircle(rightEar.position.x, rightEar.position.y, 8F, landmarkPaint)
            }
            if (face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_LEFT) != null && face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM) != null && face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_RIGHT) != null) {
                val leftMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_LEFT)!!
                val bottomMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM)!!
                val rightMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_RIGHT)!!
                canvas.drawLine(leftMouth.position.x, leftMouth.position.y, bottomMouth.position.x, bottomMouth.position.y, landmarkPaint)
                canvas.drawLine(bottomMouth.position.x, bottomMouth.position.y, rightMouth.position.x, rightMouth.position.y, landmarkPaint)
            }

            faceDetectionModels.add(FaceDetectionModel(index, "Smiling Probability  ${face.smilingProbability}"))
            faceDetectionModels.add(FaceDetectionModel(index, "Left Eye Open Probability  ${face.leftEyeOpenProbability}"))
            faceDetectionModels.add(FaceDetectionModel(index, "Right Eye Open Probability  ${face.rightEyeOpenProbability}"))
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
