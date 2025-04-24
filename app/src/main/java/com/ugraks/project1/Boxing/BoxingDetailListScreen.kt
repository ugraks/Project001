package com.ugraks.project1.Boxing // Kendi paket adınız

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState // StateFlow'u izlemek için
import androidx.compose.runtime.getValue // State değerini almak için
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
// Boks öğesi data class'ı yerine Entity kullanılacak
// import com.ugraks.project1.Boxing.BoxingItem
// import com.ugraks.project1.Boxing.loadBoxingDataFromAssets // Artık Composable'da kullanılmıyor
import com.ugraks.project1.Boxing.getBoxingImageResource // Image resource fonksiyonu hala burada veya Utils dosyasında olabilir
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel // ViewModel'ı inject etmek için
import androidx.navigation.NavController
import com.ugraks.project1.R // R sınıfını import edin
import com.ugraks.project1.data.local.entity.BoxingItemEntity // YENİ: BoxingItemEntity importu
import com.ugraks.project1.ui.viewmodels.BoxingViewModel // YENİ: BoxingViewModel importu


@Composable
fun BoxingDetailListScreen(
    navController: NavController,
    selectedCategories: List<String>, // Başlık için hala parametre olarak gelebilir (NavArgs'tan)
    // Boks öğeleri listesi artık ViewModel'dan gelecek, parametre kaldırıldı
    // allBoxingItems: List<BoxingItem> // Bu parametre kaldırıldı
    viewModel: BoxingViewModel = hiltViewModel() // YENİ: ViewModel inject et
) {
    // Boks öğeleri listesi artık ViewModel'dan geliyor ve ViewModel'da filtreleniyor
    // val filteredBoxingItems = allBoxingItems.filter { it.category in selectedCategories } // Bu satır kaldırıldı

    // ViewModel'dan filtrelenmiş boks öğeleri listesini StateFlow olarak izle
    val filteredBoxingItems by viewModel.filteredBoxingItems.collectAsState() // YENİ: ViewModel'dan al

    // Genişletilmiş öğe state'i (UI state'i olarak kalır, tipi BoxingItemEntity olacak)
    val expandedItem = remember { mutableStateOf<BoxingItemEntity?>(null) } // YENİ: Tipi BoxingItemEntity?


    Box(modifier = Modifier.fillMaxSize()) {

        // 🔙 Back Button
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 90.dp) // Space for back button and title
        ) {

            // 🏷 Title (Parametreden gelen kategorileri kullanır)
            Text(
                text = "${selectedCategories.joinToString(", ")}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 26.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            // 📋 Boxing Item Cards (ViewModel'dan gelen filteredBoxingItems listesini kullanır)
            // Liste boşsa (ViewModel henüz yüklemediyse veya filtre sonucu boşsa) boş liste gösterilir
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // itemsIndexed ViewModel'dan gelen filteredBoxingItems listesini kullanır (List<BoxingItemEntity>)
                itemsIndexed(filteredBoxingItems, key = { _, item -> item.name }) { _, item -> // item artık BoxingItemEntity tipinde
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .clickable {
                                expandedItem.value = if (expandedItem.value == item) null else item
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Boxing Item Name (item.name - BoxingItemEntity'de mevcut)
                            Text(
                                text = item.name, // YENİ: BoxingItemEntity'den adı al
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .align(Alignment.CenterHorizontally)
                            )

                            // Image or Icon (item.name - BoxingItemEntity'de mevcut)
                            val currentImageResource = if (expandedItem.value == item) {
                                // Kart açıksa, öğeye özel resmi al
                                getBoxingImageResource(item.name) // item.name hala String
                            } else {
                                // Kart kapalıysa, varsayılan ikonunu göster
                                R.drawable.baseline_sports_martial_arts_24 // Sizin varsayılan ikonunuz
                            }

                            Image(
                                painter = painterResource(id = currentImageResource),
                                contentDescription = if (expandedItem.value == item) item.name else "Boxing Icon",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(bottom = 12.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                // contentScale = ContentScale.Crop
                            )

                            // Boxing Item Details (Animated Visibility) (BoxingItemEntity'den alınır)
                            // AnimatedVisibility'nin visible kontrolü hala BoxingItemEntity tipini kullanır
                            AnimatedVisibility(visible = expandedItem.value == item) {
                                Column {
                                    // category (item.category - BoxingItemEntity'de mevcut)
                                    Text(
                                        text = "Category: ${item.category}", // YENİ: BoxingItemEntity'den al
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onBackground
                                        ),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    // description (item.description - BoxingItemEntity'de mevcut)
                                    Text(
                                        text = "Description: ${item.description}", // YENİ: BoxingItemEntity'den al
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onBackground
                                        ),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    // details (item.details - BoxingItemEntity'de mevcut)
                                    Text(
                                        text = "Details:", // Changed label
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    // details, potansiyel olarak ayırıcı ile bölünmüşse
                                    item.details.split(" | ").forEach { step -> // YENİ: BoxingItemEntity'den al, | ayırıcı varsayıldı
                                        Text(
                                            text = step.trim(),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onBackground
                                            ),
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                // Liste boşsa bilgi mesajı göster
                item {
                    if (filteredBoxingItems.isEmpty()) {
                        Box(
                            modifier = Modifier.fillParentMaxSize(), // LazyColumn içinde tam alanı kapla
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No boxing items found for selected categories.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// getBoxingImageResource fonksiyonu bu dosyadan kaldırılmadı, hala burada veya ayrı bir Utils dosyasında olabilir.
// fun getBoxingImageResource(itemName: String): Int { ... }

// BoxingItem data class'ı bu dosyadan kaldırıldı, kendi dosyasında (Boxing.kt) tanımlı
// data class BoxingItem(...)