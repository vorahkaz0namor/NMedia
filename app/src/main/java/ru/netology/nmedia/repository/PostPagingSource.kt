package ru.netology.nmedia.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import okio.IOException
import retrofit2.HttpException
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.CompanionNotMedia.customLog

class PostPagingSource(
    private val postApiService: PostApiService
) : PagingSource<Long, Post>() {
    // Предназначена для того, чтобы использовать какой-либо ключ
    // при обновлении данных
    override fun getRefreshKey(state: PagingState<Long, Post>): Long? = null

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Post> {
        // Объекты типа LoadParams реализуют действия пользователя:
        // - Refresh (при вызове swipeToRefresh);
        // - Append (пролистывание вниз);
        // - Prepend (пролистывание вверх).
        try {
            val response = when (params) {
                // Свойство loadSize возвращает размер страницы
                is LoadParams.Refresh -> {
                    postApiService.getLatest(
                        count = params.loadSize
                    )
                }
                is LoadParams.Append ->
                    postApiService.getBefore(
                        id = params.key,
                        count = params.loadSize
                    )
                // Если нет необходимости обрабатывать какое-либо событие, тогда
                // можно создать объект LoadResult.Page() с такими аргументами:
                // пустой список, предыдущий ключ - из LoadParams, следующий ключ - null
                // При этом данное событие больше не поступит в функцию load()
                is LoadParams.Prepend ->
                    return LoadResult.Page(
                        data = emptyList(),
                        prevKey = params.key,
                        nextKey = null
                    )
            }
            if (response.isSuccessful) {
                val data = response.body().orEmpty()
                // Здесь в качестве аргументов для функции Page() следует использовать
                // следующие: data - список полученных постов, предыдущий ключ - тот ключ,
                // который был использован для получения данной страницы (params.key),
                // следующий ключ - ключ, который будет использоваться для загрузки следующей
                // страницы (определяется на основе полученной страницы)
                return LoadResult.Page(
                    data = data,
                    prevKey = params.key,
                    nextKey = data.lastOrNull()?.id
                )
            } else
                throw HttpException(response)
        } catch (e: IOException) {
            return LoadResult.Error(e)
        } catch (e: HttpException) {
            customLog("GET BY PAGING", e)
            return LoadResult.Error(e)
        }
    }
}