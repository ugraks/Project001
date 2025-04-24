package com.ugraks.project1.Boxing // Kendi paket adınız

import android.content.Context // Artık Context parametresi Composable'a gerek YOK
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState // StateFlow'u izlemek için
import androidx.compose.runtime.getValue // State değerini almak için
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Toast mesajı için
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel // ViewModel'ı inject etmek için
import androidx.navigation.NavController
import com.ugraks.project1.AppNavigation.Screens // Navigasyon ekranları
// Asset okuma importları Composable'da gerek YOK
// import com.ugraks.project1.Boxing.BoxingItem
// import com.ugraks.project1.Boxing.loadBoxingDataFromAssets
import com.ugraks.project1.ui.viewmodels.BoxingViewModel // YENİ: BoxingViewModel importu


@Composable
fun BoxingMainScreen(
    navController: NavController,
    // Context parametresi kaldırıldı, ViewModel inject edilecek
    viewModel: BoxingViewModel = hiltViewModel() // YENİ: ViewModel inject et
) {
    val context = LocalContext.current // Toast mesajı için Context

    // Boks öğeleri ve kategoriler artık ViewModel'dan geliyor
    // val boxingItems = loadBoxingDataFromAssets(context) // Bu satır kaldırıldı
    // val boxingCategories = boxingItems.map { it.category }.distinct() // Bu satır kaldırıldı

    // ViewModel'dan boks kategorilerini StateFlow olarak izle
    val boxingCategories by viewModel.boxingCategories.collectAsState() // YENİ: ViewModel'dan al

    // Kullanıcı tarafından seçilen boks kategorilerini Composable state'inde tut
    val selectedBoxingCategories = remember { mutableStateListOf<String>() }


    Box(modifier = Modifier.fillMaxSize()) {

        // 🔙 Geri Butonu
        IconButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 40.dp, start = 20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Go Back",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp)
            )
        }

        // Ana İçerik
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(120.dp)) // başlıkla geri butonu arasında boşluk

            // Başlık
            Text(
                text = "Select Boxing Categories",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 26.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            // Checkbox Listesi (ViewModel'dan gelen boxingCategories listesini kullanır)
            // Eğer boxingCategories boşsa (ViewModel henüz yüklemediyse), boş liste gösterilir
            boxingCategories.forEach { category -> // YENİ: ViewModel'dan gelen listeyi kullan
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedBoxingCategories.contains(category),
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                selectedBoxingCategories.add(category)
                            } else {
                                selectedBoxingCategories.remove(category)
                            }
                            // Seçim değiştiğinde ViewModel'a bildirme artık GEREK YOK
                            // NavArgs ile bilgiyi taşıyoruz
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurface,
                            checkmarkColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category, // Display category name
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = FontFamily.SansSerif
                        )
                    )
                }
            }

            // Show Button
            Button(
                onClick = {
                    if (selectedBoxingCategories.isNotEmpty()) {
                        // Navigasyona giderken seçili kategorileri NavArgs olarak string formatında ilet
                        // ViewModel'a selectedCategories'i set etme artık GEREK YOK
                        val route = Screens.BoxingDetailListScreen.createRoute(
                            selectedBoxingCategories.joinToString(",") // NavArgs olarak string gönder
                        )
                        navController.navigate(route)
                    } else {
                        Toast.makeText(
                            context,
                            "Please select at least one boxing category",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Show Boxing Items",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontFamily = FontFamily.SansSerif
                    )
                )
            }
        }
    }
}

// loadBoxingDataFromAssets fonksiyonu bu dosyadan kaldırıldı, Repository tarafından kullanılacak
/*
fun loadBoxingDataFromAssets(context: Context): List<BoxingItem> {
    // ... fonksiyon içeriği ...
}
*/

// BoxingItem data class'ı bu dosyadan kaldırıldı, kendi dosyasında (Boxing.kt) tanımlı
// data class BoxingItem(...)