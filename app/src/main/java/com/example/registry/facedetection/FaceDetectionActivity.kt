package com.example.registry.facedetection

import android.annotation.SuppressLint
import android.graphics.*
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.registry.R
import com.example.registry.upload.Upload
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.otaliastudios.cameraview.*
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
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_DCIM + "/FaceDetected/")
        // create storage directories, if they don't exist
        storageDir?.mkdirs()
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val filename = "FaceBy$timeStamp"
        try {
            val file = File.createTempFile(
                filename, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
            )
            val fileURI = Uri.fromFile(file)
            val fileOutputStream = FileOutputStream(file)
            val bos = BufferedOutputStream(fileOutputStream)
            imageData.compress(Bitmap.CompressFormat.JPEG, 100, bos)
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
            return
        } catch (e: FileNotFoundException) {
            Log.w("TAG", "Error saving image file: " + e.message)
        } catch (e: IOException) {
            Log.w("TAG", "Error saving image file: " + e.message)
        }
        return
    }

    override fun process (frame: Frame) {

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

                when {
                    it.size == 1 -> findViewById<View>(R.id.goodMorning).visibility = View.VISIBLE
                    it.size != 1 -> hideProgress()
                }

                val bitmap = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                val dotPaint = Paint()
                dotPaint.color = Color.GREEN
                dotPaint.style = Paint.Style.FILL
                dotPaint.strokeWidth = 4F
                val linePaint = Paint()
                linePaint.color = Color.WHITE
                linePaint.style = Paint.Style.STROKE
                linePaint.strokeWidth = 2F

                for (face in it) {
                    val one = face.getContour(FirebaseVisionFaceContour.FACE).points[0]
                    val two = face.getContour(FirebaseVisionFaceContour.FACE).points[9]
                    val three = face.getContour(FirebaseVisionFaceContour.FACE).points[18]
                    val four = face.getContour(FirebaseVisionFaceContour.FACE).points[27]

//                    //Верхняя
//                    canvas.drawPoint(one.x, one.y, dotPaint)
//                    //Левая
//                    canvas.drawPoint(two.x, two.y, dotPaint)
//                    //Нижняя
//                    canvas.drawPoint(three.x, three.y, dotPaint)
//                    //Правая
//                    canvas.drawPoint(four.x, four.y, dotPaint)

                    // внешний квадрат
                    drawSquare(linePaint, canvas)

//                    //внутренний квадрат
//                    canvas.drawLine(790F, 340F, 440F, 340F, linePaint)
//                    canvas.drawLine(440F, 340F, 440F, 930F, linePaint)
//                    canvas.drawLine(440F, 930F, 790F, 930F, linePaint)
//                    canvas.drawLine(790F, 930F, 790F, 340F, linePaint)

                    if (one.y in 140F..340F && two.x in 790F..940F && three.y in 930F..1130F && four.x in 290F..440F) {
                        youCan()
                        bottomSheetButton.setOnClickListener {
                            cameraView.capturePicture()
                            Toast.makeText(this, "Идет загрузка", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                    }
                    else {
                        youCant()
                        linePaint.color = Color.RED
                        drawSquare(linePaint, canvas)
                    }
                    if (cameraFacing == Facing.FRONT) {
                        val matrix = Matrix()
                        matrix.preScale(-1F, 1F)
                        val flippedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                        face_detection_camera_image_view.setImageBitmap(flippedBitmap)
                    } else {
                        face_detection_camera_image_view.setImageBitmap(bitmap)
                    }
                }
            }
            .addOnFailureListener {
                face_detection_camera_image_view.setImageBitmap(null)
            }
        return
    }


//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//            val result = CropImage.getActivityResult(data)
//
//            if (resultCode == Activity.RESULT_OK) {
//                val imageUri = result.uri
//                analyzeImage(MediaStore.Images.Media.getBitmap(contentResolver, imageUri))
//                face_detection_camera_container.visibility = View.GONE
//            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                Toast.makeText(this, "There was some error : ${result.error.message}", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

    private fun drawSquare (linePaint : Paint, canvas: Canvas){
        canvas.drawLine(940F, 140F, 290F, 140F, linePaint)
        canvas.drawLine(290F, 140F, 290F, 1130F, linePaint)
        canvas.drawLine(290F, 1130F, 940F, 1130F, linePaint)
        canvas.drawLine(940F, 1130F, 940F, 140F, linePaint)
        return
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

                // показывает результат фото (людям лучше не видеть себя)
                // imageView.setImageBitmap(mutableImage)
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
        } else if (faces.size == 1) {
            storeImage(image)
            return
        }
        else if (faces.size != 1) {
            hideProgress()
            return
        }
    }

//    private fun showProgress() {
//        findViewById<View>(R.id.bottom_sheet_button_image).visibility = View.GONE
//        findViewById<View>(R.id.bottom_sheet_button_progress).visibility = View.VISIBLE
//        findViewById<View>(R.id.youCan).visibility = View.GONE
//        findViewById<View>(R.id.youCant).visibility = View.GONE
//        bottomSheetButton.setOnClickListener {
//            Toast.makeText(this, "Подождите, идет загрузка", Toast.LENGTH_SHORT).show()
//        }
//    }

    private fun hideProgress() {
        findViewById<View>(R.id.bottom_sheet_button_image).visibility = View.VISIBLE
        findViewById<View>(R.id.bottom_sheet_button_progress).visibility = View.GONE
        findViewById<View>(R.id.youCan).visibility = View.GONE
        findViewById<View>(R.id.youCant).visibility = View.GONE
        findViewById<View>(R.id.goodMorning).visibility = View.GONE
        bottomSheetButton.setOnClickListener {
            Toast.makeText(this, "Не видно вашего лица или несколько лиц", Toast.LENGTH_SHORT).show()
        }
        return
    }

    private fun youCan(){
        findViewById<View>(R.id.bottom_sheet_button_image).visibility = View.VISIBLE
        findViewById<View>(R.id.bottom_sheet_button_progress).visibility = View.GONE
        findViewById<View>(R.id.youCan).visibility = View.VISIBLE
        findViewById<View>(R.id.youCant).visibility = View.GONE
        return
    }

    private fun youCant(){
        findViewById<View>(R.id.bottom_sheet_button_image).visibility = View.GONE
        findViewById<View>(R.id.bottom_sheet_button_progress).visibility = View.VISIBLE
        findViewById<View>(R.id.youCant).visibility = View.VISIBLE
        findViewById<View>(R.id.youCan).visibility = View.GONE
        bottomSheetButton.setOnClickListener {
            Toast.makeText(this, "Встаньте сначала прямо", Toast.LENGTH_SHORT).show()
        }
        return
    }
}