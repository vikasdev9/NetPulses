package com.example.netpulse.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleUtils {

    fun setLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
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
}
