package com.ugraks.project1.Foods

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.ugraks.project1.R
import com.ugraks.project1.data.local.entity.FoodItemEntity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale
import kotlin.math.roundToInt

data class FoodItem(
    val name: String,
    val calories: Int, // per 1000 units
    val type: String,
    val proteinPerKgL: Double,
    val fatPerKgL: Double,
    val carbPerKgL: Double
)

data class NutritionalValues(
    val calories: Int,
    val protein: Double,
    val fat: Double,
    val carb: Double
)

fun readAndParseItemsFromAssets(context: Context): List<FoodItem> {
    val foodItems = mutableListOf<FoodItem>()
    try {
        val inputStream = context.assets.open("items.txt")
        val reader = BufferedReader(InputStreamReader(inputStream))
        reader.forEachLine { line ->
            val parts = line.split(",")
            if (parts.size == 6) {
                val name = parts[0].split(":")[1].trim()
                val calories = parts[1].split(":")[1].trim().toIntOrNull() ?: 0
                val type = parts[2].split(":")[1].trim()
                val protein = parts[3].split(":")[1].trim().toDoubleOrNull() ?: 0.0
                val fat = parts[4].split(":")[1].trim().toDoubleOrNull() ?: 0.0
                val carb = parts[5].split(":")[1].trim().toDoubleOrNull() ?: 0.0
                foodItems.add(FoodItem(name, calories, type, protein, fat, carb))
            }
        }
        reader.close()
    } catch (e: Exception) { e.printStackTrace() }
    return foodItems
}

fun calculateNutritionalValues(item: FoodItemEntity, quantity: Int, isKg: Boolean, isLiters: Boolean): NutritionalValues {
    val quantityInBaseUnits = when {
        item.type == "Food" -> if (isKg) quantity * 1000.0 else quantity.toDouble()
        item.type == "Drink" -> if (isLiters) quantity * 1000.0 else quantity.toDouble()
        else -> quantity.toDouble()
    }
    val scaleFactor = quantityInBaseUnits / 1000.0
    val totalCalories = (item.calories * scaleFactor).roundToInt()
    val totalProtein = item.proteinPerKgL * scaleFactor
    val totalFat = item.fatPerKgL * scaleFactor
    val totalCarb = item.carbPerKgL * scaleFactor
    return NutritionalValues(totalCalories, totalProtein, totalFat, totalCarb)
}

@Composable
fun getImageResource(foodName: String): Int {
    val imageName = foodName.lowercase(Locale.getDefault()).replace(" ", "_")
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
    return if (resId != 0) resId else R.drawable.ic_launcher_background
}