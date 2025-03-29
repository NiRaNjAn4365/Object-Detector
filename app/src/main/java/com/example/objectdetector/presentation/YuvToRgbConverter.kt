package com.example.objectdetector.presentation
import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import android.graphics.ImageFormat
import android.graphics.YuvImage
import java.io.ByteArrayOutputStream
import android.graphics.BitmapFactory

class YuvToRgbConverter(private val context: Context) {

    fun yuvToRgb(image: ImageProxy, bitmap: Bitmap) {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(android.graphics.Rect(0, 0, image.width, image.height), 100, out)
        val imageBytes = out.toByteArray()

        val tempBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            ?: throw NullPointerException("Bitmap conversion failed")

        // Copy to the provided bitmap
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawBitmap(tempBitmap, 0f, 0f, null)
    }
}