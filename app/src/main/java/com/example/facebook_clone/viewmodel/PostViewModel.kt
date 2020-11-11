package com.example.facebook_clone.viewmodel

import androidx.lifecycle.ViewModel
import com.example.facebook_clone.model.post.Comment
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.repository.PostsRepository
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot

class PostViewModel(private val repository: PostsRepository): ViewModel() {

    fun createPost(post: Post): Task<Void>{
        return repository.createPost(post)
    }

    fun getPostsByUserId(userId: String) : FirestoreRecyclerOptions<Post> {
        return repository.getPostsByUserId(userId)
    }

    fun createComment(postId: String,postPublisherId: String, comment: Comment): Task<Void>{
        return repository.createComment(postId,postPublisherId, comment)
    }

    fun getCommentsByPostId(publisherId: String, postId: String): Task<DocumentSnapshot>{
        return repository.getCommentsByPostId(publisherId, postId)
    }

    fun deleteComment(comment: Comment, postId: String, postPublisherId: String): Task<Void>{
        return repository.deleteComment(comment, postId, postPublisherId)
    }

    fun updateComment(comment: Comment, postId: String, postPublisherId: String): Task<Void>{
        return repository.updateComment(comment, postId, postPublisherId)
    }

}