package com.artemis.mgrsnav.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY sortOrder, name")
    fun observeAll(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders ORDER BY sortOrder, name")
    suspend fun getAll(): List<FolderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(folder: FolderEntity): Long

    @Update
    suspend fun update(folder: FolderEntity)

    @Delete
    suspend fun delete(folder: FolderEntity)
}

@Dao
interface WaypointDao {
    @Query("SELECT * FROM waypoints ORDER BY name")
    fun observeAll(): Flow<List<WaypointEntity>>

    @Query("SELECT * FROM waypoints WHERE folderId IS :folderId ORDER BY name")
    fun observeByFolder(folderId: Long?): Flow<List<WaypointEntity>>

    @Query("SELECT * FROM waypoints WHERE id = :id")
    suspend fun getById(id: Long): WaypointEntity?

    @Query("SELECT * FROM waypoints ORDER BY name")
    suspend fun getAll(): List<WaypointEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(waypoint: WaypointEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(waypoints: List<WaypointEntity>): List<Long>

    @Update
    suspend fun update(waypoint: WaypointEntity)

    @Delete
    suspend fun delete(waypoint: WaypointEntity)

    @Query("DELETE FROM waypoints WHERE id = :id")
    suspend fun deleteById(id: Long)
}
