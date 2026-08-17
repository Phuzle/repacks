package com.phuzle.labs.repacks.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist_items ORDER BY created_at DESC")
    fun observeAll(): Flow<List<WatchlistEntity>>

    @Query("SELECT keyword FROM watchlist_items")
    suspend fun snapshotKeywords(): List<String>

    @Insert
    suspend fun insert(item: WatchlistEntity)

    @Query("DELETE FROM watchlist_items WHERE id = :id")
    suspend fun delete(id: Long)
}
