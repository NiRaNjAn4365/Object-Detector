package com.example.objectdetector.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import androidx.camera.core.ImageProxy


fun Bitmap.centreCrop(desiredWidth:Int,desiredHeight:Int):Bitmap{
    val xStart=(width-desiredWidth)/2
    val yStart=(height-desiredHeight)/2

    if(xStart<0 || yStart<0 || desiredWidth>width || desiredHeight>height){
        throw IllegalArgumentException("Invalid arguments Passed to centreCrop()")
    }

    return Bitmap.createBitmap(this,xStart,yStart,desiredWidth,desiredHeight)

}

fun ImageProxy.toLitmap(context: Context): Bitmap {
    if (format != ImageFormat.YUV_420_888) {
        throw IllegalArgumentException("Unsupported image format")
    }

    val converter = YuvToRgbConverter(context)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    converter.yuvToRgb(this, bitmap)
    return bitmap
}
