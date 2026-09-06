package com.artemis.mgrsnav

import android.app.Application
import com.artemis.mgrsnav.data.db.MeridianDatabase
import com.artemis.mgrsnav.data.repository.WaypointRepository
import com.artemis.mgrsnav.domain.location.LocationRepository
import org.osmdroid.config.Configuration
import java.io.File

class MeridianApp : Application() {
    lateinit var database: MeridianDatabase
        private set
    lateinit var waypointRepository: WaypointRepository
        private set
    lateinit var locationRepository: LocationRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = MeridianDatabase.get(this)
        waypointRepository = WaypointRepository(database.waypointDao(), database.folderDao())
        locationRepository = LocationRepository(this)

        // osmdroid offline-friendly cache under app files
        Configuration.getInstance().apply {
            userAgentValue = packageName
            osmdroidBasePath = File(filesDir, "osmdroid")
            osmdroidTileCache = File(filesDir, "osmdroid/tiles")
        }
    }

    companion object {
        lateinit var instance: MeridianApp
            private set
    }
}
