package com.example.netpulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netpulse.data.wifi.WifiNetwork
import com.example.netpulse.data.wifi.WifiScannerRepository
import com.example.netpulse.utils.wifi.ScanState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WifiScannerViewModel(
    private val repository: WifiScannerRepository
) : ViewModel() {

    private val _discoveryResults = MutableStateFlow<List<WifiNetwork>>(emptyList())
    val scanResults: StateFlow<List<WifiNetwork>> = _discoveryResults.asStateFlow()
    
    private val _uiState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _uiState.asStateFlow()
    
    private var discoveryJob: Job? = null

    fun startScan() {
        _discoveryResults.value = emptyList()
        _uiState.value = ScanState.Scanning
        repository.startScan()
        
        discoveryJob?.cancel()
        discoveryJob = viewModelScope.launch {
            // Wait for scan to finish in repo
            repository.scanState.first { it is ScanState.Complete || it is ScanState.Error }
            
            val finalRepoState = repository.scanState.first()
            if (finalRepoState is ScanState.Complete) {
                val results = repository.scanResults.first()
                if (results.isNotEmpty()) {
                    val currentList = mutableListOf<WifiNetwork>()
                    // Discover top 8 for radar effect
                    results.take(8).forEach { network ->
                        delay(600) // Delay to match radar sweep
                        currentList.add(network)
                        _discoveryResults.value = currentList.toList()
                    }
                    
                    // Final delay before showing results list
                    delay(1000)
                    _discoveryResults.value = results // Set full results
                    _uiState.value = ScanState.Complete(results.size)
                } else {
                    _uiState.value = ScanState.Complete(0)
                }
            } else if (finalRepoState is ScanState.Error) {
                _uiState.value = finalRepoState
            }
        }
    }

    fun stopScan() {
        discoveryJob?.cancel()
        repository.stopScan()
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
    }
}
