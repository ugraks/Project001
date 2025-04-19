package com.ugraks.project1

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.ugraks.project1.Authenticate.readRecipesFromAssets // Tarif verilerini okuyan fonksiyonunuz
// getRecipeImageResource fonksiyonunu import ettiğinizden emin olun
// Eğer getRecipeImageResource fonksiyonu ImageMapping.kt dosyasında ise, import satırı şu şekilde olabilir:
// import com.ugraks.project1.utils.getRecipeImageResource


@Composable
fun RecipeDetailScreen(recipeName: String, navController: NavController) {
    val context = LocalContext.current
    // readRecipesFromAssets fonksiyonu Recipe nesnelerine imageResId eklemek zorunda değildir artık
    val allRecipes = remember { readRecipesFromAssets(context) }
    // Tarif nesnesini sadece detaylarını (malzemeler, talimatlar) göstermek için adına göre buluyoruz
    val recipe = allRecipes.find { it.name == recipeName }

    val colorScheme = MaterialTheme.colorScheme
    val titleFontFamily = FontFamily.Serif
    val contentFontFamily = FontFamily.SansSerif

    // --- GÖRSEL SEÇİMİ İÇİN YAPILAN DEĞİŞİKLİK BURADA ---
    // Gösterilecek görselin resource ID'sini doğrudan gelen recipeName'e göre,
    // getRecipeImageResource fonksiyonunu kullanarak belirliyoruz.
    val imageResource = getRecipeImageResource(recipeName = recipeName)
    // Artık recipe nesnesinin içinde saklanan bir görsel ID'sine ihtiyacımız yok.
    // --- DEĞİŞİKLİK SONU ---

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Geri butonu
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 10.dp, top = 10.dp)
                .zIndex(1f) // Üstte görünmesini sağlar
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Ok yönü dil/layout yönüne göre otomatik ayarlanır
                contentDescription = "Go Back",
                tint = colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }

        // Card yapısı + içeriği (Scroll edilebilir Column içinde)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), // İçeriğin kaydırılabilir olmasını sağlar
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp)) // Görsel ve üst boşluk için yer

            Box(
                contentAlignment = Alignment.TopCenter, // Görseli Card'ın üst orta kısmına yerleştirir
                modifier = Modifier
                    .fillMaxWidth() // Box'ın genişliği Card ile aynı olsun
                    .padding(horizontal = 16.dp) // Yatayda boşluk bırak
            ) {
                // Ana İçerik Kartı
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant), // Kart rengi
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp) // Görselin Kartın üzerine binmesi için üstten boşluk bırak
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp) // Kartın iç padding'i
                    ) {
                        Spacer(modifier = Modifier.height(60.dp)) // Görselin altında da boşluk bırak

                        // Yemek ismi (Ortalanmış)
                        Text(
                            text = recipe?.name ?: "Tarif Bulunamadı", // Eğer recipe null ise bu metin gösterilir
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = titleFontFamily,
                            color = colorScheme.primary,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally) // Yatayda ortala
                                .padding(bottom = 16.dp), // Altına boşluk
                            maxLines = 2, // En fazla 2 satır
                            overflow = TextOverflow.Ellipsis // Taşarsa üç nokta göster
                        )

                        // Malzemeler Başlığı
                        Text(
                            text = "Malzemeler:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = colorScheme.primary,
                            fontFamily = titleFontFamily,
                            modifier = Modifier.padding(top = 8.dp) // Üstüne boşluk
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Malzemeler Listesi (Her bir malzeme için ayrı Text)
                        // Eğer recipe null ise veya ingredients listesi boşsa hiçbir şey gösterilmez
                        recipe?.ingredients?.forEach { ingredient ->
                            Text(
                                text = "• $ingredient", // Madde işareti ekle
                                fontSize = 16.sp,
                                fontFamily = contentFontFamily,
                                color = colorScheme.onSurface,
                                modifier = Modifier.padding(start = 8.dp, top = 2.dp), // Sol ve üst boşluk
                                lineHeight = 22.sp // Satır aralığı
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp)) // Malzemeler ve Talimatlar arasına boşluk

                        // Talimatlar Başlığı
                        Text(
                            text = "Talimatlar:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = colorScheme.primary,
                            fontFamily = titleFontFamily
                        )

                        // Talimatlar Metni
                        // Eğer recipe null ise veya instructions boşsa bu metin gösterilir
                        Text(
                            text = recipe?.instructions ?: "Talimat bulunamadı.",
                            fontSize = 16.sp,
                            fontFamily = contentFontFamily,
                            color = colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp), // Üstüne boşluk
                            lineHeight = 24.sp // Satır aralığı
                        )

                        // Buraya yorumlar, besin değerleri gibi ek bilgiler eklenebilir
                    }
                }

                // Resim (Card'ın üzerine biner şekilde)
                Image(
                    // --- GÖRSEL SEÇİMİ İÇİN YAPILAN DEĞİŞİKLİK BURADA ---
                    // getRecipeImageResource fonksiyonundan gelen görsel ID'sini kullan
                    painter = painterResource(id = imageResource),
                    // --- DEĞİŞİKLİK SONU ---
                    contentDescription = recipe?.name ?: "Tarif Görseli", // Erişilebilirlik için görsel açıklaması
                    modifier = Modifier
                        .size(120.dp) // Görsel boyutu
                        .clip(RoundedCornerShape(16.dp)) // Köşeleri yuvarla
                        .border(2.dp, colorScheme.primary, RoundedCornerShape(16.dp)) // Kenarlık ekle
                        .shadow(4.dp, RoundedCornerShape(16.dp)), // Hafif gölge ekle
                    contentScale = ContentScale.Crop // Görseli boyutuna sığdırmak için kırp
                )
            }

            Spacer(modifier = Modifier.height(24.dp)) // En alt boşluk
        }
    }

    // Geri tuşuna basıldığında önceki ekrana dönme
    BackHandler {
        navController.popBackStack()
    }
}

fun getRecipeImageResource(recipeName: String): Int {
    return when (recipeName) {
        "Spaghetti Carbonara" -> R.drawable.spagetti_carbonara // Kendi drawable isimlerinizle değiştirin
        //"Tomato Soup" -> R.drawable.tomato_soup // Kendi drawable isimlerinizle değiştirin
        //"Chicken Curry" -> R.drawable.chicken_curry // Kendi drawable isimlerinizle değiştirin
        //"Turkish Lentil Soup" -> R.drawable.turkish_lentil_soup // Örnek bir Türk yemeği görseli

        else -> R.drawable.baseline_restaurant_24 // Varsayılan bir görsel (drawable klasörünüzde olmalı)
    }
}