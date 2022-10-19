package ru.netology.nmedia.dto

import android.icu.math.BigDecimal
import android.icu.math.BigDecimal.ROUND_DOWN
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityMainBinding

class PostService {

    fun postFill(binding: ActivityMainBinding, post: Post) {
        binding.apply {
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
            likesCount.text = post.likes.toString()
            sharesCount.text = countDisplay(post.shares)
            viewsCount.text = countDisplay(post.views)
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
            ROUND_DOWN
        )}${digitsMap.getValue(divisor)}"
    }
}