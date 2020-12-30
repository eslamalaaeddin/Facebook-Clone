package com.example.facebook_clone.helper.notification

import android.util.Log
import com.example.facebook_clone.model.notification.Notification
import com.example.facebook_clone.viewmodel.NotificationsFragmentViewModel
import com.example.facebook_clone.viewmodel.OthersProfileActivityViewModel
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "NotificationsHandler"
class NotificationsHandler(
    var notifierId: String? = null,
    var notifierName: String? = null,
    var notifierImageUrl: String? = null,
    var notifiedId: String? = null,
    var notifiedToken: String? = null,
    var notificationId: String? = null,
    var notificationType: String? = null,
    var postPublisherId: String? = null,
    var postId: String? = null,
    var groupName: String? = null,
    var reactType: Int? = null,
    var commentPosition: Int? = null,
    var groupId: String? = null,

    private val othersProfileActivityViewModel: OthersProfileActivityViewModel,
    private val notificationsFragmentViewModel: NotificationsFragmentViewModel
) {

    //[1]
   private fun createNotification(): Notification{
        return Notification(
            notificationType = notificationType,
            notifierId = notifierId,
            notifiedId = notifiedId,
            notifierName = notifierName,
            notifierImageUrl = notifierImageUrl,
            postId = postId,
            groupName = groupName,
            groupId = groupId,
            reactType = reactType,
            commentPosition = commentPosition,
            postPublisherId = postPublisherId
        )
    }

    //[2]
    private fun addNotificationIdToNotifiedDocument(){
        val notification = createNotification()
        addNotificationToNotificationCollection(notification).addOnCompleteListener { task ->
            if (task.isSuccessful){
                fireAnyNotification(notification)
                othersProfileActivityViewModel
                    .addNotificationIdToNotifiedDocument(notification.id.toString(), notifiedId!!)
                    .addOnCompleteListener {task2 ->
                        if (task2.isSuccessful){
                            //Notify user
//                            fireAnyNotification(notification)
                        }
                    }
            }
        }

    }

    //[3]
    private fun addNotificationToNotificationCollection(notification: Notification): Task<Void>{
        return othersProfileActivityViewModel.addNotificationToNotificationsCollection(notification, notifiedId!!)
    }

    fun handleNotificationCreationAndFiring(){
        addNotificationIdToNotifiedDocument()
    }

    //[4]
    fun deleteNotificationFromNotificationsCollection(){
        notificationsFragmentViewModel.deleteNotificationById(notifiedId!!, notificationId!!)
    }

    //[5]
    fun deleteNotificationIdFromNotifiedDocument(){
        othersProfileActivityViewModel.deleteNotificationIdFromNotifiedDocument(notificationId!!, notifiedId!!)
    }

    private fun fireAnyNotification(notification: Notification) = CoroutineScope(
        Dispatchers.IO).launch {
        try {
            //userIAmViewing.token.toString()
            val pushNotification = PushNotification(
                data = notification,
                to = notifiedToken
            )
            val response = RetrofitInstance.api.postNotification(pushNotification)
            if(response.isSuccessful) {
                Log.i(TAG, "GOGOG Response: Success")
            } else {
                Log.e(TAG, response.errorBody().toString())
            }
        } catch(e: Exception) {
            Log.e(TAG, e.toString())
        }
    }


}