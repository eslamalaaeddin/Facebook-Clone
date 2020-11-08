package com.example.facebook_clone.di

import com.example.facebook_clone.viewmodel.PasswordFragmentViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val databaseModule = module {
    fun provideDB(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    single { provideDB() }
}

val firebaseAuthModule = module {
    fun provideAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    single { provideAuth() }
}

val firebaseStorageModule = module {
    fun provideStorage() : FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
    single { provideStorage() }
}

////////////////////////////////////////////////////////////////
//Password fragment view model
val passFragViewModelModule = module {
    viewModel {
        PasswordFragmentViewModel(get())
    }

}