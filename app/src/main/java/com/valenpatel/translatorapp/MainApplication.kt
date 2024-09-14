package com.valenpatel.translatorapp

import android.app.Application
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Schedule the model download
        val modelDownloadRequest = OneTimeWorkRequest.Builder(ModelDownloadWorker::class.java).build()
        WorkManager.getInstance(this).enqueue(modelDownloadRequest)
    }
}
