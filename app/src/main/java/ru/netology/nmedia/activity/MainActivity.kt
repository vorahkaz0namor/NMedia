package ru.netology.nmedia.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import ru.netology.nmedia.adapter.OnInteractionListenerImpl
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.viewmodel.PostViewModel

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel: PostViewModel by viewModels()
    lateinit var adapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initViews()
        subscribe()
        setupListeners()
    }

    private fun initViews() {
        adapter = PostAdapter(OnInteractionListenerImpl(viewModel))
        binding.posts.adapter = adapter
    }

    private fun subscribe() {
        viewModel.data.observe(this) { posts ->
            adapter.submitList(posts)
        }
        viewModel.edited.observe(this) { post ->
            viewModel.editPostContent(binding, post)
        }
    }

    private fun setupListeners() {
        binding.apply {
            saveNewPost.setOnClickListener {
                viewModel.savePost(binding, this@MainActivity)
            }
            editContent.doAfterTextChanged {
                editGroup.visibility = View.VISIBLE
            }
            cancelEdit.setOnClickListener {
                viewModel.removeFocus(this)
            }
        }
    }
}