package com.example.objectdetector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.objectdetector.data.TfLiteObjectDetetctor
import com.example.objectdetector.domain.Detection
import com.example.objectdetector.presentation.CameraPreview
import com.example.objectdetector.presentation.RealTimeObjectAnalyzer
import com.example.objectdetector.ui.theme.ObjectDetectorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if(!hasPermisiions()){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),0)
        }
        setContent {
            var detectedResults by remember { mutableStateOf(emptyList<Detection>()) }
            val analyzer=
                RealTimeObjectAnalyzer(
                    context = applicationContext,
                    objectClassifier = TfLiteObjectDetetctor(context = applicationContext),
                    onResult = {
                        detectedResults =it
                    }
                )
            val controller= remember { LifecycleCameraController(applicationContext).apply {
                setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
                setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(applicationContext),analyzer)
            } }
            ObjectDetectorTheme {
            Box(modifier = Modifier.fillMaxSize()){
                CameraPreview(controller, modifier = Modifier.fillMaxSize())
                BoundingBoxOverlay(detections = detectedResults, imageWidth = 320, imageHeight = 320)
            }
            }
        }
    }

    private fun hasPermisiions():Boolean{
        return ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun BoundingBoxOverlay(detections: List<Detection>, imageWidth: Int, imageHeight: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        detections.forEach { detection ->
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.RED
                    strokeWidth = 8f
                    style = android.graphics.Paint.Style.STROKE
                }
                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 40f
                }

                val scaleX = size.width / imageWidth
                val scaleY = size.height / imageHeight

                val scaledBox = android.graphics.RectF(
                    detection.boundingBox.left * scaleX,
                    detection.boundingBox.top * scaleY,
                    detection.boundingBox.right * scaleX,
                    detection.boundingBox.bottom * scaleY
                )

                canvas.nativeCanvas.drawRect(scaledBox, paint)

                canvas.nativeCanvas.drawText(
                    "${detection.name} (${(detection.score * 100).toInt()}%)",
                    scaledBox.left.coerceAtLeast(0f),
                    (scaledBox.top - 10).coerceAtLeast(40f),
                    textPaint
                )
            }
        }
    }
}