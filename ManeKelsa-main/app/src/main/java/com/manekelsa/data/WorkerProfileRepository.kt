package com.manekelsa.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.manekelsa.model.WorkerProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class WorkerProfileRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val workersCollection = firestore.collection("workers")

    /**
     * Retrieves a real-time stream of all worker profiles.
     */
    fun getWorkers(): Flow<List<WorkerProfile>> {
        return workersCollection.snapshots().map { snapshot ->
            snapshot.documents.mapNotNull { document ->
                try {
                    // Manually map fields to handle type mismatches (e.g., dailyRate as String in DB)
                    val id = document.id
                    val name = document.getString("name") ?: ""
                    val skill = document.getString("skill") ?: ""
                    val photoUrl = document.getString("photoUrl") ?: ""
                    val isAvailable = document.getBoolean("isAvailable") ?: false
                    val thumbsUpCount = document.getLong("thumbsUpCount")?.toInt() ?: 0
                    val phoneNumber = document.getString("phoneNumber") ?: ""
                    val nearestStreetArea = document.getString("nearestStreetArea") ?: ""
                    val location = document.getGeoPoint("location")

                    // Robustly handle dailyRate which might be String, Double, or Long in Firestore
                    val dailyRateRaw = document.get("dailyRate")
                    val dailyRate = when (dailyRateRaw) {
                        is Double -> dailyRateRaw
                        is Long -> dailyRateRaw.toDouble()
                        is String -> dailyRateRaw.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }

                    WorkerProfile(
                        id = id,
                        name = name,
                        skill = skill,
                        photoUrl = photoUrl,
                        dailyRate = dailyRate,
                        isAvailable = isAvailable,
                        thumbsUpCount = thumbsUpCount,
                        phoneNumber = phoneNumber,
                        nearestStreetArea = nearestStreetArea,
                        location = location
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }
    }

    /**
     * Updates the availability status of a worker.
     */
    suspend fun updateAvailability(workerId: String, isAvailable: Boolean) {
        workersCollection.document(workerId)
            .update("isAvailable", isAvailable)
            .await()
    }

    /**
     * Atomically increments the thumbsUpCount field for a worker by 1.
     * Uses [FieldValue.increment] so concurrent taps from multiple devices
     * never lose a count — each one is applied server-side.
     */
    suspend fun incrementRating(workerId: String) {
        workersCollection.document(workerId)
            .update("thumbsUpCount", FieldValue.increment(1))
            .await()
    }
}
