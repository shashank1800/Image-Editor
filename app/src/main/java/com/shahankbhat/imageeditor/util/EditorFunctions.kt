package com.shahankbhat.imageeditor.util

import android.graphics.Bitmap
import android.graphics.Matrix
import com.shahankbhat.imageeditor.ui.MainActivity.Companion.FLIP_HORIZONTAL
import com.shahankbhat.imageeditor.ui.MainActivity.Companion.FLIP_VERTICAL


object EditorFunctions {

    fun flipBitmap(src: Bitmap, type: Int): Bitmap? {
        val matrix = Matrix()
        when (type) {
            FLIP_HORIZONTAL -> {
                matrix.preScale(1.0f, -1.0f)
            }
            FLIP_VERTICAL -> {
                matrix.preScale(-1.0f, 1.0f)
            }
            else -> {
                return src
            }
        }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }


    fun rotateBitmap(source: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
}