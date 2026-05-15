package com.manekelsa.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manekelsa.data.WorkerProfileRepository
import com.manekelsa.model.WorkerProfile
import com.manekelsa.util.LocationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkerViewModel(
    private val repository: WorkerProfileRepository = WorkerProfileRepository()
) : ViewModel() {

    /** User's current latitude (updated via location provider). */
    private val _userLat = MutableStateFlow(0.0)

    /** User's current longitude (updated via location provider). */
    private val _userLon = MutableStateFlow(0.0)

    /**
     * Workers sorted by proximity to the user's current location.
     * Combines the real-time Firestore stream with the user's live coordinates
     * so the list re-sorts whenever either value changes.
     */
    val uiState: StateFlow<List<WorkerProfile>> = combine(
        repository.getWorkers(),
        _userLat,
        _userLon
    ) { workers, lat, lon ->
        if (lat == 0.0 && lon == 0.0) {
            // No location available yet — fall back to alphabetical street/area sort
            workers.sortedBy { it.nearestStreetArea }
        } else {
            // Hyper-local sort using Haversine distance
            LocationUtils.sortByNearest(lat, lon, workers)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Called by the Activity/Fragment when a new location fix is obtained.
     * This triggers a re-sort of the worker list.
     */
    fun updateUserLocation(latitude: Double, longitude: Double) {
        _userLat.value = latitude
        _userLon.value = longitude
    }

    /**
     * Updates the availability status of a worker in Firestore.
     * The @get:PropertyName("isAvailable") annotation on [WorkerProfile]
     * ensures the Boolean field name maps correctly without the "is" prefix
     * being stripped by Kotlin/Firebase serialization.
     */
    fun toggleWorkerAvailability(workerId: String, newStatus: Boolean) {
        viewModelScope.launch {
            try {
                repository.updateAvailability(workerId, newStatus)
            } catch (e: Exception) {
                // In a production app, you might expose a separate error state for the UI
                e.printStackTrace()
            }
        }
    }

    /**
     * Atomically increments the thumbsUpCount for a worker in Firestore,
     * capped at a maximum of 10. The real-time snapshot listener will
     * automatically update the UI once Firestore acknowledges the write.
     */
    fun incrementRating(workerId: String) {
        val currentCount = uiState.value
            .firstOrNull { it.id == workerId }?.thumbsUpCount ?: 0
        if (currentCount >= 10) return

        viewModelScope.launch {
            try {
                repository.incrementRating(workerId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
