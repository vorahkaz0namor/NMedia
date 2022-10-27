package ru.netology.nmedia.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListenerImpl
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.util.AndroidUtils
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

    override fun onResume() {
        super.onResume()
        binding.editGroup.visibility = View.INVISIBLE
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
            if (post.id != 0L)
                binding.apply {
                    editContent.requestFocus()
                    editContent.setText(post.content)
                }
        }
    }

    private fun setupListeners() {
        binding.apply {
            saveNewPost.setOnClickListener {
                if (viewModel.savePost(editContent.text))
                    removeFocus()
                else
                    Toast.makeText(
                        this@MainActivity,
                        R.string.empty_content,
                        Toast.LENGTH_SHORT
                    ).show()
            }
            editContent.doAfterTextChanged {
                editGroup.visibility = View.VISIBLE
            }
            cancelEdit.setOnClickListener {
                removeFocus()
            }
        }
    }

    private fun removeFocus() {
        viewModel.clearEditedValue()
        binding.apply {
            editContent.setText("")
            editContent.clearFocus()
            AndroidUtils.hideKeyboard(editContent)
            editGroup.visibility = View.INVISIBLE
        }
    }
}