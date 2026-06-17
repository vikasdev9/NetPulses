package com.example.netpulse.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.UNSPECIFIED
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.FileProvider
import com.example.netpulse.data.SpeedResult
import com.example.netpulse.ui.components.ShareCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ShareUtils {

    fun shareResultAsImage(context: Context, result: SpeedResult, isPro: Boolean) {
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            val bitmap = withContext(Dispatchers.Default) {
                captureComposableAsBitmap(context) {
                    ShareCard(result = result, isPro = isPro)
                }
            }
            shareBitmap(context, bitmap)
        }
    }

    private fun captureComposableAsBitmap(
        context: Context,
        content: @Composable () -> Unit
    ): Bitmap {
        val view = ComposeView(context)
        view.setContent { content() }
        view.measure(
            View.MeasureSpec.makeMeasureSpec(1080, EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, UNSPECIFIED)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap = Bitmap.createBitmap(
            view.measuredWidth, view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun shareBitmap(context: Context, bitmap: Bitmap) {
        val file = File(context.cacheDir, "speedcheck_result.png")
        FileOutputStream(file).use { 
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) 
        }
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.provider", file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Result"))
    }

    fun shareAsText(context: Context, result: SpeedResult) {
        val text = """
    📶 SpeedCheck Pro Results
    ⬇ Download: ${result.downloadMbps} Mbps
    ⬆ Upload: ${result.uploadMbps} Mbps  
    📡 Ping: ${result.pingMs} ms
    🌐 Network: ${result.networkType} · ${result.isp}
    📍 ${result.location} · ${result.timestamp}
    🔗 speedcheckpro.app
        """.trimIndent()
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share Result"))
    }
}
