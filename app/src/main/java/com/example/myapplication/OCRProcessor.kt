package com.example.myapplication

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

object OCRProcessor {
    private val TAG: String = "OCRProcessor"

    fun recognizeText(
        bitmap: Bitmap,
        onTextExtracted: (String) -> Unit,
        onError: ((Exception) -> Unit)? = null
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                Log.d(TAG, "OCR success:\n${visionText.text}")
                onTextExtracted(visionText.text)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "OCR failed", e)
                onError?.invoke(e)
            }
    }
}