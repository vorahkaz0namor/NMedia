package ru.netology.nmedia.api

import com.google.android.gms.common.GoogleApiAvailability
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface GoogleApiAvailabilityModule {
    @Singleton
    @Binds
    fun bindsGoggleApiAvailability(
        impl: GoogleApiAvailabilityImpl
    ): GoogleApiAvailability
}