package com.ugraks.project1.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises") // Veritabanı tablo adı
data class ExerciseEntity(
    // Egzersiz adı genellikle benzersizdir, primary key olarak kullanılabilir.
    // Eğer egzersiz adları benzersiz değilse, otomatik üretilen bir ID (@PrimaryKey(autoGenerate = true) val id: Int = 0,) kullanmalısınız.
    // Şu an için adı primary key olarak varsayalım:
    @PrimaryKey val name: String,

    val muscleGroup: String,      // Kas grubu
    val description: String,      // Egzersizin açıklaması
    val howToDo: String           // Egzersizin nasıl yapılacağı açıklaması
)