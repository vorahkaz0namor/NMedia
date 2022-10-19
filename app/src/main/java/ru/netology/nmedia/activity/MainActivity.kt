package ru.netology.nmedia.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.viewmodel.PostViewModel
import java.math.BigDecimal

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val viewModel: PostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subscribe()
    }

    private fun subscribe() {
        viewModel.data.observe(this) { posts ->
            posts.forEach { post ->
                val postBinding = CardPostBinding.inflate(layoutInflater)
                postBinding.apply {
                    author.text = post.author
                    published.text = post.published
                    content.text = post.content
                    avatar.setImageResource(R.drawable.netology)
                    likes.setImageResource(
                        if (post.likedByMe)
                            R.drawable.ic_liked_24
                        else
                            R.drawable.ic_baseline_favorite_border_24
                    )
                    likesCount.text = countDisplay(post.likes)
                    sharesCount.text = countDisplay(post.shares)
                    viewsCount.text = countDisplay(post.views)
                    // Click Like
                    likes.setOnClickListener {
                        viewModel.likeById(post.id)
                    }
                    // Click Share
                    share.setOnClickListener {
                        viewModel.shareById(post.id)
                    }
                    // Click View
                    views.setOnClickListener {
                        viewModel.viewById(post.id)
                    }
                }
                binding.root.addView(postBinding.root)
            }
        }
    }

    private fun countDisplay(count: Int): String {
        if (count < 0) return "0"
        if (count < 1_000) return count.toString()
        val countBigDecimal = BigDecimal(count)
        val digitsMap = mapOf(Pair(1, ""), Pair(1_000, "K"), Pair(1_000_000, "M"))

        val divisor = digitsMap.keys.elementAt(
            if (count >= digitsMap.keys.elementAt(1))
                if (count >= digitsMap.keys.elementAt(2))
                    2
                else 1
            else 0
        )

        return "${countBigDecimal.divide(
            BigDecimal(divisor),
            if (count % divisor == 0 ||
                count % divisor < 100 ||
                count / divisor >= 10) 0 else 1,
            BigDecimal.ROUND_DOWN
        )}${digitsMap.getValue(divisor)}"
    }
}