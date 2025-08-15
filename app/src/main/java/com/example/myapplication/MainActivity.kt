package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.media.projection.MediaProjectionManager
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var resultIntent: Intent? = null
    private var resultCode : Int = 0

    private lateinit var screenCaptureLauncher : ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        registerScreenCaptureLauncher()
        requestScreenCapturePermission()
        findViewById<Button>(R.id.buttonCaptureSS).setOnClickListener {
            if(resultIntent == null) {
                requestScreenCapturePermission()
            }
            else
            {
                captureAndDoOCR();
            }
        }
    }

    private fun registerScreenCaptureLauncher() {
        screenCaptureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                this.resultIntent = result.data
                this.resultCode = result.resultCode
                android.widget.Toast.makeText(this, "Permission granted", android.widget.Toast.LENGTH_SHORT).show()
                ScreenCaptureService.startCaptureService(this, result.data!!, resultCode)
            } else {
                android.widget.Toast.makeText(this, "Permission not granted", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun requestScreenCapturePermission() {
        val permissionIntent = mediaProjectionManager.createScreenCaptureIntent()
        screenCaptureLauncher.launch(permissionIntent)
    }

    fun captureAndDoOCR() {
        val projection = mediaProjectionManager.getMediaProjection(resultCode, resultIntent!!)
        ScreenCaptureManager.capture(this, projection) { bitmap ->
            // Handle the captured bitmap, e.g., perform OCR
            // For example, you can pass it to an OCR library or save it
            android.widget.Toast.makeText(this, "Time to do OCR", android.widget.Toast.LENGTH_SHORT).show()
            OCRProcessor.recognizeText(bitmap, onTextExtracted =  { text ->
                android.widget.Toast.makeText(this, "OCR Result: $text", android.widget.Toast.LENGTH_LONG).show()
            },onError = { e ->
                android.widget.Toast.makeText(this, "OCR Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            })

        }

    }
}