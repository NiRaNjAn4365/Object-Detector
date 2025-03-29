package com.example.objectdetector.data

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.Surface
import com.example.objectdetector.domain.Detection
import com.example.objectdetector.domain.ObjectClassifier
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions
import org.tensorflow.lite.task.vision.detector.ObjectDetector


class TfLiteObjectDetetctor(
    private val context: Context,
    private val threshold: Float = 0.5f,
    private val maxResults: Int = 10
) : ObjectClassifier {

    private var detector: ObjectDetector? = null
    private var labels: List<String> = loadLabels()

    private fun setDetector() {
        val baseOptions = BaseOptions.builder()
            .setNumThreads(2)
            .build()
        val options = ObjectDetector.ObjectDetectorOptions.builder()
            .setBaseOptions(baseOptions)
            .setScoreThreshold(threshold)
            .setMaxResults(maxResults)
            .build()

        try {
            detector = ObjectDetector.createFromFileAndOptions(
                context,
                "ssd_mobile.tflite",
                options
            )
        } catch (e: Exception) {
            Log.d("Error message", e.toString())
        }
    }

    private fun loadLabels(): List<String> {
        return try {
            context.assets.open("labels.txt").bufferedReader().use { it.readLines() }
        } catch (e: Exception) {
            Log.e("TfLiteObjectDetector", "Error loading labels: ${e.localizedMessage}")
            emptyList()
        }
    }

    override fun classify(bitmap: Bitmap, rotation: Int): List<Detection> {
        if (detector == null) {
            setDetector()
        }

        if (labels.isEmpty()) {
            Log.e("TfLiteObjectDetector", "No labels found. Ensure labels.txt is in assets.")
            return emptyList()
        }

        val imageProcessor = ImageProcessor.Builder().build()
        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))
        val processedImage = ImageProcessingOptions.builder().setOrientation(getOrientationResults(rotation)).build()

        val results = detector?.detect(tensorImage, processedImage)
        return results?.mapNotNull { detection ->
            detection.categories.firstOrNull()?.let { category ->
                val labelIndex = category.index
                val label = if (labelIndex in labels.indices) labels[labelIndex] else "Unknown"
                Detection(name = label, score = category.score, boundingBox = detection.boundingBox)
            }
        } ?: emptyList()
    }

    private fun getOrientationResults(rotation: Int): ImageProcessingOptions.Orientation {
        return when (rotation) {
            Surface.ROTATION_270 -> ImageProcessingOptions.Orientation.BOTTOM_RIGHT
            Surface.ROTATION_90 -> ImageProcessingOptions.Orientation.TOP_LEFT
            Surface.ROTATION_180 -> ImageProcessingOptions.Orientation.RIGHT_BOTTOM
            else -> ImageProcessingOptions.Orientation.RIGHT_TOP
        }
    }
}