package com.example.facebook_clone.repository

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.facebook_clone.helper.Utils.COMMENTS_COLLECTION
import com.example.facebook_clone.helper.Utils.MY_COMMENTS_COLLECTION
import com.example.facebook_clone.helper.Utils.MY_GROUP_POSTS_COLLECTION
import com.example.facebook_clone.helper.Utils.NEWS_FEED_POSTS_COLLECTION
import com.example.facebook_clone.helper.Utils.POSTS_COLLECTION
import com.example.facebook_clone.helper.Utils.PROFILE_POSTS_COLLECTION
import com.example.facebook_clone.helper.Utils.SPECIFIC_NEWS_FEED_POSTS_COLLECTION
import com.example.facebook_clone.helper.Utils.USERS_COLLECTION
import com.example.facebook_clone.livedata.PostLiveData
import com.example.facebook_clone.model.post.comment.Comment
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.comment.ReactionsAndSubComments
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.model.post.share.Share
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.util.*

private const val TAG = "PostsRepository"


class PostsRepository(
    private val database: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) {

    fun createPost(post: Post): Task<Void> {
        return database
            .collection(post.firstCollectionType)
            .document(post.creatorReferenceId)
            .collection(post.secondCollectionType)
            .document(post.id.toString())
            .set(post)
    }

    fun addGroupPostToPosterCollection(post: Post): Task<Void> {
        return database.collection(POSTS_COLLECTION)
            .document(post.publisherId.orEmpty())
            .collection(MY_GROUP_POSTS_COLLECTION)
            .document(post.groupId.toString())
            .collection(MY_GROUP_POSTS_COLLECTION)//this annoys me
            .document(post.id.orEmpty())
            .set(post)
    }

    fun addSharedPostToMyPosts(post: Post, myId: String): Task<Void> {
        return database
            .collection(post.firstCollectionType)
            .document(myId)
            .collection(post.secondCollectionType)
            .document(post.id.toString())
            .set(post)
    }


    fun getUserProfilePostsLiveData(userId: String): LiveData<List<Post>> {
        var posts: MutableList<Post>? = mutableListOf()
        val postsLiveData = MutableLiveData<List<Post>>()
        database.collection(POSTS_COLLECTION).document(userId)
            .collection(PROFILE_POSTS_COLLECTION)
            .orderBy("creationTime", Query.Direction.DESCENDING)
            .addSnapshotListener { postsSnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                } else {
                    posts = postsSnapshot?.toObjects(Post::class.java)
                    postsLiveData.postValue(posts)
                }
            }
        return postsLiveData
    }

    fun addCommentToPostComments(
        post: Post,
        comment: Comment
    ): Task<Void> {
        return database
            .collection(post.firstCollectionType)
            .document(post.creatorReferenceId)
            .collection(post.secondCollectionType)
            .document(post.id.orEmpty())
            .update("comments", FieldValue.arrayUnion(comment))
    }


    fun addCommentIdToCommentsCollection(commenterId: String, commentId: String): Task<Void> {
        val reactionsAndSubComments = ReactionsAndSubComments(null, null)
        return database.collection(COMMENTS_COLLECTION).document(commenterId)
            .collection(MY_COMMENTS_COLLECTION).document(commentId).set(reactionsAndSubComments)

    }

    fun getCommentById(commenterId: String, commentId: String): Task<DocumentSnapshot> {

        return database.collection(COMMENTS_COLLECTION).document(commenterId)
            .collection(MY_COMMENTS_COLLECTION)
            .document(commentId).get()
    }

    fun getCommentLiveDataById(
        commenterId: String,
        commentId: String
    ): LiveData<ReactionsAndSubComments> {
        var reactionsAndSubComments: ReactionsAndSubComments? = null
        val liveData = MutableLiveData<ReactionsAndSubComments>()
        database.collection(COMMENTS_COLLECTION).document(commenterId)
            .collection(MY_COMMENTS_COLLECTION)
            .document(commentId).get().addOnCompleteListener {
                reactionsAndSubComments = it.result?.toObject(ReactionsAndSubComments::class.java)

                liveData.postValue(reactionsAndSubComments)
            }


        return liveData
    }

    fun getCommentUpdates(commenterId: String, commentId: String): DocumentReference {
        return database.collection(COMMENTS_COLLECTION).document(commenterId)
            .collection(MY_COMMENTS_COLLECTION).document(commentId)
    }

    fun addSubCommentToCommentById(
        commenterId: String,
        commentId: String,
        comment: Comment
    ): Task<Void> {
        return database.collection(COMMENTS_COLLECTION).document(commenterId).collection(
            MY_COMMENTS_COLLECTION
        ).document(commentId).update("subComments", FieldValue.arrayUnion(comment))
    }

    fun deleteSubCommentFromCommentById(
        commenterId: String,
        superCommentId: String,
        comment: Comment
    ): Task<Void> {
        return database.collection(COMMENTS_COLLECTION).document(commenterId).collection(
            MY_COMMENTS_COLLECTION
        ).document(superCommentId).update("subComments", FieldValue.arrayRemove(comment))
    }

    fun addReactToReactsListInCommentDocument(
        commenterId: String,
        commentId: String,
        react: React?
    ): Task<Void> {
        return database.collection(COMMENTS_COLLECTION).document(commenterId)
            .collection(MY_COMMENTS_COLLECTION).document(commentId)
            .update("reactions", FieldValue.arrayUnion(react))
    }

    fun removeReactFromReactsListInCommentDocument(
        commenterId: String,
        commentId: String,
        react: React?
    ): Task<Void> {
        return database.collection(COMMENTS_COLLECTION).document(commenterId)
            .collection(MY_COMMENTS_COLLECTION).document(commentId)
            .update("reactions", FieldValue.arrayRemove(react))
    }

    fun addSubCommentToReactsListInCommentDocument(
        commenterId: String,
        commentId: String,
        comment: Comment?
    ): Task<Void> {
        return database.collection(COMMENTS_COLLECTION).document(commenterId)
            .collection(MY_COMMENTS_COLLECTION).document(commentId)
            .update("subComments", FieldValue.arrayUnion(comment))
    }

    fun removeSubCommentFromReactsListInCommentDocument(
        commenterId: String,
        commentId: String,
        comment: Comment?
    ): Task<Void> {
        return database.collection(COMMENTS_COLLECTION).document(commenterId)
            .collection(MY_COMMENTS_COLLECTION).document(commentId)
            .update("subComments", FieldValue.arrayRemove(comment))
    }


    //better to be named getPostSnapshotByItsId
    fun getPostById(post: Post): Task<DocumentSnapshot> {
        return database
            .collection(post.firstCollectionType)
            .document(post.creatorReferenceId)
            .collection(post.secondCollectionType)
            .document(post.id.orEmpty()).get()
    }

    fun getPostLiveDataById(post: Post): PostLiveData {

        val postDocRef = database
            .collection(post.firstCollectionType)
            .document(post.creatorReferenceId)
            .collection(post.secondCollectionType)
            .document(post.id.orEmpty())

        return PostLiveData(postDocRef)
    }

    fun updatePostWithNewEdits(post: Post): Task<Void> {
        return database
            .collection(post.firstCollectionType)
            .document(post.creatorReferenceId)
            .collection(post.secondCollectionType)
            .document(post.id.orEmpty())
            .set(post)
    }

    fun updateSharedPostVisibilityWithNewEdits(
        sharerId: String,
        sharedPostId: String,
        post: Post,
        visibility: Int
    ): Task<Void> {
        return database
            .collection(post.firstCollectionType)
            .document(sharerId)
            .collection(post.secondCollectionType)
            .document(sharedPostId)
            .update("visibility", visibility)
    }

    fun deletePost(post: Post): Task<Void> {
        return database
            .collection(post.firstCollectionType)
            .document(post.creatorReferenceId)
            .collection(post.secondCollectionType)
            .document(post.id.orEmpty())
            .delete()
    }

    fun deleteCommentFromPost(
        comment: Comment,
        post: Post
    ): Task<Void> {
        return database
            .collection(post.firstCollectionType)
            .document(post.creatorReferenceId)
            .collection(post.secondCollectionType)
            .document(post.id.orEmpty())
            .update("comments", FieldValue.arrayRemove(comment))
    }

    fun deleteCommentDocumentFromCommentsCollection(
        commenterId: String,
        commentId: String
    ): Task<Void> {
        return database.collection(COMMENTS_COLLECTION).document(commenterId).collection(
            MY_COMMENTS_COLLECTION
        ).document(commentId).delete()
    }

    fun updatePostComment(comment: Comment, post: Post): Task<Void> {
        return database
            .collection(post.firstCollectionType)
            .document(post.creatorReferenceId)
            .collection(post.secondCollectionType)
            .document(post.id.orEmpty())
            .update("comments", FieldValue.arrayUnion(comment))
    }

    fun addReactToDB(react: React, post: Post): Task<Void> {
        return database
            .collection(post.firstCollectionType)
            .document(post.creatorReferenceId)
            .collection(post.secondCollectionType)
            .document(post.id.orEmpty())
            .update("reacts", FieldValue.arrayUnion(react))
    }

    fun deleteReactFromPost(react: React, post: Post): Task<Void> {
        return database
            .collection(post.firstCollectionType)
            .document(post.creatorReferenceId)
            .collection(post.secondCollectionType)
            .document(post.id.orEmpty())
            .update("reacts", FieldValue.arrayRemove(react))
    }

    fun addReactOnCommentToDB(react: React, postId: String, postPublisherId: String): Task<Void> {
        return database.collection(POSTS_COLLECTION).document(postPublisherId).collection(
            PROFILE_POSTS_COLLECTION
        ).document(postId).update("reacts", FieldValue.arrayUnion(react))
    }

    fun deleteCommentReactFromDB(
        react: React,
        postId: String,
        postPublisherId: String
    ): Task<Void> {
        return database.collection(POSTS_COLLECTION).document(postPublisherId).collection(
            PROFILE_POSTS_COLLECTION
        ).document(postId).update("reacts", FieldValue.arrayRemove(react))
    }

    fun getReact(react: React, postId: String, postPublisherId: String): Task<Void> {
        return database.collection(POSTS_COLLECTION).document(postPublisherId).collection(
            PROFILE_POSTS_COLLECTION
        ).document(postId).update("reacts", FieldValue.arrayRemove(react))
    }

    fun addShareToPost(share: Share, post: Post): Task<Void> {
        return database
            .collection(post.firstCollectionType)
            .document(post.creatorReferenceId)
            .collection(post.secondCollectionType)
            .document(post.id.orEmpty())
            .update("shares", FieldValue.arrayUnion(share))
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////

    fun uploadPostImageToCloudStorage(bitmap: Bitmap): UploadTask {
        val userId = auth.currentUser?.uid.toString()

        val firebaseStorageRef =
            storage.reference.child(userId).child("Post images").child("${UUID.randomUUID()}.jpeg")

        val byteArrayOutputStream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

        return firebaseStorageRef.putBytes(byteArrayOutputStream.toByteArray())

    }

    fun uploadPostVideoToCloudStorage(videoUri: Uri): UploadTask {
        val userId = auth.currentUser?.uid.toString()

        val firebaseStorageRef =
            storage.reference.child(userId).child("Post videos").child("${UUID.randomUUID()}.mp4")

        return firebaseStorageRef.putFile(videoUri)

    }

    fun uploadImageCommentToCloudStorage(bitmap: Bitmap): UploadTask {
        val userId = auth.currentUser?.uid.toString()

        val firebaseStorageRef =
            storage.reference.child(userId).child("Post images").child("comments")
                .child("${UUID.randomUUID()}.jpeg")

        val byteArrayOutputStream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

        return firebaseStorageRef.putBytes(byteArrayOutputStream.toByteArray())

    }

    fun uploadVideoCommentToCloudStorage(videoUri: Uri): UploadTask {
        val userId = auth.currentUser?.uid.toString()

        val firebaseStorageRef =
            storage.reference.child(userId).child("Post videos").child("comments")
                .child("${UUID.randomUUID()}.mp4")

        return firebaseStorageRef.putFile(videoUri)

    }

    fun updateReactedValue(postPublisherId: String, postId: String, reacted: Int?): Task<Void> {
        return database.collection(POSTS_COLLECTION).document(postPublisherId).collection(
            PROFILE_POSTS_COLLECTION
        ).document(postId).update("reacted", reacted)
    }

    fun updateTokenInProfilePost(userId: String, token: String) {
        database
            .collection(POSTS_COLLECTION)
            .document(userId)
            .collection(PROFILE_POSTS_COLLECTION)
            .get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val querySnapshots = task.result
                if (querySnapshots != null) {
                    for (doc in querySnapshots) {
                        database
                            .collection(POSTS_COLLECTION)
                            .document(userId)
                            .collection(PROFILE_POSTS_COLLECTION)
                            .document(doc.id)
                            .update("publisherToken", token)
                    }
                }
            }
        }
    }

    fun updateTokenInGroupPost(userId: String, groupId: String, token: String){
        database
            .collection(POSTS_COLLECTION)
            .document(userId)
            .collection(MY_GROUP_POSTS_COLLECTION)
            .document(groupId)
            .collection(MY_GROUP_POSTS_COLLECTION)
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshots = task.result
                    if (querySnapshots != null) {
                        for (doc in querySnapshots) {
                            database
                                .collection(POSTS_COLLECTION)
                                .document(userId)
                                .collection(MY_GROUP_POSTS_COLLECTION)
                                .document(groupId)
                                .collection(MY_GROUP_POSTS_COLLECTION)
                                .document(doc.id)
                                .update("publisherToken", token)
                        }
                    }
                }
            }
    }


    fun updateTokenInComment(userId: String, token: String) {
        database.collection(COMMENTS_COLLECTION).document(userId).collection(
            MY_COMMENTS_COLLECTION
        ).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val querySnapshots = task.result
                if (querySnapshots != null) {
                    for (doc in querySnapshots) {
                        database.collection(COMMENTS_COLLECTION).document(userId)
                            .collection(MY_COMMENTS_COLLECTION)
                            .document(doc.id).update("commenterToken", token)
                    }
                }
            }
        }

    }

    ///////////////////////////////////////////////// NEWS FEED /////////////////////////////////////
    fun getUserNewsFeedPostsLiveData(userId: String): LiveData<List<Post>> {
        var posts: MutableList<Post>? = mutableListOf()
        val postsLiveData = MutableLiveData<List<Post>>()
        database.collection(NEWS_FEED_POSTS_COLLECTION).document(userId)
            .collection(SPECIFIC_NEWS_FEED_POSTS_COLLECTION)
            .orderBy("creationTime", Query.Direction.DESCENDING)
            .addSnapshotListener { postsSnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                } else {
                    posts = postsSnapshot?.toObjects(Post::class.java)
                    postsLiveData.postValue(posts)
                }
            }
        return postsLiveData
    }
}
