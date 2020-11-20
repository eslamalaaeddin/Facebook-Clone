package com.example.facebook_clone.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.model.post.share.Share
import com.example.facebook_clone.repository.PostsRepository
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.UploadTask

class PostViewModel(private val repository: PostsRepository): ViewModel() {

    fun createPost(post: Post): Task<Void>{
        return repository.createPost(post)
    }

    fun getPostsByUserId(userId: String) : FirestoreRecyclerOptions<Post> {
        return repository.getPostsByUserId(userId)
    }

    fun getPostsWithoutOptions(userId: String) : LiveData<List<Post>> {
        return repository.getPostsWithoutOptions(userId)
    }

    fun createComment(postId: String,postPublisherId: String, comment: Comment): Task<Void>{
        return repository.createComment(postId,postPublisherId, comment)
    }

    fun getCommentById(postPublisherId: String, commentId: String): Task<DocumentSnapshot>{
        return repository.getCommentById(postPublisherId, commentId)
    }

    fun addCommentIdToCommentsCollection( postPublisherId: String,commentId: String): Task<Void>{
        return repository.addCommentIdToCommentsCollection(postPublisherId, commentId)
    }

    fun addReactToReactsListInCommentDocument(postPublisherId: String,commentId: String, react: React?): Task<Void>{
        return repository.addReactToReactsListInCommentDocument(postPublisherId, commentId, react)
    }

    fun removeReactFromReactsListInCommentDocument(postPublisherId: String,commentId: String, react: React?): Task<Void>{
        return repository.removeReactFromReactsListInCommentDocument(postPublisherId, commentId, react)
    }

    fun addSubCommentToReactsListInCommentDocument(postPublisherId: String,commentId: String, comment: Comment?): Task<Void>{
        return repository.addSubCommentToReactsListInCommentDocument(postPublisherId, commentId, comment)
    }

    fun removeSubCommentFromReactsListInCommentDocument(postPublisherId: String,commentId: String, comment: Comment?): Task<Void>{
        return repository.removeSubCommentFromReactsListInCommentDocument(postPublisherId, commentId, comment)
    }

    fun getPostById(publisherId: String, postId: String): Task<DocumentSnapshot>{
        return repository.getPostById(publisherId, postId)
    }

    fun deleteComment(comment: Comment, postId: String, postPublisherId: String): Task<Void>{
        return repository.deleteComment(comment, postId, postPublisherId)
    }

    fun updateComment(comment: Comment, postId: String, postPublisherId: String): Task<Void>{
        return repository.updateComment(comment, postId, postPublisherId)
    }

    fun addReactToDB(react: React, postId: String, postPublisherId: String): Task<Void>{
        return repository.addReactToDB(react, postId, postPublisherId)
    }

    fun deleteReact(react: React, postId: String, postPublisherId: String): Task<Void>{
        return repository.deleteReact(react, postId, postPublisherId)
    }

    fun createShare(share: Share, postId: String, postPublisherId: String): Task<Void>{
        return repository.createShare(share, postId, postPublisherId)
    }

    fun uploadPostImageToCloudStorage(bitmap: Bitmap): UploadTask{
        return repository.uploadPostImageToCloudStorage(bitmap)
    }

    fun uploadPostVideoToCloudStorage(videoUri: Uri): UploadTask{
        return repository.uploadPostVideoToCloudStorage(videoUri)
    }

    fun uploadImageCommentToCloudStorage(bitmap: Bitmap): UploadTask{
        return repository.uploadImageCommentToCloudStorage(bitmap)
    }

    fun uploadVideoCommentToCloudStorage(videoUri: Uri): UploadTask{
        return repository.uploadVideoCommentToCloudStorage(videoUri)
    }

    fun updateReactedValue(postPublisherId: String, postId: String, reacted: Int?): Task<Void>{
        return repository.updateReactedValue(postPublisherId, postId, reacted)
    }

}