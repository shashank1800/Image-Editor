package com.shahankbhat.imageeditor.ui

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.shahankbhat.imageeditor.PointXY
import com.shahankbhat.imageeditor.R
import com.shahankbhat.imageeditor.databinding.ActivityMainBinding
import com.shahankbhat.imageeditor.deltaX
import com.shahankbhat.imageeditor.deltaY
import com.shahankbhat.imageeditor.util.EditorFunctions.flipBitmap
import com.shahankbhat.imageeditor.util.EditorFunctions.rotateBitmap
import com.shahankbhat.imageeditor.viewmodel.MainActivityViewModel
import java.io.File
import kotlin.math.abs


class MainActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {

    lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels()
    lateinit var selectedImageBitmap: Bitmap
    lateinit var canvasBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = viewModel

        viewModel.imageUri.observe(this, {
            it?.let {
                viewModel.isImageAvailable.set(true)
                binding.imageView.setImageURI(it)
                selectedImageBitmap = (binding.imageView.drawable as BitmapDrawable).bitmap
                canvasBitmap = Bitmap.createBitmap(
                    selectedImageBitmap.width,
                    selectedImageBitmap.height,
                    Bitmap.Config.ARGB_8888
                )

                right = selectedImageBitmap.width - 200f
                bottom = selectedImageBitmap.height - 200f
                drawCropLine()

            }
        })

        binding.imageView.setOnTouchListener(this)

        initOnClickListener()
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            binding.btnGallery.id -> {
                getContent.launch("image/*")
            }

            binding.btnCamera.id -> {
                val file = File(filesDir, "ImageEditor")
                if (!file.exists())
                    file.mkdir()

                viewModel.imageUri.value = FileProvider.getUriForFile(
                    this,
                    applicationContext.packageName + ".provider",
                    file
                )
                takePicture.launch(viewModel.imageUri.value)
            }

            binding.btnFlipXAxis.id -> {
                val bitmap = (binding.imageView.drawable as BitmapDrawable).bitmap
                binding.imageView.setImageBitmap(flipBitmap(bitmap, FLIP_VERTICAL))
            }

            binding.btnFlipYAxis.id -> {
                val bitmap = (binding.imageView.drawable as BitmapDrawable).bitmap
                binding.imageView.setImageBitmap(flipBitmap(bitmap, FLIP_HORIZONTAL))
            }

            binding.btnRotateLeft.id -> {
                val bitmap = (binding.imageView.drawable as BitmapDrawable).bitmap
                binding.imageView.setImageBitmap(rotateBitmap(bitmap, ROTATE_LEFT))
            }

            binding.btnRotateRight.id -> {
                val bitmap = (binding.imageView.drawable as BitmapDrawable).bitmap
                binding.imageView.setImageBitmap(rotateBitmap(bitmap, ROTATE_RIGHT))
            }

            binding.btnCrop.id -> {
                viewModel.isCropEnabled.set(true)
            }

            binding.btnCropSubmit.id -> {
                viewModel.isCropEnabled.set(false)

                selectedImageBitmap = Bitmap.createBitmap(
                    selectedImageBitmap,
                    left.toInt(),
                    top.toInt(),
                    (right - left).toInt(),
                    (bottom - top).toInt()
                )

                right = selectedImageBitmap.width - 200f
                bottom = selectedImageBitmap.height - 200f

                binding.imageView.setImageBitmap(selectedImageBitmap)
            }
        }
    }


    val getContent = registerForActivityResult(GetContent()) { uri: Uri? ->
        viewModel.imageUri.value = uri
    }

    val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) {

    }

    private fun drawCropLine() {
        canvasBitmap = Bitmap.createBitmap(
            selectedImageBitmap.width,
            selectedImageBitmap.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(canvasBitmap)
        canvas.drawRect(
            left,
            top,
            right,
            bottom,
            linePaint
        )

        val newBitmap = BitmapDrawable(Resources.getSystem(), canvasBitmap)
        binding.canvas.setImageBitmap(newBitmap.bitmap)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val view: ImageView = v as ImageView

        when (view.id) {
            binding.imageView.id -> when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Log.i("press", "ACTION_DOWN ${event.x} - ${event.y}")
                    startEvent = PointXY(event.x, event.y)
                }
                MotionEvent.ACTION_MOVE -> {
                    Log.i("press", "ACTION_MOVE ${event.x} - ${event.y}")
                }
                MotionEvent.ACTION_UP -> {
                    Log.i("press", "ACTION_UP ${event.x} - ${event.y}")
                    changeCoordinateWhichIsNear(PointXY(event.x, event.y))
                }
                MotionEvent.ACTION_CANCEL -> {
                    Log.i("press", "ACTION_CANCEL ${event.x} - ${event.y}")
                }
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
            if (deltaLeft < deltaRight){
                val displacement = startEvent.x - endEvent.x
                if (left > (right - displacement))
                    left = right - 100
                else
                    left -= displacement
            }
            else {
                val displacement = startEvent.x - endEvent.x
                if (left > (right - displacement))
                    right = left + 100
                else
                    right -= displacement
            }

        } else {
            Log.i("drag", "y axis")

            val deltaTop = abs(startEvent.y - top)
            val deltaBottom = abs(startEvent.y - bottom)

            /** Check which line is nearer
             */
            if (deltaTop < deltaBottom)
                top -= startEvent.y - endEvent.y
            else
                bottom -= startEvent.y - endEvent.y
        }

        drawCropLine()
    }

    private lateinit var startEvent: PointXY

    var left = 200f
    var top = 200f
    var right = 200f
    var bottom = 200f

    private var linePaint = Paint()
    private var background = Paint()

    init {
        linePaint.color = Color.parseColor("#72050505")
        background.color = Color.parseColor("#E2303030")

        linePaint.strokeWidth = 20f

        //        canvas.drawRect(
//            0f,
//            0f,
//            bitmap.width.toFloat(),
//            bitmap.height.toFloat(),
//            background
//        )

    }

    private fun initOnClickListener() {
        binding.btnGallery.setOnClickListener(this)
        binding.btnCamera.setOnClickListener(this)

        binding.btnFlipXAxis.setOnClickListener(this)
        binding.btnFlipYAxis.setOnClickListener(this)
        binding.btnRotateLeft.setOnClickListener(this)
        binding.btnRotateRight.setOnClickListener(this)
        binding.btnCrop.setOnClickListener(this)
        binding.btnCropSubmit.setOnClickListener(this)
    }

    companion object {
        const val FLIP_VERTICAL = 1
        const val FLIP_HORIZONTAL = 2

        const val ROTATE_LEFT = -90f
        const val ROTATE_RIGHT = 90f
    }
}