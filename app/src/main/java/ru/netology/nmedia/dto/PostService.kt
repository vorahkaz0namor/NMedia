package ru.netology.nmedia.dto

import android.icu.math.BigDecimal
import android.icu.math.BigDecimal.ROUND_DOWN
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityMainBinding

class PostService {

    fun postFill(binding: ActivityMainBinding, post: Post) {
        binding.author.text = post.author
        binding.published.text = post.published
        binding.content.text = post.content
        binding.avatar.setImageResource(R.drawable.netology)
        if (post.likedByMe)
            binding.likes.setImageResource(R.drawable.ic_liked_24)
        binding.likesCount.text = post.likes.toString()
        binding.sharesCount.text = post.shares.toString()
        binding.viewsCount.text = post.views.toString()
    }

    fun countDisplay(count: Int): String {
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