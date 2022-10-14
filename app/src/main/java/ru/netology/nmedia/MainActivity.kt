package ru.netology.nmedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.*

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val post = Post(
            id = 1,
            author = "Нетология. Университет интернет-профессий будущего",
            published = "21 мая в 18:36",
            content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
            likes = 10,
            shares = 5,
            views = 5
        )
        val postService = PostService()

        binding.apply {
            postService.postFill(this, post)

//            Click Like
            likes.setOnClickListener {
                post.likedByMe = !post.likedByMe
                likes.setImageResource(
                    if (post.likedByMe) {
                        post.likes++
                        R.drawable.ic_liked_24
                    }
                    else {
                        post.likes--
                        R.drawable.ic_baseline_favorite_border_24
                    }
                )
                likesCount.text = post.likes.toString()
            }

            share.setOnClickListener {
                post.shares += 500
                sharesCount.text = postService.countDisplay(post.shares)
            }

            unshare.setOnClickListener {
                post.shares -= 500
                sharesCount.text = postService.countDisplay(post.shares)
            }

            views.setOnClickListener {
                post.views += 100_000
                viewsCount.text = postService.countDisplay(post.views)
            }

            unview.setOnClickListener {
                post.views -= 100_000
                viewsCount.text = postService.countDisplay(post.views)
            }
        }
    }
}