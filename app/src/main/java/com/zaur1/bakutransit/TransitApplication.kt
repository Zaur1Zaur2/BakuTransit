package com.zaur1.bakutransit

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.zaur1.bakutransit.data.local.TransitDatabase
import com.zaur1.bakutransit.data.repository.TransitRepository
import com.zaur1.bakutransit.ui.util.getCustomImageLoader

class TransitApplication : Application() {
    val database by lazy { TransitDatabase.getDatabase(this) }
    val repository by lazy { TransitRepository(database.transitDao()) }
}
