package com.niya.gps.data

import org.osmdroid.util.GeoPoint
import java.io.Serializable

data class LocationModel(
    val speed: Float = 0.0f,
    val distance: Float = 0.0f,
    val geoPointList: ArrayList<GeoPoint>
): Serializable
