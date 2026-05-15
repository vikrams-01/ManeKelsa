package com.manekelsa

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.manekelsa.data.WorkerProfileRepository
import com.manekelsa.ui.WorkerListScreen
import com.manekelsa.ui.WorkerViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewModel: WorkerViewModel

    /**
     * Registers a callback for the runtime location permission request.
     * On grant, immediately fetches the current location to feed into the ViewModel.
     */
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            fetchCurrentLocation()
        } else {
            Log.w("ManeKelsa", "Location permission denied — falling back to area-name sort")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 2. Initialize the WorkerProfileRepository
        val repository = WorkerProfileRepository()

        // 3. Use a ViewModelProvider.Factory to instantiate the WorkerViewModel
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(WorkerViewModel::class.java)) {
                    return WorkerViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
        viewModel = ViewModelProvider(this, factory)[WorkerViewModel::class.java]

        // 4. Request location permission (or fetch immediately if already granted)
        requestLocationPermission()

        setContent {
            MaterialTheme {
                // Collect the Flow as state
                val uiState by viewModel.uiState.collectAsState()

                WorkerListScreen(
                    workers = uiState,
                    onAvailabilityChanged = { worker, isAvailable ->
                        viewModel.toggleWorkerAvailability(worker.id, isAvailable)
                    },
                    onRatingClicked = { worker ->
                        viewModel.incrementRating(worker.id)
                    }
                )
            }
        }
    }

    /**
     * Checks whether location permission is already granted.
     * If yes, fetches the location immediately. If not, launches the permission dialog.
     */
    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Already granted
                fetchCurrentLocation()
            }
            else -> {
                // Request both fine and coarse — the system will show one dialog
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    /**
     * Fetches the device's current location using [FusedLocationProviderClient]
     * and feeds it into the ViewModel so workers are sorted by proximity.
     *
     * Uses getCurrentLocation() with PRIORITY_HIGH_ACCURACY for a fresh GPS fix,
     * falling back to lastLocation if the fresh fix fails.
     */
    @Suppress("MissingPermission") // Permission is checked before this is called
    private fun fetchCurrentLocation() {
        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            if (location != null) {
                Log.d("ManeKelsa", "Location: ${location.latitude}, ${location.longitude}")
                viewModel.updateUserLocation(location.latitude, location.longitude)
            } else {
                // Fallback: try lastLocation (may be cached and stale, but better than nothing)
                fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                    lastLoc?.let {
                        Log.d("ManeKelsa", "Last location: ${it.latitude}, ${it.longitude}")
                        viewModel.updateUserLocation(it.latitude, it.longitude)
                    }
                }
            }
        }.addOnFailureListener { e ->
            Log.e("ManeKelsa", "Failed to get current location", e)
            // Fallback to lastLocation
            fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                lastLoc?.let {
                    viewModel.updateUserLocation(it.latitude, it.longitude)
                }
            }
        }
    }
}
