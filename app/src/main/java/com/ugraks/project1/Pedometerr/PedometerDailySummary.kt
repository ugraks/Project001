package com.ugraks.project1.Pedometerr

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ugraks.project1.AppNavigation.Screens
import com.ugraks.project1.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailySummaryPage(navController: NavController) {
    val context = LocalContext.current
    val dailySteps = remember { mutableStateListOf<String>() }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<String?>(null) }

    // Verileri yükle
    LaunchedEffect(Unit) {
        val file = File(context.filesDir, "daily_steps.txt")
        if (file.exists()) {
            val lines = file.readLines()
            dailySteps.clear()
            dailySteps.addAll(lines)
        }
    }

    // Ana Sayfa
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Geri Butonu ve Başlık
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 24.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                "Daily Summary",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Liste Alanı - LazyColumn
        LazyColumn(
            modifier = Modifier
                .weight(1f) // Burada LazyColumn’a sabit alan veriyoruz
                .fillMaxWidth()
        ) {
            itemsIndexed(dailySteps) { index, entry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Parçalama
                        val parts = entry.split(",").map { it.trim() }
                        val dateAndSteps = parts.getOrNull(0)?.split(":") ?: listOf("Unknown", "N/A")
                        val date = dateAndSteps.getOrNull(0)?.trim() ?: "Unknown Date"
                        val steps = dateAndSteps.getOrNull(1)?.trim() ?: "N/A"
                        val target = parts.find { it.startsWith("Target:") }?.removePrefix("Target:")?.trim()
                        val status = parts.find { it.startsWith("Success:") }?.removePrefix("Success:")?.trim()

                        // Tarih
                        Text(
                            text = date,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Adımlar
                        Text(
                            text = "Steps Taken: $steps",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Hedef adım sayısı varsa
                        if (target != null) {
                            Text(
                                text = "Target Steps: $target",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                            Text(
                                text = "Target Steps: Unknown",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Başarı durumu
                        if (status != null) {
                            Text(
                                text = when (status) {
                                    "Successful" -> "Status: ✅ Goal Achieved!"
                                    "Unsuccessful" -> "Status: ❌ Goal Not Achieved"
                                    else -> "Status: Unknown"
                                },
                                color = when (status) {
                                    "Successful" -> Color(0xFF2E7D32)
                                    "Unsuccessful" -> Color.Red
                                    else -> Color.Gray
                                },
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        } else {
                            Text(
                                text = "Status: Unknown",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Silme ikonu
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = { entryToDelete = entry }
                            ) {
                                androidx.compose.material.Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "Delete Entry",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }


        }


        Box() {

            // Clear All Button - Alt kısımda sabit
            Button(
                onClick = { showDeleteAllDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Text("Clear All Summaries")
            }



        }

        // Bireysel silme dialog
        if (entryToDelete != null) {
            AlertDialog(
                onDismissRequest = { entryToDelete = null },
                title = { Text("Delete Entry", color = MaterialTheme.colorScheme.primary) },
                text = { Text("Are you sure you want to delete this entry?") },
                confirmButton = {
                    TextButton(onClick = {
                        deleteEntry(context, entryToDelete!!)
                        dailySteps.remove(entryToDelete)
                        entryToDelete = null
                    }) {
                        Text("Yes", color = MaterialTheme.colorScheme.primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { entryToDelete = null }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }

        // Tümünü temizleme dialog
        if (showDeleteAllDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAllDialog = false },
                title = { Text("Clear All Entries", color = MaterialTheme.colorScheme.primary) },
                text = { Text("Are you sure you want to delete all daily summaries?") },
                confirmButton = {
                    TextButton(onClick = {
                        clearAllSummaries(context)
                        dailySteps.clear()
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



