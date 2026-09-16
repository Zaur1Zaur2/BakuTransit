package com.zaur1.bakutransit.ui.util

import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest
import com.zaur1.bakutransit.BuildConfig
import okhttp3.OkHttpClient

object StopImageProvider {
    private const val BASE_URL = "https://api.maptiler.com/maps/streets/static"
    private const val API_KEY = BuildConfig.MAPTILER_API_KEY

    /**
     * Baku Center Map URL
     * Uses optimized zoom and size for city-wide static view
     */
    fun getStaticMapUrl(latitude: Double, longitude: Double, zoom: Int = 11): String {
        return "$BASE_URL/$longitude,$latitude,$zoom/800x600.png?key=$API_KEY"
    }

    /**
     * Baku Metro Scheme Image URL (Official / Perspective)
     */
    const val METRO_SCHEME_URL = "https://metro.gov.az/images/uploads/perspektiv_sxem_az.png"
}

fun getCustomImageLoader(context: Context): ImageLoader {
    return ImageLoader.Builder(context)
        .crossfade(true)
        .build()
}
