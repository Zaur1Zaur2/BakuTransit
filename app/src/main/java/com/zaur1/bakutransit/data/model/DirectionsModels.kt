package com.zaur1.bakutransit.data.model

data class GraphHopperResponse(
    val paths: List<GraphHopperPath>
)

data class GraphHopperPath(
    val points: String, // Encoded polyline
    val distance: Double,
    val time: Long,
    val points_order: List<Int>? = null
)
