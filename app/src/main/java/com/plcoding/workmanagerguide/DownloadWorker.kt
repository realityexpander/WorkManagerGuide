package com.plcoding.workmanagerguide

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.random.Random

@Suppress("BlockingMethodInNonBlockingContext")
class DownloadWorker(
    private val context: Context,
    private val workerParams: WorkerParameters
): CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        startForegroundService()
        delay(2000L)
        val response = FileApi.instance.downloadImage()
        response.body()?.let { body ->
            return withContext(Dispatchers.IO) {
                val file = File(context.cacheDir, "image.jpg")
                val outputStream = FileOutputStream(file)
                outputStream.use { stream ->
                    try {
                        stream.write(body.bytes())
                    } catch(e: IOException) {
                        return@withContext Result.failure(
                            // Corresponds to downloadInfo.outputData.putString(WorkerKeys.ERROR_MSG, e.message)
                            workDataOf(
                                WorkerKeys.ERROR_MSG to e.localizedMessage
                            )
                        )
                    }
                }
                Result.success(
                    workDataOf(
                        WorkerKeys.IMAGE_URI to file.toUri().toString()
                    )
                )
            }
        }
        if(!response.isSuccessful) {
            if(response.code().toString().startsWith("5")) {
                return Result.retry()
            }
            return Result.failure(
                workDataOf(
                    WorkerKeys.ERROR_MSG to "Network error: ${response.code()}"
                )
            )
        }
        return Result.failure(
            workDataOf(WorkerKeys.ERROR_MSG to "Unknown error")
        )
    }

    private suspend fun startForegroundService() {
        setForeground(
            ForegroundInfo(
                Random.nextInt(),
                NotificationCompat.Builder(context, "download_channel")
                    .setSmallIcon(android.R.drawable.btn_star)
                    .setContentText("Downloading...")
                    .setContentTitle("Download in progress")
                    .build()
            )
        )
    }
}