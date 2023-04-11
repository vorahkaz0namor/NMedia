package ru.netology.nmedia.model

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.scan
import ru.netology.nmedia.model.RemotePresentationState.*

enum class RemotePresentationState {
    INITIAL,
    REMOTE_LOADING,
    SOURCE_LOADING,
    PRESENTED
}

fun Flow<CombinedLoadStates>.asRemotePresentationState(): Flow<RemotePresentationState> =
    scan(INITIAL) { state, loadState ->
        when (state) {
            INITIAL, PRESENTED ->
                when {
                    loadState.mediator?.refresh is LoadState.Loading ||
                    loadState.mediator?.prepend is LoadState.Loading -> REMOTE_LOADING
                    // Поскольку при начале работы приложения пробрасывается состояние
                    // source.refresh, поэтому реализовано переключение по нему на
                    // SOURCE_LOADING
                    loadState.source.refresh is LoadState.Loading -> SOURCE_LOADING
                    // Переключение при mediator.append на INITIAL реализовано для того,
                    // чтобы обновление cachedPagingDataFromRepo в PostViewModel не вызывало
                    // излишних срабатываний функции shouldScrollToTop.collectLatest {}
                    loadState.mediator?.append is LoadState.Loading -> INITIAL
                    else -> state
                }
            REMOTE_LOADING ->
                when (loadState.source.refresh) {
                    is LoadState.Loading -> SOURCE_LOADING
                    else -> state
                }
            SOURCE_LOADING ->
                when (loadState.refresh) {
                    // В случае, когда произошла загрузка из БД, но еще
                    // продолжается загрузка по сети, то переключаемся на REMOTE_LOADING,..
                    is LoadState.Loading -> REMOTE_LOADING
                    // ...если же поизошла только загрузка из БД, то - PRESENTED
                    is LoadState.NotLoading -> PRESENTED
                    else -> state
                }
        }
    }
        .distinctUntilChanged()