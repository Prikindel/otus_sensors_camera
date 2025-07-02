package com.example.myapplication

import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

private const val REQUEST_PERMISSION = 1

class MainActivity : AppCompatActivity() {

    private val takePhotoLauncher: ActivityResultLauncher<*> =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let(::showBitmap)
        }
    private lateinit var imageCapture: ImageCapture

    private lateinit var videoCapture: VideoCapture<Recorder>
    private var currentRecording: Recording? = null

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private var isTookPhoto = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.takePhotoButton.setOnClickListener {
            when {
                isTookPhoto -> showPreview()
                currentRecording == null -> startRecording()
                currentRecording != null -> stopRecording()
                else -> takePhoto()
            }
//            if (isTookPhoto) {
//                showPreview()
//            } else {
//                takePhoto()
//            }
//            takePhotoLauncher.launch(null)
        }

        doWithCameraStream()
    }

    override fun onResume() {
        super.onResume()
        if (!isPermissionGranted()) {
            requestPermission()
        }
    }

    private fun doWithCameraStream() {
        val cameraProvider = ProcessCameraProvider.getInstance(this)

        cameraProvider.addListener({
            val camera = cameraProvider.get()
            val preview = Preview.Builder()
                .build()

//            imageCapture = ImageCapture.Builder()
//                .build()

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            preview.setSurfaceProvider(findViewById<PreviewView>(R.id.image_preview).surfaceProvider)

//            camera.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            camera.bindToLifecycle(this, cameraSelector, preview, videoCapture)

        }, ContextCompat.getMainExecutor(this))
    }

    private fun startRecording() {
        val outputDirectory = getOutputDirectory()
        val videoFile = File(
            outputDirectory,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault())
                .format(System.currentTimeMillis()) + ".mp4"
        )

        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        binding.takePhotoButton.text = getString(R.string.stop_recording)

        currentRecording = videoCapture.output
            .prepareRecording(this, outputOptions)
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        // Начало записи видео
                        Log.d("CameraX", "Recording started")
                    }
                    is VideoRecordEvent.Finalize -> {
                        // Завершение записи видео
                        if (!recordEvent.hasError()) {
                            Log.d("CameraX", "Video saved successfully: ${recordEvent.outputResults.outputUri}")
                        } else {
                            Log.e("CameraX", "Video recording error: ${recordEvent.error}")
                        }
                        currentRecording?.close()
                        currentRecording = null

                        showVideoUri(recordEvent.outputResults.outputUri)
                    }
                }
            }
    }

    private fun stopRecording() {
        currentRecording?.stop()
    }

    private fun showPreview() {
        isTookPhoto = false
        with(binding) {
            image.visibility = View.GONE
            videoView.visibility = View.GONE
            imagePreview.visibility = View.VISIBLE

            takePhotoButton.text = getString(R.string.take_photo)
        }
    }

    private fun showVideoUri(uri: Uri) {
        isTookPhoto = true
        with(binding) {
            videoView.setVideoURI(uri)

            videoView.visibility = View.VISIBLE
            imagePreview.visibility = View.GONE

            takePhotoButton.text = getString(R.string.retake_photo)

            videoView.start()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf("android.permission.CAMERA"), REQUEST_PERMISSION)
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") == PERMISSION_GRANTED
    }

    private fun showBitmap(bitmap: Bitmap) {
        binding.image.setImageBitmap(bitmap)
    }

    private fun showImageUri(uri: Uri) {
        isTookPhoto = true
        with(binding) {
            image.setImageURI(uri)

            image.visibility = View.VISIBLE
            imagePreview.visibility = View.GONE

            takePhotoButton.text = getString(R.string.retake_photo)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    private fun takePhoto() {
        val outputDirectory = getOutputDirectory()
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault())
                .format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // Обработка сохраненного снимка
                    // Здесь вы можете выполнить любые действия после сохранения изображения
                    // Например, можно отобразить сообщение пользователю или открыть снимок в другом экране
                    val savedUri = outputFileResults.savedUri

                    // Пример: запуск новой активности для отображения сохраненного изображения
                    // val intent = Intent(this@MainActivity, DisplayImageActivity::class.java).apply {
                    //     putExtra("image_uri", savedUri.toString())
                    // }
                    // startActivity(intent)

                    savedUri?.let(::showImageUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    // Обработка ошибок
                }
            }
        )
    }

}