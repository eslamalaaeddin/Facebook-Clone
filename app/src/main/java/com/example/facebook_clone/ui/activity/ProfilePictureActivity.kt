package com.example.facebook_clone.ui.activity

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.facebook_clone.R
import com.example.facebook_clone.helper.Utils
import com.example.facebook_clone.model.user.recentloggedinuser.RecentLoggedInUser
import com.example.facebook_clone.viewmodel.activity.NewsFeedActivityViewModel
import com.example.facebook_clone.viewmodel.activity.ProfilePictureActivityViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_profile_picture.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


private const val REQUEST_CODE = 159
private const val IMAGE_REQUEST_CODE = 123
private const val TAG = "ProfilePictureActivity"

class ProfilePictureActivity : AppCompatActivity() {
    private val profilePictureActivityViewModel by viewModel<ProfilePictureActivityViewModel>()
    private val newsFeedActivityViewModel by viewModel<NewsFeedActivityViewModel>()
    private val auth: FirebaseAuth by inject()
//    private var progressDialog: ProgressDialog? = null
    private lateinit var userName: String
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var token: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_picture)

        val gender = intent.getStringExtra("gender").toString()
         userName = intent.getStringExtra("name").toString()
         email = intent.getStringExtra("email").toString()
         password = intent.getStringExtra("password").toString()
         token = intent.getStringExtra("token").toString()



        if (gender == "male"){
            val defaultProfileImageBitmap = BitmapFactory.decodeResource(
                resources,
                R.drawable.hairstyle
            )
            profileImagePreView.setImageBitmap(defaultProfileImageBitmap)
            handleProfileImage(defaultProfileImageBitmap)
        }

        else{
            val defaultProfileImageBitmap = BitmapFactory.decodeResource(
                resources,
                R.drawable.niqab
            )
            profileImagePreView.setImageBitmap(defaultProfileImageBitmap)
            handleProfileImage(defaultProfileImageBitmap)

        }

        val defaultCoverImageBitmap = BitmapFactory.decodeResource(
            resources,
            R.drawable.cover
        )

        handleCoverImage(defaultCoverImageBitmap)


        skipProfilePictureActivity.setOnClickListener {
            navigateToProfileActivity()
        }

        takePhotoButton.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, REQUEST_CODE)
            }
        }

        chooseProfilePictureFromGalleryButton.setOnClickListener {
            val imageIntent = Intent(Intent.ACTION_GET_CONTENT)
            imageIntent.type = "image/*"
            startActivityForResult(
                Intent.createChooser(imageIntent, "Choose an image"),
                IMAGE_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //image from camera
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            profileImagePreView.setImageBitmap(bitmap)
            handleProfileImage(bitmap)
        }
        //image from gallery
        else if(requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK){
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, data?.data!!)
            profileImagePreView.setImageBitmap(bitmap)
            handleProfileImage(bitmap)
        }
    }

    private fun handleProfileImage(bitmap: Bitmap){
        //profileImagePreView.setImageBitmap(bitmap)
        //progressDialog = Utils.showProgressDialog(this, "Please wait...")
        profilePictureActivityViewModel.uploadProfileImageToCloudStorage(bitmap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.storage?.downloadUrl?.addOnSuccessListener { photoUrl ->
                        uploadProfileImageToUserCollection(photoUrl.toString())
                        val currentUser = RecentLoggedInUser(email, password, userName, photoUrl.toString())
                        addUserToRecentUsers(currentUser, token).addOnCompleteListener {
                            if (!it.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    it.exception?.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                       // progressDialog?.dismiss()
                    }
                }
            }
    }

    private fun handleCoverImage(bitmap: Bitmap){
       // profileImagePreView.setImageBitmap(bitmap)
        //progressDialog = Utils.showProgressDialog(this, "Please wait...")
        profilePictureActivityViewModel.uploadCoverImageToCloudStorage(bitmap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.storage?.downloadUrl?.addOnSuccessListener { photoUrl ->
                        uploadCoverImageToUserCollection(photoUrl.toString())
                      //  progressDialog?.dismiss()
                    }
                }
            }
    }

    private fun uploadProfileImageToUserCollection(photoUrl: String) {
        profilePictureActivityViewModel.uploadProfileImageToUserCollection(photoUrl)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                   // Utils.toastMessage(this, "Image updated successfully")
                    //navigateToProfileActivity()
                } else {
                    Utils.toastMessage(this, task.exception?.message.toString())
                }
            }
    }

    private fun uploadCoverImageToUserCollection(photoUrl: String) {
        profilePictureActivityViewModel.uploadCoverImageToUserCollection(photoUrl)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                  //  Utils.toastMessage(this, "Image uploaded successfully")
                } else {
                    Utils.toastMessage(this, task.exception?.message.toString())
                }
            }
    }

    private fun navigateToProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("firstTime", true)
        startActivity(intent)
        finish()
    }

    private fun addUserToRecentUsers(user: RecentLoggedInUser, token: String): Task<Void> {
        return newsFeedActivityViewModel.addUserToRecentLoggedInUsers(user, token)
    }


}