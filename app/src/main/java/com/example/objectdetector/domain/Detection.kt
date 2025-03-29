package com.example.objectdetector.domain

import android.graphics.RectF

data class Detection(
    val name:String,
    val score:Float,
    val boundingBox: RectF
) {
}