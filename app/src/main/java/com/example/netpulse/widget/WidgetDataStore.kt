package com.example.netpulse.widget

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.glance.state.GlanceStateDefinition
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object WidgetDataStore {

    val definition = object : GlanceStateDefinition<WidgetData> {
        override suspend fun getDataStore(context: Context, fileKey: String): DataStore<WidgetData> {
            return context.widgetDataStore
        }

        override fun getLocation(context: Context, fileKey: String): File {
            return File(context.filesDir, "datastore/$fileKey")
        }
    }

    suspend fun updateData(context: Context, data: WidgetData) {
        context.widgetDataStore.updateData { data }
    }

    suspend fun getData(context: Context): WidgetData {
        return context.widgetDataStore.data.first()
    }

    private val Context.widgetDataStore: DataStore<WidgetData> by dataStore(
        fileName = "widget_data.json",
        serializer = WidgetDataSerializer
    )
}

object WidgetDataSerializer : Serializer<WidgetData> {
    override val defaultValue: WidgetData = WidgetData()

    override suspend fun readFrom(input: InputStream): WidgetData {
        try {
            return Json.decodeFromString(
                WidgetData.serializer(),
                input.readBytes().decodeToString()
            )
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read WidgetData", serialization)
        }
    }

    override suspend fun writeTo(t: WidgetData, output: OutputStream) {
        output.write(Json.encodeToString(WidgetData.serializer(), t).toByteArray())
    }
}
