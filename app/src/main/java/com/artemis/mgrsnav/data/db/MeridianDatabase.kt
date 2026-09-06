package com.artemis.mgrsnav.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FolderEntity::class, WaypointEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MeridianDatabase : RoomDatabase() {
    abstract fun folderDao(): FolderDao
    abstract fun waypointDao(): WaypointDao

    companion object {
        @Volatile private var instance: MeridianDatabase? = null

        fun get(context: Context): MeridianDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MeridianDatabase::class.java,
                    "meridian.db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}
