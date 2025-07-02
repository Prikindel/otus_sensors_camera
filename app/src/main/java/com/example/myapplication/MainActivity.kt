package com.example.myapplication

import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivityMainBinding

private const val REQUEST_PERMISSION = 1

class MainActivity : AppCompatActivity() {

    private val takePhotoLauncher: ActivityResultLauncher<*> =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let(::showBitmap)
        }

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.takePhotoButton.setOnClickListener {
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

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            preview.setSurfaceProvider(findViewById<PreviewView>(R.id.image_preview).surfaceProvider)

            camera.bindToLifecycle(this, cameraSelector, preview)


        }, ContextCompat.getMainExecutor(this))
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}