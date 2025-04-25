package com.ugraks.project1.Boxing


import android.content.Context
import com.ugraks.project1.R // Assuming R is accessible
import java.io.BufferedReader
import java.io.InputStreamReader

// Data class for a Boxing Item
data class BoxingItem(
    val name: String,      // Name of the item (e.g., Jab, Peek-a-boo Style)
    val category: String,  // Category (e.g., Techniques, Styles)
    val description: String, // Brief description
    val details: String    // Detailed explanation or how-to
)

// Function to load boxing data from assets
fun loadBoxingDataFromAssets(context: Context): List<BoxingItem> {
    val boxingItems = mutableListOf<BoxingItem>()
    try {
        // Read from boxing.txt
        val inputStream = context.assets.open("boxing.txt")
        val reader = BufferedReader(InputStreamReader(inputStream))
        reader.useLines { lines ->
            lines.forEach { line ->
                val parts = line.split(";") // Split by semicolon
                if (parts.size == 4) {
                    val name = parts[0].trim()
                    val category = parts[1].trim()
                    val description = parts[2].trim()
                    val details = parts[3].trim()
                    boxingItems.add(BoxingItem(name, category, description, details))
                } else {
                    // Optional: Log a warning for incorrect line format
                    println("Skipping malformed line in boxing.txt: $line")
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        // Handle error loading file
    }
    return boxingItems
}

// Function to get image resource for a boxing item (using default for now)
fun getBoxingImageResource(itemName: String): Int {
    // TODO: Add specific image mappings for boxing items later
    return R.drawable.baseline_sports_mma_24 // Placeholder icon
}