package com.yaya.apod.viewmodels

import androidx.lifecycle.*
import com.yaya.apod.api.ApiResponse
import com.yaya.apod.api.ApodResponse
import com.yaya.apod.data.repo.ApodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PicturesViewModel @Inject constructor(
    apodRepository: ApodRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

//    private val userId: String =
//        savedStateHandle["uid"] ?: throw IllegalArgumentException("missing user id")

    private lateinit var _content :  LiveData<ApiResponse<ApodResponse>>
    lateinit var content: LiveData<ApiResponse<ApodResponse>>

//    fun getToday(): LiveData<ApiResponse<ApodResponse>> {
//        return  apodRepository.getTodayContent()
//    }

    init {
        viewModelScope.launch {
            content = apodRepository.getTodayContent()
        }
    }
}