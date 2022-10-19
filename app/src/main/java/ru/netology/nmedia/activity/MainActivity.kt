package ru.netology.nmedia.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.viewmodel.PostViewModel

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val viewModel: PostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subscribe(PostService())
        setupListeners()
    }

    private fun subscribe(postService: PostService) {
        viewModel.data.observe(this) { post ->
            postService.postFill(binding, post)
        }
    }

    private fun setupListeners() {
        binding.apply {
//            Click Like
            likes.setOnClickListener {
                viewModel.like()
            }

//            Click Share
            share.setOnClickListener {
                viewModel.share()
            }

//            Click Unshare
            unshare.setOnClickListener {
                viewModel.unshare()
            }

//            Click View
            views.setOnClickListener {
                viewModel.view()
            }

//            Click Unview
            unview.setOnClickListener {
                viewModel.unview()
            }
        }
    }
}