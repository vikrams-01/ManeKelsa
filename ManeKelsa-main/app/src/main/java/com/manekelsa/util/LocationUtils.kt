package com.manekelsa.util

import com.google.firebase.firestore.GeoPoint
import com.manekelsa.model.WorkerProfile
import kotlin.math.*

/**
 * Utility object for hyper-local distance calculations and sorting.
 * Uses the Haversine formula to compute the great-circle distance between
 * two points on the Earth's surface given their latitude and longitude.
 */
object LocationUtils {

    /** Earth's mean radius in kilometers. */
    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Calculates the great-circle distance between two geographic coordinates
     * using the Haversine formula.
     *
     * @param lat1 Latitude of the first point in degrees.
     * @param lon1 Longitude of the first point in degrees.
     * @param lat2 Latitude of the second point in degrees.
     * @param lon2 Longitude of the second point in degrees.
     * @return Distance between the two points in kilometers.
     */
    fun haversineDistanceKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val radLat1 = Math.toRadians(lat1)
        val radLat2 = Math.toRadians(lat2)

        val a = sin(dLat / 2).pow(2) +
                cos(radLat1) * cos(radLat2) * sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c
    }

    /**
     * Calculates the distance in kilometers between the user's location
     * and a Firestore [GeoPoint].
     *
     * @param userLat User's current latitude in degrees.
     * @param userLon User's current longitude in degrees.
     * @param geoPoint The worker's Firestore GeoPoint.
     * @return Distance in kilometers, or [Double.MAX_VALUE] if the GeoPoint is null.
     */
    fun distanceToWorker(userLat: Double, userLon: Double, geoPoint: GeoPoint?): Double {
        if (geoPoint == null) return Double.MAX_VALUE
        return haversineDistanceKm(userLat, userLon, geoPoint.latitude, geoPoint.longitude)
    }

    /**
     * Sorts a list of [WorkerProfile] objects by their proximity to the user's
     * current location (nearest first). Workers without a location field are
     * placed at the end of the list.
     *
     * This fulfills the "Hyper-Local Sorting: Nearest Street/Area" success criterion.
     *
     * @param userLat User's current latitude in degrees.
     * @param userLon User's current longitude in degrees.
     * @param workers The unsorted list of worker profiles.
     * @return A new list sorted by ascending distance from the user.
     */
    fun sortByNearest(
        userLat: Double,
        userLon: Double,
        workers: List<WorkerProfile>
    ): List<WorkerProfile> {
        return workers.sortedBy { worker ->
            distanceToWorker(userLat, userLon, worker.location)
        }
    }
}
