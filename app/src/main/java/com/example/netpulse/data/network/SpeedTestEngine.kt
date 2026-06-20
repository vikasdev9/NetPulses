package com.example.netpulse.data.network

import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs

/*
 * WHY OUR RESULT MAY DIFFER FROM GOOGLE/OOKLA:
 *
 * 1. SERVER LOCATION — Google uses nearest Google server.
 *    We use Cloudflare. Different routing = different result.
 *    Both are valid. Neither is "wrong".
 *
 * 2. PROTOCOL — Google uses QUIC/HTTP3 which is faster
 *    on lossy networks. We use HTTP/2 via OkHttp.
 *
 * 3. PARALLEL CONNECTIONS — We use 3, Google uses adaptive.
 *    On slow connections, 3 parallel is overkill.
 *    TODO: start with 1, scale up based on speed detected.
 *
 * 4. MEASUREMENT WINDOW — We measure over 10 seconds.
 *    Google measures until result is statistically stable.
 *    Short bursts (fast WiFi) may complete in 3-4 seconds
 *    giving less accurate averages.
 *
 * 5. NETWORK CONGESTION — Both tools measure at the same
 *    moment but congestion changes second by second.
 *    Run both at same time for closest comparison.
 *
 * ACCURACY TARGET: Our result should be within ±15% 
 * of Speedtest.net on the same network.
 */
object SpeedTestEngine {

    // Use Cloudflare as primary — 
    // it's closest to accurate for Indian users
    private const val PRIMARY_URL = 
        "https://speed.cloudflare.com/__down?bytes=25000000"

    private val client = OkHttpClient.Builder()
        .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun measureDownload(
        onLiveSpeed: (Double) -> Unit
    ): Double = withContext(Dispatchers.IO) {

        val WARMUP_MS = 2000L      // skip first 2 seconds
        val MAX_DURATION_MS = 10000L // max 10 seconds
        val SAMPLE_INTERVAL_MS = 200L // sample every 200ms

        val samples = mutableListOf<Double>() // Mbps per sample
        val startTime = System.currentTimeMillis()
        val totalMeasuredBytes = AtomicLong(0L)
        
        val scope = CoroutineScope(Dispatchers.IO)
        
        // Launch 3 parallel download streams
        val jobs = List(3) {
            scope.launch {
                try {
                    val request = Request.Builder()
                        .url(PRIMARY_URL)
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .header("Connection", "keep-alive")
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) return@launch
                        val stream = response.body?.byteStream() ?: return@launch
                        val buffer = ByteArray(32768) // 32KB chunks
                        var bytesRead: Int = 0

                        while (
                            isActive &&
                            stream.read(buffer).also { bytesRead = it } != -1 &&
                            System.currentTimeMillis() - startTime < MAX_DURATION_MS
                        ) {
                            val elapsed = System.currentTimeMillis() - startTime
                            if (elapsed >= WARMUP_MS) {
                                totalMeasuredBytes.addAndGet(bytesRead.toLong())
                            }
                        }
                    }
                } catch (e: Exception) {
                    // One stream failed — others continue
                }
            }
        }

        // Sampling coroutine — runs every 200ms
        // measures instantaneous speed
        val samplerJob = scope.launch {
            delay(WARMUP_MS)
            var prevBytes = 0L
            var prevTime = System.currentTimeMillis()

            while(
                isActive &&
                System.currentTimeMillis() - startTime < MAX_DURATION_MS
            ) {
                delay(SAMPLE_INTERVAL_MS)
                val currentBytes = totalMeasuredBytes.get()
                val currentTime = System.currentTimeMillis()
                val deltaBytes = currentBytes - prevBytes
                val deltaTime = (currentTime - prevTime) / 1000.0

                if (deltaTime > 0 && deltaBytes > 0) {
                    // Convert bytes to Mbps
                    val instantMbps = (deltaBytes * 8.0) / (deltaTime * 1_000_000.0)
                    samples.add(instantMbps)

                    // Emit live speed to UI
                    withContext(Dispatchers.Main) {
                        onLiveSpeed(instantMbps)
                    }
                }
                prevBytes = currentBytes
                prevTime = currentTime
            }
        }

        // Wait for all to complete or timeout
        val remainingTime = MAX_DURATION_MS - (System.currentTimeMillis() - startTime)
        if (remainingTime > 0) {
            delay(remainingTime)
        }
        
        jobs.forEach { it.cancel() }
        samplerJob.cancel()

        val finalResult = calculateAverage(samples)
        
        // Log debug info
        SpeedTestValidator.logDebugInfo(DebugInfo(
            phase = "DOWNLOAD",
            totalBytes = totalMeasuredBytes.get(),
            measurementDuration = System.currentTimeMillis() - startTime,
            sampleCount = samples.size,
            rawSamples = samples,
            finalResult = finalResult
        ))

        finalResult
    }

    suspend fun measureUpload(
        onLiveSpeed: (Double) -> Unit
    ): Double = withContext(Dispatchers.IO) {

        // Cloudflare upload endpoint — 
        // accepts POST with random bytes
        val UPLOAD_URL = "https://speed.cloudflare.com/__up"
        val PAYLOAD_SIZE = 10 * 1024 * 1024 // 10MB per stream
        val WARMUP_MS = 1500L
        val MAX_DURATION_MS = 10000L

        val samples = mutableListOf<Double>()
        val startTime = System.currentTimeMillis()
        val totalSentBytes = AtomicLong(0L)
        val scope = CoroutineScope(Dispatchers.IO)

        // Generate random payload once, reuse across streams
        val payload = ByteArray(PAYLOAD_SIZE).also { 
            java.util.Random().nextBytes(it) 
        }

        val jobs = List(3) {
            scope.launch {
                try {
                    val request = Request.Builder()
                        .url(UPLOAD_URL)
                        .post(object : RequestBody() {
                            override fun contentType() = "application/octet-stream".toMediaTypeOrNull()
                            override fun contentLength() = PAYLOAD_SIZE.toLong()
                            override fun writeTo(sink: okio.BufferedSink) {
                                var offset = 0
                                val chunkSize = 32768
                                while (
                                    offset < PAYLOAD_SIZE &&
                                    isActive &&
                                    System.currentTimeMillis() - startTime < MAX_DURATION_MS
                                ) {
                                    val end = minOf(offset + chunkSize, PAYLOAD_SIZE)
                                    val toWrite = end - offset
                                    sink.write(payload, offset, toWrite)
                                    sink.flush()

                                    val elapsed = System.currentTimeMillis() - startTime
                                    if (elapsed >= WARMUP_MS) {
                                        totalSentBytes.addAndGet(toWrite.toLong())
                                    }
                                    offset = end
                                }
                            }
                        })
                        .build()
                    
                    client.newCall(request).execute().use { }
                } catch (e: Exception) { }
            }
        }

        // Sampler
        val samplerJob = scope.launch {
            delay(WARMUP_MS)
            var prevBytes = 0L
            var prevTime = System.currentTimeMillis()

            while(
                isActive &&
                System.currentTimeMillis() - startTime < MAX_DURATION_MS
            ) {
                delay(200)
                val currentBytes = totalSentBytes.get()
                val currentTime = System.currentTimeMillis()
                val deltaBytes = currentBytes - prevBytes
                val deltaTime = (currentTime - prevTime) / 1000.0

                if (deltaTime > 0 && deltaBytes > 0) {
                    val instantMbps = (deltaBytes * 8.0) / (deltaTime * 1_000_000.0)
                    samples.add(instantMbps)
                    withContext(Dispatchers.Main) {
                        onLiveSpeed(instantMbps)
                    }
                }
                prevBytes = currentBytes
                prevTime = currentTime
            }
        }

        val remainingTime = MAX_DURATION_MS - (System.currentTimeMillis() - startTime)
        if (remainingTime > 0) {
            delay(remainingTime)
        }
        
        jobs.forEach { it.cancel() }
        samplerJob.cancel()

        val finalResult = calculateAverage(samples)

        // Log debug info
        SpeedTestValidator.logDebugInfo(DebugInfo(
            phase = "UPLOAD",
            totalBytes = totalSentBytes.get(),
            measurementDuration = System.currentTimeMillis() - startTime,
            sampleCount = samples.size,
            rawSamples = samples,
            finalResult = finalResult
        ))

        finalResult
    }

    suspend fun measurePing(): Pair<Double, Double> = 
        withContext(Dispatchers.IO) {

        // Use HTTP HEAD to Cloudflare — 
        // most accurate for real-world ping
        val PING_URL = "https://speed.cloudflare.com/__down?bytes=0"
        val SAMPLES = 10
        val latencies = mutableListOf<Long>()

        repeat(SAMPLES) {
            try {
                val t1 = System.nanoTime()
                val request = Request.Builder()
                    .url(PING_URL)
                    .head()
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build()
                
                client.newCall(request).execute().use {
                    val t2 = System.nanoTime()
                    val latencyMs = (t2 - t1) / 1_000_000L
                    // Sanity check — discard if > 2000ms (timeout noise)
                    if (latencyMs < 2000) {
                        latencies.add(latencyMs)
                    }
                }
            } catch (e: Exception) { }
            delay(100) // 100ms between pings
        }

        if (latencies.isEmpty()) return@withContext Pair(-1.0, -1.0)

        // Ping = average of all samples
        val ping = latencies.average()

        // Jitter = mean of absolute differences 
        // between consecutive pings
        val jitter = if (latencies.size > 1) {
            latencies.zipWithNext { a, b -> 
                abs(b - a).toDouble() 
            }.average()
        } else 0.0

        Pair(ping, jitter)
    }

    private fun calculateAverage(samples: List<Double>): Double {
        if (samples.isEmpty()) return -1.0
        
        val sorted = samples.sorted()
        val trimCount = (sorted.size * 0.10).toInt()
        val trimmed = if (sorted.size > 4) {
            sorted.subList(trimCount, sorted.size - trimCount)
        } else sorted

        return if (trimmed.isEmpty()) -1.0 else trimmed.average()
    }
}
