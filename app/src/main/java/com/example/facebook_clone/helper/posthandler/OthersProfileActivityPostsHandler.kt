package com.example.facebook_clone.helper.posthandler

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.helper.listener.CommentsBottomSheetListener
import com.example.facebook_clone.helper.listener.FriendClickListener
import com.example.facebook_clone.helper.listener.PostListener
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.SharedPost
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.model.post.share.Share
import com.example.facebook_clone.ui.activity.NewsFeedActivity
import com.example.facebook_clone.viewmodel.PostViewModel

private const val FIRST_COLLECTION_TYPE = Utils.POSTS_COLLECTION
private const val SECOND_COLLECTION_TYPE = Utils.PROFILE_POSTS_COLLECTION
private const val TAG = "OthersProfileActivityPo"
class OthersProfileActivityPostsHandler(private val context: Context, private val postViewModel: PostViewModel):
    BasePostHandler(context, postViewModel), PostListener,CommentsBottomSheetListener{

    override fun onReactButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        reacted: Boolean,
        currentReact: React?,
        postPosition: Int
    ) {
        currentEditedPostPosition = postPosition
        val modifiedPost = handlePostLocation(post, FIRST_COLLECTION_TYPE, post.publisherId.orEmpty(), SECOND_COLLECTION_TYPE)
        if (!reacted) {
            val myReact = createReact(interactorId, interactorName, interactorImageUrl)
            addReactOnPostToDb(interactorId, myReact, modifiedPost)
        } else {
            deleteReactFromPost(currentReact!!, modifiedPost).addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Utils.toastMessage(context, task.exception?.message.toString())
                }
            }
        }
    }

    override fun onReactButtonLongClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        reacted: Boolean,
        currentReact: React?,
        postPosition: Int
    ) {

        val modifiedPost = handlePostLocation(post, FIRST_COLLECTION_TYPE, post.publisherId.orEmpty(), SECOND_COLLECTION_TYPE)
        currentEditedPostPosition = postPosition
        showReactsChooserDialog(
            interactorId,
            interactorName,
            interactorImageUrl,
            modifiedPost,
            currentReact
        )
    }

    override fun onCommentButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {
        val modifiedPost = handlePostLocation(post, FIRST_COLLECTION_TYPE, post.publisherId.orEmpty(), SECOND_COLLECTION_TYPE)
        currentEditedPostPosition = postPosition
        openCommentsBottomSheet(modifiedPost, interactorId, interactorName, interactorImageUrl, postPosition, this)

    }

    override fun onShareButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {
        val modifiedPost = handlePostLocation(post, FIRST_COLLECTION_TYPE, post.publisherId.orEmpty(), SECOND_COLLECTION_TYPE)
        currentEditedPostPosition = postPosition
        val share = Share(
            sharerId = interactorId,
            sharerName = interactorName,
            sharerImageUrl = interactorImageUrl,
            sharedPost = SharedPost(
                id = modifiedPost.id,
                content = modifiedPost.content,
                attachmentUrl = modifiedPost.attachmentUrl,
                attachmentType = modifiedPost.attachmentType,
                publisherId = modifiedPost.publisherId,
                publisherImageUrl = modifiedPost.publisherImageUrl,
                publisherName = modifiedPost.publisherName,
                visibility = modifiedPost.visibility,
                creationTime =modifiedPost.creationTime
            )
        )
        val postId = modifiedPost.id.toString()
        val postPublisherId = modifiedPost.publisherId.toString()
        modifiedPost.shares?.add(share)
        modifiedPost.reacts = null
        modifiedPost.comments = null


        addShareToPost(share, modifiedPost).addOnCompleteListener { task ->
            val myPost = Post(
                id = share.id,
                publisherId = interactorId,
                content = null,
                visibility = 0,
                publisherName = interactorName,
                publisherImageUrl = interactorImageUrl,
                shares = mutableListOf(share),
                reacts = null,
                comments = null,
                //publisherToken = NewsFeedActivity.getTokenFromSharedPreference(context),
                firstCollectionType = Utils.POSTS_COLLECTION,
                secondCollectionType = Utils.PROFILE_POSTS_COLLECTION
            )
            if (task.isSuccessful) {
                //post.shares?.add(share)
                Log.i(TAG, "YOYO onShareButtonClicked: $myPost")

                //this trick is to add recent share data to my post collections also
                addSharedPostToMyPosts(myPost, interactorId)
//                notificationsHandler.also {
//                    it.notificationType = "share"
//                    it.postId = postId
//                    it.handleNotificationCreationAndFiring()
//                }
                Toast.makeText(context, "Notify him", Toast.LENGTH_SHORT).show()
            } else {
                Utils.toastMessage(context, task.exception?.message.toString())
            }
        }
    }

    private fun addSharedPostToMyPosts(post: Post, myId: String){
        postViewModel.addSharedPostToMyPosts(post, myId)
    }

    override fun onReactLayoutClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {
        val modifiedPost = handlePostLocation(post, FIRST_COLLECTION_TYPE, post.publisherId.orEmpty(), SECOND_COLLECTION_TYPE)
        currentEditedPostPosition = postPosition
        openCommentsBottomSheet(modifiedPost, interactorId, interactorName, interactorImageUrl, postPosition, this)
    }

    override fun onMediaPostClicked(mediaUrl: String) {
        handleMediaClicks(mediaUrl)
    }

    override fun onPostMoreDotsClicked(post: Post, shared: Boolean?) {

    }

    override fun onSharedPostClicked(originalPostPublisherId: String, postId: String) {

    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    override fun onAnotherUserCommented(commentPosition: Int, commentId: String, postId: String) {
        Log.i(TAG, "ISLAM onAnotherUserCommented: $commentPosition")
        Log.i(TAG, "ISLAM onAnotherUserCommented: $commentId")
        Toast.makeText(context, "Notify him", Toast.LENGTH_SHORT).show()
    }


}