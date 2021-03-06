package com.shahankbhat.imageeditor.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.exifinterface.media.ExifInterface
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.shahankbhat.imageeditor.PointXY
import com.shahankbhat.imageeditor.R
import com.shahankbhat.imageeditor.bottom_sheet.AppInformationBottomSheetDialog
import com.shahankbhat.imageeditor.bottom_sheet.ExifInformationBottomSheetDialog
import com.shahankbhat.imageeditor.databinding.ActivityMainBinding
import com.shahankbhat.imageeditor.deltaX
import com.shahankbhat.imageeditor.deltaY
import com.shahankbhat.imageeditor.util.EditorFunctions.flipBitmap
import com.shahankbhat.imageeditor.util.EditorFunctions.rotateBitmap
import com.shahankbhat.imageeditor.util.saveBitmapToStorage
import com.shahankbhat.imageeditor.viewmodel.MainActivityViewModel
import java.io.IOException
import kotlin.math.abs


class MainActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var selectedImageBitmap: Bitmap
    private lateinit var canvasBitmap: Bitmap
    private lateinit var exifInterface: ExifInterface
    private lateinit var sharedPreferences: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        binding.viewModel = viewModel

        viewModel.imageUri.observe(this, {
            it?.let {
                viewModel.isImageAvailable.set(true)
                binding.imageView.setImageURI(it)

                getExifInformation(it)
                selectedImageChanged()
                drawCropLine()
            }
        })

        initOnClickListener()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getExifInformation(uri: Uri?) {
        try {
            uri?.let { url ->
                val inOutStream = contentResolver.openInputStream(url)
                inOutStream?.let {
                    exifInterface = ExifInterface(it)
                }
            }
        } catch (ex: IOException) {
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onClick(v: View?) {
        when (v?.id) {

            binding.btnGallery.id -> {
                getContent.launch("image/*")
            }


            binding.btnFlipXAxis.id -> {
                val bitmap = (binding.imageView.drawable as BitmapDrawable).bitmap
                binding.imageView.setImageBitmap(flipBitmap(bitmap, FLIP_VERTICAL))
            }

            binding.btnFlipYAxis.id -> {
                val bitmap = (binding.imageView.drawable as BitmapDrawable).bitmap
                binding.imageView.setImageBitmap(flipBitmap(bitmap, FLIP_HORIZONTAL))

                selectedImageChanged()
            }

            binding.btnRotateLeft.id -> {
                val bitmap = (binding.imageView.drawable as BitmapDrawable).bitmap
                binding.imageView.setImageBitmap(rotateBitmap(bitmap, ROTATE_LEFT))
                selectedImageChanged()
            }

            binding.btnRotateRight.id -> {
                val bitmap = (binding.imageView.drawable as BitmapDrawable).bitmap
                binding.imageView.setImageBitmap(rotateBitmap(bitmap, ROTATE_RIGHT))

                selectedImageChanged()
            }

            binding.btnCrop.id -> {

                if (selectedImageBitmap.width > 200 && selectedImageBitmap.height > 200) {
                    selectedImageChanged()
                    drawCropLine()

                    viewModel.isCropEnabled.set(true)
                    binding.imageView.setOnTouchListener(this)
                } else {
                    Snackbar.make(binding.root, "Size of image is too less to crop", Snackbar.LENGTH_SHORT).show()
                }
            }

            binding.btnCropSubmit.id -> {

                val ratio = getRatioOfWH()
                if (ratio < 3) {
                    viewModel.isCropEnabled.set(false)
                    binding.imageView.setOnTouchListener(null)

                    if (left.toInt() < 0)
                        left = 0f
                    if (top.toInt() < 0)
                        top = 0f
                    if (right > selectedImageBitmap.width)
                        right = selectedImageBitmap.width.toFloat()
                    if (bottom > selectedImageBitmap.height)
                        bottom = selectedImageBitmap.height.toFloat()

                    selectedImageBitmap = Bitmap.createBitmap(
                        selectedImageBitmap,
                        left.toInt(),
                        top.toInt(),
                        (right - left).toInt(),
                        (bottom - top).toInt()
                    )

                    right = if (left > (selectedImageBitmap.width - MIN_WIDTH))
                        left + MIN_WIDTH
                    else
                        selectedImageBitmap.width - MIN_WIDTH

                    bottom = if (top > (selectedImageBitmap.height - MIN_WIDTH))
                        top + MIN_WIDTH
                    else
                        selectedImageBitmap.height - MIN_WIDTH

                    binding.imageView.setImageBitmap(selectedImageBitmap)
                } else {
                    Snackbar.make(binding.root, "Height to Width ratio should be below 3", Snackbar.LENGTH_SHORT).show()
                }
            }

            binding.btnDarkMode.id -> {
                viewModel.isDarkModeEnabled.set(false)
                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
                )
            }

            binding.btnLightMode.id -> {
                viewModel.isDarkModeEnabled.set(true)
                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
                )
            }

            binding.btnSave.id -> {

                runWithPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE){
                    saveBitmapToStorage(
                        selectedImageBitmap,
                        "Image-Editor-${System.currentTimeMillis()}"
                    )
                    viewModel.isImageAvailable.set(false)
                    viewModel.isCropEnabled.set(false)
                    viewModel.imageUri.value = null
                }

            }

            binding.btnInfo.id -> {
                val bottomSheet = ExifInformationBottomSheetDialog(exifInterface)
                bottomSheet.show(supportFragmentManager, bottomSheet.tag)
            }
        }
    }

    private fun getRatioOfWH(): Int {
        val width = abs(right - left)
        val height = abs(bottom - top)

        return if (width < height)
            (height / width).toInt()
        else (width / height).toInt()
    }


    private val getContent = registerForActivityResult(GetContent()) { uri: Uri? ->
        viewModel.imageUri.value = uri
    }

    private fun selectedImageChanged() {
        selectedImageBitmap = (binding.imageView.drawable as BitmapDrawable).bitmap
        canvasBitmap = Bitmap.createBitmap(
            selectedImageBitmap.width,
            selectedImageBitmap.height,
            Bitmap.Config.ARGB_8888
        )

        val smallerSide =
            if (selectedImageBitmap.width < selectedImageBitmap.height) selectedImageBitmap.width else selectedImageBitmap.height


        val smallerSideByThree = smallerSide / 3
        val centerX = selectedImageBitmap.width / 2
        val centerY = selectedImageBitmap.height / 2

        left = (centerX - smallerSideByThree).toFloat()
        right = (centerX + smallerSideByThree).toFloat()

        top = (centerY - smallerSideByThree).toFloat()
        bottom = (centerY + smallerSideByThree).toFloat()

    }

    private fun drawCropLine() {
        canvasBitmap = Bitmap.createBitmap(
            selectedImageBitmap.width,
            selectedImageBitmap.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(canvasBitmap)
        canvas.drawRect(0f, 0f, left, selectedImageBitmap.height.toFloat(), background)
        canvas.drawRect(left, 0f, selectedImageBitmap.width.toFloat(), top, background)
        canvas.drawRect(
            right,
            top,
            selectedImageBitmap.width.toFloat(),
            selectedImageBitmap.height.toFloat(),
            background
        )
        canvas.drawRect(left, bottom, right, selectedImageBitmap.height.toFloat(), background)

        val newBitmap = BitmapDrawable(Resources.getSystem(), canvasBitmap)
        binding.canvas.setImageBitmap(newBitmap.bitmap)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val view: ImageView = v as ImageView

        when (view.id) {
            binding.imageView.id -> when (event.action) {
                MotionEvent.ACTION_DOWN -> startEvent = PointXY(event.x, event.y)
                MotionEvent.ACTION_UP -> changeCoordinateWhichIsNear(PointXY(event.x, event.y))
            }
        }
        return true
    }

    private fun changeCoordinateWhichIsNear(endEvent: PointXY) {

        /** If deltaX greater than deltaY then
         *  intention is dragging line along x axis
         */
        if (startEvent.deltaX(endEvent) > startEvent.deltaY(endEvent)) {
            Log.i("drag", "x axis")

            val deltaLeft = abs(startEvent.x - left)
            val deltaRight = abs(startEvent.x - right)

            /** Check which line is nearer
             */
            if (deltaLeft < deltaRight) {
                val displacement = endEvent.x - startEvent.x
                if (left + MIN_WIDTH < (right - displacement))
                    left += displacement
                else
                    left = right - MIN_WIDTH
            } else {
                val displacement = startEvent.x - endEvent.x
                if (left + MIN_WIDTH < (right - displacement))
                    right -= displacement
                else
                    right = left + MIN_WIDTH

            }

        } else {
            Log.i("drag", "y axis")
            /** Check which line is nearer
             */

            val deltaTop = abs(startEvent.y - top)
            val deltaBottom = abs(startEvent.y - bottom)

            if (deltaTop < deltaBottom) {
                val displacement = endEvent.y - startEvent.y
                if (top + MIN_WIDTH < (bottom - displacement))
                    top += displacement
                else
                    top = bottom - MIN_WIDTH
            } else {
                val displacement = startEvent.y - endEvent.y
                if (top + MIN_WIDTH < (bottom - displacement))
                    bottom -= displacement
                else
                    bottom = top + MIN_WIDTH

            }
        }

        drawCropLine()
    }

    private lateinit var startEvent: PointXY

    private var left = 200f
    private var top = 200f
    private var right = 200f
    private var bottom = 200f

    private var background = Paint()

    init {
        background.color = Color.parseColor("#BF171717")
    }

    private fun initOnClickListener() {
        binding.btnGallery.setOnClickListener(this)

        binding.btnFlipXAxis.setOnClickListener(this)
        binding.btnFlipYAxis.setOnClickListener(this)
        binding.btnRotateLeft.setOnClickListener(this)
        binding.btnRotateRight.setOnClickListener(this)
        binding.btnCrop.setOnClickListener(this)
        binding.btnCropSubmit.setOnClickListener(this)

        binding.btnSave.setOnClickListener(this)
        binding.btnInfo.setOnClickListener(this)
        binding.btnLightMode.setOnClickListener(this)
        binding.btnDarkMode.setOnClickListener(this)
    }

    companion object {
        const val FLIP_VERTICAL = 1
        const val FLIP_HORIZONTAL = 2

        const val ROTATE_LEFT = -90f
        const val ROTATE_RIGHT = 90f

        const val MIN_WIDTH = 200f
        const val IMAGES_FOLDER_NAME = "ImageEditor"
    }

    override fun onBackPressed() {
        if (viewModel.isImageAvailable.get()) {
            val materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
            materialAlertDialogBuilder.setTitle("You have unsaved changes")
                .setPositiveButton(
                    "Save & Exit"
                ) { dialog, _ ->
                    dialog.dismiss()

                    saveBitmapToStorage(
                        selectedImageBitmap,
                        "Image-Editor-${System.currentTimeMillis()}"
                    )
                    viewModel.isImageAvailable.set(false)
                    viewModel.isCropEnabled.set(false)
                    viewModel.imageUri.value = null
                }
                .setNegativeButton(
                    "Discard"
                ) { dialog, _ ->
                    dialog.dismiss()
                    viewModel.isImageAvailable.set(false)
                    viewModel.isCropEnabled.set(false)
                    viewModel.imageUri.value = null
                }

            materialAlertDialogBuilder.show()
        } else {
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.main_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val appInfo = AppInformationBottomSheetDialog()
        appInfo.show(supportFragmentManager, appInfo.tag)

        return super.onOptionsItemSelected(item)
    }

}