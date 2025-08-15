package com.example.myapplication;

import android.app.Activity.RESULT_OK
import android.app.Notification
import android.app.Service;
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.ScreenCaptureManager.saveBitmap

class ScreenCaptureService : Service() {
    // This service will handle the screen capture functionality
    // You can implement the logic to start capturing the screen here
    // For example, using MediaProjectionManager to capture the screen
    // and save it to a file or stream it somewhere.


    companion object {
        private var mediaProjectionIntent: Intent? = null
        private var mediaProjectionResultCode: Int = 0
        private const val NOTIFICATION_ID = 101

        /**
         * Entry point for external callers to start the screen capture service.
         * It stores the MediaProjection permission intent and launches the service.
         */
        fun startCaptureService(context: Context, projectionIntent: Intent, resultCode: Int) {
            mediaProjectionIntent = projectionIntent
            mediaProjectionResultCode = resultCode
            val serviceIntent = Intent(context, ScreenCaptureService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }

    private val notificationId = 1;
    override fun onBind(intent: android.content.Intent?) = null

    // Implement other necessary methods for the service lifecycle

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        // Handle the start command for the service
        // You can start capturing the screen here
        startForeground(notificationId,createCaptureNotification())
        startScreenCapture()
        return START_STICKY
    }

    private fun startScreenCapture() {
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val mediaProjection: MediaProjection =
            projectionManager.getMediaProjection(mediaProjectionResultCode, mediaProjectionIntent!!)
        ScreenCaptureManager.capture(this, mediaProjection){ bitmap ->
            android.widget.Toast.makeText(this, "Bitmap ready", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun createCaptureNotification(): Notification? {
        return NotificationUtils.createMediaProjectionNotification(this)
    }

}
