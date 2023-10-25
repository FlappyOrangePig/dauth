package com.infras.dauth.ui.fiat.transaction.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import com.infras.dauth.util.LogUtil
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.roundToInt

/**
 * Image scale util
 *
 * @constructor Create empty Image scale util
 */
object ImageScaleUtil {

    private const val TAG = "ImageScaleUtil"
    private const val SCALE_IMAGE_WIDTH = 856
    private const val SCALE_IMAGE_HEIGHT = 540

    fun getScaledImage(dstDir: File, imagePath: String, suffix: String): String {
        val originalFile = File(imagePath)
        if (!originalFile.exists()) {
            return imagePath
        }
        val fileSize = originalFile.length()
        LogUtil.d(TAG, "original img size:$fileSize")
        if (fileSize <= 100 * 1024) {
            LogUtil.d(TAG, "use original small image")
            return imagePath
        }

        val image: Bitmap = decodeScaleImage(imagePath, SCALE_IMAGE_WIDTH, SCALE_IMAGE_HEIGHT)
        try {
            if (!dstDir.exists()) {
                dstDir.mkdirs()
            }
            val tempFile = File.createTempFile("scaled${suffix}_", ".jpg", dstDir)
            val stream = FileOutputStream(tempFile)
            image.compress(Bitmap.CompressFormat.JPEG, 70, stream)
            stream.close()
            LogUtil.d(TAG, "${originalFile.length()} -> ${tempFile.length()}")
            return tempFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return imagePath
    }

    private fun decodeScaleImage(imagePath: String, reqWidth: Int, reqHeight: Int): Bitmap {
        val options: BitmapFactory.Options = getBitmapOptions(imagePath)

        // Ignore the EXIF image angle and directly ensure that the width is greater than or equal to the height
        val rotate = if (options.outWidth < options.outHeight) {
            val temp = options.outHeight
            options.outHeight = options.outWidth
            options.outWidth = temp
            true
        } else {
            false
        }

        // No longer render according to the angle of the image itself
        val degree: Int = readPictureDegree(imagePath)

        val sampleSize: Int = calculateInSampleSize(options, reqWidth, reqHeight)
        LogUtil.d(
            TAG,
            "original w=${options.outWidth} h=${options.outHeight} sample=$sampleSize, rotate=$rotate, degree=$degree"
        )
        options.inSampleSize = sampleSize
        options.inJustDecodeBounds = false
        val bm = BitmapFactory.decodeFile(imagePath, options)

        return if (bm != null && rotate) {
            val rotateBm = rotateImageView(270, bm) // Take pictures of the top of the phone, the bottom on the left, and the right
            bm.recycle()
            rotateBm
        } else {
            bm
        }
    }

    private fun getBitmapOptions(imagePath: String): BitmapFactory.Options {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imagePath, options)
        return options
    }

    private fun readPictureDegree(path: String): Int {
        var degree = 0
        try {
            val exifInterface = ExifInterface(path)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return degree
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = (height.toFloat() / reqHeight.toFloat()).roundToInt()
            val widthRatio = (width.toFloat() / reqWidth.toFloat()).roundToInt()

            inSampleSize = if (heightRatio > widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
    }

    private fun rotateImageView(angle: Int, bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}