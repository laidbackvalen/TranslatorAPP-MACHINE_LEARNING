package com.valenpatel.translatorapp

import android.app.ProgressDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.valenpatel.translatorapp.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var translator: Translator? = null
    private var progressDialog: ProgressDialog? = null

    // Maps language display names to their codes
    private val languageNameToCodeMap: MutableMap<String, String> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.statusBarColor = getColor(R.color.white)
        // Load available languages for AutoCompleteTextView
        loadAvailableLanguages()

        // Set up translation on button click
        binding.linearlayout.setOnClickListener {
            translateText()
        }
    }

    private fun loadAvailableLanguages() {
        // Get all available languages (language codes)
        val languageCodes = TranslateLanguage.getAllLanguages()

        // Convert language codes to display names and create a map
        val languageNames = languageCodes.map { code ->
            val locale = Locale(TranslateLanguage.fromLanguageTag(code) ?: code)
            val displayName = locale.displayLanguage
            languageNameToCodeMap[displayName] = code  // Map display name to language code
            displayName
        }.sorted() // Sort alphabetically for better UX

        // Create an ArrayAdapter for AutoCompleteTextView
        val adapter = ArrayAdapter(this, R.layout.dropdown_list, languageNames)

        // Set the adapter and dropdown appearance
        binding.fromLanguageText.setAdapter(adapter)
        binding.fromLanguageText.setDropDownBackgroundDrawable(ColorDrawable(Color.WHITE))
        binding.toLanguageText.setAdapter(adapter)
        binding.toLanguageText.setDropDownBackgroundDrawable(ColorDrawable(Color.WHITE))
    }

    private fun translateText() {
        val sourceLanguageName = binding.fromLanguageText.text.toString()
        val targetLanguageName = binding.toLanguageText.text.toString()

        // Get the corresponding language codes from the map
        val sourceLanguageCode = languageNameToCodeMap[sourceLanguageName]
        val targetLanguageCode = languageNameToCodeMap[targetLanguageName]

        if (sourceLanguageCode != null && targetLanguageCode != null) {
            // Create TranslatorOptions dynamically based on user selections
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.fromLanguageTag(sourceLanguageCode)!!)
                .setTargetLanguage(TranslateLanguage.fromLanguageTag(targetLanguageCode)!!)
                .build()

            translator = Translation.getClient(options)

            progressDialog = ProgressDialog(this).apply {
                setMessage("Downloading translation model...")
                setCancelable(false)
                show()
            }

            // Check if the model is downloaded; if not, download it
            translator?.downloadModelIfNeeded()
                ?.addOnSuccessListener {
                    // Model is available; proceed with translation
                    translateWith(translator!!)
                }
                ?.addOnFailureListener { exception ->
                    // Handle model download error
                    progressDialog?.dismiss()
                    Toast.makeText(this, "Failed to download model: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Invalid language selection. Please select both languages.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun translateWith(translator: Translator) {
        translator.translate(binding.editText.text.toString().trim())
            .addOnSuccessListener { translatedText ->
                progressDialog?.dismiss()
                // Create the alert dialog
                val alertDialog = AlertDialog.Builder(this)
                    .setTitle("Translation Result: ")
                    .setMessage(translatedText)
                    .setNegativeButton("OK") { dialog, _ ->
                        dialog.dismiss() // Action for negative button
                    }
                    .setPositiveButton("COPY TEXT") { dialog, _ ->
                        val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Translated Text", translatedText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(this, "Text Copied", Toast.LENGTH_SHORT).show()
                    }
                    .create() // Create the dialog

                // Set custom background
                alertDialog.window?.setBackgroundDrawableResource(R.drawable.alertdialog_bg)
                // Show the dialog
                alertDialog.show()
            }
            .addOnFailureListener { exception ->
                progressDialog?.dismiss()
                Toast.makeText(this, "Translation failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        // Close the translator if it's not null
        translator?.close()
        super.onDestroy()
    }
}
