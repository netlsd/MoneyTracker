package com.netlsd.moneytracker.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM note ORDER BY id DESC")
    fun getAllNote(): Flow<List<Note>>

    @Query("SELECT * FROM note WHERE name LIKE '%' || :name || '%' AND date BETWEEN :startDate AND :endDate ORDER BY id DESC")
    fun query(name: String?, startDate: String, endDate: String): Flow<List<Note>>

    @Query("SELECT * FROM note WHERE id IN (:noteIds)")
    fun loadAllByIds(noteIds: IntArray): List<Note>

    @Query("SELECT name FROM note")
    fun getAllName(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg notes: Note)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Delete
    suspend fun delete(note: Note)

}
