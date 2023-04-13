package ru.netology.nmedia.repository

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.LoadType.*
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import retrofit2.HttpException
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val postApiService: PostApiService,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb
) : RemoteMediator<Int, PostEntity>() {
    override suspend fun load(loadType: LoadType, state: PagingState<Int, PostEntity>): MediatorResult {
        try {
            // Теперь вместо запроса из оперативной памяти первого и
            // последнего значений списка, получим их из БД
            val latestIdOnCurrentPage = postRemoteKeyDao.max()
            val earliestIdOnCurrentPage = postRemoteKeyDao.min()
            val response = when (loadType) {
                REFRESH -> {
                    Log.d("REFRESH FROM MEDIATOR",
                          "latest = $latestIdOnCurrentPage\n" +
                                  "earliest = $earliestIdOnCurrentPage\n" +
                                  "prefetch = ${state.config.prefetchDistance}")
                    postApiService.getLatest(state.config.pageSize)
                }
                // Скролл вверх
                PREPEND -> {
                    // Параметр endOfPaginationReached сообщает о том,
                    // достигнут ли конец/начало страницы или нет.
                    // Здесь, в случае отсутствия latestIdOnCurrentPage
                    // следует указать, что конец/начало страницы еще не достигнут.
                    // Хотя в официальной документации на сайте developer.android.com
                    // в одном из примеров рекоментуется указывать значение true.
//                    latestIdOnCurrentPage ?:
                    return MediatorResult.Success(true)
//                    Log.d("PREPEND FROM MEDIATOR",
//                          "latest = $latestIdOnCurrentPage\nearliest = $earliestIdOnCurrentPage")
//                    postApiService.getAfter(
//                        latestIdOnCurrentPage,
//                        state.config.pageSize
//                    )
                }
                // Скролл вниз
                APPEND -> {
                    earliestIdOnCurrentPage ?: return MediatorResult.Success(false)
                    Log.d("APPEND FROM MEDIATOR",
                          "latest = $latestIdOnCurrentPage\nearliest = $earliestIdOnCurrentPage")
                    postApiService.getBefore(
                        earliestIdOnCurrentPage,
                        state.config.pageSize
                    )
                }
            }
            if (response.isSuccessful) {
                val body = response.body() ?: throw HttpException(response)
                // После получения результата с определенными данными,
                // надо произвести запись в таблицу ключей...
                if (body.isNotEmpty())
                    appDb.withTransaction {
                        val first = body.first().id
                        val last = body.last().id
                        Log.d("IDs FROM BODY", "first = $first\nlast = $last")
                        when (loadType) {
                            REFRESH -> {
                                postRemoteKeyDao.saveRemoteKey(
                                    listOf(
                                        PostRemoteKeyEntity(
                                            PostRemoteKeyEntity.KeyType.AFTER,
                                            first
                                        ),
                                        PostRemoteKeyEntity(
                                            PostRemoteKeyEntity.KeyType.BEFORE,
                                            last
                                        )
                                    )
                                )
                            }
                            PREPEND -> {
                                postRemoteKeyDao.saveRemoteKey(
                                    PostRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.AFTER,
                                        first
                                    )
                                )
                            }
                            APPEND -> {
                                postRemoteKeyDao.saveRemoteKey(
                                    PostRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.BEFORE,
                                        last
                                    )
                                )
                            }
                        }
                        // ...а потом еще и сохранить эти данные в БД
                        updatePostsByIdFromServer(posts = body)
                    }
                return MediatorResult.Success(body.isEmpty())
            } else
                throw HttpException(response)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    private suspend fun updatePostsByIdFromServer(
        posts: List<Post>,
        hidden: Boolean = false
    ) {
        var loadedPosts: List<PostEntity> = emptyList()
        val allExistingPosts = postDao.getAll()
        posts.map { singlePost ->
            val findExistingPost =
                allExistingPosts.find { it.idFromServer == singlePost.id }
            loadedPosts = if (findExistingPost != null)
                loadedPosts.plus(PostEntity.fromDto(
                    singlePost.copy(
                        id = findExistingPost.id,
                        idFromServer = findExistingPost.idFromServer,
                    )
                ).copy(hidden = hidden))
            else
                loadedPosts.plus(PostEntity.fromDto(
                    singlePost.copy(id = 0L, idFromServer = singlePost.id)
                ).copy(hidden = hidden))
        }
        Log.d(
            "CTRL SYNC FROM MEDIATOR",
            "GET FROM SERVER => ${posts.size}\n" +
                    "GET FROM DB => ${allExistingPosts.size}\n" +
                    "max = ${postRemoteKeyDao.max()}\n" +
                    "min = ${postRemoteKeyDao.min()}"
        )
        postDao.updatePostsByIdFromServer(loadedPosts)
    }
}