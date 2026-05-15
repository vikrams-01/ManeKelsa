package com.manekelsa

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.manekelsa.ui.WorkerListScreenPreview
import com.manekelsa.ui.WorkerProfileCardPreview
import org.junit.Rule
import org.junit.Test

class PreviewTests {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material.Light.NoActionBar",
        maxPercentDifference = 0.0
    )

    @Test
    fun workerProfileCardPreview() {
        paparazzi.snapshot {
            WorkerProfileCardPreview()
        }
    }

    @Test
    fun workerListScreenPreview() {
        paparazzi.snapshot {
            WorkerListScreenPreview()
        }
    }
}
