package com.ugraks.project1.KeepNoteComposable

import android.content.Context
import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.io.File
import java.io.InputStreamReader
import java.time.LocalDate
import kotlin.math.roundToInt

data class FoodItemKeepNote(
    val name: String,
    val calories: Int, // Calories per 1000 units (kg or ml)
    val type: String, // "Food" or "Drink"
    val proteinPerKgL: Double, // Grams of protein per 1000 units (kg or L)
    val fatPerKgL: Double,     // Grams of fat per 1000 units (kg or L)
    val carbPerKgL: Double     // Grams of carbs per 1000 units (kg or L)
)

data class DailySummary(
    val date: String,
    val calories: Int,
    val protein: Double,
    val fat: Double,
    val carbs: Double
)

data class CalorieRecord(
    val foodItem: FoodItemKeepNote, // Stores the item details (name, original calories per 1000, type, P/F/C per 1000)
    val quantity: Double,
    val unit: String, // "g", "kg", "ml", "L"
    val time: String,
    val calories: Int, // Total calories for this specific record
    val protein: Double, // Total protein for this specific record
    val fat: Double,     // Total fat for this specific record
    val carb: Double     // Total carb for this specific record
)

// Function to read food items from assets (Assets dosyasından yiyecekleri okuma - Değişiklik Yok)
fun readFoodItemsFromAssets(context: Context): List<FoodItemKeepNote> {
    val items = mutableListOf<FoodItemKeepNote>()
    try {
        val inputStream = context.assets.open("items.txt")
        InputStreamReader(inputStream).forEachLine { line ->
            val parts = line.split(",")
            if (parts.size == 6) { // Expect 6 parts now (item,calories,type,protein_g,fat_g,carb_g)
                val itemName = parts[0].split(":")[1].trim()
                val calories = parts[1].split(":")[1].trim().toIntOrNull() ?: 0
                val itemType = parts[2].split(":")[1].trim()
                val protein = parts[3].split(":")[1].trim().toDoubleOrNull() ?: 0.0 // Read protein
                val fat = parts[4].split(":")[1].trim().toDoubleOrNull() ?: 0.0     // Read fat
                val carb = parts[5].split(":")[1].trim().toDoubleOrNull() ?: 0.0     // Read carbs

                // Assuming calories, protein_g, fat_g, carb_g in items.txt are per 1 kg or 1 Liter (1000 units)
                items.add(FoodItemKeepNote(itemName, calories, itemType, protein, fat, carb))
            } else {
                // Log an error or warning for malformed lines
                println("Skipping malformed line in items.txt: $line")
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return items
}

// --- Dosya İşlemleri Fonksiyonları (Kalıcılık için Eklendi) ---

// Kayıtları dosyaya kaydetme
fun saveCalorieRecords(context: Context, records: List<CalorieRecord>) {
    val fileName = "calorie_records.txt"
    val file = File(context.filesDir, fileName) // Uygulamanın dahili depolama alanı

    try {
        file.bufferedWriter().use { writer ->
            records.forEach { record ->
                // Kayıt formatı: foodName|originalCaloriesPer1000|originalType|originalProteinPerKgL|originalFatPerKgL|originalCarbPerKgL|quantity|unit|time|totalCalories|totalProtein|totalFat|totalCarb
                // Manuel girişlerde original* değerleri 0 olabilir, bu beklenen bir durumdur.
                val line = "${record.foodItem.name}|${record.foodItem.calories}|${record.foodItem.type}|${record.foodItem.proteinPerKgL}|${record.foodItem.fatPerKgL}|${record.foodItem.carbPerKgL}|${record.quantity}|${record.unit}|${record.time}|${record.calories}|${record.protein}|${record.fat}|${record.carb}"
                writer.write(line)
                writer.newLine() // Her kayıt arasına yeni satır ekle
            }
        }
        println("Calorie records saved to $fileName") // Log for confirmation
    } catch (e: Exception) {
        e.printStackTrace()
        println("Error saving calorie records: ${e.message}") // Log error
    }
}

// Kayıtları dosyadan yükleme
fun loadCalorieRecords(context: Context): List<CalorieRecord> {
    val fileName = "calorie_records.txt"
    val file = File(context.filesDir, fileName) // Uygulamanın dahili depolama alanı
    val loadedRecords = mutableListOf<CalorieRecord>()

    if (!file.exists()) {
        println("$fileName does not exist. Returning empty list.") // Log if file not found
        return emptyList() // Dosya yoksa boş liste döndür
    }

    try {
        file.bufferedReader().use { reader ->
            reader.forEachLine { line ->
                val parts = line.split("|")
                // Beklenen parça sayısı: 13 (FoodItem'dan 6, Record'dan 7)
                if (parts.size == 13) {
                    try {
                        val foodName = parts[0]
                        val originalCaloriesPer1000 = parts[1].toIntOrNull() ?: 0
                        val originalType = parts[2]
                        val originalProteinPerKgL = parts[3].toDoubleOrNull() ?: 0.0
                        val originalFatPerKgL = parts[4].toDoubleOrNull() ?: 0.0
                        val originalCarbPerKgL = parts[5].toDoubleOrNull() ?: 0.0

                        val quantity = parts[6].toDoubleOrNull() ?: 0.0
                        val unit = parts[7]
                        val time = parts[8]
                        val totalCalories = parts[9].toIntOrNull() ?: 0
                        val totalProtein = parts[10].toDoubleOrNull() ?: 0.0
                        val totalFat = parts[11].toDoubleOrNull() ?: 0.0
                        val totalCarb = parts[12].toDoubleOrNull() ?: 0.0

                        // FoodItem'ı yeniden oluştur (manuel girişlerdeki 0 değerlerini korur)
                        val foodItem = FoodItemKeepNote(
                            name = foodName,
                            calories = originalCaloriesPer1000,
                            type = originalType,
                            proteinPerKgL = originalProteinPerKgL,
                            fatPerKgL = originalFatPerKgL,
                            carbPerKgL = originalCarbPerKgL
                        )

                        // CalorieRecord'u yeniden oluştur (kaydedilen toplam değerleri kullanır)
                        val record = CalorieRecord(
                            foodItem = foodItem, // Orijinal veya manuel FoodItem detayları
                            quantity = quantity,
                            unit = unit,
                            time = time,
                            calories = totalCalories, // Kaydedilen toplam kalori
                            protein = totalProtein,   // Kaydedilen toplam protein
                            fat = totalFat,         // Kaydedilen toplam yağ
                            carb = totalCarb        // Kaydedilen toplam karbonhidrat
                        )
                        loadedRecords.add(record)

                    } catch (e: NumberFormatException) {
                        println("Skipping malformed line due to number format error: $line - ${e.message}")
                        // Hatalı formatlı satırları atla
                    } catch (e: Exception) {
                        println("Skipping malformed line due to unexpected error: $line - ${e.message}")
                        // Diğer hataları yakala
                    }
                } else {
                    println("Skipping malformed line (incorrect part count): $line")
                    // Beklenen parça sayısında olmayan satırları atla
                }
            }
        }
        println("Calorie records loaded from $fileName. Loaded ${loadedRecords.size} records.") // Log for confirmation
    } catch (e: Exception) {
        e.printStackTrace()
        println("Error loading calorie records: ${e.message}") // Log error
        return emptyList() // Hata olursa boş liste döndür
    }

    return loadedRecords
}

fun saveTodaySummary(context: Context, calories: Int, protein: Double, fat: Double, carbs: Double): SaveSummaryResult {
    val date = LocalDate.now().toString()  // Bugünün tarihi
    val file = File(context.filesDir, "daily_summaries.txt")

    val existingSummaries = try {
        readDailySummaries(context).toMutableList()  // Dosyadaki mevcut özetleri oku
    } catch (e: Exception) {
        Log.e("KeepNoteComposable", "Error reading daily summaries for save/update: ${e.message}")
        return SaveSummaryResult.ERROR // Okuma hatası
    }

    val existingSummaryIndex = existingSummaries.indexOfFirst { it.date == date }

    if (existingSummaryIndex == -1) {
        // Eğer aynı tarihte bir özet YOKSA, yeni özet oluştur ve ekle
        val newSummary = DailySummary(date, calories, protein, fat, carbs)
        existingSummaries.add(newSummary)

        // Dosyaya kaydet
        try {
            saveSummariesToFile(context, existingSummaries)
            Log.d("KeepNoteComposable", "Newly saved daily summary for $date. Data: C$calories P${protein.roundToInt()} F${fat.roundToInt()} C${carbs.roundToInt()}")
            return SaveSummaryResult.NEWLY_SAVED // Başarılı şekilde kaydedildi
        } catch (e: Exception) {
            Log.e("KeepNoteComposable", "Error saving new daily summary for $date: ${e.message}")
            return SaveSummaryResult.ERROR // Kaydetme hatası
        }

    } else {
        // Eğer aynı tarihte bir özet VARSA, mevcut özeti GÜNCELLE
        val existingSummary = existingSummaries[existingSummaryIndex]
        // Mevcut özeti kopyala ve değerleri güncelle
        val updatedSummary = existingSummary.copy(
            calories = calories,
            protein = protein,
            fat = fat,
            carbs = carbs
        )
        existingSummaries[existingSummaryIndex] = updatedSummary // Listede güncelle

        // Dosyaya kaydet (tüm listeyi yeniden yaz)
        try {
            saveSummariesToFile(context, existingSummaries)
            Log.d("KeepNoteComposable", "Updated daily summary for $date. New Data: C$calories P${protein.roundToInt()} F${fat.roundToInt()} C${carbs.roundToInt()}")
            return SaveSummaryResult.UPDATED // Başarılı şekilde güncellendi
        } catch (e: Exception) {
            Log.e("KeepNoteComposable", "Error saving updated daily summary for $date: ${e.message}")
            return SaveSummaryResult.ERROR // Kaydetme hatası
        }
    }
}

fun readDailySummaries(context: Context): List<DailySummary> {
    val file = File(context.filesDir, "daily_summaries.txt")
    if (!file.exists()) return emptyList()

    val summaries = mutableListOf<DailySummary>()
    val content = file.readText()
    val entries = content.split("---").map { it.trim() }.filter { it.isNotEmpty() }

    for (entry in entries) {
        val lines = entry.lines()
        val date = lines.getOrNull(0)?.removePrefix("Date: ")?.trim() ?: continue
        val calories = lines.getOrNull(1)?.removePrefix("Calories: ")?.replace("kcal", "")?.trim()?.toIntOrNull() ?: 0
        val protein = lines.getOrNull(2)?.removePrefix("Protein: ")?.replace("g", "")?.trim()?.toDoubleOrNull() ?: 0.0
        val fat = lines.getOrNull(3)?.removePrefix("Fat: ")?.replace("g", "")?.trim()?.toDoubleOrNull() ?: 0.0
        val carbs = lines.getOrNull(4)?.removePrefix("Carbs: ")?.replace("g", "")?.trim()?.toDoubleOrNull() ?: 0.0

        summaries.add(DailySummary(date, calories, protein, fat, carbs))
    }

    return summaries
}

enum class SaveSummaryResult {
    NEWLY_SAVED, // Yeni özet kaydedildi
    UPDATED,     // Mevcut özet güncellendi
    NO_ACTION,   // Herhangi bir işlem yapılmadı (örn: iptal edildi, veri yok) - Bu senaryo bu fonksiyonda pek olmayacak
    ERROR        // Hata oluştu
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

