package com.manekelsa.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.manekelsa.R
import com.manekelsa.model.WorkerProfile

/**
 * A full-detail dialog that displays all information about a [WorkerProfile].
 * Shown when the user taps a card in the worker list.
 *
 * Design choices:
 *  - Large text sizes (20–28 sp) and high-contrast colours for semi-literate users.
 *  - All labels use [stringResource] so the UI is 100 % Kannada-localized.
 *  - A single large "Close" button as the dismissal target.
 */
@Composable
fun WorkerDetailDialog(
    worker: WorkerProfile,
    onRatingClicked: () -> Unit = {},
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Title ────────────────────────────────────────────
                Text(
                    text = stringResource(id = R.string.worker_details_title),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── Profile icon ─────────────────────────────────────
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ── Name ─────────────────────────────────────────────
                DetailRow(
                    label = stringResource(id = R.string.worker_name_label),
                    value = worker.name
                )

                // ── Skill ────────────────────────────────────────────
                DetailRow(
                    label = stringResource(id = R.string.skill_label),
                    value = worker.skill
                )

                // ── Daily Rate ───────────────────────────────────────
                DetailRow(
                    label = stringResource(id = R.string.daily_rate, "").trimEnd(),
                    value = "₹${worker.dailyRate.toInt()}",
                    valueColor = Color(0xFF1B5E20) // Dark green for monetary amounts
                )

                // ── Phone ────────────────────────────────────────────
                DetailRow(
                    label = stringResource(id = R.string.phone_label),
                    value = worker.phoneNumber
                )

                // ── Thumbs-up / Rating (tappable) ───────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRatingClicked() }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.rating_label),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = stringResource(id = R.string.rating_thumbs_up),
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = worker.thumbsUpCount.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ── Close button (large touch target) ────────────────
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.close_button),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * A reusable row that shows a bold label on the left and a value on the right,
 * both at 20 sp minimum for readability.
 */
@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, locale = "kn")
@Composable
fun WorkerDetailDialogPreview() {
    MaterialTheme {
        WorkerDetailDialog(
            worker = WorkerProfile(
                id = "1",
                name = "Ravi",
                skill = "ಸ್ವಚ್ಛಗೊಳಿಸುವಿಕೆ",
                isAvailable = true,
                phoneNumber = "9876543210",
                thumbsUpCount = 15,
                dailyRate = 500.0,
                nearestStreetArea = "Majestic"
            ),
            onRatingClicked = {},
            onDismiss = {}
        )
    }
}
