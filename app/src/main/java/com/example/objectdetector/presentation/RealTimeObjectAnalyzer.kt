package com.example.objectdetector.presentation

import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.objectdetector.domain.Detection
import com.example.objectdetector.domain.ObjectClassifier


class RealTimeObjectAnalyzer(
    private val context: Context,
    private val objectClassifier: ObjectClassifier,
    private val onResult: (List<Detection>) -> Unit
) : ImageAnalysis.Analyzer {

    private var frameBuffer = 0

    override fun analyze(image: ImageProxy) {
        try {
            // Adjust frame processing frequency
            if (frameBuffer % 5 == 0) {
                val imageRotation = image.imageInfo.rotationDegrees
                val bitmap = image.toLitmap(context)?.centreCrop(320, 320)

                bitmap?.let {
                    val results = objectClassifier.classify(it, imageRotation)
                    onResult(results)
                }
            }
            frameBuffer = (frameBuffer + 1) % 1000 // Reset to prevent overflow
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            image.close()
        }
    }
}