package com.ugraks.project1.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// daily_steps.txt dosyasındaki her bir kaydedilmiş adımı temsil eden tablo
@Entity(tableName = "daily_steps")
data class DailyStepEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Her bir giriş için benzersiz ID

    val date: String, // "YYYY-MM-DD" formatında, hangi güne ait olduğunu belirtir
    val timestamp: Long, // Bu girişin kaydedildiği zaman (milisaniye olarak), sıralama için kullanışlı
    val steps: Int,
    val target: Int?, // Null olabilir
    val status: String // "Successful", "Unsuccessful" veya "Unknown"
)