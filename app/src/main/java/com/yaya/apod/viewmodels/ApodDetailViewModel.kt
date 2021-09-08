package com.yaya.apod.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.yaya.apod.data.model.Apod
import com.yaya.apod.data.repo.ApodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApodDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val apodRepository: ApodRepository,
) : ViewModel() {

    private val apodId = savedStateHandle.get<String>(APOD_ID_SAVED_STATE_KEY)!!
    val apod = apodRepository.getApod(apodId)

    fun addApodToFavorite(apod: Apod) = viewModelScope.launch {
        apodRepository.updateApod(apod)
    }

    val isFavorite = apodRepository.isFavorite(apodId).asLiveData()

    companion object {
        const val APOD_ID_SAVED_STATE_KEY = "apodId"
    }
}