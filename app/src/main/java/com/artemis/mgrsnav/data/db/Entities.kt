package com.artemis.mgrsnav.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val visible: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "waypoints",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("folderId"), Index("name")]
)
data class WaypointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val mgrs: String,
    val elevationMeters: Double? = null,
    val note: String? = null,
    val colorArgb: Int = 0xFFC4A35A.toInt(),
    val folderId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
