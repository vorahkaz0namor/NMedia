package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.util.CompanionNotMedia.ATTACHMENT_PREVIEW
import ru.netology.nmedia.util.CompanionNotMedia.ATTACHMENT_URI
import ru.netology.nmedia.util.CompanionNotMedia.POST_CONTENT
import ru.netology.nmedia.util.CompanionNotMedia.POST_ID
import ru.netology.nmedia.adapter.OnInteractionListenerImpl
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.viewmodel.PostViewModel

class FeedFragment : Fragment(R.layout.fragment_feed) {
    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    private var _binding: FragmentFeedBinding? = null
    private val binding: FragmentFeedBinding
        get() = _binding!!
    private lateinit var adapter: PostAdapter
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        subscribe()
        setupListeners()
    }

    private fun initViews() {
        adapter = PostAdapter(OnInteractionListenerImpl(viewModel))
        binding.recyclerView.posts.adapter = adapter
        navController = findNavController()
    }

    private fun subscribe() {
        viewModel.apply {
            data.observe(viewLifecycleOwner) { state ->
                adapter.submitList(state.posts)
                binding.apply {
                    progressBarView.progressBar.isVisible = state.loading
                    errorView.errorGroup.isVisible = state.error
                    emptyTextView.emptyText.isVisible = state.empty
                    recyclerView.postsList.isVisible = state.showing
                }
            }
            edited.observe(viewLifecycleOwner) { post ->
                if (post.id != 0L)
                    navController.navigate(
                        R.id.action_feedFragment_to_newPostFragment,
                        Bundle().apply { POST_CONTENT = post.content }
                    )
            }
            hasShared.observe(viewLifecycleOwner) { post ->
                if (post.id != 0L)
                    navController.navigate(
                        R.id.action_feedFragment_to_sharePostFragment,
                        Bundle().apply { POST_CONTENT = post.content }
                    )
            }
            singlePostToView.observe(viewLifecycleOwner) { post ->
                if (post.id != 0L) {
                    navController.navigate(
                        R.id.action_feedFragment_to_singlePostFragment,
                        Bundle().apply {
                            POST_ID = post.id
                            ATTACHMENT_PREVIEW = "Post view"
                        }
                    )
                }
            }
            viewingAttachments.observe(viewLifecycleOwner) { post ->
                if (post.id != 0L) {
                    navController.navigate(
                        R.id.action_feedFragment_to_attachmentsFragment,
                        Bundle().apply {
                            POST_CONTENT = post.content
                            // Временно организовано определение preview по имени автора
                            ATTACHMENT_PREVIEW = post.author
                            ATTACHMENT_URI = post.attachments ?: "https://"
                        }
                    )
                }
            }
            postEvent.observe(viewLifecycleOwner) { loadPosts() }
        }
    }

    private fun setupListeners() {
        binding.apply {
            recyclerView.addNewPost.setOnClickListener {
                navController.navigate(
                    R.id.action_feedFragment_to_newPostFragment
                )
            }
            errorView.retryButton.setOnClickListener {
                viewModel.loadPosts()
            }
            recyclerView.refreshPosts.setOnRefreshListener {
                recyclerView.refreshPosts.isRefreshing = false
                viewModel.loadPosts()
            }
        }
    }
}