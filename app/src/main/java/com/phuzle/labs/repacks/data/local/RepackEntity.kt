package com.phuzle.labs.repacks.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "repack_items",
    indices = [
        Index(value = ["guid"], unique = true),
        Index(value = ["provider", "slug"], unique = true),
    ],
)
data class RepackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "guid") val guid: String,
    @ColumnInfo(name = "provider") val provider: String,
    @ColumnInfo(name = "slug") val slug: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "banner_url") val bannerUrl: String?,
    @ColumnInfo(name = "original_url") val originalUrl: String,
    @ColumnInfo(name = "original_size") val originalSize: String?,
    @ColumnInfo(name = "repack_size") val repackSize: String?,
    @ColumnInfo(name = "genres") val genres: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "is_nsfw") val isNsfw: Boolean = false,
    @ColumnInfo(name = "is_favorited") val isFavorited: Boolean = false,
)
