package com.example.netpulse.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.UNSPECIFIED
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.netpulse.data.SpeedResult
import com.example.netpulse.ui.components.ShareCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ShareUtils {

    fun shareResultAsImage(
        context: Context, 
        result: SpeedResult, 
        isPro: Boolean,
        parentContext: CompositionContext? = null
    ) {
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            // Capture bitmap on Main thread because it involves UI/ComposeView
            val bitmap = captureComposableAsBitmap(context, parentContext) {
                ShareCard(result = result, isPro = isPro)
            }
            // Share bitmap (this will handle file IO on IO dispatcher)
            shareBitmap(context, bitmap)
        }
    }

    private fun captureComposableAsBitmap(
        context: Context,
        parentContext: CompositionContext?,
        content: @Composable () -> Unit
    ): Bitmap {
        val view = ComposeView(context)
        
        // Important: Set the owners so Compose knows which lifecycle to follow
        val lifecycleOwner = findOwner<LifecycleOwner>(context)
        val viewModelStoreOwner = findOwner<ViewModelStoreOwner>(context)
        val savedStateRegistryOwner = findOwner<SavedStateRegistryOwner>(context)

        lifecycleOwner?.let { view.setViewTreeLifecycleOwner(it) }
        viewModelStoreOwner?.let { view.setViewTreeViewModelStoreOwner(it) }
        savedStateRegistryOwner?.let { view.setViewTreeSavedStateRegistryOwner(it) }

        // Use the parent composition context to provide the recomposer
        if (parentContext != null) {
            view.setParentCompositionContext(parentContext)
        }

        view.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)

        view.setContent { content() }
        
        // Force measure and layout to ensure it's ready to be drawn
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

    private inline fun <reified T> findOwner(context: Context): T? {
        var innerContext = context
        while (innerContext is ContextWrapper) {
            if (innerContext is T) return innerContext
            innerContext = innerContext.baseContext
        }
        return null
    }

    private suspend fun shareBitmap(context: Context, bitmap: Bitmap) {
        withContext(Dispatchers.IO) {
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
            withContext(Dispatchers.Main) {
                context.startActivity(Intent.createChooser(intent, "Share Result"))
            }
        }
    }

    fun shareAsText(context: Context, result: SpeedResult) {
        val text = """
    📶 NetPulse Results
    ⬇ Download: ${result.downloadMbps} Mbps
    ⬆ Upload: ${result.uploadMbps} Mbps  
    📡 Ping: ${result.pingMs} ms
    🌐 Network: ${result.networkType} · ${result.isp}
    📍 ${result.location} · ${result.timestamp}
    🔗 netpulse.app
        """.trimIndent()
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share Result"))
    }
}
