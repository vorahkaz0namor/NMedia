package ru.netology.nmedia.repository

import android.icu.text.SimpleDateFormat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.R
import ru.netology.nmedia.dto.*
import java.util.*

open class PostRepositoryInMemoryImpl : PostRepository {
    protected var posts = listOf(
        Post(
            id = 7,
            author = "Проект Гитарин",
            published = "04 ноября в 14:44",
            content = "Гитарин - проект Виталия Каменюченко и Евгения Соколова, который появился в 2011 году на YouTube как обучающий канал для начинающих гитаристов. Со временем Гитарин трансформировался в творческую единицу с авторским материалом.",
            likes = 73,
            shares = 25,
            views = 78,
            attachments = listOf(Attachment(
                id = 0,
                preview = R.drawable.pickovskiy,
                attachment = "https://www.youtube.com/watch?v=NGoITotysLc")
            )
        ),
        Post(
            id = 6,
            author = "Нетология. Университет интернет-профессий будущего",
            published = "19 октября в 13:27",
            content = "Системный администратор — специалист, который отвечает за стабильное и безотказное функционирование IT-инфраструктуры, занимается настройкой сетей, мониторингом, следит за безопасностью данных, а также проводит инвентаризацию и обновление программного обеспечения компании. Курс даст углубленные знания и подготовит вас к работе: обучитесь системному администрированию на практике, получите возможность найти работу уже во время обучения, будете знать больше, чем нужно работодателям, изучите современные инструменты для работы с инфраструктурой, получите углубленные знания основ администрирования Linux, откроете новые возможности с помощью английского языка.",
            likes = 9,
            shares = 5,
            views = 7
        ),
        Post(
            id = 5,
            author = "Нетология. Университет интернет-профессий будущего",
            published = "03 сентября в 10:16",
            content = "Android — самая популярная мобильная платформа. Android-разработчики востребованы всё больше: согласно Statcounter, Android занимает больше 70% рынка мобильных устройств, и число пользователей во всём мире растёт каждый год. За время курса вы создадите полноценное приложение под Android — социальную сеть формата LinkedIn с размещением постов, информацией о профессиональных связях, местах работы и чекинах. Такой проект позволит вам применить разные возможности Kotlin, включая работу с серверной частью и локальной базой данных, с камерой смартфона и его GPS-модулем.",
            likes = 21,
            shares = 7,
            views = 23
        ),
        Post(
            id = 4,
            author = "Нетология. Университет интернет-профессий будущего",
            published = "22 августа в 17:35",
            content = "Тестировщик следит за качеством программного продукта — проверяет, насколько реальное поведение программы соответствует ожиданиям. Спрос на тестировщиков очень высок — каждая команда разработки стремится выпускать качественный продукт без багов. Эта профессия может стать первой ступенью вашей карьеры в IT-проекте или основным делом.",
            likes = 50,
            shares = 12,
            views = 41
        ),
        Post(
            id = 3,
            author = "Нетология. Университет интернет-профессий будущего",
            published = "17 июля в 12:57",
            content = "Java-разработчик. На java работают сайты, CRM-системы, Android-приложения, игры, программы для умных домов и ракет, так как это кроссплатформенный язык программирования. Программисты занимаются созданием таких продуктов, как Tesla, Minecraft, IntelliJ, Deutsche Bank, Barclays и миллионов других. Java-разработчики проектируют и реализуют логику работы приложения.",
            likes = 59,
            shares = 19,
            views = 44
        ),
        Post(
            id = 2,
            author = "Нетология. Университет интернет-профессий будущего",
            published = "29 июня в 19:22",
            content = "iOS-разработчик. Всё больше компаний создают приложения для iOS. Средняя зарплата IOS‑разработчика выше, чем у других мобильных разработчиков. Конкуренция в этом направлении ниже, так как меньше приложений и разработчиков в этой сфере. Вам потребуется меньше года, чтобы получить  навыки для старта карьеры. За 1,5 года junior-специалист может стать middle.",
            likes = 77,
            shares = 34,
            views = 64
        ),
        Post(
            id = 1,
            author = "Нетология. Университет интернет-профессий будущего",
            published = "21 мая в 18:36",
            content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
            likes = 100,
            shares = 52,
            views = 118
        )
    )
    private val data = MutableLiveData(posts)
    protected var nextId: Long = posts.size.toLong() + 1
    private val updatePost = { post: Post, id: Long, block: (Post) -> Post ->
        if (post.id != id)
            post
        else
            block(post)
    }
    private val actualTime = { now: Long ->
        SimpleDateFormat("dd MMMM, H:mm", Locale.US).format(Date(now))
    }

    override fun getAll(): LiveData<List<Post>> = data

    override fun save(post: Post) {
        posts = if (post.id == 0L)
                    listOf(
                        post.copy(
                            id = nextId++,
                            author = "Zakharov Roman, AN-34",
                            published = actualTime(System.currentTimeMillis())
                        )
                    ) + posts
                else
                    if (posts.none { it.id == post.id })
                        (listOf(
                            post.copy(
                                published = actualTime(System.currentTimeMillis())
                            )
                        ) + posts).sortedByDescending { it.id }
                    else
                        posts.map {
                            updatePost(it, post.id) {
                                it.copy(
                                    published = actualTime(System.currentTimeMillis()),
                                    content = post.content
                                )
                            }
                        }
        sync()
    }

    override fun likeById(id: Long): Boolean {
        posts = posts.map {
            updatePost(it, id) {
                it.copy(
                    likedByMe = !it.likedByMe,
                    likes = if (!it.likedByMe)
                                it.likes + 1
                            else
                                it.likes - 1
                )
            }
        }
        sync()
        return true
    }

    override fun shareById(id: Long): Boolean {
        posts = posts.map {
            updatePost(it, id) { it.copy(shares = it.shares + 1) }
        }
        sync()
        return true
    }

    override fun viewById(id: Long): Boolean {
        posts = posts.map {
            updatePost(it, id) { it.copy(views = it.views + 1) }
        }
        sync()
        return true
    }

    override fun removeById(id: Long) {
        posts = posts.filter { it.id != id }
        sync()
    }

    protected open fun sync() {
        data.value = posts
    }
}