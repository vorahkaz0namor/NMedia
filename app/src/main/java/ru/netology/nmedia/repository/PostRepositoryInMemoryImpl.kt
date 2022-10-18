package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post

class PostRepositoryInMemoryImpl : PostRepository {
    private var post = Post(
        id = 1,
        author = "Нетология. Университет интернет-профессий будущего",
        published = "21 мая в 18:36",
        content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
        likes = 10,
        shares = 5,
        views = 5
    )
    private val data = MutableLiveData(post)

    override fun get(): LiveData<Post> =data

    override fun like() {
        val changeLikedByMe = !post.likedByMe
        post = post.copy(
            likedByMe = changeLikedByMe,
            likes = if (changeLikedByMe)
                        ++post.likes
                    else
                        --post.likes
        )
        data.value = post
    }

    override fun share() {
        post = post.copy(
            shares = post.shares + 500
        )
        data.value = post
    }

    override fun unshare() {
        val count = post.shares - 500
        post = post.copy(
            shares = if (count < 0)
                         0
                     else
                         count
        )
        data.value = post
    }

    override fun view() {
        post = post.copy(
            views = post.views + 100_000
        )
        data.value = post
    }

    override fun unview() {
        val count = post.views - 100_000
        post = post.copy(
            views = if (count < 0)
                        0
                    else
                        count
        )
        data.value = post
    }
}