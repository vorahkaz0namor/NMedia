package ru.netology.nmedia.repository

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import retrofit2.HttpException
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.util.CompanionNotMedia.listToString

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val service: PostApiService,
    private val postDao: PostDao
) : RemoteMediator<Int, PostEntity>() {
    override suspend fun load(loadType: LoadType, state: PagingState<Int, PostEntity>): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> service.getLatest(state.config.pageSize)
                // Скролл вверх
                LoadType.PREPEND -> {
                    val latestIdOnCurrentPage = state.firstItemOrNull()?.id
                        // Параметр endOfPaginationReached сообщает о том,
                        // достигнут ли конец страницы или нет.
                        // Здесь, в случае отсутствия latestIdOnCurrentPage
                        // следует указать, что конец страницы еще не достигнут.
                        ?: return MediatorResult.Success(false)
                    service.getAfter(
                        latestIdOnCurrentPage,
                        state.config.pageSize
                    )
                }
                // Скролл вниз
                LoadType.APPEND -> {
                    val earliestIdOnCurrentPage = state.lastItemOrNull()?.id
                        ?: return MediatorResult.Success(false)
                    service.getBefore(
                        earliestIdOnCurrentPage,
                        state.config.pageSize
                    )
                }
            }
            if (response.isSuccessful) {
                val body = response.body() ?: throw HttpException(response)
                // После получения результата с определенными данными,
                // надо эти данные сохранить в БД
                postDao.updatePostsByIdFromServer(
                    body.map(PostEntity::fromDto)
                )
                Log.d("MEDIATOR WRITES", listToString(body))
                return MediatorResult.Success(body.isEmpty())
            } else
                throw HttpException(response)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}