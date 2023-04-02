package ru.netology.nmedia.activity

import android.os.Bundle
import android.text.util.Linkify
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.map
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import okhttp3.internal.http.HTTP_OK
import ru.netology.nmedia.R
import ru.netology.nmedia.util.CompanionNotMedia.ATTACHMENT_PREVIEW
import ru.netology.nmedia.util.CompanionNotMedia.POST_CONTENT
import ru.netology.nmedia.util.CompanionNotMedia.POST_ID
import ru.netology.nmedia.adapter.OnInteractionListenerImpl
import ru.netology.nmedia.adapter.PostViewHolder
import ru.netology.nmedia.util.viewBinding
import ru.netology.nmedia.databinding.SingleCardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.CompanionNotMedia.overview
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

class SinglePostFragment : Fragment(R.layout.single_card_post) {
    private val viewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    private val binding by viewBinding(SingleCardPostBinding::bind)
    private val postBind = { post: Post ->
        PostViewHolder(
            binding.singlePost,
            OnInteractionListenerImpl( viewModel, authViewModel )
        )
            .bind(post)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            viewModel.flowPosts()
            findNavController().navigateUp()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        subscribe()
        setupListeners()
    }

    private fun initViews() {
        if (arguments?.ATTACHMENT_PREVIEW == "Post view") {
            viewModel.viewById(arguments?.POST_ID!!)
            arguments?.ATTACHMENT_PREVIEW = ""
        }
        binding.singlePost.content.autoLinkMask = Linkify.WEB_URLS
    }

    private fun subscribe() {
        viewModel.apply {
            dataState.observe(viewLifecycleOwner) { state ->
                binding.apply {
                    progressBarView.progressBar.isVisible = state.loading
                    errorView.errorTitle.isVisible = state.error
                    singlePostView.isVisible = state.showing
                    refreshPost.isRefreshing = state.refreshing
                }
            }
            lifecycleScope.launchWhenCreated {
                dataFlow?.collectLatest {
                    // Try to get post from PagingData, but this way don't work
                    var post: Post? = null
                    var dataIds: List<Long> = emptyList()
                    it.map { existingPost ->
                        dataIds = dataIds.plus(existingPost.id)
                        if (existingPost.id == arguments?.POST_ID)
                            post = existingPost
                    }
                    Log.d("POST_ID", "${arguments?.POST_ID}")
                    Log.d("DATA IDs", "$dataIds")
                    Log.d("SINGLE POST", "${post?.id}")
                    if (post != null)
                        postBind(post!!)
                    else
                        findNavController().navigateUp()
                }
            }
            postEvent.observe(viewLifecycleOwner) { code ->
                if (code != HTTP_OK)
                    Snackbar.make(
                        binding.root,
                        overview(code),
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction(R.string.retry_loading) {
                            flowPosts()
                        }
                        .show()
            }
            edited.observe(viewLifecycleOwner) { post ->
                if (post.id != 0L)
                    findNavController().navigate(
                        R.id.action_singlePostFragment_to_newPostFragment,
                        Bundle().apply { POST_CONTENT = post.content }
                    )
            }
            hasShared.observe(viewLifecycleOwner) { post ->
                if (post.id != 0L)
                    findNavController().navigate(
                        R.id.action_singlePostFragment_to_sharePostFragment,
                        Bundle().apply { POST_CONTENT = post.content }
                    )
            }
            viewingAttachments.observe(viewLifecycleOwner) { post ->
                if (post.id != 0L) {
                    findNavController().navigate(
                        R.id.action_singlePostFragment_to_attachmentsFragment
                    )
                }
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            refreshPost.setOnRefreshListener {
                viewModel.flowPosts()
            }
        }
    }
}