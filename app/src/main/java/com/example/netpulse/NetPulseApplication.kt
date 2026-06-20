package com.example.netpulse

import android.app.Application
import com.example.netpulse.data.NetPulseDatabase

class NetPulseApplication : Application() {
    val database by lazy { NetPulseDatabase.getDatabase(this) }
}
