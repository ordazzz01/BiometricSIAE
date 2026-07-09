package com.siae.biometricsiae.di

import com.siae.biometricsiae.data.remote.api.AsistenciasApi
import com.siae.biometricsiae.data.remote.firebase.FirebaseAuthManager
import com.siae.biometricsiae.data.remote.firebase.FirestoreManager
import com.siae.biometricsiae.data.remote.firebase.StorageManager
import com.siae.biometricsiae.data.repository.AttendanceRepository
import com.siae.biometricsiae.data.repository.AuthRepository
import com.siae.biometricsiae.data.repository.DeviceRepository
import com.siae.biometricsiae.data.repository.EmployeeRepository
import com.siae.biometricsiae.data.repository.IncidentRepository
import com.siae.biometricsiae.data.repository.SyncRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuthManager: FirebaseAuthManager,
        api: AsistenciasApi
    ): AuthRepository {
        return AuthRepository(firebaseAuthManager, api)
    }

    @Provides
    @Singleton
    fun provideEmployeeRepository(
        firestoreManager: FirestoreManager,
        api: AsistenciasApi
    ): EmployeeRepository {
        return EmployeeRepository(firestoreManager, api)
    }

    @Provides
    @Singleton
    fun provideAttendanceRepository(
        firestoreManager: FirestoreManager,
        api: AsistenciasApi
    ): AttendanceRepository {
        return AttendanceRepository(firestoreManager, api)
    }

    @Provides
    @Singleton
    fun provideDeviceRepository(
        firestoreManager: FirestoreManager,
        api: AsistenciasApi
    ): DeviceRepository {
        return DeviceRepository(firestoreManager, api)
    }

    @Provides
    @Singleton
    fun provideIncidentRepository(
        firestoreManager: FirestoreManager,
        api: AsistenciasApi
    ): IncidentRepository {
        return IncidentRepository(firestoreManager, api)
    }

    @Provides
    @Singleton
    fun provideSyncRepository(
        firestoreManager: FirestoreManager,
        storageManager: StorageManager,
        api: AsistenciasApi
    ): SyncRepository {
        return SyncRepository(firestoreManager, storageManager, api)
    }
}
