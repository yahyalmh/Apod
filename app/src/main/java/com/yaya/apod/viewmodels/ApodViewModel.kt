package com.yaya.apod.viewmodels

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yaya.apod.api.ApiResponse
import com.yaya.apod.data.model.Apod
import com.yaya.apod.data.repo.ApodRepository
import com.yaya.apod.data.repo.DbApodRepository
import com.yaya.apod.util.Util
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PicturesViewModel @Inject constructor(
    private val apodRepository: ApodRepository,
    private val dbApodRepository: DbApodRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    companion object {
        const val KEY_SUBREDDIT = "subreddit"
        const val DEFAULT_SUBREDDIT = "androiddev"
    }
    init {
        if (!savedStateHandle.contains(KEY_SUBREDDIT)) {
            savedStateHandle.set(KEY_SUBREDDIT, DEFAULT_SUBREDDIT)
        }
    }
//    private val userId: String =
//        savedStateHandle["uid"] ?: throw IllegalArgumentException("missing user id")

    private lateinit var _content: LiveData<ApiResponse<Apod>>
    lateinit var content: LiveData<ApiResponse<MutableList<Apod>>>

    private var currentQueryValue: String? = null
    private var currentSearchResult: LiveData<PagingData<Apod>>? = null

    private val clearListCh = Channel<Unit>(Channel.CONFLATED)
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val apods = flowOf(
        clearListCh.receiveAsFlow().map { PagingData.empty() },
        savedStateHandle.getLiveData<String>(KEY_SUBREDDIT)
            .asFlow()
            .flatMapLatest { dbApodRepository.apods("it", 7) }
            // cachedIn() shares the paging state across multiple consumers of posts,
            // e.g. different generations of UI across rotation config change
            .cachedIn(viewModelScope)
    ).flattenMerge(2)

    fun updateApod(apod: Apod) = viewModelScope.launch {
        apodRepository.updateApod(apod)
    }

    var todayContent = apodRepository.getTodayContent()
    var contents = apodRepository.getContents(Util.getDateAfterToday(1), 10)

}