package com.example.myapplication

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), SensorEventListener {

    lateinit var sensorManager: SensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        showLightSensor()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun showLightSensor() {
        sensorManager = getSystemService(SensorManager::class.java)

        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            Log.e("SENSORS", "Sensor not found")
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        Log.d("SENSORS", "onSensorChanged: ${event.values.map { it.toString() }.joinToString(", ")}")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d("SENSORS", "onAccuracyChanged:  $accuracy")
    }

    private fun showDeviceSensors() {
        val sensorManager = getSystemService(SensorManager::class.java)
        val sensors = sensorManager.getSensorList(Sensor.TYPE_LIGHT)

        Log.d("SENSORS", "List:  $sensors ")
    }

}