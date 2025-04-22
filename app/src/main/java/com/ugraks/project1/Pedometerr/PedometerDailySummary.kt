package com.ugraks.project1.Pedometerr // Kendi paket adınız

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // items yerine itemsIndexed de kullanabilirsiniz
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.* // remember, mutableStateOf, collectAsState, getValue, setValue, rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel // ViewModel için import
import androidx.navigation.NavController
import com.ugraks.project1.data.local.entity.DailyStepEntity // YENİ Room Entity
import com.ugraks.project1.ui.viewmodels.PedometerViewModel // YENİ ViewModel'ınız
import kotlinx.coroutines.launch // Coroutine başlatmak için
import java.time.LocalDate // Gerekirse
import java.util.Date // Gerekirse SimpleDateFormat için
import java.text.SimpleDateFormat // Gerekirse zaman damgasını formatlamak için
import java.util.Locale // Gerekirse SimpleDateFormat için
import kotlin.math.roundToInt // Gerekirse

// Eski dosya okuma fonksiyonları ve data class'ı Room Entity'sine taşındığı için artık burada GEREKMEZ.
// private const val ENTRY_DELIMITER = " | " // Kaldırıldı
// private const val DATE_FORMAT = "yyyy-MM-dd" // Kaldırıldı
// data class DailyStepEntryDetail(...) // Kaldırıldı
// fun loadDailySummaries(...) // Kaldırıldı
// fun deleteEntry(...) // Kaldırıldı
// fun clearAllSummaries(...) // Kaldırıldı

@Composable
fun DailySummaryPage(
    navController: NavController,
    viewModel: PedometerViewModel = hiltViewModel() // YENİ: PedometerViewModel'ı inject et
) {
    val context = LocalContext.current // Toast vb. için (eğer kullanılacaksa)
    val coroutineScope = rememberCoroutineScope() // Dialoglarda suspend fonksiyonu çağırmak için

    // summaries listesi artık ViewModel'dan Room'dan gelen Flow<Map<String, List<DailyStepEntity>>>'i izleyecek
    val dailySummariesMap by viewModel.dailyStepSummariesByDate.collectAsState() // YENİ: Room'dan gelen ve gruplanmış adım verisi (Map<String, List<DailyStepEntity>>)

    var showDeleteAllDialog by remember { mutableStateOf(false) }
    // Silinecek öğe için DailyStepEntity saklayalım
    var entryToDelete by remember { mutableStateOf<DailyStepEntity?>(null) } // Tipi DailyStepEntity oldu


    // Ana Sayfa
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Geri Butonu ve Başlık aynı kalır
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 24.dp)
        ) {
            // Geri Butonu
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, // İki yönlü ok ikonunu kullanmak daha doğru olabilir
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary // Veya onBackground
                )
            }

            // Başlık (ortalanmış)
            Text(
                text = "Daily Step Summaries", // Başlığı netleştirdik
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.primary, // Veya onBackground
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Liste Alanı - LazyColumn (Room'dan gelen dailySummariesMap kullanılır)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Map entry'lerini tarihe göre sırala ve listele
            // ViewModel'daki ORDER BY date DESC, timestamp DESC sayesinde zaten sıralı gelecektir.
            items(
                items = dailySummariesMap.entries.toList().sortedByDescending { it.key }, // Map entry'leri (date, List<DailyStepEntity>)
                key = { it.key } // Her gün için benzersiz anahtar (tarih stringi)
            ) { (date, entriesForDay) -> // date String, entriesForDay List<DailyStepEntity>
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp), // Kartlar arasına dikey boşluk
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Tarih
                        Text(
                            text = date, // Tarih stringi Map key'inden geliyor
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary // Veya başka bir renk
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // O güne ait her bir kaydı listele - entriesForDay Room Entity listesidir
                        entriesForDay.forEachIndexed { index, entryEntity -> // entryEntity DailyStepEntity
                            // Ayirici ekle
                            if (index > 0) {
                                Divider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    // entryEntity.steps kullanın
                                    Text(
                                        text = "Steps: ${entryEntity.steps}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    // entryEntity.target kullanın
                                    entryEntity.target?.let { target ->
                                        Text(
                                            text = "Target: $target",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    } ?: Text(
                                        text = "Target: Unknown",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    // entryEntity.status kullanın
                                    Text(
                                        text = when (entryEntity.status) {
                                            "Successful" -> "Status: ✅ Goal Achieved!"
                                            "Unsuccessful" -> "Status: ❌ Goal Not Achieved"
                                            else -> "Status: Unknown"
                                        },
                                        color = when (entryEntity.status) {
                                            "Successful" -> Color(0xFF2E7D32) // Yeşil
                                            "Unsuccessful" -> Color.Red
                                            else -> Color.Gray
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    // (Opsiyonel) Zaman damgasını göstermek isterseniz:
                                    // val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(entryEntity.timestamp))
                                    // Text("Time: $timeString", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                // Silme ikonu - İŞLEM DEĞİŞTİ
                                IconButton(
                                    onClick = {
                                        // Silinecek öğe Room Entity'sidir
                                        entryToDelete = entryEntity // YENİ: Silinecek entity'yi state'e kaydet
                                        // Dialogu göstermek için bir state (showDeleteConfirmationDialog gibi) kullanmanız gerekebilir.
                                        // Şu an direkt dialog içinde olduğunuz için sadece state'i set etmek yeterli.
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete Entry",
                                        tint = Color.Red,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Toplam adım sayısını hesapla (aynı kalır, sadece list tipi DailyStepEntity oldu)
                        val totalStepsForDay = entriesForDay.sumOf { it.steps }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Toplam Adım Sayısı (aynı kalır)
                        Text(
                            text = "Total Steps for $date: $totalStepsForDay",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary // Veya başka bir renk
                        )
                    }
                }
            }
            // Eğer hiç özet yoksa bilgi mesajı göster (aynı kalır, map boşsa çalışır)
            if (dailySummariesMap.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No daily summaries saved yet.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Clear All Button - İŞLEM DEĞİŞTİ
        Button(
            onClick = { showDeleteAllDialog = true }, // Dialog gösterme state'ini set et
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Clear All Summaries") // Metin aynı kalır
        }


        // Bireysel silme dialog - İŞLEM DEĞİŞTİ
        // showDeleteConfirmationDialog gibi bir state tarafından kontrol edilmesi daha uygun olabilir
        // Ancak current implementasyonda sadece entryToDelete != null ise dialog görünüyor.
        if (entryToDelete != null) { // entryToDelete DailyStepEntity tipinde
            AlertDialog(
                onDismissRequest = { entryToDelete = null }, // Dialog dışına tıklayınca kapat
                title = { Text("Delete Entry", color = MaterialTheme.colorScheme.primary) },
                text = { Text("Are you sure you want to delete this specific saved entry?") },
                confirmButton = {
                    TextButton(onClick = {
                        entryToDelete?.let { entry -> // Room Entity'yi al
                            // YENİ: ViewModel üzerinden Room'dan sil
                            coroutineScope.launch { // suspend fonksiyonu CoroutineScope içinde çağır
                                viewModel.deleteStepEntry(entry)
                            }
                            // ESKİ: deleteEntry(...) ve state güncelleme kaldırıldı
                            // Room Flow'u güncelleyecek ve UI otomatik değişecek.
                        }
                        entryToDelete = null // Dialogu kapat
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

        // Tümünü temizleme dialog - İŞLEM DEĞİŞTİ
        if (showDeleteAllDialog) { // showDeleteAllDialog state'i tarafından kontrol edilir
            AlertDialog(
                onDismissRequest = { showDeleteAllDialog = false }, // Dialog dışına tıklayınca kapat
                title = { Text("Clear All Entries", color = MaterialTheme.colorScheme.primary) },
                text = { Text("Are you sure you want to delete all daily summaries?") },
                confirmButton = {
                    TextButton(onClick = {
                        // YENİ: ViewModel üzerinden Room'dan tüm girişleri sil
                        coroutineScope.launch { // suspend fonksiyonu CoroutineScope içinde çağır
                            viewModel.clearAllStepEntries()
                        }
                        // ESKİ: clearAllSummaries(context) ve dailySummaries.clear() çağrıları kaldırıldı
                        // Room Flow'u güncelleyecek ve UI otomatik değişecek.
                        showDeleteAllDialog = false // Dialogu kapat
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