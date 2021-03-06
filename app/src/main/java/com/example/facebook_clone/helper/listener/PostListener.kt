package com.example.facebook_clone.helper.listener

import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.react.React


interface PostListener{

    fun onReactButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        reacted: Boolean,
        currentReact: React?,
        postPosition: Int,
        notifiedToken: String? = null
    )

    fun onReactButtonLongClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        reacted: Boolean,
        currentReact: React?,
        postPosition: Int,
        notifiedToken: String? = null
    )

    fun onCommentButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    )

    fun onShareButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int,
        notifiedToken: String? = null
    )

    fun onReactLayoutClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    )

    fun onMediaPostClicked(mediaUrl: String)

    fun onPostMoreDotsClicked(interactorId: String, post: Post, shared: Boolean?)

    fun onSharedPostClicked(originalPostPublisherId: String, postId: String)
}