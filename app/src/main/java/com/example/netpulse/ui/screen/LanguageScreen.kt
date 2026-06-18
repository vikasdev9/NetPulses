package com.example.netpulse.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netpulse.ui.theme.DarkColor
import com.example.netpulse.ui.theme.Teal200

data class AppLanguage(
    val code: String,        // "hi", "de", "ar" etc
    val nameNative: String,  // "हिंदी", "Deutsch" etc
    val nameEnglish: String, // "Hindi", "German" etc
    val flag: String,        // emoji flag "🇮🇳"
    val cpmTier: String      // "High CPM", "Medium CPM"
)

val supportedLanguages = listOf(
    AppLanguage("en", "English", "English", "🇺🇸", "High CPM"),
    AppLanguage("hi", "हिंदी", "Hindi", "🇮🇳", "High Volume"),
    AppLanguage("ta", "தமிழ்", "Tamil", "🇮🇳", "High Volume"),
    AppLanguage("te", "తెలుగు", "Telugu", "🇮🇳", "High Volume"),
    AppLanguage("bn", "বাংলা", "Bengali", "🇧🇩", "High Volume"),
    AppLanguage("mr", "मराठी", "Marathi", "🇮🇳", "High Volume"),
    AppLanguage("de", "Deutsch", "German", "🇩🇪", "High CPM"),
    AppLanguage("nl", "Nederlands", "Dutch", "🇳🇱", "High CPM"),
    AppLanguage("sv", "Svenska", "Swedish", "🇸🇪", "High CPM"),
    AppLanguage("no", "Norsk", "Norwegian", "🇳🇴", "High CPM"),
    AppLanguage("da", "Dansk", "Danish", "🇩🇰", "High CPM"),
    AppLanguage("ar", "العربية", "Arabic", "🇸🇦", "High CPM"),
    AppLanguage("pt", "Português", "Portuguese","🇧🇷", "Medium CPM"),
    AppLanguage("fr", "Français", "French", "🇫🇷", "Medium CPM"),
    AppLanguage("es", "Español", "Spanish", "🇪🇸", "Medium CPM"),
    AppLanguage("it", "Italiano", "Italian", "🇮🇹", "Medium CPM"),
    AppLanguage("ja", "日本語", "Japanese", "🇯🇵", "High CPM"),
    AppLanguage("ko", "한국어", "Korean", "🇰🇷", "High CPM"),
    AppLanguage("id", "Indonesia", "Indonesian","🇮🇩", "Medium CPM"),
    AppLanguage("tr", "Türkçe", "Turkish", "🇹🇷", "Medium CPM"),
    AppLanguage("ru", "Русский", "Russian", "🇷🇺", "Low CPM"),
    AppLanguage("pl", "Polski", "Polish", "🇵🇱", "Medium CPM")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(
    currentLanguageCode: String,
    onLanguageSelected: (AppLanguage) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Language", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkColor)
            )
        },
        containerColor = DarkColor
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(supportedLanguages) { lang ->
                LanguageItem(
                    language = lang,
                    isSelected = lang.code == currentLanguageCode,
                    onClick = { onLanguageSelected(lang) }
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            }
        }
    }
}

@Composable
fun LanguageItem(
    language: AppLanguage,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = language.flag, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = language.nameNative,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = language.nameEnglish,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Badge(
                containerColor = when (language.cpmTier) {
                    "High CPM" -> Color(0xFF4CAF50)
                    "Medium CPM" -> Color(0xFFFF9800)
                    else -> Color(0xFF9E9E9E)
                },
                contentColor = Color.White,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(language.cpmTier, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontSize = 10.sp)
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Teal200
                )
            }
        }
    }
}
