package com.example.netpulse.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object SettingsUtils {

    // Open Play Store for rating
    fun openPlayStore(context: Context) {
        val uri = Uri.parse("market://details?id=${context.packageName}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback for emulator or devices without Play Store
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")))
        }
    }

    // Share app link
    fun shareApp(context: Context) {
        val text = "Check your internet speed with SpeedCheck Pro!\n" +
                   "https://play.google.com/store/apps/details?id=" +
                   context.packageName
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share App"))
    }

    // Open privacy policy URL
    fun openPrivacyPolicy(context: Context) {
        val uri = Uri.parse("https://speedcheckpro.app/privacy")
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
