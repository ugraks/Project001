package com.ugraks.project1.Boxing

import android.widget.Toast
import androidx.compose.foundation.BorderStroke // Border için hala importta dursun, gerekirse kullanılır
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ugraks.project1.AppNavigation.Screens
import com.ugraks.project1.ui.viewmodels.BoxingViewModel

@Composable
fun BoxingMainScreen(
    navController: NavController,
    viewModel: BoxingViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // ViewModel'dan boks kategorilerini StateFlow olarak izle
    val boxingCategories by viewModel.boxingCategories.collectAsState()

    // Çoklu seçim için kullanılacak state
    val selectedBoxingCategories = remember { mutableStateListOf<String>() }

    // Kartlar için seçili olmayan durumdaki şeffaf gri renk tonu
    val unselectedCardColor = Color.Gray.copy(alpha = 0.2f) // Şeffaf gri tonu

    // Kartlar için seçili durumdaki renk temanın ana rengi olacak
    val selectedCardColor = MaterialTheme.colorScheme.primary


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
                .padding(horizontal = 24.dp) // Yatay padding ile içeriği sınırlıyoruz
                .align(Alignment.TopCenter) // Column'u üstte ortala
                // İçeriğin kendisini de yatayda ortalamak için
                .wrapContentSize(Alignment.TopCenter), // İçeriği top-center'a sar
            horizontalAlignment = Alignment.CenterHorizontally // İçindeki satırları ve diğer öğeleri yatayda ortala
        ) {
            Spacer(modifier = Modifier.height(120.dp)) // Başlıkla içerik arasına boşluk

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
                    .fillMaxWidth() // Başlığın genişliği doldurmasını sağla
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center // Başlık metnini ortala
            )

            // Manuel Grid Benzeri Layout (Column ve Row kullanarak)
            val totalCategories = boxingCategories.size
            val rowCount = (totalCategories + 2) / 3

            // Her satır için döngü
            for (i in 0 until rowCount) {
                val startIndex = i * 3
                val endIndex = minOf(startIndex + 3, totalCategories)

                Row(
                    modifier = Modifier
                        .fillMaxWidth() // Satır genişliği doldursun
                        .padding(vertical = 4.dp), // Satırlar arasına dikey boşluk
                    horizontalArrangement = Arrangement.spacedBy(8.dp), // Kartlar arasına yatay boşluk
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bu satırdaki her kategori için döngü
                    for (j in startIndex until endIndex) {
                        val category = boxingCategories[j] // Kategori objesini al
                        val isSelected = selectedBoxingCategories.contains(category) // Seçili mi?

                        Card(
                            // Modifier sırası: ağırlık (layout için), oran (şekil için), sonra tıklama
                            modifier = Modifier
                                .weight(1f) // Row içinde eşit ağırlık/yer kapla
                                .aspectRatio(1f) // Kare şeklini koru (1:1 oran)
                                .clickable { // Tıklama özelliği
                                    // Tıklanınca seçili durumunu değiştir
                                    if (isSelected) {
                                        selectedBoxingCategories.remove(category)
                                    } else {
                                        selectedBoxingCategories.add(category)
                                    }
                                },
                            shape = RoundedCornerShape(12.dp), // Yuvarlak köşeler
                            colors = CardDefaults.cardColors(
                                // Seçili ise temanın ana rengi, değilse şeffaf gri
                                containerColor = if (isSelected) selectedCardColor else unselectedCardColor
                            ),
                            // Seçili border'ı kaldırdık, renk değişimi yeterli gösterge
                            border = null
                        ) {
                            Box( // Kartın içeriğini ortala
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category, // Kategori adını göster
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        // Seçili ise temanın onPrimary rengi (genellikle beyaz/siyah), değilse onSurface
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = FontFamily.SansSerif
                                    ),
                                    textAlign = TextAlign.Center // Metni ortala
                                    // maxLines = 1, overflow = TextOverflow.Ellipsis // İhtiyaca göre eklenebilir
                                )
                            }
                        }
                    }

                    // Son satırda 3'ten az kart varsa, boşluk ekleyerek düzeni koru
                    val itemsInRow = endIndex - startIndex
                    if (itemsInRow < 3) {
                        repeat(3 - itemsInRow) {
                            Spacer(modifier = Modifier.weight(1f)) // Eksik kartlar yerine boşluk ekle
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp)) // Kartlar ve buton arasına boşluk

            // "Show Button"
            Button(
                onClick = {
                    if (selectedBoxingCategories.isNotEmpty()) {
                        // Seçili kategorileri alıp virgülle birleştirerek navigasyon yap
                        val route = Screens.BoxingDetailListScreen.createRoute(
                            selectedBoxingCategories.joinToString(",") // Virgülle ayrılmış string olarak gönder
                        )
                        navController.navigate(route)
                    } else {
                        // Hiçbir şey seçilmediyse Toast mesajı göster
                        Toast.makeText(
                            context,
                            "Please select at least one boxing category",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Show Selected Boxing Items", // Buton metni
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontFamily = FontFamily.SansSerif
                    )
                )
            }
        }
    }
}