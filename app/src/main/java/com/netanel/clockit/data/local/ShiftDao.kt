package com.netanel.clockit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ShiftDao {
    @Insert suspend fun insert(entity: ShiftEntity): Long

    @Update suspend fun update(entity: ShiftEntity)

    @Query("DELETE FROM shifts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM shifts WHERE date BETWEEN :from AND :to ORDER BY date ASC")
    fun observeByDateRange(from: LocalDate, to: LocalDate): Flow<List<ShiftEntity>>

    @Query("SELECT * FROM shifts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ShiftEntity?

    @Query("SELECT * FROM shifts WHERE date = :date LIMIT 1")       // ← חדש
    suspend fun getByDate(date: LocalDate): ShiftEntity?            // ← חדש
}
