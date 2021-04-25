package com.shahankbhat.imageeditor.bottom_sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.shahankbhat.imageeditor.databinding.BottomSheetAppInformationBinding

class AppInformationBottomSheetDialog :
    BottomSheetDialogFragment() {

    lateinit var binding: BottomSheetAppInformationBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetAppInformationBinding.inflate(inflater, container, false)
        return binding.root
    }

}