package me.alberto.mymiles.screens.map

import android.location.Location
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import me.alberto.mymiles.database.Miles
import me.alberto.mymiles.repository.MilesRepository
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MapViewModel(private val repository: MilesRepository) : ViewModel() {


    private val _latlngList = MutableLiveData<ArrayList<LatLng>>()
    val latlngList: LiveData<ArrayList<LatLng>>
        get() = _latlngList

    private val _start = MutableLiveData<Boolean>()
    val start: LiveData<Boolean>
        get() = _start

    private var startLocation: Location? = null
    private var destinationLocation: Location? = null
    var clearMap: Boolean = false
        private set

    private val _startMarkerPosition = MutableLiveData<Location>()
    val startMarkerPosition: LiveData<Location>
        get() = _startMarkerPosition


    private val _distanceCovered = MutableLiveData<Float>()
    val distanceCovered: LiveData<Float>
        get() = _distanceCovered

    private val aggregateDistance = ArrayList<Float>()

    private fun calcaluteDistance() {

        if (destinationLocation != null && startLocation != null) {
            val distanceFloat = FloatArray(2)
            Location.distanceBetween(
                startLocation!!.latitude,
                startLocation!!.longitude,
                destinationLocation!!.latitude,
                destinationLocation!!.longitude,
                distanceFloat
            )

            val value = distanceFloat[0]
//            aggregateDistance.add(value)
//            var avg = 0F
//            if (aggregateDistance.size == 5) {
//                avg = aggregateDistance.sum() / 5
//                aggregateDistance.clear()
//            }
//            val accuracy = avg - 3
            _distanceCovered.value = value


        }
    }


    fun updateDeviceLocation(list: ArrayList<LatLng>) {
        _latlngList.value = list
    }


    fun toggleStart() {
        if (_start.value == null) {
            _start.value = true
            return
        }
        _start.value = _start.value?.let { !it }
    }

    fun reset() {
        _latlngList.value = null
        _start.value = null
        startLocation = null
        destinationLocation = null
    }

    fun captureStartLocation(currentLocation: Location?) {
        if (startLocation == null) {
            startLocation = currentLocation
        }
    }

    fun captureDestinationLocation(destination: Location?) {
        destinationLocation = destination
        calcaluteDistance()
    }

    fun updateStartMarker() {
        if (_startMarkerPosition.value == null) {
            _startMarkerPosition.value = startLocation
        }
    }

    fun saveToDatabase() {


        val format = SimpleDateFormat("EEE, d MMM yyyy, HH:mm", Locale.getDefault())
        val date = format.format(Date())

        val mile = Miles(
            distanceCovered = _distanceCovered.value!!,
            coordinates = latlngList.value as List<LatLng>,
            date = date
        )

        viewModelScope.launch {
            repository.addMiles(mile)
        }
        clearMap = true
    }

    fun resetClearMap() {
        clearMap = false
    }


    class Factory(private val repository: MilesRepository) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
                return MapViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown viewModel class")
        }
    }
}