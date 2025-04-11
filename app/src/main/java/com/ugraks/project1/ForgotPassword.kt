package com.ugraks.project1

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.ugraks.project1.AppNavigation.Screens
import java.io.File

@Composable
fun ForgotPassword(navController: NavController) {

    // State'ler
    var email by remember { mutableStateOf("") }
    var registrationCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var isValidUser by remember { mutableStateOf(false) }
    var showPasswordFields by remember { mutableStateOf(false) }

    val context = LocalContext.current

    fun validateUserCredentials(email: String, registrationCode: String): Boolean {
        val file = File(context.filesDir, "users.txt")
        if (!file.exists()) return false

        val lines = file.readLines()
        for (line in lines) {
            val parts = line.split(";")
            val emailPart = parts.find { it.startsWith("email=") }?.substringAfter("email=")
            val codePart = parts.find { it.startsWith("code=") }?.substringAfter("code=")
            if (emailPart == email && codePart == registrationCode) return true
        }
        return false
    }

    fun updatePassword(email: String, newPassword: String) {
        val file = File(context.filesDir, "users.txt")
        if (!file.exists()) return

        val lines = file.readLines().toMutableList()
        for (i in lines.indices) {
            val parts = lines[i].split(";")
            val emailPart = parts.find { it.startsWith("email=") }?.substringAfter("email=")

            if (emailPart == email) {
                lines[i] = lines[i].replace(Regex("password=[^;]*"), "password=$newPassword")
                break
            }
        }

        file.writeText(lines.joinToString("\n"))
        Toast.makeText(context, "Password successfully updated.", Toast.LENGTH_LONG).show()
        navController.navigate(Screens.ScreenLoginPage)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // Geri butonu
        IconButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 32.dp, start = 16.dp) // Biraz daha aşağıya aldık
                .zIndex(1f)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Go Back",
                tint = Color.Magenta,
                modifier = Modifier.size(30.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 90.dp, start = 20.dp, end = 20.dp), // IconButton'un altına koyduk
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top // Yukarıdan aşağıya sıralama
        ) {
            Text(
                "Please Fill In Your Account Information:",
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp,
                color = Color.Magenta
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (!showPasswordFields) {
                // E-Mail
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-Mail :") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Registration Code
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    value = registrationCode,
                    onValueChange = { registrationCode = it },
                    label = { Text("Registration Code :") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Verify Button
                Button(
                    onClick = {
                        if (validateUserCredentials(email, registrationCode)) {
                            isValidUser = true
                            showPasswordFields = true
                        } else {
                            Toast.makeText(context, "Invalid email or registration code", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta)
                ) {
                    Text("Verify", color = Color.Black)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Yeni şifre alanları
            if (showPasswordFields) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password :") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (newPassword.isNotEmpty()) {
                            updatePassword(email, newPassword)
                        } else {
                            Toast.makeText(context, "Please enter a new password", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta)
                ) {
                    Text("Change Password", color = Color.Black)
                }
            }
        }
    }
}

@Preview
@Composable
fun ForgotPasswordPreview() {
    // ForgotPassword(navController = NavController) // Burada doğru şekilde preview ekleyebilirsiniz
}