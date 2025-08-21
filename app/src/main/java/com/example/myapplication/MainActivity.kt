package com.example.myapplication

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "MainActivity"
    }
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var resultIntent: Intent? = null
    private var resultCode: Int = RESULT_CANCELED

    private lateinit var screenCaptureLauncher: ActivityResultLauncher<Intent>
    private val viewModel : ScreenOcrViewModel by viewModels()

    private var permissionDialog : AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        initializeTimerDropdown()

        registerScreenCaptureLauncher()
//        requestScreenCapturePermission()
        findViewById<Button>(R.id.buttonCaptureSS).setOnClickListener {
            val hasPermission = viewModel.isProjectionPermissionGranted(resultCode, resultIntent)
            if (viewModel.shouldRequestPermission(hasPermission)) {
                showPermissionInfoAndRequest()
            }
        }
    }

    private fun initializeTimerDropdown() {
        val dropDown = findViewById<AutoCompleteTextView>(R.id.dropdownOcrInterval)
        val labels = resources.getStringArray(R.array.ocr_interval_labels)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, labels)
        dropDown.setAdapter(adapter)
        dropDown.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> stopOcrInterval()
                1 -> startOcrInterval(10_000)
                2 -> startOcrInterval(30_000)
                3 -> startOcrInterval(60_000)
                4 -> startOcrInterval(5 * 60_000)
            }
        }
    }

    private fun registerScreenCaptureLauncher() {
        screenCaptureLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (viewModel.isProjectionPermissionGranted(result.resultCode, result.data)) {
                    this.resultIntent = result.data
                    this.resultCode = result.resultCode
                    viewModel.startScreenCapture();
                    android.widget.Toast.makeText(
                        this,
                        "Permission granted",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    android.widget.Toast.makeText(
                        this,
                        "Permission not granted",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    fun requestScreenCapturePermission() {
        val permissionIntent = mediaProjectionManager.createScreenCaptureIntent()
        screenCaptureLauncher.launch(permissionIntent)
    }


    fun startOcrInterval(interval: Long) {
        if (resultIntent == null || resultCode != RESULT_OK) {
            android.widget.Toast.makeText(this, "Requesting screen capture permission…", android.widget.Toast.LENGTH_SHORT).show()
            requestScreenCapturePermission()
            return
        }

        val serviceIntent = Intent(this, OcrForegroundService::class.java).apply {
            putExtra(OcrForegroundService.EXTRA_INTERVAL_MS, interval)
            putExtra(OcrForegroundService.EXTRA_RESULT_CODE, resultCode)
            putExtra(OcrForegroundService.EXTRA_RESULT_INTENT, resultIntent)
        }
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    fun stopOcrInterval() {
        stopService(Intent(this, OcrForegroundService::class.java))
    }

    private fun showPermissionInfoAndRequest() {
        permissionDialog?.dismiss()
        permissionDialog = AlertDialog.Builder(this)
            .setTitle("Allow screen capture")
            .setMessage("We’ll ask Android for permission to capture your screen so we can run OCR. No images leave your device.")
            .setPositiveButton("Continue") { _, _ -> requestScreenCapturePermission() }
            .setNegativeButton("Not now", null).create()
        permissionDialog?.show()
    }

}