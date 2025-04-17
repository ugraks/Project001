package com.ugraks.project1

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.ugraks.project1.Authenticate.getUserDataByEmail
import com.ugraks.project1.Authenticate.updateUserData

@Composable
fun ProfileEditPage(
    navController: NavHostController,
    email: String
) {
    val context = LocalContext.current
    var isNewPasswordVisibleProfileEdit by remember { mutableStateOf(false) }

    // Bu state'leri kullanalım
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Kullanıcı verilerini alalım
    val userData = getUserDataByEmail(context, email)

    // Kullanıcı verilerini aldıktan sonra UI'yi güncellemek için LaunchedEffect kullanıyoruz
    LaunchedEffect(userData) {
        userData?.let {
            // Kullanıcı verilerini state'lere atıyoruz
            username = it.username
            phone = it.phone
            businessName = it.businessName
        }
    }

    // Bu Column, formu gösterecek
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background) // Tema uyumu
    ) {
        // Geri butonu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(30.dp),
                    tint = MaterialTheme.colorScheme.primary // Tema uyumu
                )
            }
        }

        // Başlık
        Text(
            "Edit Profile",
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .padding(top = 50.dp)
                .align(Alignment.CenterHorizontally)
        )

        // Kullanıcı adı input
        OutlinedTextField(
            value = username,
            onValueChange = { username = it }, // Bu kısımda yazı yazılabilir
            label = { Text("Username") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp), // Yuvarlak köşeler
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary, // Odaklanınca tema rengini kullanalım
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f) // Düşük opaklıkta kenarlık
            )
        )

        // Telefon numarası input
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        )

        // İş yeri adı input
        OutlinedTextField(
            value = businessName,
            onValueChange = { businessName = it },
            label = { Text("Business Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        )

        // Şifre input
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (isNewPasswordVisibleProfileEdit) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (isNewPasswordVisibleProfileEdit) Icons.Filled.Close else Icons.Filled.Info
                IconButton(onClick = { isNewPasswordVisibleProfileEdit = !isNewPasswordVisibleProfileEdit }) {
                    Icon(
                        imageVector = icon,
                        contentDescription = if (isNewPasswordVisibleProfileEdit) "Hide" else "Show"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        )

        // Güncelleme butonu
        Button(
            onClick = {
                // Profil bilgilerini güncelle
                val updated = updateUserData(
                    context,
                    email,
                    username,
                    phone,
                    businessName,
                    password // Yeni şifreyi de ekliyoruz
                )
                if (updated) {
                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    navController.navigateUp()  // Güncelleme sonrası geri git
                } else {
                    Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(56.dp), // Buton yüksekliğini sabit tutuyoruz
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp), // Buton köşe yuvarlama
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            Text("Update Profile", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}