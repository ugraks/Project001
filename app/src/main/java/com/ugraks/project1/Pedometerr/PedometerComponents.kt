package com.ugraks.project1.Pedometerr

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.IOException // IOException eklendi

// Aynı satırdaki birden fazla giriş için ayırıcı
private const val ENTRY_DELIMITER = " | "
private const val DATE_FORMAT = "yyyy-MM-dd"

fun saveDailyStepCount(
    context: Context,
    stepCount: Int,
    targetStep: Int? = null,
    goalReached: Boolean? = null
) {
    val currentDateString = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
    val file = File(context.filesDir, "daily_steps.txt")

    val targetText = targetStep?.let { "Target: $it" } ?: "Target: Unknown"
    val statusText = when (goalReached) {
        true -> "Success: Successful"
        false -> "Success: Unsuccessful"
        else -> "Success: Unknown"
    }

    // Tek bir kaydetme detayının formatı (tarihsiz)
    val saveEntryDetailString = "$stepCount, $targetText, $statusText"

    val lines = try {
        if (file.exists()) file.readLines().toMutableList() else mutableListOf()
    } catch (e: IOException) {
        Log.e("SaveDailyStepCount", "Error reading file before saving: ${e.message}")
        return // Dosya okuma hatası varsa kaydetme
    }


    var foundDateLineIndex = -1
    // Tarih satırını ara
    for (i in lines.indices) {
        if (lines[i].startsWith(currentDateString + ":")) {
            foundDateLineIndex = i
            break
        }
    }

    if (foundDateLineIndex != -1) {
        // Eğer tarih satırı bulunursa, yeni giriş detayını mevcut satıra ekle
        val existingLine = lines[foundDateLineIndex]
        lines[foundDateLineIndex] = "$existingLine$ENTRY_DELIMITER$saveEntryDetailString"
        Log.d("SaveDailyStepCount", "Appended entry to existing line for $currentDateString.")
    } else {
        // Eğer tarih satırı bulunamazsa, bu tarih için yeni bir satır ekle
        lines.add("$currentDateString:$saveEntryDetailString")
        Log.d("SaveDailyStepCount", "Added new line for date $currentDateString.")
    }

    try {
        // Güncellenmiş satırlarla tüm dosyayı yeniden yaz
        file.writeText(lines.joinToString("\n"))
        Log.d("SaveDailyStepCount", "File rewritten successfully.")
    } catch (e: IOException) {
        Log.e("SaveDailyStepCount", "Error writing file after saving: ${e.message}")
    }
}

// deleteEntry, belirli bir tarihteki satırdan belirli bir detay stringini kaldıracak şekilde değiştirildi
fun deleteEntry(context: Context, date: String, indexToDelete: Int) { // <-- Parametreler değişti
    val file = File(context.filesDir, "daily_steps.txt")
    if (!file.exists()) {
        Log.w("DeleteEntry", "File not found for deletion.")
        return
    }

    val lines = try {
        file.readLines().toMutableList()
    } catch (e: IOException) {
        Log.e("DeleteEntry", "Error reading file for deletion: ${e.message}")
        return
    }

    var lineModifiedOrRemoved = false
    // mapNotNull kullanarak güncellenmiş satır listesini oluştur
    val updatedLines = lines.mapNotNull { line ->
        if (line.startsWith("$date:")) {
            val dateAndData = line.split(":", limit = 2)
            if (dateAndData.size == 2) {
                val datePart = dateAndData[0]
                val dataPart = dateAndData[1]

                val entriesForDay = dataPart.split(ENTRY_DELIMITER).toMutableList()

                // İndex kontrolü yap ve eğer geçerliyse o indeksteki öğeyi kaldır - BURADA DEĞİŞTİ
                if (indexToDelete >= 0 && indexToDelete < entriesForDay.size) {
                    val removedEntry = entriesForDay.removeAt(indexToDelete) // <-- İndekse göre kaldır
                    lineModifiedOrRemoved = true // Bir öğe kaldırıldı
                    Log.d("DeleteEntry", "Removed entry from file at index $indexToDelete for date $date. Content: ${removedEntry.trim()}")

                    if (entriesForDay.isNotEmpty()) {
                        // Kalan girişleri birleştir ve satırı tut
                        "$datePart:${entriesForDay.joinToString(ENTRY_DELIMITER)}"
                    } else {
                        // Bu tarih için hiç giriş kalmadıysa, tüm satırı kaldır
                        null // mapNotNull için null döndürmek öğeyi kaldırır
                    }
                } else {
                    // Geçersiz index, bu satırı değiştirmeden tut - Log eklendi/güncellendi
                    Log.w("DeleteEntry", "Attempted to delete entry with invalid index $indexToDelete for date $date. Line not modified.")
                    line // Satırı değiştirmeden bırak
                }
            } else {
                // Hedef tarih için hatalı biçimlendirilmiş satır, sakla ama uyarı logu ver
                Log.w("DeleteEntry", "Malformed line found for date $date during deletion attempt: $line")
                line // Hatalı satırı değiştirmeden bırak
            }
        } else {
            // Hedef tarih satırı değil, sakla
            line
        }
    }

    // Eğer herhangi bir değişiklik yapıldıysa dosyayı yeniden yaz
    if (lineModifiedOrRemoved) {
        try {
            // Güncellenmiş satırları dosyaya geri yaz
            file.writeText(updatedLines.joinToString("\n"))
            Log.d("DeleteEntry", "File updated after deletion for date: $date")
        } catch (e: IOException) {
            Log.e("DeleteEntry", "Error rewriting file after deletion: ${e.message}")
            // Dosya yazma hatası durumunda ne yapılacağı düşünülmeli (örn. kullanıcıya bilgi verme)
        }
    } else {
        // lineModifiedOrRemoved false ise dosya değişmedi demektir.
        Log.w("DeleteEntry", "No entry deleted for date $date, index $indexToDelete. Index might have been invalid or line not found or malformed.")
    }
}

fun clearAllSummaries(context: Context) {
    val file = File(context.filesDir, "daily_steps.txt")
    if (file.exists()) {
        try {
            file.delete()
            Log.d("ClearAllSummaries", "All summaries cleared.")
        } catch (e: IOException) {
            Log.e("ClearAllSummaries", "Error clearing summaries: ${e.message}")
        }
    } else {
        Log.w("ClearAllSummaries", "File not found for clearing.")
    }
}