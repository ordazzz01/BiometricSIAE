package com.siae.biometricsiae.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.siae.biometricsiae.data.local.converter.Converters
import com.siae.biometricsiae.data.local.dao.AttendanceDao
import com.siae.biometricsiae.data.local.dao.DeviceDao
import com.siae.biometricsiae.data.local.dao.EmployeeDao
import com.siae.biometricsiae.data.local.dao.IncidentDao
import com.siae.biometricsiae.data.local.dao.SyncQueueDao
import com.siae.biometricsiae.data.local.entity.AttendanceEntity
import com.siae.biometricsiae.data.local.entity.BranchEntity
import com.siae.biometricsiae.data.local.entity.DeviceEntity
import com.siae.biometricsiae.data.local.entity.EmployeeEntity
import com.siae.biometricsiae.data.local.entity.FaceEvidenceEntity
import com.siae.biometricsiae.data.local.entity.IncidentEntity
import com.siae.biometricsiae.data.local.entity.ScheduleEntity
import com.siae.biometricsiae.data.local.entity.SyncQueueEntity

@Database(
    entities = [
        EmployeeEntity::class,
        AttendanceEntity::class,
        DeviceEntity::class,
        BranchEntity::class,
        ScheduleEntity::class,
        IncidentEntity::class,
        SyncQueueEntity::class,
        FaceEvidenceEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun deviceDao(): DeviceDao
    abstract fun incidentDao(): IncidentDao
    abstract fun syncQueueDao(): SyncQueueDao
}
