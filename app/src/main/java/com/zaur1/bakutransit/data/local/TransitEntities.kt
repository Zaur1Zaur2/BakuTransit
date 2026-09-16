package com.zaur1.bakutransit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.zaur1.bakutransit.data.model.TransportType
import com.zaur1.bakutransit.data.model.VehicleModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "bus_routes")
data class BusRouteEntity(
    @PrimaryKey val id: String,
    val number: String,
    val name: String,
    val startPoint: String,
    val endPoint: String,
    val operatingHours: String,
    val interval: Int,
    val tariff: Double,
    val vehicleModel: VehicleModel,
    val stopIds: List<String>
)

@Entity(tableName = "transit_stops")
data class TransitStopEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: TransportType,
    val latitude: Double,
    val longitude: Double,
    val lineIds: List<String>
)

class TransitConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromTransportType(value: TransportType): String = value.name

    @TypeConverter
    fun toTransportType(value: String): TransportType = TransportType.valueOf(value)

    @TypeConverter
    fun fromVehicleModel(value: VehicleModel): String = value.name

    @TypeConverter
    fun toVehicleModel(value: String): VehicleModel = VehicleModel.valueOf(value)
}
