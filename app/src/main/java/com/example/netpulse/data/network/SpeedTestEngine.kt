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

object SpeedTestEngine {

    private const val PRIMARY_URL = 
        "https://speed.cloudflare.com/__down?bytes=25000000"

    private val client = OkHttpClient.Builder()
        .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun measureDownload(
        parallelConnections: Int = 3,
        durationSeconds: Int = 20,
        onLiveSpeed: (Double) -> Unit
    ): Double = withContext(Dispatchers.IO) {

        val WARMUP_MS = 2000L      
        val MAX_DURATION_MS = durationSeconds * 1000L
        val SAMPLE_INTERVAL_MS = 200L 

        val samples = mutableListOf<Double>() 
        val startTime = System.currentTimeMillis()
        val totalMeasuredBytes = AtomicLong(0L)
        
        val scope = CoroutineScope(Dispatchers.IO)
        
        val jobs = List(parallelConnections) {
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
                        val buffer = ByteArray(32768) 
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
                } catch (e: Exception) { }
            }
        }

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

        calculateAverage(samples)
    }

    suspend fun measureUpload(
        parallelConnections: Int = 3,
        durationSeconds: Int = 20,
        onLiveSpeed: (Double) -> Unit
    ): Double = withContext(Dispatchers.IO) {

        val UPLOAD_URL = "https://speed.cloudflare.com/__up"
        val PAYLOAD_SIZE = 10 * 1024 * 1024 
        val WARMUP_MS = 1500L
        val MAX_DURATION_MS = durationSeconds * 1000L

        val samples = mutableListOf<Double>()
        val startTime = System.currentTimeMillis()
        val totalSentBytes = AtomicLong(0L)
        val scope = CoroutineScope(Dispatchers.IO)

        val payload = ByteArray(PAYLOAD_SIZE).also { 
            java.util.Random().nextBytes(it) 
        }

        val jobs = List(parallelConnections) {
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

        calculateAverage(samples)
    }

    suspend fun measurePing(): Pair<Double, Double> = 
        withContext(Dispatchers.IO) {

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
                    if (latencyMs < 2000) {
                        latencies.add(latencyMs)
                    }
                }
            } catch (e: Exception) { }
            delay(100) 
        }

        if (latencies.isEmpty()) return@withContext Pair(-1.0, -1.0)
        val ping = latencies.average()
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
