package ru.netology.nmedia.repository

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import retrofit2.HttpException
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.util.CompanionNotMedia.listToString

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val service: PostApiService,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb
) : RemoteMediator<Int, PostEntity>() {
    override suspend fun load(loadType: LoadType, state: PagingState<Int, PostEntity>): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    Log.d("REFRESH FROM MEDIATOR", "${state.config.prefetchDistance}")
                    service.getLatest(state.config.pageSize)
                }
                // Скролл вверх
                LoadType.PREPEND -> {
                    Log.d("PREPEND/MEDIATOR/RAM", "${state.firstItemOrNull()?.id}")
                    Log.d("PREPEND/MEDIATOR/DB", "${postRemoteKeyDao.max()}")
                    // Теперь вместо запроса из оперативной памяти первого значения
                    // списка, получим его из БД
                    val latestIdOnCurrentPage = postRemoteKeyDao.max()
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
                    Log.d("APPEND/MEDIATOR/RAM", "${state.lastItemOrNull()?.id}")
                    Log.d("APPEND/MEDIATOR/DB", "${postRemoteKeyDao.min()}")
                    val earliestIdOnCurrentPage = postRemoteKeyDao.min()
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
                // надо произвести запись в таблицу ключей...
                appDb.withTransaction {
                    when (loadType) {
                        LoadType.REFRESH -> {
                            postDao.removeAllPosts()
                            postRemoteKeyDao.saveRemoteKey(
                                listOf(
                                    PostRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.AFTER,
                                        body.first().id
                                    ),
                                    PostRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.BEFORE,
                                        body.last().id
                                    )
                                )
                            )
                        }
                        LoadType.PREPEND -> {
                            postRemoteKeyDao.saveRemoteKey(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.AFTER,
                                    body.first().id
                                )
                            )
                        }
                        LoadType.APPEND -> {
                            postRemoteKeyDao.saveRemoteKey(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.BEFORE,
                                    body.last().id
                                )
                            )
                        }
                    }
                    // ...а потом еще и сохранить эти данные в БД
                    postDao.updatePostsByIdFromServer(
                        body.map(PostEntity::fromDto)
                    )
                }
                Log.d("MEDIATOR WRITES", listToString(body))
                return MediatorResult.Success(body.isEmpty())
            } else
                throw HttpException(response)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}