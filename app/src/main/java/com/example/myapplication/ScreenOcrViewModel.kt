package com.example.myapplication

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel

class ScreenOcrViewModel : ViewModel() {

    fun isProjectionPermissionGranted(resultCode: Int, resultIntent: Intent? ): Boolean {
        return resultIntent != null && resultCode == Activity.RESULT_OK
    }

    fun shouldRequestPermission(hasPermission: Boolean): Boolean = !hasPermission

    fun startScreenCapture() {
    }
}