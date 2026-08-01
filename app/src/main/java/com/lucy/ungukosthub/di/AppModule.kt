package com.lucy.ungukosthub.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lucy.ungukosthub.data.remote.KamarRemoteDataSource
import com.lucy.ungukosthub.data.remote.KamarRemoteDataSourceImpl
import com.lucy.ungukosthub.data.repository.AuthRepositoryImpl
import com.lucy.ungukosthub.data.repository.KamarRepositoryImpl
import com.lucy.ungukosthub.domain.repository.AuthRepository
import com.lucy.ungukosthub.domain.repository.KamarRepository
import com.lucy.ungukosthub.domain.usecase.GetDaftarKamarUseCase
import com.lucy.ungukosthub.domain.usecase.LoginUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Dependency Injection Module.
 * Mengatur penyediaan (provides) instance untuk Firebase, DataSource, Repository, dan UseCase.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseApp(
        @ApplicationContext context: Context
    ): FirebaseApp {
        return if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context) ?: FirebaseApp.getInstance()
        } else {
            FirebaseApp.getInstance()
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(
        app: FirebaseApp
    ): FirebaseAuth {
        return Firebase.auth(app)
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(
        app: FirebaseApp
    ): FirebaseFirestore {
        return Firebase.firestore(app)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth
    ): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideLoginUseCase(
        repository: AuthRepository
    ): LoginUseCase {
        return LoginUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideKamarRemoteDataSource(
        firestore: FirebaseFirestore
    ): KamarRemoteDataSource {
        return KamarRemoteDataSourceImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideKamarRepository(
        remoteDataSource: KamarRemoteDataSource
    ): KamarRepository {
        return KamarRepositoryImpl(remoteDataSource)
    }

    @Provides
    @Singleton
    fun provideGetDaftarKamarUseCase(
        repository: KamarRepository
    ): GetDaftarKamarUseCase {
        return GetDaftarKamarUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideRoomRepository(
        firestore: FirebaseFirestore
    ): com.lucy.ungukosthub.domain.repository.RoomRepository {
        return com.lucy.ungukosthub.data.repository.RoomRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideTenantRepository(
        firestore: FirebaseFirestore
    ): com.lucy.ungukosthub.domain.repository.TenantRepository {
        return com.lucy.ungukosthub.data.repository.TenantRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideFacilityRepository(
        firestore: FirebaseFirestore
    ): com.lucy.ungukosthub.domain.repository.FacilityRepository {
        return com.lucy.ungukosthub.data.repository.FacilityRepositoryImpl(firestore)
    }
}
