package ru.netology.nmedia.activity

import android.os.Bundle
import android.text.util.Linkify
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.util.CompanionNotMedia.ATTACHMENT_PREVIEW
import ru.netology.nmedia.util.CompanionNotMedia.ATTACHMENT_URI
import ru.netology.nmedia.util.CompanionNotMedia.POST_CONTENT
import ru.netology.nmedia.util.CompanionNotMedia.POST_ID
import ru.netology.nmedia.adapter.OnInteractionListenerImpl
import ru.netology.nmedia.adapter.PostViewHolder
import ru.netology.nmedia.util.viewBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel

class SinglePostFragment : Fragment(R.layout.card_post) {
    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    private val binding by viewBinding(CardPostBinding::bind)
    private val postBind = { post: Post ->
        PostViewHolder(binding, OnInteractionListenerImpl(viewModel)).bind(post)
    }

    private fun post() =
        viewModel.data.value?.find { post ->
            post.id == (arguments?.POST_ID)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        subscribe()
    }

    private fun initViews() {
        binding.content.autoLinkMask = Linkify.WEB_URLS
        postBind(post()!!)
    }

    private fun subscribe() {
        viewModel.apply {
            data.observe(viewLifecycleOwner) {
                if (post() != null)
                    postBind(post()!!)
                else
                    findNavController().navigateUp()
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
                    val first = post.attachments.firstOrNull()
                    findNavController().navigate(
                        R.id.action_singlePostFragment_to_attachmentsFragment,
                        Bundle().apply {
                            POST_CONTENT = post.content
                            ATTACHMENT_PREVIEW = first?.preview.toString()
                            ATTACHMENT_URI = first?.attachment ?: "https://"
                        }
                    )
                }
            }
        }
    }
}