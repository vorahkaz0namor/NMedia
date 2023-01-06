package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity

class PostRepositoryRoomDBImpl(
    private val dao: PostDao
) : PostRepositoryOld {
    override fun getAll(): LiveData<List<Post>> =
        Transformations.map(dao.getAll()) { list ->
            list.map {
                it.toDto()
            }
        }

    override fun getDraftCopy(): String? =
        dao.getDraftCopy()

    override fun saveDraftCopy(content: String?) {
        dao.saveDraftCopy(content)
    }

    override fun save(post: Post) {
        dao.save(PostEntity.fromDto(post))
    }

    override fun likeById(id: Long) {
        dao.likeById(id)
    }

    override fun shareById(id: Long) {
        dao.shareById(id)
    }

    override fun viewById(id: Long) {
        dao.viewById(id)
    }

    override fun removeById(id: Long) {
        dao.removeById(id)
    }
}