package com.artemis.mgrsnav.data.repository

import com.artemis.mgrsnav.data.db.FolderDao
import com.artemis.mgrsnav.data.db.FolderEntity
import com.artemis.mgrsnav.data.db.WaypointDao
import com.artemis.mgrsnav.data.db.WaypointEntity
import com.artemis.mgrsnav.data.gpx.GpxCodec
import com.artemis.mgrsnav.domain.mgrs.MgrsConverter
import com.artemis.mgrsnav.domain.mgrs.MgrsPrecision
import kotlinx.coroutines.flow.Flow

class WaypointRepository(
    private val waypointDao: WaypointDao,
    private val folderDao: FolderDao
) {
    fun observeWaypoints(): Flow<List<WaypointEntity>> = waypointDao.observeAll()
    fun observeFolders(): Flow<List<FolderEntity>> = folderDao.observeAll()

    suspend fun saveWaypoint(
        name: String,
        latitude: Double,
        longitude: Double,
        note: String? = null,
        folderId: Long? = null,
        id: Long = 0
    ): Long {
        val mgrs = MgrsConverter.toMgrs(latitude, longitude, MgrsPrecision.DIGITS_10)
        val entity = WaypointEntity(
            id = id,
            name = name,
            latitude = latitude,
            longitude = longitude,
            mgrs = mgrs,
            note = note,
            folderId = folderId,
            updatedAt = System.currentTimeMillis()
        )
        return waypointDao.upsert(entity)
    }

    suspend fun deleteWaypoint(id: Long) = waypointDao.deleteById(id)

    suspend fun upsertFolder(name: String, id: Long = 0): Long =
        folderDao.upsert(FolderEntity(id = id, name = name))

    suspend fun setFolderVisible(folder: FolderEntity, visible: Boolean) =
        folderDao.update(folder.copy(visible = visible))

    suspend fun exportGpx(): String = GpxCodec.exportWaypoints(waypointDao.getAll())

    suspend fun importGpx(gpxText: String): Int {
        val imported = GpxCodec.importWaypointsLightweight(gpxText)
        val entities = GpxCodec.toEntities(imported)
        waypointDao.upsertAll(entities)
        return entities.size
    }

    suspend fun getWaypoint(id: Long): WaypointEntity? = waypointDao.getById(id)
}
