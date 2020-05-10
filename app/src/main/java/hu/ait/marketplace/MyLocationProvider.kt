package hu.ait.marketplace

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Looper
import android.widget.Toast
import com.google.android.gms.location.*

class MyLocationProvider(
    context: Context,
    private val onNewLocationAvailable: OnNewLocationAvailable) {

    interface OnNewLocationAvailable {
        fun onNewLocation(location: Location)
    }

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            onNewLocationAvailable.onNewLocation(locationResult.lastLocation)
        }
    }


    @Throws(SecurityException::class)
    fun startLocationMonitoring() {
        val locationRequest = LocationRequest()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 500
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback, Looper.myLooper()
        )

    }

    @Throws(SecurityException::class)
    fun stopLocationMonitoring() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


}