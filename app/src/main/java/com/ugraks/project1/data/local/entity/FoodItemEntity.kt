package com.ugraks.project1.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Yemek öğelerini temsil eden tablo
// items.txt dosyasındaki FoodItem yapısına karşılık gelir
@Entity(tableName = "food_items")
data class FoodItemEntity(
    // name'i Primary Key yapalım, yemek isimleri benzersiz olmalı.
    @PrimaryKey val name: String,

    val calories: Int, // 1000 birimdeki kalori
    val type: String, // "Food" veya "Drink"
    val proteinPerKgL: Double, // 1000 birimdeki (kg veya Lt) protein miktarı (gram olarak)
    val fatPerKgL: Double,     // 1000 birimdeki (kg veya Lt) yağ miktarı (gram olarak)
    val carbPerKgL: Double     // 1000 birimdeki (kg veya Lt) karbonhidrat miktarı (gram olarak)
)