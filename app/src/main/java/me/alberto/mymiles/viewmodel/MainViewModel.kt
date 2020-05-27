package me.alberto.mymiles.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng

class MainViewModel : ViewModel() {


    private val _latlngList = MutableLiveData<ArrayList<LatLng>>()
    val latlngList: LiveData<ArrayList<LatLng>>
        get() = _latlngList

    private val _start = MutableLiveData<Boolean>()
    val start: LiveData<Boolean>
        get() = _start

    fun updateDeviceLocation(latLng: LatLng) {
        _latlngList.value?.add(latLng)
    }





    class Factory : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel() as T
            }
            throw IllegalArgumentException("Unknown viewModel class")
        }
    }
}