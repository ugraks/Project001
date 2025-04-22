package com.ugraks.project1.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Günlük özetleri temsil eden tablo
@Entity(tableName = "daily_summaries")
data class DailySummaryEntity(
    // Tarih, her gün için bir özet olacağı için primary key
    @PrimaryKey val date: String, // Örneğin "YYYY-MM-DD" formatında
    val calories: Int,
    val protein: Double,
    val fat: Double,
    val carbs: Double
)