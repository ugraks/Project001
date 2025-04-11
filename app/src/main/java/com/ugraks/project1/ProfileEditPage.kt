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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
            .background(Color.White) // Arka plan beyaz
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
                    tint = Color.Magenta
                )
            }
        }

        // Başlık
        Text(
            "Edit Profile",
            style = MaterialTheme.typography.titleMedium.copy(
                color = Color.Magenta,
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
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        // Telefon numarası input
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it }, // Bu kısımda yazı yazılabilir
            label = { Text("Phone") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        // İş yeri adı input
        OutlinedTextField(
            value = businessName,
            onValueChange = { businessName = it }, // Bu kısımda yazı yazılabilir
            label = { Text("Business Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        // Şifre input
        OutlinedTextField(
            value = password,
            onValueChange = { password = it }, // Bu kısımda yazı yazılabilir
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
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
                .padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            Text("Update Profile", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}