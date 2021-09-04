package com.yaya.apod.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yaya.apod.api.ApiResponse
import com.yaya.apod.data.model.Apod
import com.yaya.apod.data.repo.ApodRepository
import com.yaya.apod.data.repo.DbApodRepository
import com.yaya.apod.util.DateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApodViewModel @Inject constructor(
    private val apodRepository: ApodRepository,
    private val dbApodRepository: DbApodRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
//    private val userId: String =
//        savedStateHandle["uid"] ?: throw IllegalArgumentException("missing user id")

    lateinit var content: LiveData<ApiResponse<MutableList<Apod>>>

    private val clearListCh = Channel<Unit>(Channel.CONFLATED)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun apods(): Flow<PagingData<Apod>> {
        return flowOf(
            clearListCh.receiveAsFlow().map { PagingData.empty() },
            dbApodRepository.fetchApodPage().cachedIn(viewModelScope)
        ).flattenMerge(2)
    }

    fun updateApod(apod: Apod) = viewModelScope.launch {
        apodRepository.updateApod(apod)
    }

    var todayContent = apodRepository.getTodayContent()
    var contents = apodRepository.getApods(DateUtil.getDateAfterDate(DateUtil.todayDate(), 1), 10)

}