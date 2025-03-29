package com.example.objectdetector.domain

import android.graphics.Bitmap

interface ObjectClassifier {
    fun classify(bitmap: Bitmap,rotation:Int):List<Detection>
}