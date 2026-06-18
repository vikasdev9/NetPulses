package com.example.netpulse

import android.content.Context
import androidx.activity.ComponentActivity
import com.example.netpulse.utils.LocaleUtils

abstract class BaseActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val lang = LocaleUtils.getSavedLanguage(newBase)
        val context = if (lang.isNotEmpty()) {
            LocaleUtils.setLocale(newBase, lang)
        } else newBase
        super.attachBaseContext(context)
    }
}
