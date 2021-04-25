package com.shahankbhat.imageeditor.util

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.NonNull
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.shahankbhat.imageeditor.ui.MainActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

@Throws(IOException::class)
fun Context.saveBitmapToStorage(bitmap: Bitmap, @NonNull name: String) {

    val saved: Boolean
    var fos: OutputStream? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = applicationContext.contentResolver
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        contentValues.put(
            MediaStore.MediaColumns.RELATIVE_PATH,
            "DCIM/${MainActivity.IMAGES_FOLDER_NAME}"
        )
        val imageUri =
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        fos = resolver.openOutputStream(imageUri!!)!!
    } else {
        val imagesDir = (Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DCIM
        ).toString() + File.separator + MainActivity.IMAGES_FOLDER_NAME)
        val file = File(imagesDir)
        if (!file.exists()) {
            file.mkdir()
        }
        val image = File(imagesDir, "$name.png")
        fos = FileOutputStream(image)
    }

    saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
    fos?.flush()
    fos?.close()

    if (saved)
        Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show()

}