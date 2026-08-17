package com.phuzle.labs.repacks.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RepackDao {

    @Query("SELECT * FROM repack_items ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<RepackEntity>>

    @Query("SELECT * FROM repack_items WHERE provider = :provider AND slug = :slug LIMIT 1")
    fun observeOne(provider: String, slug: String): Flow<RepackEntity?>

    /** Of [guids], returns the ones already present — used to work out which parsed feed items are actually new. */
    @Query("SELECT guid FROM repack_items WHERE guid IN (:guids)")
    suspend fun filterExistingGuids(guids: List<String>): List<String>

    /** Returns the row id assigned to each inserted item, in the same order as [items] — or -1 for
     * any that were ignored due to a guid conflict. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<RepackEntity>): List<Long>

    @Query("UPDATE repack_items SET is_favorited = :favorited WHERE id = :id")
    suspend fun setFavorited(id: Long, favorited: Boolean)

    @Query("DELETE FROM repack_items WHERE timestamp < :cutoffMillis AND is_favorited = 0")
    suspend fun deleteOlderThan(cutoffMillis: Long)
}
