package com.ugraks.project1.data.local.entity // Kendi paket adınız

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters // TypeConverters import'unu ekleyin
import com.ugraks.project1.data.local.converter.Converters // Converters sınıfını import edin


// Tarifleri temsil eden tablo
@Entity(tableName = "recipes")
// YENİ: TypeConverters annotation'ını Entity seviyesinde de ekleyin
@TypeConverters(Converters::class)
data class RecipeEntity(
    @PrimaryKey val name: String,

    // ingredients alanı List<String> olarak tanımlanacak.
    // Room, hem AppDatabase'teki hem de buradaki @TypeConverters sayesinde bunu dönüştürecek.
    val ingredients: List<String>,

    val instructions: String
)