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
                    loadState.mediator?.prepend is LoadState.Loading ||
                    loadState.mediator?.append is LoadState.Loading -> REMOTE_LOADING
                    else -> state
                }
            REMOTE_LOADING ->
                when (loadState.source.refresh) {
                    is LoadState.Loading -> SOURCE_LOADING
                    else -> state
                }
            SOURCE_LOADING ->
                when (loadState.source.refresh) {
                    is LoadState.NotLoading -> PRESENTED
                    else -> state
                }
        }
    }
        .distinctUntilChanged()