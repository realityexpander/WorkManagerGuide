package com.plcoding.workmanagerguide

import android.content.Context
import android.graphics.*
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Suppress("BlockingMethodInNonBlockingContext")
class ColorFilterWorker(
    private val context: Context,
    private val workerParams: WorkerParameters
): CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val imageFile = workerParams.inputData.getString(WorkerKeys.IMAGE_URI)
            ?.toUri()
            ?.toFile()
        delay(2000L)
        return imageFile?.let { file ->
            try {
                val bmp = BitmapFactory.decodeFile(file.absolutePath)
                val resultBmp = bmp.copy(bmp.config, true)
                val paint = Paint()
                paint.colorFilter = LightingColorFilter(0x08FF04, 1)
                val canvas = Canvas(resultBmp)
                canvas.drawBitmap(resultBmp, 0f, 0f, paint)

                withContext(Dispatchers.IO) {
                    val resultImageFile = File(context.cacheDir, "new-image.jpg")
                    val outputStream = FileOutputStream(resultImageFile)
                    val successful = resultBmp.compress(
                        Bitmap.CompressFormat.JPEG,
                        90,
                        outputStream
                    )
                    if (successful) {
                        return@withContext Result.success(
                            workDataOf(
                                WorkerKeys.FILTER_URI to resultImageFile.toUri().toString()
                            )
                        )
                    }
                    return@withContext Result.failure(
                        workDataOf(
                            WorkerKeys.ERROR_MSG to "Error while compressing image"
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(
                    workDataOf(
                        WorkerKeys.ERROR_MSG to e.message
                    )
                )
            }
        } ?: Result.failure(
            workDataOf(
                WorkerKeys.ERROR_MSG to "Image file not found"
            )
        )
    }
}