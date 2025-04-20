package com.ugraks.project1.KeepNoteComposable

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DailySummariesPage(context: Context, navController: NavController) {
    val summaries = remember { mutableStateListOf<DailySummary>().apply { addAll(readDailySummaries(context)) } }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var summaryToDelete by remember { mutableStateOf<DailySummary?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Başlık ve Geri Butonunu Ortalamak


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 24.dp)
        ) {
            // Geri Dön Butonu
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.TopStart) // Sol üst köşeye yerleştir
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Başlık (ortalanmış)
            Text(
                "Saved Summaries",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .align(Alignment.Center) // Yatayda ve dikeyde ortalar
            )
        }


        // Kartlar ve Veriler
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            summaries.forEach { summary ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Date: ${summary.date}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Calories: ${summary.calories} kcal", style = MaterialTheme.typography.bodyMedium)
                            Text("Protein: ${summary.protein.roundToInt()} g", style = MaterialTheme.typography.bodyMedium)
                            Text("Fat: ${summary.fat.roundToInt()} g", style = MaterialTheme.typography.bodyMedium)
                            Text("Carbs: ${summary.carbs.roundToInt()} g", style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(onClick = { summaryToDelete = summary }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }

        // Temizleme Butonu
        Button(
            onClick = { showDeleteAllDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                contentColor = Color.White
            )
        ) {
            Text("Clear All Summaries")
        }

        // Silme ve Temizleme Dialogları
        if (summaryToDelete != null) {
            AlertDialog(
                onDismissRequest = { summaryToDelete = null },
                title = { Text("Delete Entry", color = MaterialTheme.colorScheme.primary) },
                text = { Text("Are you sure you want to delete this entry?") },
                confirmButton = {
                    TextButton(onClick = {
                        deleteSummary(context, summaryToDelete!!, summaries)
                        summaryToDelete = null
                    }) {
                        Text("Yes", color = MaterialTheme.colorScheme.primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { summaryToDelete = null }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }

        if (showDeleteAllDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAllDialog = false },
                title = { Text("Clear All Entries", color = MaterialTheme.colorScheme.primary) },
                text = { Text("Are you sure you want to delete all daily summaries?") },
                confirmButton = {
                    TextButton(onClick = {
                        clearAllSummaries(context)
                        summaries.clear()
                        showDeleteAllDialog = false
                    }) {
                        Text("Yes", color = MaterialTheme.colorScheme.primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAllDialog = false }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    }
}

fun deleteSummary(context: Context, summary: DailySummary, summaries: SnapshotStateList<DailySummary>) {
    summaries.remove(summary)
    saveSummariesToFile(context, summaries)
}

fun clearAllSummaries(context: Context) {
    File(context.filesDir, "daily_summaries.txt").delete()
}

fun saveSummariesToFile(context: Context, summaries: List<DailySummary>) {
    val content = summaries.joinToString("\n---\n") {
        """
        Date: ${it.date}
        Calories: ${it.calories} kcal
        Protein: ${it.protein.roundToInt()} g
        Fat: ${it.fat.roundToInt()} g
        Carbs: ${it.carbs.roundToInt()} g
        """.trimIndent()
    }

    context.openFileOutput("daily_summaries.txt", Context.MODE_PRIVATE).use {
        it.write(content.toByteArray())
    }
}


