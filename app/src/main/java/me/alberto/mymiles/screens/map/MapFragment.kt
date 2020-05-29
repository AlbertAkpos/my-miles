package me.alberto.mymiles.screens.map

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import me.alberto.mymiles.R
import me.alberto.mymiles.database.MilesDatabase
import me.alberto.mymiles.databinding.FragmentMapBinding
import me.alberto.mymiles.repository.MilesRepository

/**
 * A simple [Fragment] subclass.
 */

const val RC_LOCATION_PERMISSION = 1
const val RC_CHECK_SETTINGS = 2
const val TAG = "map"

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var settingsClient: SettingsClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationSettingsRequest: LocationSettingsRequest
    private lateinit var locationCallback: LocationCallback
    private var locationPermission = false
    private var isLocationEnable = false
    private var currentLocation: Location? = null

    private val listOfLatLng = ArrayList<LatLng>()

    private lateinit var locationManager: LocationManager
    private lateinit var binding: FragmentMapBinding

    private lateinit var viewModel: MapViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        init()

        return binding.root
    }

    private fun observe() {
        viewModel.start.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer
            if (it) {
                googleMap.clear()
                dropMarker(currentLocation)
                startLocationUpdate()
                viewModel.captureStartLocation(currentLocation)
            } else {
                stopLocationUpdate()
                drawPolyLine()
            }
        })

        viewModel.startMarkerPosition.observe(viewLifecycleOwner, Observer { startMarker ->
            val marker: Location? = startMarker ?: currentLocation

            dropMarker(marker)
        })
    }

    private fun drawPolyLine() {
        viewModel.latlngList.value ?: return

        val options = PolylineOptions()


        for (latlng in viewModel.latlngList.value!!) {
            options.add(latlng)
        }
        options.clickable(true)
        options.color(Color.RED)

        googleMap.addPolyline(options)

        googleMap.addMarker(
            MarkerOptions().position(
                LatLng(
                    currentLocation!!.latitude,
                    currentLocation!!.longitude
                )
            )
        )

        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    currentLocation!!.latitude,
                    currentLocation!!.longitude
                ), 15F
            )
        )

        viewModel.saveToDatabase()

        reset()
    }

    private fun reset() {
        listOfLatLng.clear()
        viewModel.reset()
    }


    private fun updateCamera() {
        val latLng = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
//        googleMap.animateCamera(CameraUpdateFactory.zoomTo(14F))
    }


    private fun init() {

        createFusedProviderClient()
        val repository = MilesRepository(MilesDatabase.getDatabase())
        viewModel =
            ViewModelProvider(this, MapViewModel.Factory(repository)).get(MapViewModel::class.java)

        createLocationCallback()

        createLocationRequest()

        createBuilder()

        getLocationPermission()


        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
        supportMapFragment.getMapAsync(this)


    }

    private fun createBuilder() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        locationSettingsRequest = builder.build()
    }

    private fun createFusedProviderClient() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        settingsClient = LocationServices.getSettingsClient(requireContext())
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult ?: return
                currentLocation = locationResult.lastLocation

                val latLng = LatLng(
                    currentLocation!!.latitude,
                    currentLocation!!.longitude
                )

                viewModel.captureDestinationLocation(currentLocation)

                listOfLatLng.add(latLng)

                updateCamera()
                viewModel.updateDeviceLocation(listOfLatLng)


            }
        }
    }


    override fun onMapReady(map: GoogleMap?) {
        map?.let {
            googleMap = it
            updateLocationUI()
            observe()

        }
    }

    private fun dropMarker(marker: Location?) {
        marker ?: return

        googleMap.addMarker(
            MarkerOptions().position(
                LatLng(
                    marker.latitude,
                    marker.longitude
                )
            )
        )
    }

    private fun startLocationUpdate() {
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener(object : OnSuccessListener<LocationSettingsResponse> {
                override fun onSuccess(settingsResponse: LocationSettingsResponse) {
                    fusedLocationProviderClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback, Looper.getMainLooper()
                    )
                    viewModel.updateStartMarker()
                }
            })
            .addOnFailureListener(
                requireActivity()
            ) { exception ->
                val statusCode = (exception as ApiException).statusCode
                when (statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        val resolveException = exception as ResolvableApiException
                        resolveException.startResolutionForResult(
                            requireActivity(),
                            RC_CHECK_SETTINGS
                        )
                    }
                }
            }
    }

    fun stopLocationUpdate() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireActivity().applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            try {
               isLocationEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                if (isLocationEnable) {
                    locationPermission = true
                    getDeviceLocation()
                } else {
                    binding.startStopBtn.isEnabled = false
                    requireContext().startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            } catch (e: Exception) {
            }


        } else {
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                RC_LOCATION_PERMISSION
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_LOCATION_PERMISSION) {

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isLocationEnable = true
                    locationPermission = true
                    getDeviceLocation()

            } else {
                binding.startStopBtn.isEnabled = false
            }

        }

        updateLocationUI()
    }

    private fun updateLocationUI() {
        googleMap ?: return

        try {
            if (locationPermission && isLocationEnable) {
                googleMap.isMyLocationEnabled = true
                googleMap.uiSettings.isMyLocationButtonEnabled = true
            } else {
                googleMap.isMyLocationEnabled = false
                googleMap.uiSettings.isMyLocationButtonEnabled = false
                Toast.makeText(context, "Allow app to access device location", Toast.LENGTH_LONG)
                    .show()

            }
        } catch (error: SecurityException) {
            Log.d("security", error.message.toString())
        }
    }

    private fun getDeviceLocation() {
        try {

            if (locationPermission && isLocationEnable) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        currentLocation = task.result
                        currentLocation ?: return@addOnCompleteListener
                        googleMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(currentLocation!!.latitude, currentLocation!!.longitude),
                                14.0F
                            )
                        )
                        googleMap.animateCamera(CameraUpdateFactory.zoomTo(14F))
                        googleMap.uiSettings.isZoomControlsEnabled = true
                    }

                }
            }


        } catch (error: SecurityException) {
            Log.d("security", error.message.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.latlngList.value != null) {
            listOfLatLng.addAll(viewModel.latlngList.value!!)
            if (viewModel.start.value == false) {
                drawPolyLine()
            }
        }
    }


    override fun onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }

}
