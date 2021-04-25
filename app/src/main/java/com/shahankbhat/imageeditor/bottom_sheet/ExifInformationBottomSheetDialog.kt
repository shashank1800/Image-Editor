package com.shahankbhat.imageeditor.bottom_sheet

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.library.baseAdapters.BR
import androidx.exifinterface.media.ExifInterface
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.shahankbhat.imageeditor.R
import com.shahankbhat.imageeditor.databinding.AdapterExifInfoBinding
import com.shahankbhat.imageeditor.databinding.BottomSheetExifInformationBinding
import com.shahankbhat.imageeditor.model.ExifInfoModel
import com.shahankbhat.recyclergenericadapter.RecyclerGenericAdapter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ExifInformationBottomSheetDialog(private val exifInterface: ExifInterface) :
    BottomSheetDialogFragment() {

    lateinit var binding: BottomSheetExifInformationBinding

    lateinit var adapter: RecyclerGenericAdapter<AdapterExifInfoBinding, ExifInfoModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetExifInformationBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerAdapter()

        val testModelList = ArrayList<ExifInfoModel>()

        val date = exifInterface.getAttribute(ExifInterface.TAG_DATETIME)?.let {
            val dateFormatBefore = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
            val dateFormatAfter = SimpleDateFormat("dd MMM yyyy hh:mm aa", Locale.getDefault())
            dateFormatAfter.format(dateFormatBefore.parse(it)!!)
        }


        val model = exifInterface.getAttribute(ExifInterface.TAG_MODEL)
        val make = exifInterface.getAttribute(ExifInterface.TAG_MAKE)

        val focalLengthString = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)
        val isoString = exifInterface.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY)
        val apertureString = exifInterface.getAttribute(ExifInterface.TAG_APERTURE_VALUE)
        val exposureTimeString = exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)
        val shutterSpeedString = exifInterface.getAttribute(ExifInterface.TAG_SHUTTER_SPEED_VALUE)

        testModelList.add(
            ExifInfoModel(
                "Taken on : ",
                date.toString()
            )
        )
        testModelList.add(ExifInfoModel("Model : ", "$model, $make"))
        testModelList.add(ExifInfoModel("Focal length : ", "$focalLengthString"))
        testModelList.add(ExifInfoModel("Aperture : ", "$apertureString"))
        testModelList.add(ExifInfoModel("Exposure time : ", "$exposureTimeString"))
        testModelList.add(ExifInfoModel("Shutter speed : ", "$shutterSpeedString"))
        testModelList.add(ExifInfoModel("ISO : ", "$isoString"))
        adapter.submitList(testModelList)
    }

    private fun initRecyclerAdapter() {
        val linearLayoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.rvList.layoutManager = linearLayoutManager
        adapter = RecyclerGenericAdapter(
            R.layout.adapter_exif_info,
            BR.model
        )
        binding.rvList.adapter = adapter
    }

}