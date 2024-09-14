package com.valenpatel.translatorapp

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class ModelDownloadWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val languages = listOf(TranslateLanguage.ENGLISH, TranslateLanguage.FRENCH) // List required languages

        languages.forEach { sourceLanguage ->
            languages.forEach { targetLanguage ->
                if (sourceLanguage != targetLanguage) {
                    val options = TranslatorOptions.Builder()
                        .setSourceLanguage(sourceLanguage)
                        .setTargetLanguage(targetLanguage)
                        .build()

                    val translator = Translation.getClient(options)
                    translator.downloadModelIfNeeded()
                        .addOnSuccessListener {
                            // Model downloaded successfully
                        }
                        .addOnFailureListener { exception ->
                            // Handle model download error
                            println("Failed to download model: ${exception.message}")
                        }
                }
            }
        }

        return Result.success()
    }
}
