package me.alberto.mymiles.screens.map

import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.LocationListener
import com.google.android.gms.maps.LocationSource

class DeviceLocation(context: Context): LocationSource, LocationListener {

    private lateinit var locationChangedListener: LocationSource.OnLocationChangedListener
    private var locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private  val criteria = Criteria()

    private  lateinit var bestAvailableProvider: String

    private val minTime = 10000L
    private val minDistance = 10F

    init {

        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.powerRequirement = Criteria.POWER_LOW
        criteria.isAltitudeRequired = true
        criteria.isBearingRequired = true
        criteria.isSpeedRequired = true
        criteria.isCostAllowed = true

    }


    fun getBestAvailableProvider() {
        locationManager.getBestProvider(criteria, true).let { bestAvailableProvider = it }
    }


    override fun deactivate() {
        TODO("Not yet implemented")
    }

    override fun activate(listener: LocationSource.OnLocationChangedListener?) {
        listener?.let { locationChangedListener = it }
        bestAvailableProvider?.let {
        }
    }

    override fun onLocationChanged(p0: Location?) {
        TODO("Not yet implemented")
    }
}