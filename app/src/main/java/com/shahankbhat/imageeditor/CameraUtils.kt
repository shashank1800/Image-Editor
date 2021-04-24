package com.shahankbhat.imageeditor

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.text.format.Time
import android.widget.Toast
import java.io.File

object CameraUtils {

    fun generateTimeStampPhotoFileUri(context: Context): Uri? {
        var photoFileUri: Uri? = null
        val outputDir = getPhotoDirectory(context)
        if (outputDir != null) {
            val photoFile = File(
                outputDir, System.currentTimeMillis()
                    .toString() + ".png"
            )
            photoFileUri = Uri.fromFile(photoFile)
        }
        return photoFileUri
    }

    private fun getPhotoDirectory(context: Context): File? {
        var outputDir: File? = null
        val externalStorageState = Environment.getExternalStorageState()
        if (externalStorageState == Environment.MEDIA_MOUNTED) {
            val photoDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            outputDir = File(photoDir, context.resources.getString(R.string.app_name))
            if (!outputDir.exists()) if (!outputDir.mkdirs()) {
                Toast.makeText(
                    context, "Failed to create directory "
                            + outputDir.absolutePath,
                    Toast.LENGTH_SHORT
                ).show()
                outputDir = null
            }
        }
        return outputDir
    }
}