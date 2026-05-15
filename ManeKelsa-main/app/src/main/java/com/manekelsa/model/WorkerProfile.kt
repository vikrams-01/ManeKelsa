package com.manekelsa.model

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName

/**
 * Represents a worker's profile on the Mane-Kelsa platform.
 * Syncs with Firebase Firestore.
 */
data class WorkerProfile(
    val id: String = "",
    val name: String = "",
    val skill: String = "",
    val photoUrl: String = "",

    // Now matches your Double change in Firebase
    val dailyRate: Double = 0.0,

    // Annotation ensures the mapping to "isAvailable" in Firestore is perfect
    @get:PropertyName("isAvailable")
    @set:PropertyName("isAvailable")
    var isAvailable: Boolean = false,

    val thumbsUpCount: Int = 0,
    val phoneNumber: String = "", // Matches "phoneNumber" in your screenshot
    val nearestStreetArea: String = "",
    val location: GeoPoint? = null
)