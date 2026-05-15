package com.manekelsa.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.manekelsa.R
import com.manekelsa.model.WorkerProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerListScreen(
    workers: List<WorkerProfile>,
    onAvailabilityChanged: (WorkerProfile, Boolean) -> Unit,
    onRatingClicked: (WorkerProfile) -> Unit = {}
) {
    // Track which worker's detail dialog is currently showing (null = none)
    var selectedWorker by remember { mutableStateOf<WorkerProfile?>(null) }

    // Read the current locale to initialise the toggle position.
    // After setApplicationLocales() the Activity recreates, so this re-reads correctly.
    val currentLocales = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    var isKannada by remember { mutableStateOf(currentLocales.contains("kn")) }

    // Workers are already sorted by the ViewModel (hyper-local or alphabetical fallback).
    // No additional sorting is needed here.

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(id = R.string.nearest_workers),
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Language toggle ──────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "English",
                    fontSize = 18.sp,
                    fontWeight = if (!isKannada) FontWeight.ExtraBold else FontWeight.Normal,
                    color = if (!isKannada) MaterialTheme.colorScheme.primary else Color.Gray
                )
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = isKannada,
                    onCheckedChange = { checked ->
                        isKannada = checked
                        val tag = if (checked) "kn" else "en"
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(tag)
                        )
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4CAF50),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFF2196F3)
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "ಕನ್ನಡ",
                    fontSize = 18.sp,
                    fontWeight = if (isKannada) FontWeight.ExtraBold else FontWeight.Normal,
                    color = if (isKannada) Color(0xFF4CAF50) else Color.Gray
                )
            }

            // ── Worker list ─────────────────────────────────────
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(workers) { worker ->
                    WorkerProfileCard(
                        worker = worker,
                        onAvailabilityChanged = { isAvailable ->
                            onAvailabilityChanged(worker, isAvailable)
                        },
                        onRatingClicked = { onRatingClicked(worker) },
                        modifier = Modifier.clickable { selectedWorker = worker }
                    )
                }
            }
        }
    }

    // Show the detail dialog when a worker card is tapped
    selectedWorker?.let { worker ->
        WorkerDetailDialog(
            worker = worker,
            onRatingClicked = { onRatingClicked(worker) },
            onDismiss = { selectedWorker = null }
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, locale = "kn")
@Composable
fun WorkerListScreenPreview() {
    MaterialTheme {
        WorkerListScreen(
            workers = listOf(
                WorkerProfile(
                    id = "1",
                    name = "Ravi",
                    skill = "ಸ್ವಚ್ಛಗೊಳಿಸುವಿಕೆ",
                    isAvailable = true,
                    phoneNumber = "1234567890",
                    thumbsUpCount = 10,
                    dailyRate = 500.0,
                    nearestStreetArea = "Majestic"
                ),
                WorkerProfile(
                    id = "2",
                    name = "Latha",
                    skill = "ತೋಟಗಾರಿಕೆ",
                    isAvailable = false,
                    phoneNumber = "0987654321",
                    thumbsUpCount = 20,
                    dailyRate = 600.0,
                    nearestStreetArea = "Jayanagar"
                )
            ),
            onAvailabilityChanged = { _, _ -> },
            onRatingClicked = {}
        )
    }
}
