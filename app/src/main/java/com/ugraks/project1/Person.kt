package com.ugraks.project1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.ugraks.project1.AppNavigation.Screens
import com.ugraks.project1.AppNavigation.Screens.ScreenProfileEditPage
import com.ugraks.project1.Authenticate.getUserDataByEmail

@Composable
fun PersonPage(
    navController: NavHostController,
    email: String
) {
    val context = LocalContext.current
    val userData = remember(email) { getUserDataByEmail(context, email) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Tema rengi uyumu
            .padding(16.dp)
    ) {
        // Geri Butonu (IconButton)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = {
                    navController.navigateUp() // Geri gitme işlevi
                },
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

        // Profil Görseli
        Icon(
            Icons.Default.Person,
            contentDescription = "Person",
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
                .padding(top = 24.dp),
            tint = MaterialTheme.colorScheme.primary // Tema uyumu
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Kullanıcı Bilgileri
        if (userData != null) {
            // Hoşgeldin Mesajı
            Text(
                text = "Hello, ${userData.username}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface, // Tema uyumu
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Kullanıcı bilgileri
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                InfoRow(label = "Email", value = userData.email)
                InfoRow(label = "Phone", value = userData.phone)
                InfoRow(label = "Business", value = userData.businessName)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Profil Düzenle Butonu
            Button(
                onClick = {
                    navController.navigate(ScreenProfileEditPage(email))
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp) // Sabit buton yüksekliği
                    .clip(RoundedCornerShape(12.dp)) // Yuvarlatılmış köşeler
            ) {
                Text("Edit Profile", color = MaterialTheme.colorScheme.onPrimary)
            }

            // Kullanıcıya ait random kodu gösterme
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = "Your Registration Code: ${userData.code}", // Random kodu burada gösteriyoruz
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            Text(text = "User not found", fontSize = 18.sp, color = Color.Red)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), // Tema uyumu
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface, // Tema uyumu
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}