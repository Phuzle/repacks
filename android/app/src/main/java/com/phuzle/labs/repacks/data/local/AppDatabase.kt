package com.phuzle.labs.repacks.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [RepackEntity::class, WatchlistEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun repackDao(): RepackDao

    abstract fun watchlistDao(): WatchlistDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "repacks.db",
                )
                    // repack_items is purely a local RSS cache that re-syncs from the network on
                    // the next refresh, so destructively recreating it (version 2 added the
                    // `details` column) is safe. dropAllTables = false so this only touches tables
                    // whose schema actually changed — watchlist_items (real user-typed keywords)
                    // is untouched.
                    .fallbackToDestructiveMigration(dropAllTables = false)
                    .build().also { instance = it }
            }
    }
}
