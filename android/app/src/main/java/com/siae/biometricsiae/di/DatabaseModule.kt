package com.siae.biometricsiae.di

import android.content.Context
import androidx.room.Room
import com.siae.biometricsiae.data.local.AppDatabase
import com.siae.biometricsiae.data.local.dao.AttendanceDao
import com.siae.biometricsiae.data.local.dao.DeviceDao
import com.siae.biometricsiae.data.local.dao.EmployeeDao
import com.siae.biometricsiae.data.local.dao.IncidentDao
import com.siae.biometricsiae.data.local.dao.SyncQueueDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "biometricsiae.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideEmployeeDao(database: AppDatabase): EmployeeDao {
        return database.employeeDao()
    }

    @Provides
    fun provideAttendanceDao(database: AppDatabase): AttendanceDao {
        return database.attendanceDao()
    }

    @Provides
    fun provideDeviceDao(database: AppDatabase): DeviceDao {
        return database.deviceDao()
    }

    @Provides
    fun provideIncidentDao(database: AppDatabase): IncidentDao {
        return database.incidentDao()
    }

    @Provides
    fun provideSyncQueueDao(database: AppDatabase): SyncQueueDao {
        return database.syncQueueDao()
    }
}
