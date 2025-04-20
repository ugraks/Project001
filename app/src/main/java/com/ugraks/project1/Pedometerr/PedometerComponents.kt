package com.ugraks.project1.Pedometerr

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun saveDailyStepCount(
    context: Context,
    stepCount: Int,
    targetStep: Int? = null,
    goalReached: Boolean? = null
) {
    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val file = File(context.filesDir, "daily_steps.txt")

    // Hedefi kontrol et
    val targetText = targetStep?.let { "Target: $it" } ?: "Target: Unknown" // Hedef belirlenmemişse 'Unknown'

    // Başarı durumu kontrolü
    val statusText = when {
        targetStep == null -> "Success: Unknown"  // Hedef yoksa başarı durumu 'Unknown'
        goalReached == true -> "Success: Successful"  // Hedef belirtilmişse ve başarılıysa
        goalReached == false -> "Success: Unsuccessful"  // Hedef belirtilmişse ve başarısızsa
        else -> "Success: Unknown" // Başarı durumu belirlenmemişse
    }

    // Örnek satır: 2025-04-20: 5000, Target: 6000, Success: Unsuccessful
    val newLine = buildString {
        append("$currentDate: $stepCount") // Adım sayısını ekle
        append(", $targetText")            // Hedefi ekle
        append(", $statusText")            // Başarı durumunu ekle
    }

    // Dosyadaki mevcut satırları oku
    val lines = if (file.exists()) file.readLines() else emptyList()

    // Eğer aynı gün için yeni bir satır varsa, mevcut satırı güncelle
    val updatedLines = lines.map { line ->
        val parts = line.split(":")
        if (parts.isNotEmpty() && parts[0].trim() == currentDate) {
            newLine // Aynı tarihteki veriyi yeni satırla değiştir
        } else {
            line // Diğer satırları olduğu gibi bırak
        }
    }.toMutableList()

    // Eğer mevcut satırda tarih bulunmuyorsa, yeni bir satır ekle
    if (updatedLines.none { it.startsWith(currentDate) }) {
        updatedLines.add(newLine)
    }

    // Güncellenmiş satırları dosyaya yaz
    file.writeText(updatedLines.joinToString("\n"))
}



fun deleteEntry(context: Context, entry: String) {
    val file = File(context.filesDir, "daily_steps.txt")
    if (file.exists()) {
        val lines = file.readLines().toMutableList()
        lines.remove(entry)
        file.writeText(lines.joinToString("\n"))
    }
}

fun clearAllSummaries(context: Context) {
    val file = File(context.filesDir, "daily_steps.txt")
    if (file.exists()) {
        file.delete()
    }
}


