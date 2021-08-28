package com.yaya.apod.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yaya.apod.api.ApiResponse
import com.yaya.apod.api.ApodResponse
import com.yaya.apod.data.repo.ApodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PicturesViewModel @Inject constructor(
    apodRepository: ApodRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

//    private val userId: String =
//        savedStateHandle["uid"] ?: throw IllegalArgumentException("missing user id")

    private lateinit var _content: LiveData<ApiResponse<ApodResponse>>

    //    lateinit var content: LiveData<ApiResponse<ApodResponse>>
    lateinit var content: LiveData<ApiResponse<MutableList<ApodResponse>>>

//    fun getToday(): LiveData<ApiResponse<ApodResponse>> {
//        return  apodRepository.getTodayContent()
//    }

    init {
        viewModelScope.launch {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CANADA)
            val calendar = Calendar.getInstance()
            val today = simpleDateFormat.format(calendar.time);

            calendar.add(Calendar.DAY_OF_YEAR, -4)
            val fourDaysAgo = simpleDateFormat.format(Date(calendar.timeInMillis))
            content = apodRepository.getContentWithDate(fourDaysAgo, today)
        }
    }
}