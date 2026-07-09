package com.siae.biometricsiae.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.siae.biometricsiae.data.local.entity.EmployeeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees WHERE active = 1 ORDER BY name ASC")
    fun getAllActiveEmployees(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE branchIds LIKE '%' || :branchId || '%' AND active = 1 ORDER BY name ASC")
    fun getEmployeesByBranch(branchId: String): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getEmployeeById(id: String): EmployeeEntity?

    @Query("SELECT * FROM employees WHERE code = :code AND active = 1")
    suspend fun getEmployeeByCode(code: String): EmployeeEntity?

    @Query("SELECT * FROM employees WHERE name LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%'")
    fun searchEmployees(query: String): Flow<List<EmployeeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: EmployeeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployees(employees: List<EmployeeEntity>)

    @Update
    suspend fun updateEmployee(employee: EmployeeEntity)

    @Delete
    suspend fun deleteEmployee(employee: EmployeeEntity)

    @Query("DELETE FROM employees")
    suspend fun deleteAllEmployees()

    @Query("SELECT COUNT(*) FROM employees WHERE active = 1")
    suspend fun getActiveEmployeeCount(): Int
}
