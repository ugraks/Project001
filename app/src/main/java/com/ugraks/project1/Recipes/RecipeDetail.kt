package com.ugraks.project1.Recipes // Kendi paket adınız

import androidx.activity.compose.BackHandler // Geri tuşu yakalama
import androidx.compose.foundation.Image // Resim gösterme
import androidx.compose.foundation.background // Arka plan rengi
import androidx.compose.foundation.border // Kenarlık
import androidx.compose.foundation.layout.* // Layout bileşenleri (Column, Row, Box vb.)
import androidx.compose.foundation.rememberScrollState // Kaydırma state'i
import androidx.compose.foundation.shape.RoundedCornerShape // Köşe yuvarlama şekli
import androidx.compose.foundation.verticalScroll // Dikey kaydırma
import androidx.compose.material.icons.Icons // İkon kütüphanesi
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Geri oku ikonu
import androidx.compose.material3.* // Material 3 bileşenleri (Card, Text, Icon, IconButton vb.)
import androidx.compose.runtime.* // remember, getValue, collectAsState, LaunchedEffect
import androidx.compose.ui.Alignment // Hizalama
import androidx.compose.ui.Modifier // Modifier
import androidx.compose.ui.draw.clip // Kırpma (şekle göre)
import androidx.compose.ui.draw.shadow // Gölge
import androidx.compose.ui.layout.ContentScale // Resim ölçeklendirme
import androidx.compose.ui.platform.LocalContext // Context alma
import androidx.compose.ui.res.painterResource // Drawable kaynaktan resim yükleme
import androidx.compose.ui.text.font.FontFamily // Yazı tipi ailesi
import androidx.compose.ui.text.font.FontWeight // Yazı tipi kalınlığı
import androidx.compose.ui.text.style.TextOverflow // Metin taşması
import androidx.compose.ui.unit.dp // dp birimi
import androidx.compose.ui.unit.sp // sp birimi
import androidx.compose.ui.zIndex // Z-index (üstte görünme)
import androidx.hilt.navigation.compose.hiltViewModel // ViewModel için import
import androidx.navigation.NavController // Navigasyon kontrolcüsü
import com.ugraks.project1.R // Drawable kaynaklarına erişim için R sınıfı
import com.ugraks.project1.data.local.entity.RecipeEntity // YENİ Room Entity importu
import com.ugraks.project1.ui.viewmodel.RecipeViewModel
// Eğer getRecipeImageResource başka bir dosyada ise import edin
// import com.ugraks.project1.utils.getRecipeImageResource // Kendi utils paketiniz importu


@Composable
fun RecipeDetailScreen(
    recipeName: String, // Navigasyondan gelen tarif adı parametresi
    navController: NavController, // Navigasyon kontrolcüsü parametresi
    viewModel: RecipeViewModel = hiltViewModel() // YENİ: RecipeViewModel'ı Hilt ile inject et
) {
    val context = LocalContext.current // Gerekirse

    // Tarif detayını ViewModel'dan Room'dan reaktif olarak al
    // recipeName parametresi değiştiğinde (navigasyonla yeni tarif detayına gidilince)
    // remember(recipeName) bloku yeniden çalışır ve yeni tarif için Flow toplanır.
    val recipeEntity by remember(recipeName) { // recipeName değiştiğinde Flow yeniden toplanır
        viewModel.getRecipeDetail(recipeName) // ViewModel'dan belirli tarifi getiren metodu çağır
    }.collectAsState(initial = null) // Flow'u state olarak topla, başlangıç değeri null (veri yüklenene kadar)


    val colorScheme = MaterialTheme.colorScheme // Tema renk şeması
    val titleFontFamily = FontFamily.Serif // Başlıklar için yazı tipi
    val contentFontFamily = FontFamily.SansSerif // İçerik için yazı tipi

    // Görsel kaynağı hala tarif adına göre belirleniyor (aynı kalır, Room'a taşınmaz)
    val imageResource = getRecipeImageResource(recipeName = recipeName)


    Box( // Ana Box, tüm ekranı kaplar
        modifier = Modifier
            .fillMaxSize() // Tam ekran
            .background(colorScheme.background) // Arka plan rengi
            .statusBarsPadding() // Durum çubuğu boşluğu
            .navigationBarsPadding() // Navigasyon çubuğu boşluğu
    ) {
        // Geri butonu - Üstte ve solda sabit durur
        IconButton(
            onClick = { navController.popBackStack() }, // Geri gitme aksiyonu
            modifier = Modifier
                .align(Alignment.TopStart) // Üst sola hizala
                .padding(start = 10.dp, top = 10.dp) // Padding
                .zIndex(1f) // Z-index ile diğer bileşenlerin üstünde görünmesini sağla
        ) {
            Icon( // Material 3 Icon kullanın
                imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Otomatik yönlü geri oku
                contentDescription = "Go Back", // Erişilebilirlik
                tint = colorScheme.primary, // Renk
                modifier = Modifier.size(28.dp) // Boyut
            )
        }

        // Ana İçerik - Kaydırılabilir Column içinde
        Column(
            modifier = Modifier
                .fillMaxSize() // Tam boyut
                .verticalScroll(rememberScrollState()), // Dikey kaydırmayı etkinleştir
            horizontalAlignment = Alignment.CenterHorizontally // İçindeki öğeleri yatayda ortala
        ) {
            Spacer(modifier = Modifier.height(80.dp)) // Görsel ve üst boşluk için yer bırak

            // Card ve Görselin bulunduğu Box
            Box(
                contentAlignment = Alignment.TopCenter, // İçeriği (Görseli) üst ortaya hizala
                modifier = Modifier
                    .fillMaxWidth() // Box'ın genişliği tam
                    .padding(horizontal = 16.dp) // Yatayda padding
            ) {
                // Ana İçerik Kartı
                Card(
                    shape = RoundedCornerShape(16.dp), // Köşe yuvarlama
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant), // Kart rengi
                    modifier = Modifier
                        .fillMaxWidth() // Tam genişlik
                        .padding(top = 60.dp) // Görselin kartın üzerine binmesi için üstten boşluk
                ) {
                    Column( // Kartın içindeki Column
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp) // Kartın iç padding'i
                    ) {
                        Spacer(modifier = Modifier.height(60.dp)) // Görselin altında da boşluk bırak

                        // Yemek ismi (Ortalanmış) - recipe?.name yerine recipeEntity?.name kullanın
                        Text(
                            text = recipeEntity?.name ?: "Tarif Yükleniyor/Bulunamadı", // YENİ: recipeEntity?.name kullanılır (null ise placeholder)
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

                        // Malzemeler Başlığı aynı kalır
                        Text(
                            text = "Ingredients",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = colorScheme.primary,
                            fontFamily = titleFontFamily,
                            modifier = Modifier.padding(top = 8.dp) // Üstüne boşluk
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Malzemeler Listesi (Her bir malzeme için ayrı Text)
                        // YENİ DÜZELTME: Delegated property hatasını çözmek için malzemeler listesini güvenli kontrol et
                        // recipeEntity null ise ingredientsList de null olur
                        val ingredientsList = recipeEntity?.ingredients // Güvenli çağrı ile malzemeler listesini al (List<String>?)

                        if (ingredientsList.isNullOrEmpty()) { // isNullOrEmpty() hem null listeyi hem de boş listeyi kontrol eder
                            Text(
                                text = "No ingredient information found.",
                                fontSize = 16.sp,
                                fontFamily = contentFontFamily,
                                color = colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                            )
                        } else {
                            // Liste null değilse ve boş değilse, döngüyü çalıştırın
                            ingredientsList.forEach { ingredient -> // ingredientsList artık List<String>
                                Text(
                                    text = "• $ingredient", // Madde işareti ekle
                                    fontSize = 16.sp,
                                    fontFamily = contentFontFamily,
                                    color = colorScheme.onSurface,
                                    modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                                    lineHeight = 22.sp
                                )
                            }
                        }


                        Spacer(modifier = Modifier.height(24.dp)) // Malzemeler ve Talimatlar arasına boşluk

                        // Talimatlar Başlığı aynı kalır
                        Text(
                            text = "Instructions",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = colorScheme.primary,
                            fontFamily = titleFontFamily
                        )

                        // Talimatlar Metni - recipe?.instructions yerine recipeEntity?.instructions kullanın
                        Text(
                            text = recipeEntity?.instructions ?: "No instruction information found.", // YENİ: recipeEntity?.instructions kullanılır (güvenli çağrı)
                            fontSize = 16.sp,
                            fontFamily = contentFontFamily,
                            color = colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp),
                            lineHeight = 24.sp
                        )

                        // Buraya yorumlar, besin değerleri gibi ek bilgiler eklenebilir
                    } // İç Column sonu
                } // Card sonu

                // Resim (Card'ın üzerine biner şekilde) - aynı kalır
                Image(
                    // getRecipeImageResource fonksiyonundan gelen görsel ID'sini kullan
                    painter = painterResource(id = imageResource),
                    contentDescription = recipeEntity?.name ?: "Tarif Görseli", // recipeEntity?.name kullanılır (Erişilebilirlik için güvenli çağrı)
                    modifier = Modifier
                        .size(120.dp) // Boyut
                        .clip(RoundedCornerShape(16.dp)) // Köşe yuvarlama
                        .border(2.dp, colorScheme.primary, RoundedCornerShape(16.dp)) // Kenarlık
                        .shadow(4.dp, RoundedCornerShape(16.dp)), // Gölge
                    contentScale = ContentScale.Crop // Ölçeklendirme
                )
            } // Box sonu

            Spacer(modifier = Modifier.height(24.dp)) // En alt boşluk
        } // Ana Column sonu

        // Eğer recipeEntity hala null ise (yükleniyor veya bulunamadı) Yükleniyor göstergesi gösterebilirsiniz
        // recipeName boş gelirse (navigasyon hatası?) veya gerçekten bulunamazsa recipeEntity null kalabilir.
        if (recipeEntity == null && recipeName.isNotEmpty()) { // recipeName boş değilse ve recipeEntity null ise yükleniyor/bulunamadı demektir
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colorScheme.primary) // Yükleniyor göstergesi
            }
        }
    } // Ana Box sonu

    // Geri tuşuna basıldığında önceki ekrana dönme aynı kalır
    BackHandler {
        navController.popBackStack()
    }
}