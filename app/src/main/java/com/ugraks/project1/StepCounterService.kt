package com.ugraks.project1

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager // Bu satır eklendi


class StepCounterService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var initialStep = -1
    private var stepCount = 0
    private lateinit var vibrator: Vibrator
    private lateinit var sharedPreferences: android.content.SharedPreferences
    private var isStarted = false

    companion object {
        const val ACTION_START = "com.ugraks.project1.ACTION_START"
        const val ACTION_STOP = "com.ugraks.project1.ACTION_STOP"
        const val ACTION_RESET = "com.ugraks.project1.ACTION_RESET"
        const val STEP_COUNT_KEY = "step_count"
        const val IS_STARTED_KEY = "is_started"
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) // Trying STEP_DETECTOR
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        sharedPreferences = getSharedPreferences("step_counter_prefs", Context.MODE_PRIVATE)
        loadState()
    }

    private fun loadState() {
        stepCount = sharedPreferences.getInt(STEP_COUNT_KEY, 0)
        isStarted = sharedPreferences.getBoolean(IS_STARTED_KEY, false)
        if (isStarted && stepSensor != null) {
            startListening()
        }
    }

    private fun saveState() {
        sharedPreferences.edit()
            .putInt(STEP_COUNT_KEY, stepCount)
            .putBoolean(IS_STARTED_KEY, isStarted)
            .apply()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                if (stepSensor != null) {
                    startListening()
                    isStarted = true
                    saveState()
                } else {
                    Toast.makeText(this, "Adım dedektörü sensörü bulunamadı", Toast.LENGTH_LONG).show()
                    stopSelf()
                }
            }
            ACTION_STOP -> {
                stopListening()
                isStarted = false
                saveState()
                // stopSelf() // İhtiyaca göre açılabilir
            }
            ACTION_RESET -> {
                stepCount = 0
                initialStep = -1
                isStarted = false
                saveState()
                vibrate(200L)
                stopListening() // Reset sonrası dinlemeyi durdur
                // stopSelf() // İhtiyaca göre açılabilir
            }
        }
        return START_STICKY // Uygulama kapatılırsa servis yeniden başlatılır
    }

    private fun startListening() {
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI) // VEYA SensorManager.SENSOR_DELAY_GAME

        } else {
            Toast.makeText(this, "Adım dedektörü bulunamadığı için dinleme başlatılamadı", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopListening() {
        sensorManager.unregisterListener(this)

    }

    private fun vibrate(duration: Long = 100L) {
        if (vibrator.hasVibrator()) {
            val effect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
            stepCount++
            saveState()
            val intent = Intent("STEP_COUNT_UPDATED")
            intent.putExtra("step_count", stepCount)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? {
        return null // Bağlanma gerekmeyecek
    }

    override fun onDestroy() {
        super.onDestroy()
        stopListening()
    }
}