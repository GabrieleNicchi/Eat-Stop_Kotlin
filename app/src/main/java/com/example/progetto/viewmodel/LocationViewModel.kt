//package com.example.progetto.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/*class LocationRepository {

    // Checks whether the user has provided permissions or not
    fun checkLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

}

class LocationViewModel(
    private val locationRepository: LocationRepository
) : ViewModel() {

    /* ----------------------------- Permits on location stuff ----------------------------- */

    // Variable to take into account user permissions
    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission

    // Inserts the status of the permissions into _hasPermission: true if granted, false otherwise
    fun checkPermission(context: Context) {
        _hasPermission.value = locationRepository.checkLocationPermission(context)
    }

    // Requires permissions to access the location
    fun requestPermission(permissionLauncher: ActivityResultLauncher<String>) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Update user permissions
    fun updatePermissionStatus(isGranted: Boolean) {
        _hasPermission.value = isGranted
        Log.d("LocationViewModel", "Permission granted: $isGranted")
    }

    /* ----------------------------- Position calculation stuff ----------------------------- */

    // Variable to save user position
    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location

    // Gets the current location
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        viewModelScope.launch {
            try {
                val task = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                val location = task.await()
                _location.value = location
                Log.d("LocationViewModel", "Lat: ${location.latitude}, Lon: ${location.longitude}")
            } catch (e: Exception) {
                Log.d("LocationViewModel", "Impossibile ottenere la posizione: ${e.message}")
            }
        }
    }



}*/