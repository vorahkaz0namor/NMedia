package ru.netology.nmedia.api

import com.google.firebase.messaging.FirebaseMessaging
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface FirebaseMessagingModule {
//    @Singleton
//    @Binds
//    fun bindsFirebaseMessaging(
//        impl: FirebaseMessagingImpl
//    ): FirebaseMessaging
}