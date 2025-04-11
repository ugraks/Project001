package com.ugraks.project1.Authenticate


import android.content.Context
import android.util.Log
import android.widget.Toast
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

fun isEmailAlreadyRegistered(context: Context, email: String): Boolean {
    val file = File(context.filesDir, "users.txt")
    if (!file.exists()) return false

    return file.readLines().any { line ->
        val emailPart = line.split(";").find { it.startsWith("email=") }
        emailPart?.substringAfter("email=") == email
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

    // Yeni formatta kullanıcı bilgisi
    val randomCode = generateRandomCode()  // Rastgele kod oluşturuluyor
    val userInfo = "username=$username;businessName=$businessName;phone=$phone;email=$email;password=$password;code=$randomCode"
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