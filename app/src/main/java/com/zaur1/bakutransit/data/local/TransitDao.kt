package com.zaur1.bakutransit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransitDao {
    @Query("SELECT * FROM bus_routes")
    fun getAllBusRoutes(): Flow<List<BusRouteEntity>>

    @Query("SELECT * FROM transit_stops")
    fun getAllStops(): Flow<List<TransitStopEntity>>

    @Query("SELECT * FROM transit_stops WHERE id IN (:stopIds)")
    suspend fun getStopsByIds(stopIds: List<String>): List<TransitStopEntity>

    @Query("SELECT * FROM bus_routes WHERE id = :routeId")
    suspend fun getBusRouteById(routeId: String): BusRouteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusRoutes(routes: List<BusRouteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStops(stops: List<TransitStopEntity>)
}
