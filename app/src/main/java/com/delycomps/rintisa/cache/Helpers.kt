package com.delycomps.rintisa.cache
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class Helpers() {
    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap? {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun bitmapToFile(
        context: Context?,
        bitmap: Bitmap,
        fileNameToSave: String
    ): File? { // File name like "image.png"
        //create a file to write bitmap data
        var file: File? = null
        return try {
            file =
                File(context?.cacheDir, "$fileNameToSave.png")
            file.createNewFile()

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos) // YOU can also save it in JPEG
            val bitmapdata: ByteArray = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file // it will return null
        }
    }

    fun saveBitmapToFile(file: File): File? {
        return try { // BitmapFactory options to downsize the image

            val oldExif = ExifInterface(file.path)
            val orientation: Int = oldExif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )

            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            o.inSampleSize = 6
            // factor of downsizing the image
            var inputStream = FileInputStream(file)
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o)
            inputStream.close()
            // The new size we want to scale to
            val REQUIRED_SIZE = 75
            // Find the correct scale value. It should be the power of 2.
            var scale = 1
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                o.outHeight / scale / 2 >= REQUIRED_SIZE
            ) {
                scale *= 2
            }
            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            inputStream = FileInputStream(file)
            var selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2)
            inputStream.close()
            file.createNewFile()
            val outputStream = FileOutputStream(file)
            if (selectedBitmap != null) {
                selectedBitmap = rotateBitmap(selectedBitmap, orientation)
            }
            selectedBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

            file
        } catch (e: Exception) {
            null
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap? {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_NORMAL -> return bitmap
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale((-1).toFloat(), 1.toFloat())
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180.toFloat())
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.setRotate((180).toFloat())
                matrix.postScale((-1.00).toFloat(), (1).toFloat())
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate((90).toFloat())
                matrix.postScale((-1).toFloat(), (1).toFloat())
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90.toFloat())
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate((-90).toFloat())
                matrix.postScale((-1).toFloat(), 1.toFloat())
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate((-90).toFloat())
            else -> return bitmap
        }
        return try {
            val bmRotated =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            bitmap.recycle()
            bmRotated
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            null
        }
    }


}