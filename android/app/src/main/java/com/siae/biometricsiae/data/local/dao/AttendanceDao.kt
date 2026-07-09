package com.siae.biometricsiae.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.siae.biometricsiae.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentRecords(limit: Int = 100): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records WHERE employeeId = :employeeId ORDER BY timestamp DESC")
    fun getRecordsByEmployee(employeeId: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records WHERE branchId = :branchId AND timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getRecordsByBranchAndDate(branchId: String, startDate: String, endDate: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records WHERE syncStatus = 'PENDING' ORDER BY timestamp ASC")
    suspend fun getPendingSyncRecords(): List<AttendanceEntity>

    @Query("SELECT * FROM attendance_records WHERE syncStatus = 'PENDING' ORDER BY timestamp ASC")
    fun getPendingSyncRecordsFlow(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records WHERE id = :id")
    suspend fun getRecordById(id: String): AttendanceEntity?

    @Query("SELECT * FROM attendance_records WHERE employeeId = :employeeId AND type = :type AND timestamp BETWEEN :startTime AND :endTime LIMIT 1")
    suspend fun findDuplicate(employeeId: String, type: String, startTime: String, endTime: String): AttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<AttendanceEntity>)

    @Update
    suspend fun updateRecord(record: AttendanceEntity)

    @Query("UPDATE attendance_records SET syncStatus = :status, serverTimestamp = :serverTimestamp WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String, serverTimestamp: String?)

    @Query("UPDATE attendance_records SET syncStatus = 'FAILED', retryCount = retryCount + 1 WHERE id = :id")
    suspend fun incrementRetryCount(id: String)

    @Query("DELETE FROM attendance_records WHERE syncStatus = 'SYNCED' AND timestamp < :olderThan")
    suspend fun cleanupOldSyncedRecords(olderThan: String)

    @Query("SELECT COUNT(*) FROM attendance_records WHERE syncStatus = 'PENDING'")
    suspend fun getPendingCount(): Int
}
