package me.alberto.mymiles.screens.map

import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import me.alberto.mymiles.R
import me.alberto.mymiles.databinding.FragmentMapBinding
import me.alberto.mymiles.viewmodel.MainViewModel

/**
 * A simple [Fragment] subclass.
 */

const val RC_LOCATION_PERMISSION = 1

class MapFragment : Fragment(), OnMapReadyCallback, LocationSource.OnLocationChangedListener {

    private lateinit var googleMap: GoogleMap
    private var startLocation: LatLng? = null
    private var endLocation: LatLng? = null
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var locationPermission = false
    private var lastLocation: Location? = null

    private lateinit var binding: FragmentMapBinding

    private lateinit var viewModel: MainViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    private fun observe() {
        viewModel.latlngList.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer
            if (viewModel.start.value != true) return@Observer

            val polyLine = googleMap.addPolyline(PolylineOptions().addAll(it))
            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        lastLocation!!.latitude,
                        lastLocation!!.longitude
                    ), 15F
                )
            )
            polyLine.color = Color.CYAN
            polyLine.startCap = RoundCap()
        })

        viewModel.start.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer
            if (it) getCurrentLocation()
        })
    }

    private fun getCurrentLocation() {
        val latLng = LatLng(lastLocation!!.latitude, lastLocation!!.longitude)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14F))
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15F))
    }


    private fun init() {
        fusedLocationProviderClient = FusedLocationProviderClient(requireActivity())
        viewModel = requireActivity().run {
            ViewModelProvider(this).get(MainViewModel::class.java)
        }
        getLocationPermission()

        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
        supportMapFragment.getMapAsync(this)
        observe()
    }


    override fun onMapReady(map: GoogleMap?) {
        map?.let {
            googleMap = it
            updateLocationUI()
        }
    }

    override fun onLocationChanged(currentLocation: Location?) {
        currentLocation?.let {
            lastLocation = it
            if (viewModel.start.value != true) return

            val latLng = LatLng(it.latitude, it.longitude)
            viewModel.updateDeviceLocation(latLng)
        }
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireActivity().applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermission = true
            getDeviceLocation()
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
                locationPermission = true
                getDeviceLocation()
            }

        }

        updateLocationUI()
    }

    private fun updateLocationUI() {
        googleMap ?: return

        try {
            if (locationPermission) {
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

            println(
                """
                
               device location called
                permision: $locationPermission
                
            """
            )

            if (locationPermission) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        lastLocation = task.result

                        googleMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastLocation!!.latitude, lastLocation!!.longitude), 14.0F
                            )
                        )
                        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15F))
                        googleMap.uiSettings.isZoomControlsEnabled = true
                    }

                }
            }


        } catch (error: SecurityException) {
            Log.d("security", error.message.toString())
        }
    }
}
