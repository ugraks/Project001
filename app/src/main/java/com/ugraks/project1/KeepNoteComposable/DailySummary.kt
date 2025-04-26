package com.ugraks.project1.KeepNoteComposable // Kendi paket adınız

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // items yerine itemsIndexed de kullanabilirsiniz
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState // StateFlow'u izlemek için
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel // Hilt ViewModel için import
import androidx.navigation.NavController
import com.ugraks.project1.data.local.entity.DailySummaryEntity // YENİ Room Entity
import com.ugraks.project1.ui.viewmodels.CalorieViewModel // Kendi ViewModel'ınız
import kotlin.math.roundToInt

@Composable
fun DailySummariesPage(
    navController: NavController,
    viewModel: CalorieViewModel = hiltViewModel() // ViewModel'ı Hilt ile inject et
) {
    val context =
        LocalContext.current // LocalContext.current ? context parametresi yerine LocalContext.current kullanın

    // summaries listesi artık ViewModel'dan Room'dan gelen Flow/StateFlow'u izleyecek
    // ESKİ: val summaries = remember { mutableStateListOf<DailySummary>().apply { addAll(readDailySummaries(context)) } }
    val summaries by viewModel.dailySummaries.collectAsState() // YENİ: Room'dan gelen özet listesi

    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var summaryToDelete by remember { mutableStateOf<DailySummaryEntity?>(null) } // Tipi DailySummaryEntity oldu

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
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, // İki yönlü ok ikonunu kullanmak daha doğru olabilir
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


        // Kartlar ve Veriler (Room'dan gelen summaries listesi kullanılır)
        // Column yerine LazyColumn kullanmak performans için daha iyi olabilir eğer çok özet olacaksa
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // summaries.forEach { summary -> // summary artık DailySummaryEntity
            items(summaries) { summaryEntity -> // YENİ: summaryEntity kullanın
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp), // Padding zaten üstte Column'da var
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
                            // summary.date, summary.calories, summary.protein, summary.fat, summary.carbs yerine
                            Text("${summaryEntity.date}", fontWeight = FontWeight.Bold, fontSize = 20.sp, style = MaterialTheme.typography.bodyMedium)
                            Text("Calories: ${summaryEntity.calories} kcal", style = MaterialTheme.typography.bodyMedium)
                            Text("Protein: ${summaryEntity.protein.roundToInt()} g", style = MaterialTheme.typography.bodyMedium)
                            Text("Fat: ${summaryEntity.fat.roundToInt()} g", style = MaterialTheme.typography.bodyMedium)
                            Text("Carbs: ${summaryEntity.carbs.roundToInt()} g", style = MaterialTheme.typography.bodyMedium)
                        }
                        // Silme butonu: summaryToDelete artık DailySummaryEntity tipinde olacak
                        IconButton(onClick = { summaryToDelete = summaryEntity }) {
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
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary
            )
        ) {
            Text("Clear All Summaries")
        }

        // Silme ve Temizleme Dialogları

        // Delete Entry Dialog
        if (summaryToDelete != null) { // summaryToDelete DailySummaryEntity tipinde
            AlertDialog(
                onDismissRequest = { summaryToDelete = null },
                title = { Text("Delete Entry", color = MaterialTheme.colorScheme.primary) },
                text = { Text("Are you sure you want to delete this entry?") },
                confirmButton = {
                    TextButton(onClick = {
                        // YENİ: ViewModel üzerinden belirli özeti sil
                        summaryToDelete?.let { viewModel.deleteDailySummary(it) } // ViewModel metodunu çağır
                        // ESKİ: deleteSummary(context, summaryToDelete!!, summaries) çağrısı kaldırıldı
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

        // Clear All Entries Dialog
        if (showDeleteAllDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAllDialog = false },
                title = { Text("Clear All Entries", color = MaterialTheme.colorScheme.primary) },
                text = { Text("Are you sure you want to delete all daily summaries?") },
                confirmButton = {
                    TextButton(onClick = {
                        // YENİ: ViewModel üzerinden tüm özetleri sil
                        viewModel.clearAllDailySummaries() // ViewModel metodunu çağır
                        // ESKİ: clearAllSummaries(context) ve summaries.clear() çağrıları kaldırıldı (ViewModel Flow'u güncelleyecektir)
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