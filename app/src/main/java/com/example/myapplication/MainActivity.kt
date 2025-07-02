package com.example.myapplication

import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    lateinit var sensorManager: SensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        showDeviceSensors()
    }

    private fun showDeviceSensors() {
        val sensorManager = getSystemService(SensorManager::class.java)
        val sensors = sensorManager.getSensorList(Sensor.TYPE_LIGHT)

        Log.d("SENSORS", "List:  $sensors ")
    }

}