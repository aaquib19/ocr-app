package com.example.myapplication

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel

class ScreenOcrViewModel : ViewModel() {

    fun isProjectionPermissionGranted(resultCode: Int, resultIntent: Intent? ): Boolean {
        return resultCode == Activity.RESULT_OK && resultIntent != null
    }

    fun startScreenCapture() {
    }
}