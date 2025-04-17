package com.ugraks.project1.Authenticate


import android.content.Context
import android.util.Log
import android.widget.Toast
import com.ugraks.project1.FoodItemKeepNote
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

fun isEmailAlreadyRegistered(context: Context, email: String): Boolean {
    val file = File(context.filesDir, "users.txt")
    if (!file.exists()) return false

    return file.readLines().any { line ->
        val emailPart = line.split(";").find { it.startsWith("email=") }
        emailPart?.substringAfter("email=") == email
    }
}

fun isPhoneAlreadyRegistered(context: Context, phone: String): Boolean {
    val file = File(context.filesDir, "users.txt")
    if (!file.exists()) return false

    return file.readLines().any { line ->
        val phonePart = line.split(";").find { it.startsWith("phone=") }
        phonePart?.substringAfter("phone=") == phone
    }
}

fun saveUserToFile(
    context: Context,
    username: String,
    businessName: String,
    phone: String,
    email: String,
    password: String
) {
    val file = File(context.filesDir, "users.txt")
    if (!file.exists()) {
        file.createNewFile()
    }

    // Kullanıcı zaten kayıtlı mı?
    if (isEmailAlreadyRegistered(context, email)) {
        Toast.makeText(context, "This email is already registered.", Toast.LENGTH_LONG).show()
        return
    }

    if (isPhoneAlreadyRegistered(context, phone)) {
        Toast.makeText(context, "This phone number is already registered.", Toast.LENGTH_LONG).show()
        return
    }

    // Yeni formatta kullanıcı bilgisi
    val randomCode = generateRandomCode()  // Rastgele kod oluşturuluyor
    val userInfo = "username=$username;businessName=$businessName;phone=$phone;email=$email;password=$password;code=$randomCode"

    val fileContent = file.readText()
    if (fileContent.isNotEmpty() && !fileContent.endsWith("\n")) {
        // Dosya içeriği varsa ve sonu satır sonu karakteri ile bitmiyorsa, ekle
        file.appendText("\n")
    }

    file.appendText("$userInfo\n")

    Toast.makeText(context, "User registered successfully.", Toast.LENGTH_LONG).show()
}


fun checkUserCredentials(context: Context, email: String, password: String): Boolean {
    val file = File(context.filesDir, "users.txt")
    if (!file.exists()) return false

    val lines = file.readLines()
    for (line in lines) {
        val parts = line.split(";")
        val emailPart = parts.find { it.startsWith("email=") }?.substringAfter("email=")
        val passwordPart = parts.find { it.startsWith("password=") }?.substringAfter("password=")

        if (emailPart == email && passwordPart == password) {
            return true
        }
    }

    return false
}


fun getUsernameByEmail(context: Context, email: String): String? {
    val file = File(context.filesDir, "users.txt")
    if (!file.exists()) return null

    val lines = file.readLines()
    for (line in lines) {
        val parts = line.split(";")
        val emailPart = parts.find { it.startsWith("email=") }?.substringAfter("email=")
        val usernamePart = parts.find { it.startsWith("username=") }?.substringAfter("username=")

        if (emailPart == email) {
            return usernamePart
        }
    }

    return null
}

fun deleteUserByEmail(context: Context, email: String) {
    val file = File(context.filesDir, "users.txt")
    if (!file.exists()) return

    val updatedLines = file.readLines().filterNot { it.contains("email=$email") }
    file.writeText(updatedLines.joinToString("\n"))

    deleteRatingByEmail(context, email)
    deleteFoodByEmail(context, email)
}


fun getUserDataByEmail(context: Context, email: String): UserData? {
    val file = File(context.filesDir, "users.txt")
    if (!file.exists()) return null

    val lines = file.readLines()
    for (line in lines) {
        val parts = line.split(";")
        val emailPart = parts.find { it.startsWith("email=") }?.substringAfter("email=")

        if (emailPart == email) {
            val username = parts.find { it.startsWith("username=") }?.substringAfter("username=") ?: ""
            val businessName = parts.find { it.startsWith("businessName=") }?.substringAfter("businessName=") ?: ""
            val phone = parts.find { it.startsWith("phone=") }?.substringAfter("phone=") ?: ""
            val code = parts.find { it.startsWith("code=") }?.substringAfter("code=") ?: ""  // Code'u ekliyoruz

            return UserData(username, businessName, phone, emailPart ?: "", code)
        }
    }

    return null
}

data class UserData(
    val username: String,
    val businessName: String,
    val phone: String,
    val email: String,
    val code: String
)



fun updateUserData(
    context: Context,
    email: String,
    username: String,
    phone: String,
    businessName: String,
    password: String
): Boolean {
    val file = File(context.filesDir, "users.txt")
    if (!file.exists()) return false

    val lines = file.readLines().toMutableList()
    for (i in lines.indices) {
        val line = lines[i]
        if (line.contains("email=$email")) {
            val parts = line.split(";").associate {
                val (key, value) = it.split("=")
                key to value
            }.toMutableMap()

            // Sadece güncellenecek alanları değiştir
            parts["username"] = username
            parts["phone"] = phone
            parts["businessName"] = businessName
            if (password.isNotEmpty()) {
                parts["password"] = password
            }

            // Güncellenmiş satırı oluştur
            val updatedLine = parts.entries.joinToString(";") { "${it.key}=${it.value}" }
            lines[i] = updatedLine

            file.writeText(lines.joinToString("\n"))
            return true
        }
    }
    return false
}

fun generateRandomCode(): String {
    val characters = ('A'..'Z') + ('0'..'9')
    return (1..8)
        .map { characters.random() }
        .joinToString("")
}

fun updateOrAddRating(context: Context, email: String, rating: Int) {
    val file = File(context.filesDir, "rating.txt")
    if (!file.exists()) {
        // Eğer dosya yoksa, yeni bir dosya oluştur
        file.createNewFile()
    }

    // Dosyadaki satırları oku
    val lines = file.readLines().toMutableList()

    // Kullanıcının email'ine sahip olan satırı bul
    val existingLineIndex = lines.indexOfFirst { it.contains("email=$email") }

    if (existingLineIndex != -1) {
        // Eğer kullanıcı zaten oy vermişse, mevcut satırı güncelle
        val existingLine = lines[existingLineIndex]
        val updatedLine = existingLine.replaceAfter("rating=", "rating=$rating")  // Oyu güncelle
        lines[existingLineIndex] = updatedLine
    } else {
        // Eğer kullanıcı henüz oy vermemişse, yeni bir satır ekle
        val newLine = "email=$email;rating=$rating"
        lines.add(newLine)
    }

    // Güncellenmiş satırları dosyaya yaz
    file.writeText(lines.joinToString("\n"))
}

fun deleteRatingByEmail(context: Context, email: String) {
    val file = File(context.filesDir, "rating.txt")
    if (!file.exists()) return  // Dosya yoksa hiçbir şey yapma

    // Dosyadaki tüm satırları oku
    val lines = file.readLines().toMutableList()

    // Email'e sahip olan satırı filtrele ve sil
    val updatedLines = lines.filterNot { it.contains("email=$email") }

    // Güncellenmiş satırları dosyaya yaz
    file.writeText(updatedLines.joinToString("\n"))
}

fun saveFoodToFile(context: Context, email: String, foodItem: FoodItemKeepNote) {
    val file = File(context.filesDir, "foodlist.txt")

    // Dosya yoksa oluştur
    if (!file.exists()) {
        file.createNewFile()
    }

    // Dosyadaki tüm satırları oku
    val lines = file.readLines().toMutableList()

    var userFound = false
    var userLineIndex = -1

    // Kullanıcıyı bulalım ve yiyecek ekleyelim
    for (i in lines.indices) {
        if (lines[i].startsWith("$email:")) {
            userFound = true
            userLineIndex = i
            break
        }
    }

    if (userFound) {
        // Kullanıcı varsa, yiyeceği ekle
        val currentFoodList = lines[userLineIndex].substringAfter(":")
        if (currentFoodList.isNotEmpty()) {
            // Eğer mevcut yiyecek listesi varsa, yeni yiyeceği ekle
            val updatedFoodList = "$currentFoodList,${foodItem.name}|${foodItem.amount}|${foodItem.time}"
            lines[userLineIndex] = "$email:$updatedFoodList"
        } else {
            // Eğer mevcut yiyecek listesi boşsa, virgül eklememek için doğrudan ekle
            lines[userLineIndex] = "$email:${foodItem.name}|${foodItem.amount}|${foodItem.time}"
        }
    } else {
        // Eğer kullanıcı bulunmazsa, yeni kullanıcı ekleyelim
        lines.add("$email:${foodItem.name}|${foodItem.amount}|${foodItem.time}")
    }

    // Dosyayı tekrar yaz
    file.writeText(lines.joinToString("\n"))
}

fun loadFoodListFromFile(context: Context, email: String): MutableList<FoodItemKeepNote> {
    val file = File(context.filesDir, "foodlist.txt")
    val foodList = mutableListOf<FoodItemKeepNote>()

    if (!file.exists()) return foodList

    val lines = file.readLines()

    // Kullanıcıyı bulalım ve yiyeceklerini alalım
    for (line in lines) {
        if (line.startsWith("$email:")) {
            val foodData = line.substringAfter(":")
            val foodItems = foodData.split(",") // Virgülle ayrılmış yiyecekler

            for (food in foodItems) {
                val parts = food.split("|")
                if (parts.size == 3) {
                    foodList.add(FoodItemKeepNote(parts[0], parts[1], parts[2]))
                }
            }
            break
        }
    }

    return foodList
}

fun deleteFoodFromFile(context: Context, email: String, foodItem: FoodItemKeepNote) {
    val file = File(context.filesDir, "foodlist.txt")

    if (!file.exists()) {
        Log.e("DeleteFoodError", "foodlist.txt bulunamadı.")
        return
    }

    val lines = file.readLines().toMutableList()
    var userLineIndex = -1

    // Kullanıcıyı bulalım
    for (i in lines.indices) {
        if (lines[i].startsWith("$email:")) {
            userLineIndex = i
            break
        }
    }

    if (userLineIndex != -1) {
        // Yiyecekleri ayıralım
        val currentFoodList = lines[userLineIndex].substringAfter(":")
        val foodItems = currentFoodList.split(",").toMutableList()

        // Silinecek yiyeceği bulalım ve kaldıralım
        foodItems.removeIf { it.startsWith(foodItem.name) }

        // Güncellenmiş yiyecekleri dosyaya yazalım
        lines[userLineIndex] = "$email:${foodItems.joinToString(",")}"
        file.writeText(lines.joinToString("\n"))
    } else {
        Log.e("DeleteFoodError", "Kullanıcı bulunamadı.")
    }
}

fun clearFoodListFromFile(context: Context, email: String) {
    val file = File(context.filesDir, "foodlist.txt")

    if (!file.exists()) return

    val lines = file.readLines().toMutableList()
    var userLineIndex = -1

    // Kullanıcıyı bulalım
    for (i in lines.indices) {
        if (lines[i].startsWith("$email:")) {
            userLineIndex = i
            break
        }
    }

    if (userLineIndex != -1) {
        // Yiyecek listesi silinmişse, sadece e-posta kısmını bırak
        val currentFoodList = lines[userLineIndex].substringAfter(":")
        if (currentFoodList.isNotEmpty()) {
            lines[userLineIndex] = "$email:"  // Yiyecekleri tamamen temizle
        } else {
            // Eğer zaten boşsa, değiştirmeye gerek yok
            lines[userLineIndex] = "$email:"
        }
        file.writeText(lines.joinToString("\n"))
    }
}

fun deleteFoodByEmail(context: Context, email: String) {
    val foodFile = File(context.filesDir, "foodlist.txt")

    if (!foodFile.exists()) {
        Log.e("DeleteFoodError", "foodlist.txt bulunamadı.")
        return
    }

    val lines = foodFile.readLines().toMutableList()

    // E-posta adresine sahip kullanıcıyı bul
    val updatedLines = lines.filterNot { it.startsWith("$email:") }

    // Güncellenmiş satırları dosyaya yaz
    foodFile.writeText(updatedLines.joinToString("\n"))
}


fun readRecipesFromAssets(context: Context): List<Recipe> {
    val recipeList = mutableListOf<Recipe>()
    val inputStream = context.assets.open("recipes.txt")
    val lines = inputStream.bufferedReader().readLines()

    var currentName: String? = null
    val currentIngredients = mutableListOf<String>()
    var currentInstructions = StringBuilder()
    var readingIngredients = false
    var readingInstructions = false

    fun saveCurrentRecipe() {
        if (currentName != null) {
            recipeList.add(
                Recipe(
                    name = currentName!!,
                    ingredients = currentIngredients.toList(),
                    instructions = currentInstructions.toString().trim()
                )
            )
        }
        currentName = null
        currentIngredients.clear()
        currentInstructions = StringBuilder()
        readingIngredients = false
        readingInstructions = false
    }

    for (line in lines) {
        when {
            line.startsWith("[") && line.endsWith("]") -> {
                saveCurrentRecipe()
                currentName = line.removeSurrounding("[", "]")
            }

            line.startsWith("Ingredients:", true) -> {
                readingIngredients = true
                readingInstructions = false
            }

            line.startsWith("Instructions:", true) -> {
                readingIngredients = false
                readingInstructions = true
            }

            line.isBlank() -> continue

            readingIngredients -> currentIngredients.add(line.trimStart('-').trim())
            readingInstructions -> currentInstructions.appendLine(line.trim())
        }
    }

    saveCurrentRecipe() // Son tarifi ekle

    return recipeList
}

data class Recipe(
    val name: String,
    val ingredients: List<String>,
    val instructions: String
)


fun hasUserLikedRecipe(context: Context, email: String, recipeName: String): Boolean {
    val file = File(context.filesDir, "likes.txt")
    if (!file.exists()) {
        // Eğer dosya yoksa, yeni bir dosya oluştur
        createLikesFileIfNotExists(context)
        return false  // Başlangıçta, kullanıcı beğeni yapmamış kabul edelim.
    }

    try {
        val likesContent = file.readText() // Dosyayı oku
        // Beğeni kontrolü yapma işlemleri
        return likesContent.contains("email=$email;recipe=$recipeName")
    } catch (e: IOException) {
        // Dosya okunamıyorsa, hatayı yakalayın
        e.printStackTrace()
        return false
    }
}

fun toggleLike(context: Context, userEmail: String, recipeName: String) {
    val file = File(context.filesDir, "likes.txt")
    var content = file.readText()

    // Kullanıcının beğenip beğenmediğini kontrol et
    if (hasUserLikedRecipe(context, userEmail, recipeName)) {
        // Beğeni geri alındığında, o tarifi dosyadan kaldır
        content = content.replace("email=$userEmail;recipe=$recipeName\n", "")
    } else {
        // Beğeni eklendiğinde, dosyaya yaz
        content += "email=$userEmail;recipe=$recipeName\n"
    }

    // Güncellenmiş içeriği dosyaya yaz
    file.writeText(content)
}

fun getLikeCountForRecipe(context: Context, recipeName: String): Int {
    val file = File(context.filesDir, "likes.txt")
    val content = file.readText()

    // Tarife ait olan satırların sayısını döndürür
    return content.lines().count { it.contains("recipe=$recipeName") }
}

fun createLikesFileIfNotExists(context: Context) {
    val file = File(context.filesDir, "likes.txt")
    if (!file.exists()) {
        try {
            file.createNewFile()  // Eğer dosya yoksa, oluştur
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}