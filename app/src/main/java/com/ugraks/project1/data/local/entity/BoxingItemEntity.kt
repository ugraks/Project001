package com.ugraks.project1.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "boxing_items") // Veritabanı tablo adı
data class BoxingItemEntity(
    // Boks öğesinin adı genellikle benzersizdir, primary key olarak kullanılabilir.
    // Eğer boks öğesi adları benzersiz değilse, otomatik üretilen bir ID (@PrimaryKey(autoGenerate = true) val id: Int = 0,) kullanmalısınız.
    // Şu an için adı primary key olarak varsayalım:
    @PrimaryKey val name: String,

    val category: String,  // Kategori
    val description: String, // Açıklama
    val details: String    // Detaylar
)