package com.example.myapplication

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat

class OcrForegroundService : Service() {
    companion object {
        const val CHANNEL_ID = "OCR_FOREGROUND_CHANNEL"
        const val INTERVAL_MS = 10000L
        const val EXTRA_INTERVAL_MS = "extra_interval_ms"
        const val EXTRA_RESULT_INTENT = "extra_result_intent"
        const val EXTRA_RESULT_CODE = "extra_result_code"
    }

    private var resultIntent: Intent? = null
    private var resultCode: Int = 0
    private lateinit var mediaProjectionManager: MediaProjectionManager

    private var intervalMs = 10_000L
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run(){
            captureAndDoOCR()
            handler.postDelayed(this, INTERVAL_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel();
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "OCR Foreground Service"
            val descriptionText = "Channel for OCR foreground service"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    fun playRingerSound(context: Context) {
        val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val ringtone: Ringtone = RingtoneManager.getRingtone(context, notification)
        ringtone.play()
        // Stop after 5 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            if (ringtone.isPlaying) {
                ringtone.stop()
            }
        }, 5000) //
    }
    private fun captureAndDoOCR() {
        // Placeholder for actual OCR work.
        mediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val projection = mediaProjectionManager.getMediaProjection(resultCode, resultIntent!!)
        ScreenCaptureManager.capture(this, projection) { bitmap ->
            OCRProcessor.recognizeText(bitmap, onTextExtracted = { text ->

                val res = text.contains("FOOD") && text.contains("RESCUE");

                if(res)
                {
                    playRingerSound(this)
                    android.widget.Toast.makeText(
                        this,
                        "Food rescue",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }


            }, onError = { e ->
                android.widget.Toast.makeText(
                    this,
                    "OCR Error: ${e.message}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            })

        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intervalMs = intent?.getLongExtra(EXTRA_INTERVAL_MS, intervalMs) ?: intervalMs
        resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, resultCode) ?: resultCode
        resultIntent = intent?.getParcelableExtra<Intent>(EXTRA_RESULT_INTENT)


        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Auto OCR Running")
            .setContentText("Running ocr every ${INTERVAL_MS/1000}} seconds")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .build()
        startForeground(1, notification)
        handler.post(runnable)
        return START_STICKY
    }

    override fun onBind(intent: android.content.Intent?) = null

    override fun onDestroy() {
        handler.removeCallbacks(runnable)
        super.onDestroy()
    }
}