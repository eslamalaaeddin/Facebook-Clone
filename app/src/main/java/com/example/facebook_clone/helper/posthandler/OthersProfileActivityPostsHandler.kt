package com.example.facebook_clone.helper.posthandler

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.helper.listener.CommentsBottomSheetListener
import com.example.facebook_clone.helper.listener.PostListener
import com.example.facebook_clone.helper.notification.NotificationsHandler
import com.example.facebook_clone.model.post.Post
import com.example.facebook_clone.model.post.SharedPost
import com.example.facebook_clone.model.post.react.React
import com.example.facebook_clone.model.post.share.Share
import com.example.facebook_clone.viewmodel.fragment.NotificationsFragmentViewModel
import com.example.facebook_clone.viewmodel.activity.OthersProfileActivityViewModel
import com.example.facebook_clone.viewmodel.PostViewModel

private const val FIRST_COLLECTION_TYPE = Utils.POSTS_COLLECTION
private const val SECOND_COLLECTION_TYPE = Utils.PROFILE_POSTS_COLLECTION
private const val TAG = "OthersProfileActivityPo"

class OthersProfileActivityPostsHandler(
    private val context: Context,
    private val postViewModel: PostViewModel,
    private val notificationsFragmentViewModel: NotificationsFragmentViewModel,
    private val othersProfileActivityViewModel: OthersProfileActivityViewModel,
//    private val notifiedToken: String
) :
    BasePostHandler(
        context,
        postViewModel,
        notificationsFragmentViewModel,
        othersProfileActivityViewModel
    ), PostListener, CommentsBottomSheetListener {

    private val notificationsHandler = NotificationsHandler(
        notificationsFragmentViewModel = notificationsFragmentViewModel,
        othersProfileActivityViewModel = othersProfileActivityViewModel
    )

    fun dummy(name: String) {
        Toast.makeText(context, name, Toast.LENGTH_SHORT).show()
    }

    override fun onReactButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        reacted: Boolean,
        currentReact: React?,
        postPosition: Int,
        notifiedToken: String?
    ) {
        currentEditedPostPosition = postPosition

        if (!reacted) {
            val myReact = createReact(interactorId, interactorName, interactorImageUrl)
            val notificationsHandler = buildNotificationHandlerForPostReacts(
                notifierId = interactorId,
                notifierName = interactorName,
                notifierImageUrl = interactorImageUrl,
                notifiedId = post.publisherId.orEmpty(),
                notifiedToken = notifiedToken.orEmpty(),
                notificationType = "reactOnPost",
                postPublisherId = post.publisherId.orEmpty(),
                postId = post.id.orEmpty(),
                firstCollectionType = post.firstCollectionType,
                creatorReferenceId = post.creatorReferenceId,
                secondCollectionType = post.secondCollectionType,
            )

            notificationsHandler.reactType = 1
            addReactOnPostToDb(interactorId, myReact, post, notificationsHandler)
        } else {
            deleteReactFromPost(currentReact!!, post).addOnCompleteListener { task ->
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
        postPosition: Int,
        notifiedToken: String?
    ) {

        val notificationsHandler = buildNotificationHandlerForPostReacts(
            notifierId = interactorId,
            notifierName = interactorName,
            notifierImageUrl = interactorImageUrl,
            notifiedId = post.publisherId.orEmpty(),
            notifiedToken = notifiedToken.orEmpty(),
            notificationType = "reactOnPost",
            postPublisherId = post.publisherId.orEmpty(),
            postId = post.id.orEmpty(),
            firstCollectionType = post.firstCollectionType,
            creatorReferenceId = post.creatorReferenceId,
            secondCollectionType = post.secondCollectionType,
        )
        currentEditedPostPosition = postPosition
        showReactsChooserDialog(
            interactorId,
            interactorName,
            interactorImageUrl,
            post,
            currentReact,
            notificationsHandler
        )
    }

    override fun onCommentButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {
//        val modifiedPost = handlePostLocation(
//            post,
//            FIRST_COLLECTION_TYPE,
//            post.publisherId.orEmpty(),
//            SECOND_COLLECTION_TYPE
//        )
        currentEditedPostPosition = postPosition
        openCommentsBottomSheet(
            post,
            interactorId,
            interactorName,
            interactorImageUrl,
            postPosition,
            this
        )

    }

    override fun onShareButtonClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int,
        notifiedToken: String?
    ) {
//        val modifiedPost = handlePostLocation(
//            post,
//            FIRST_COLLECTION_TYPE,
//            post.publisherId.orEmpty(),
//            SECOND_COLLECTION_TYPE
//        )

        val notificationsHandler =
            buildNotificationHandlerForPostShares(
                post,
                interactorId,
                interactorName,
                interactorImageUrl,
                postPosition,
                notifiedToken.orEmpty(),
                firstCollectionType = post.firstCollectionType,
                creatorReferenceId = post.creatorReferenceId,
                secondCollectionType = post.secondCollectionType,
            )

        currentEditedPostPosition = postPosition
        val share = Share(
            sharerId = interactorId,
            sharerName = interactorName,
            sharerImageUrl = interactorImageUrl,
            sharedPost = SharedPost(
                id = post.id,
                content = post.content,
                attachmentUrl = post.attachmentUrl,
                attachmentType = post.attachmentType,
                publisherId = post.publisherId,
                publisherImageUrl = post.publisherImageUrl,
                publisherName = post.publisherName,
                visibility = post.visibility,
                creationTime = post.creationTime
            )
        )
        val postId = post.id.toString()
        val postPublisherId = post.publisherId.toString()
        post.shares?.add(share)
        post.reacts = null
        post.comments = null


        addShareToPost(share, post).addOnCompleteListener { task ->
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
                creatorReferenceId = share.sharerId.orEmpty(),
                secondCollectionType = Utils.PROFILE_POSTS_COLLECTION
            )
            if (task.isSuccessful) {
                //post.shares?.add(share)
                Log.i(TAG, "YOYO onShareButtonClicked: $myPost")

                //this trick is to add recent share data to my post collections also
                addSharedPostToMyPosts(myPost, interactorId)
                notificationsHandler.handleNotificationCreationAndFiring()
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

    private fun addSharedPostToMyPosts(post: Post, myId: String) {
        postViewModel.addSharedPostToMyPosts(post, myId)
    }

    override fun onReactLayoutClicked(
        post: Post,
        interactorId: String,
        interactorName: String,
        interactorImageUrl: String,
        postPosition: Int
    ) {

        currentEditedPostPosition = postPosition
        openCommentsBottomSheet(
            post,
            interactorId,
            interactorName,
            interactorImageUrl,
            postPosition,
            this
        )
    }

    override fun onMediaPostClicked(mediaUrl: String) {
        handleMediaClicks(mediaUrl)
    }

    override fun onPostMoreDotsClicked(interactorId: String, post: Post, shared: Boolean?) {

    }

    override fun onSharedPostClicked(originalPostPublisherId: String, postId: String) {

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    override fun onAnotherUserCommented(
        notifierId: String,
        notifierName: String,
        notifierImageUrl: String,
        notifiedId: String,
        notifiedToken: String,
        notificationType: String,
        postPublisherId: String,
        postId: String,
        firstCollectionType: String,
        creatorReferenceId: String,
        secondCollectionType: String,
        commentId: String
    ) {

        val notificationsHandler = buildNotificationHandlerForPostComments(
            notifierId,
            notifierName,
            notifierImageUrl,
            notifiedId,
            notifiedToken,
            notificationType,
            postPublisherId,
            postId,
            firstCollectionType,
            creatorReferenceId,
            secondCollectionType,
            commentId
        )
        notificationsHandler.handleNotificationCreationAndFiring()
    }


}