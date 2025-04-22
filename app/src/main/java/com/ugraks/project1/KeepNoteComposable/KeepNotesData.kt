package com.ugraks.project1.KeepNoteComposable

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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



