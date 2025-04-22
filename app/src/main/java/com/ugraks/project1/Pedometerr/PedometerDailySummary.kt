package com.ugraks.project1.Pedometerr

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.io.File
import java.io.IOException // IOException eklendi
import java.lang.NumberFormatException

// Dosyada kullanılan ayırıcı (saveDailyStepCount.kt ile eşleşmeli)
private const val ENTRY_DELIMITER = " | "
private const val DATE_FORMAT = "yyyy-MM-dd"

// Her bir kaydedilmiş adım sayımı detayını tutacak veri sınıfı
data class DailyStepEntryDetail(
    val steps: Int,
    val target: Int?,
    val status: String,
    // Silme için bu detayın orijinal string temsilini sakla
    val originalDetailString: String // "adım, Target: hedef, Success: durum" gibi
)

@Composable
fun DailySummaryPage(navController: NavController) {
    val context = LocalContext.current
    // Veriyi güne göre gruplandırmak için Map kullanıyoruz: Map<TarihStringi, List<DailyStepEntryDetail>>
    val dailySummaries = remember { mutableStateMapOf<String, MutableList<DailyStepEntryDetail>>() }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    // Silinecek öğe için Pair<TarihStringi, OriginalDetailString> saklayalım
    var entryToDelete by remember { mutableStateOf<Pair<String, Pair<Int, String>>?>(null) }


    // Verileri yükleme ve gruplandırma
    fun loadDailySummaries() {
        val file = File(context.filesDir, "daily_steps.txt")
        dailySummaries.clear() // Önce mevcut veriyi temizle

        if (!file.exists()) {
            Log.d("DailySummaryPage", "Daily steps file not found.")
            return
        }

        val lines = try {
            file.readLines()
        } catch (e: IOException) {
            Log.e("DailySummaryPage", "Error reading summaries file: ${e.message}")
            return // Dosya okuma hatası varsa yükleme
        }


        lines.forEach { line ->
            // Boş veya sadece boşluklardan oluşan satırları atla
            if (line.isBlank()) {
                Log.d("DailySummaryPage", "Skipping blank line.")
                return@forEach
            }

            // Böl: Tarih:Data
            val dateAndData = line.split(":", limit = 2)
            if (dateAndData.size != 2) {
                Log.w("DailySummaryPage", "Skipping malformed line (date/data): $line")
                return@forEach
            }

            val date = dateAndData[0].trim()
            val dataPart = dateAndData[1].trim()

            // Böl: Ayrı kaydedilmiş detaylar (ENTRY_DELIMITER ile ayrılmış)
            val rawEntriesForDay = dataPart.split(ENTRY_DELIMITER)

            val parsedEntriesForDay = mutableListOf<DailyStepEntryDetail>()
            rawEntriesForDay.forEach { entryString ->
                val trimmedEntryString = entryString.trim() // Ayrıştırmadan önce trimle
                val parts = trimmedEntryString.split(",", limit = 3).map { it.trim() }

                if (parts.isNotEmpty()) { // Adım sayısı kısmı (ilk kısım) mevcut olmalı
                    val stepsString = parts[0]
                    val steps = try {
                        stepsString.toInt()
                    } catch (e: NumberFormatException) {
                        Log.e("DailySummaryPage", "Invalid step count format: $stepsString in entry: $trimmedEntryString", e)
                        0 // Hata durumunda varsayılan olarak 0
                    }

                    val target = parts.getOrNull(1)?.removePrefix("Target:")?.trim()?.toIntOrNull()
                    val status = parts.getOrNull(2)?.removePrefix("Success:")?.trim() ?: "Unknown"

                    // Detay nesnesini, orijinal detay stringini de dahil ederek oluştur
                    val detail = DailyStepEntryDetail(steps, target, status, trimmedEntryString)

                    parsedEntriesForDay.add(detail)
                } else {
                    Log.w("DailySummaryPage", "Skipping empty or malformed entry detail string after split: $trimmedEntryString in line: $line")
                }
            }

            // Gruplandırma için Map'e ekle
            if (parsedEntriesForDay.isNotEmpty()) {
                // toMutableStateList() kullanarak bu listedeki öğe ekleme/çıkarma/güncelleme durumlarını Composable'ın gözlemlemesini sağla
                dailySummaries[date] = parsedEntriesForDay.toMutableStateList()
            } else {
                Log.w("DailySummaryPage", "No valid entries found for date $date in line: $line")
            }
        }
        Log.d("DailySummaryPage", "Loaded ${dailySummaries.size} unique dates.")
    }

    // Sayfa ilk yüklendiğinde verileri yükle
    LaunchedEffect(Unit) {
        loadDailySummaries()
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
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
        // Liste Alanı - LazyColumn
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // *** SIRALAMA BURADA YAPILDI ***
            // Tarih key'ine (String) göre azalan sırada (yeni tarihten eskiye) sırala
            items(
                items = dailySummaries.entries.toList().sortedByDescending { it.key }, // Entry'leri tarihe göre azalan sırada sırala
                key = { it.key } // Her gün için benzersiz anahtar (tarih stringi)
            ) { (date, entriesForDay) ->
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
                        // Tarih
                        Text(
                            text = date,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // O güne ait her bir kaydı listele - BURADA DEĞİŞTİ (forEachIndexed)
                        entriesForDay.forEachIndexed { index, entryDetail -> // <-- index alındı
                            // İlk kayıt hariç diğerlerinden önce ayırıcı ekle
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
                                    // Adımlar
                                    Text(
                                        text = "Steps: ${entryDetail.steps}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    // Hedef adım sayısı varsa
                                    entryDetail.target?.let { target ->
                                        Text(
                                            text = "Target: $target",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    } ?: Text(
                                        text = "Target: Unknown",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    // Başarı durumu
                                    Text(
                                        text = when (entryDetail.status) {
                                            "Successful" -> "Status: ✅ Goal Achieved!"
                                            "Unsuccessful" -> "Status: ❌ Goal Not Achieved"
                                            else -> "Status: Unknown"
                                        },
                                        color = when (entryDetail.status) {
                                            "Successful" -> Color(0xFF2E7D32) // Yeşil
                                            "Unsuccessful" -> Color.Red
                                            else -> Color.Gray
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                // Silme ikonu - Tıklanınca index ve string kaydediliyor - BURADA DEĞİŞTİ
                                IconButton(
                                    onClick = {
                                        // Silinecek öğe için tarihi, indeksi ve orijinal detay stringini sakla
                                        entryToDelete = Pair(date, Pair(index, entryDetail.originalDetailString)) // <-- index de eklendi
                                        Log.d("DailySummaryPage", "Prepare to delete entry at index $index for date $date with content: ${entryDetail.originalDetailString}")
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

                        // Toplam adım sayısını hesapla
                        val totalStepsForDay = entriesForDay.sumOf { it.steps }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Toplam Adım Sayısı
                        Text(
                            text = "Total Steps for $date: $totalStepsForDay",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            // Eğer hiç özet yoksa bilgi mesajı göster
            if (dailySummaries.isEmpty()) {
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


        // Clear All Button - Alt kısımda sabit
        Button(
            onClick = { showDeleteAllDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Clear All Summaries")
        }


        // Bireysel silme dialog
        // Bireysel silme dialog - BURADA DEĞİŞTİ
        if (entryToDelete != null) {
            AlertDialog(
                onDismissRequest = { entryToDelete = null },
                title = { Text("Delete Entry", color = MaterialTheme.colorScheme.primary) },
                text = { Text("Are you sure you want to delete this specific saved entry?") },
                confirmButton = {
                    TextButton(onClick = {
                        val (dateKey, indexAndDetail) = entryToDelete!!
                        val (indexToRemove, originalDetailString) = indexAndDetail // indeksi ve stringi al

                        // Dosyadan silme fonksiyonunu indeksle çağır - BURADA DEĞİŞTİ
                        deleteEntry(context, dateKey, indexToRemove) // <-- İndeks gönderiliyor

                        // State'ten de kaldır (artık indeksi biliyoruz) - BURADA DEĞİŞTİ
                        val entriesForDay = dailySummaries[dateKey]
                        if (entriesForDay != null && indexToRemove >= 0 && indexToRemove < entriesForDay.size) {
                            // Belirtilen indeksteki öğeyi state listesinden kaldır
                            val removedDetail = entriesForDay.removeAt(indexToRemove) // <-- İndekse göre kaldır
                            Log.d("DailySummaryPage", "Removed item from state at index $indexToRemove for date $dateKey. Content: ${removedDetail.originalDetailString}")

                            // Eğer bu tarih için State listesinde hiç giriş kalmadıysa, tarihi map'ten kaldır
                            if (entriesForDay.isEmpty()) {
                                dailySummaries.remove(dateKey)
                                Log.d("DailySummaryPage", "Removed date key $dateKey from state as it's now empty.")
                            }

                        } else {
                            // Bu hata durumu, eğer state ile dosya arasında ciddi bir tutarsızlık olursa oluşabilir.
                            // Bu durumda veriyi yeniden yüklemek iyi bir çözüm olabilir
                            Log.w("DailySummaryPage", "Attempted to remove item from state at invalid index $indexToRemove for date $dateKey. State list size: ${entriesForDay?.size}")
                            loadDailySummaries() // State bozuksa dosyadan tekrar yükle
                        }

                        entryToDelete = null // Dialogu kapat
                        Log.d("DailySummaryPage", "Confirmed deletion attempt for entry at index $indexToRemove for date $dateKey. UI state updated.")

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
                        dailySummaries.clear()
                        showDeleteAllDialog = false
                        Log.d("DailySummaryPage", "Confirmed clearing all summaries.")
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