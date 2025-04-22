package com.ugraks.project1.data.local.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

// TXT dosyasındaki bireysel kayıtları temsil eden tablo
@Entity(tableName = "calorie_records")
data class CalorieRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Her kayıt için otomatik üretilen benzersiz ID

    // FoodItemKeepNote'dan gelen bilgiler (manuel girişleri de barındırmak için buraya alındı)
    val foodName: String,
    val originalCaloriesPer1000: Int, // 1000 birimdeki kalori değeri (kg/L)
    val foodType: String, // "Food" veya "Drink"
    val originalProteinPerKgL: Double,
    val originalFatPerKgL: Double,
    val originalCarbPerKgL: Double,

    // Kayıt için girilen detaylar
    val quantity: Double,
    val unit: String, // "g", "kg", "ml", "L"
    val time: String, // "HH:mm" formatında string olabilir

    // Hesaplanan toplam makro/kalori değerleri
    val totalCalories: Int,
    val totalProtein: Double,
    val totalFat: Double,
    val totalCarb: Double // Alan adını netleştirdik
)