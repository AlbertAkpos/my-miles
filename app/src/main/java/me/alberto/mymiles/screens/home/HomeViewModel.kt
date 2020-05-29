package me.alberto.mymiles.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.alberto.mymiles.repository.MilesRepository

class HomeViewModel(repository: MilesRepository) : ViewModel() {
    val miles = repository.getMiles()


    class Factory(private val repository: MilesRepository) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown viewModel class")
        }
    }
}