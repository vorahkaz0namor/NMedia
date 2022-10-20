package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post

class PostRepositoryInMemoryImpl : PostRepository {
    private var posts = mutableListOf(
        Post(
            id = 3,
            author = "Нетология. Университет интернет-профессий будущего",
            published = "19 октября в 13:27",
            content = "Системный администратор — специалист, который отвечает за стабильное и безотказное функционирование IT-инфраструктуры, занимается настройкой сетей, мониторингом, следит за безопасностью данных, а также проводит инвентаризацию и обновление программного обеспечения компании. Курс даст углубленные знания и подготовит вас к работе: обучитесь системному администрированию на практике, получите возможность найти работу уже во время обучения, будете знать больше, чем нужно работодателям, изучите современные инструменты для работы с инфраструктурой, получите углубленные знания основ администрирования Linux, откроете новые возможности с помощью английского языка.",
            likes = 9,
            shares = 5,
            views = 7
        ),
        Post(
            id = 2,
            author = "Нетология. Университет интернет-профессий будущего",
            published = "11 августа в 16:51",
            content = "Android — самая популярная мобильная платформа. Android-разработчики востребованы всё больше: согласно Statcounter, Android занимает больше 70% рынка мобильных устройств, и число пользователей во всём мире растёт каждый год. За время курса вы создадите полноценное приложение под Android — социальную сеть формата LinkedIn с размещением постов, информацией о профессиональных связях, местах работы и чекинах. Такой проект позволит вам применить разные возможности Kotlin, включая работу с серверной частью и локальной базой данных, с камерой смартфона и его GPS-модулем.",
            likes = 31,
            shares = 7,
            views = 23
        ),
        Post(
            id = 1,
            author = "Нетология. Университет интернет-профессий будущего",
            published = "21 мая в 18:36",
            content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
            likes = 100,
            shares = 12,
            views = 51
        )
    )
    private val data = MutableLiveData(posts as List<Post>)
    private val postById = { postId: Long ->
        posts.asSequence().withIndex()
            .find { it.value.id == postId }
    }

    override fun getAll(): LiveData<List<Post>> = data

    override fun likeById(id: Long): Boolean {
        val post = postById(id) ?: return false
        val changeLikedByMe = !post.value.likedByMe
        posts[post.index] = post.value.copy(
            likedByMe = changeLikedByMe,
            likes = if (changeLikedByMe)
                ++post.value.likes
            else
                --post.value.likes
        )
        data.value = posts
        return true
    }

    override fun shareById(id: Long): Boolean {
        val post = postById(id) ?: return false
        posts[post.index] = post.value.copy(
            shares = post.value.shares + 500
        )
        data.value = posts
        return true
    }

    override fun viewById(id: Long): Boolean {
        val post = postById(id) ?: return false
        posts[post.index] = post.value.copy(
            views = post.value.views + 100_000
        )
        data.value = posts
        return true
    }
}