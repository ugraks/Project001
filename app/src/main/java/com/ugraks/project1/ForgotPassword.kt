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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    var email by remember { mutableStateOf("") }
    var registrationCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    var showPasswordFields by remember { mutableStateOf(false) }

    // Göstermek/gizlemek için
    var isRegCodeVisible by remember { mutableStateOf(false) }
    var isNewPasswordVisible by remember { mutableStateOf(false) }

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
            .background(MaterialTheme.colorScheme.background)
    ) {
        IconButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Please Fill In Your Account Information:",
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!showPasswordFields) {
                // E-mail input
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Registration Code with toggle
                OutlinedTextField(
                    value = registrationCode,
                    onValueChange = { registrationCode = it },
                    label = { Text("Registration Code") },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (isRegCodeVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (isRegCodeVisible) Icons.Filled.Close else Icons.Filled.Info
                        IconButton(onClick = { isRegCodeVisible = !isRegCodeVisible }) {
                            Icon(
                                imageVector = icon,
                                contentDescription = if (isRegCodeVisible) "Hide" else "Show"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (validateUserCredentials(email, registrationCode)) {
                            showPasswordFields = true
                        } else {
                            Toast.makeText(context, "Invalid email or registration code", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Verify", color = MaterialTheme.colorScheme.onPrimary)
                }

                Spacer(modifier = Modifier.height(20.dp))
            } else {
                // New Password with toggle
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (isNewPasswordVisible) Icons.Filled.Close else Icons.Filled.Info
                        IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                            Icon(
                                imageVector = icon,
                                contentDescription = if (isNewPasswordVisible) "Hide" else "Show"
                            )
                        }
                    },
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Change Password", color = MaterialTheme.colorScheme.onPrimary)
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