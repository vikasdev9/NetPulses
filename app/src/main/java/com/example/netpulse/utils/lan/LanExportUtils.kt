package com.example.netpulse.utils.lan

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.text.StaticLayout
import android.text.TextPaint
import com.example.netpulse.data.lan.LanDevice
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object LanExportUtils {

    fun exportToCsv(context: Context, devices: List<LanDevice>): File? {
        val fileName = "NetPulse_LAN_Report_${System.currentTimeMillis()}.csv"
        val file = File(context.cacheDir, fileName)
        
        return try {
            val writer = FileOutputStream(file).bufferedWriter()
            writer.write("IP Address,Hostname,Nickname,Device Type,Status,Latency (ms),First Seen,Last Seen\n")
            
            devices.forEach { device ->
                val row = "${device.ipAddress},${device.hostname},${device.nickname ?: ""},${device.deviceType.label},${if (device.isOnline) "Online" else "Offline"},${device.latencyMs},${formatDate(device.firstSeen)},${formatDate(device.lastSeen)}\n"
                writer.write(row)
            }
            writer.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportToJson(context: Context, devices: List<LanDevice>): File? {
        val fileName = "NetPulse_LAN_Report_${System.currentTimeMillis()}.json"
        val file = File(context.cacheDir, fileName)
        
        return try {
            val json = StringBuilder("[\n")
            devices.forEachIndexed { index, device ->
                json.append("  {\n")
                json.append("    \"ip\": \"${device.ipAddress}\",\n")
                json.append("    \"hostname\": \"${device.hostname}\",\n")
                json.append("    \"nickname\": \"${device.nickname ?: ""}\",\n")
                json.append("    \"type\": \"${device.deviceType.label}\",\n")
                json.append("    \"online\": ${device.isOnline},\n")
                json.append("    \"latency\": ${device.latencyMs}\n")
                json.append("  }${if (index < devices.size - 1) "," else ""}\n")
            }
            json.append("]")
            
            file.writeText(json.toString())
            file
        } catch (e: Exception) {
            null
        }
    }

    fun exportToPdf(context: Context, devices: List<LanDevice>): File? {
        val fileName = "NetPulse_LAN_Report_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)
        
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = android.graphics.Paint()
        
        var y = 40f
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("NetPulse LAN Scanner Report", 50f, y, paint)
        
        y += 30f
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("Generated on: ${formatDate(System.currentTimeMillis())}", 50f, y, paint)
        
        y += 40f
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("Discovered Devices (${devices.size})", 50f, y, paint)
        
        y += 20f
        paint.textSize = 9f
        paint.isFakeBoldText = false
        
        devices.take(35).forEach { device -> // Limit to 1 page for simplicity now
            val text = "${device.ipAddress} | ${device.nickname ?: device.hostname} | ${device.deviceType.label} | ${if (device.isOnline) "Online" else "Offline"}"
            canvas.drawText(text, 50f, y, paint)
            y += 15f
        }
        
        pdfDocument.finishPage(page)
        
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            pdfDocument.close()
            null
        }
    }

    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
    }
}
