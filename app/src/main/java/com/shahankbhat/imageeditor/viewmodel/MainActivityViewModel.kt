package com.shahankbhat.imageeditor.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    var imageUri: MutableLiveData<Uri> = MutableLiveData()

    var isImageAvailable = ObservableBoolean(false)
    var isCropEnabled = ObservableBoolean(false)

    val isDarkModeEnabled = ObservableBoolean(false)

}
