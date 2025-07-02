package com.example.myapplication

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMainBinding

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
            takePhotoLauncher.launch(null)
        }
    }

    private fun showBitmap(bitmap: Bitmap) {
        binding.image.setImageBitmap(bitmap)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}