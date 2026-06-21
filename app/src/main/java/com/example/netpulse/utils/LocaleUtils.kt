package com.example.netpulse.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleUtils {

    fun setLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val res = context.resources
        val config = Configuration(res.configuration)
        
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        
        // Update resources for the current context
        @Suppress("DEPRECATION")
        res.updateConfiguration(config, res.displayMetrics)
        
        // Also update Application resources to ensure global string loading works
        val appContext = context.applicationContext
        if (appContext != null && appContext !== context) {
            @Suppress("DEPRECATION")
            appContext.resources.updateConfiguration(config, appContext.resources.displayMetrics)
        }
        
        return context.createConfigurationContext(config)
    }

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(
            "app_prefs", Context.MODE_PRIVATE
        )
        return prefs.getString("language", "") ?: ""
    }

    fun saveLanguage(context: Context, code: String) {
        context.getSharedPreferences("app_prefs", 
            Context.MODE_PRIVATE)
            .edit()
            .putString("language", code)
            .apply()
    }

    fun getLanguageName(code: String): String {
        return when (code) {
            "en" -> "English"
            "hi" -> "Hindi"
            "ta" -> "Tamil"
            "te" -> "Telugu"
            "bn" -> "Bengali"
            "mr" -> "Marathi"
            "de" -> "German"
            "nl" -> "Dutch"
            "sv" -> "Swedish"
            "no" -> "Norwegian"
            "da" -> "Danish"
            "ar" -> "Arabic"
            "pt" -> "Portuguese"
            "fr" -> "French"
            "es" -> "Spanish"
            "it" -> "Italian"
            "ja" -> "Japanese"
            "ko" -> "Korean"
            "id" -> "Indonesian"
            "tr" -> "Turkish"
            "ru" -> "Russian"
            "pl" -> "Polish"
            else -> "English"
        }
    }
}
