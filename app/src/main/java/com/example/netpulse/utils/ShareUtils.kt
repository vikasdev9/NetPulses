package com.example.netpulse.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.View.MeasureSpec.AT_MOST
import android.view.View.MeasureSpec.EXACTLY
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
import kotlinx.coroutines.delay
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
                ShareCard(result = result)
            }
            // Share bitmap (this will handle file IO on IO dispatcher)
            shareBitmap(context, bitmap)
        }
    }

    private suspend fun captureComposableAsBitmap(
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
        
        // Ensure the view is added to a window-like environment so composition runs
        val root = (context as? android.app.Activity)?.window?.decorView as? android.view.ViewGroup
        if (root != null) {
            // Add view momentarily to ensure it's "attached" for Compose to run
            withContext(Dispatchers.Main) {
                val dummyContainer = android.widget.FrameLayout(context).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(1, 1)
                    alpha = 0f // Invisible
                }
                root.addView(dummyContainer)
                dummyContainer.addView(view)
                
                // Wait for composition to be applied and layout to settle
                delay(200) 
                
                // Force measure and layout
                view.measure(
                    View.MeasureSpec.makeMeasureSpec(1080, EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(2000, AT_MOST)
                )
                
                val width = view.measuredWidth.coerceAtLeast(1)
                val height = view.measuredHeight.coerceAtLeast(1)
                
                view.layout(0, 0, width, height)
                
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                view.draw(canvas)
                
                // Cleanup
                dummyContainer.removeView(view)
                root.removeView(dummyContainer)
                
                return@withContext bitmap
            }
        }

        // Fallback if we can't attach to root
        delay(200)
        view.measure(
            View.MeasureSpec.makeMeasureSpec(1080, EXACTLY),
            View.MeasureSpec.makeMeasureSpec(2000, AT_MOST)
        )
        val width = view.measuredWidth.coerceAtLeast(1)
        val height = view.measuredHeight.coerceAtLeast(1)
        view.layout(0, 0, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
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
            val imagesDir = File(context.cacheDir, "images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            val file = File(imagesDir, "speedcheck_result.png")
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
                // Also add FLAG_ACTIVITY_NEW_TASK if sharing from a non-Activity context
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            withContext(Dispatchers.Main) {
                val chooser = Intent.createChooser(intent, "Share Result")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
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

    fun downloadReportAsImage(
        context: Context,
        content: @Composable () -> Unit
    ) {
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            val bitmap = captureComposableAsBitmap(context, null) {
                content()
            }
            saveBitmapToGallery(context, bitmap)
        }
    }

    private suspend fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
        withContext(Dispatchers.IO) {
            val filename = "NetPulse_Report_${System.currentTimeMillis()}.png"
            val fos: java.io.OutputStream?
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES)
                }
                val uri = context.contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = uri?.let { context.contentResolver.openOutputStream(it) }
            } else {
                val imagesDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES)
                if (!imagesDir.exists()) imagesDir.mkdirs()
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)
            }
            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(context, "Report saved to Pictures", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}
