package com.zaur1.bakutransit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zaur1.bakutransit.data.repository.TransitRepository

class TransitViewModelFactory(private val repository: TransitRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransitViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
