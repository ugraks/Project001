package com.ugraks.project1.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey // Aktivite adını primary key olarak belirliyoruz
    @ColumnInfo(name = "name") // Sütun adını belirliyoruz
    val name: String,

    @ColumnInfo(name = "met_value") // MET değeri için sütun
    val metValue: Double
)