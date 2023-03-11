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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import okhttp3.internal.http.HTTP_BAD_REQUEST
import okhttp3.internal.http.HTTP_NOT_FOUND
import okhttp3.internal.http.HTTP_OK
import ru.netology.nmedia.R
import ru.netology.nmedia.util.CompanionNotMedia.ATTACHMENT_PREVIEW
import ru.netology.nmedia.util.CompanionNotMedia.ATTACHMENT_URI
import ru.netology.nmedia.util.CompanionNotMedia.POST_CONTENT
import ru.netology.nmedia.util.CompanionNotMedia.POST_ID
import ru.netology.nmedia.adapter.OnInteractionListenerImpl
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.util.CompanionNotMedia.overview
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

class FeedFragment : Fragment(R.layout.fragment_feed) {
    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    private val authViewModel: AuthViewModel by viewModels(
        ownerProducer = ::requireActivity
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
        adapter = PostAdapter(OnInteractionListenerImpl(viewModel, authViewModel))
        binding.recyclerView.posts.adapter = adapter
        navController = findNavController()
    }

    private fun subscribe() {
        viewModel.apply {
            dataState.observe(viewLifecycleOwner) { state ->
                binding.apply {
                    progressBarView.progressBar.isVisible = state.loading
                    errorView.errorTitle.isVisible = state.error
                    recyclerViewAndEmptyView.isVisible = state.showing
                    recyclerView.refreshPosts.isRefreshing = state.refreshing
                }
            }
            data.observe(viewLifecycleOwner) { data ->
                adapter.submitList(data.posts)
                binding.emptyTextView.emptyText.isVisible = data.empty
            }
            // Добавление плавного скролла при добавлении новых постов
            adapter.registerAdapterDataObserver(
                object : RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        // Если что-то добавилось наверх списка,
                        if (positionStart == 0)
                            // тогда плавно заскроллиться до самого верха
                            binding.recyclerView.posts.smoothScrollToPosition(0)
                    }
                }
            )
            newerCount.observe(viewLifecycleOwner) { count ->
                binding.recyclerView.newPosts.apply {
                    isVisible = (count != null && count != 0)
                }
            }
            postEvent.observe(viewLifecycleOwner) { code ->
                if (code != HTTP_OK)
                    Snackbar.make(
                        binding.root,
                        overview(code),
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                        .setAction(R.string.retry_loading) {
                            loadPosts()
                        }
                        .show()
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
                        R.id.action_feedFragment_to_attachmentsFragment
                    )
                }
            }
        }
        authViewModel.apply {
            data.observe(viewLifecycleOwner) {
                viewModel.refresh()
            }
            checkAuthorized.observe(viewLifecycleOwner) {
                if (it) {
                    if (!authViewModel.authorized)
                        AuthDialogFragment().show(
                            childFragmentManager,
                            AuthDialogFragment.AUTH_TAG
                        )
                }
            }
            authError.observe(viewLifecycleOwner) { code ->
                if ( code != HTTP_OK &&
                    (code != HTTP_BAD_REQUEST || code != HTTP_NOT_FOUND) ) {
                    clearAuthError()
                    viewModel.refresh()
                }
            }
        }

    }

    private fun setupListeners() {
        binding.recyclerView.apply {
            addNewPost.setOnClickListener {
                if (!authViewModel.authorized)
                    AuthDialogFragment().show(
                        childFragmentManager,
                        AuthDialogFragment.AUTH_TAG
                    )
                if (authViewModel.authorized)
                    navController.navigate(
                        R.id.action_feedFragment_to_newPostFragment
                    )
            }
            refreshPosts.setOnRefreshListener {
                viewModel.refresh()
            }
            toLoadSampleImage.setOnClickListener {
                navController.navigate(R.id.action_feedFragment_to_sampleFragment)
            }
            newPosts.setOnClickListener {
                it.isVisible = false
                viewModel.showUnreadPosts()
            }
        }
    }
}