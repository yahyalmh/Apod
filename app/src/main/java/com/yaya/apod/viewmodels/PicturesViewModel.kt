package com.yaya.apod.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yaya.apod.api.ApiResponse
import com.yaya.apod.data.model.Apod
import com.yaya.apod.data.repo.ApodRepository
import com.yaya.apod.util.Util
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PicturesViewModel @Inject constructor(
    private val apodRepository: ApodRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

//    private val userId: String =
//        savedStateHandle["uid"] ?: throw IllegalArgumentException("missing user id")

    private lateinit var _content: LiveData<ApiResponse<Apod>>
    lateinit var content: LiveData<ApiResponse<MutableList<Apod>>>

    fun updateApod(apod: Apod) = viewModelScope.launch {
        apodRepository.updateApod(apod)
    }

    var todayContent = apodRepository.getTodayContent()
    var contents = apodRepository.getContents(Util.getDateAfterToday(1), 10)

    init {
    }
}