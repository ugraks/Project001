package com.ugraks.project1.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson // Gson kütüphanesini import edin
import com.google.gson.reflect.TypeToken // TypeToken için import edin
import java.util.Collections // EmptyList için import edin
// import android.util.Log // Hata loglamak isterseniz import edin

class Converters {
    // Gson objesini bir kere oluşturup tekrar kullanıyoruz
    private val gson = Gson()

    // List<String>'i Room'a kaydetmek için String'e (JSON) dönüştürür
    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        // Gson, null veya boş listeyi de geçerli JSON'a dönüştürebilir (null -> "null", [] -> "[]")
        // Bu nedenle null kontrolü gerekli olmayabilir, ancak açık olmak faydalı.
        // Eğer list null ise veya boşsa boş string kaydetmek yerine boş JSON array ("[]") kaydetmek
        // okuma (toStringList) metodunu biraz daha sağlam yapar.
        if (list == null) {
            return gson.toJson(Collections.emptyList<String>()) // Null listeyi boş JSON array olarak kaydet
        }
        // Listeyi JSON string'ine dönüştür
        return gson.toJson(list)
    }

    // Room'dan okunan String'i (JSON) List<String>'e dönüştürür
    @TypeConverter
    fun toStringList(string: String?): List<String> {
        // Null veya boş string gelirse boş liste döndür
        if (string.isNullOrBlank()) {
            return Collections.emptyList()
        }

        // JSON string'ini tekrar List<String>'e dönüştürmek için TypeToken kullanılır
        val listType = object : TypeToken<List<String>>() {}.type

        return try {
            // Gson ile JSON string'ini List<String>'e dönüştürmeyi dene
            val resultList: List<String>? = gson.fromJson(string, listType)
            // Gson ayrıştırması başarılı olursa listeyi döndür, null dönerse boş liste döndür
            resultList ?: Collections.emptyList()
        } catch (e: Exception) {
            // JSON string'i ayrıştırılamazsa (hatalı format vb.)
            // Hata logu ekleyerek sorunu teşhis etmeye yardımcı olabilirsiniz.
            // Log.e("Converters", "Error converting JSON string to List<String>: '$string'", e)
            Collections.emptyList() // Hata durumunda boş liste döndürerek uygulamanın çökmesini engelle
        }
    }

    // Uygulamanızda başka özel tipler kullanıyorsanız, onlar için de buraya TypeConverter'lar ekleyin
    // Örneğin Date objeleri için Timestamp (Long) ile dönüşüm:
    /*
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
     */
}