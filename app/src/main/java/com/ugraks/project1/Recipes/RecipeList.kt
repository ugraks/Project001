package com.ugraks.project1.Recipes // Kendi paket adınız

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed // items yerine itemsIndexed kullanın
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.* // Material 3 bileşenleri
import androidx.compose.runtime.* // remember, mutableStateOf, getValue, setValue, collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel // ViewModel için import
import androidx.navigation.NavController
import com.ugraks.project1.ui.viewmodel.RecipeViewModel



@Composable
fun RecipeListScreen(
    navController: NavController,
    viewModel: RecipeViewModel = hiltViewModel() // YENİ: RecipeViewModel'ı Hilt ile inject et
) {
    val context = LocalContext.current // Gerekirse (örneğin Toast mesajı için)

    // allRecipes listesi ve filtreleme artık ViewModel'dan Room aracılığıyla gelecek.
    // ESKİ: val allRecipes = remember { readRecipesFromAssets(context) } // Bu kaldırıldı
    // ESKİ: var searchText by remember { mutableStateOf("") } // ViewModel'da yönetilecek
    // ESKİ: val filteredRecipes = allRecipes.filter { ... } // ViewModel'da yönetilecek

    // ViewModel'dan arama metnini ve filtrelenmiş tarif adları listesini reaktif olarak izle
    val searchText by viewModel.searchText.collectAsState() // ViewModel'dan gelen arama metni state'i
    // filteredRecipes Flow'u List<String> yayınlıyor
    val filteredRecipeNames by viewModel.filteredRecipes.collectAsState() // ViewModel'dan gelen filtrelenmiş tarif adları (List<String>)


    val colorScheme = MaterialTheme.colorScheme // MaterialTheme renk şeması

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background) // Arka plan rengi
            .padding(horizontal = 20.dp, vertical = 20.dp) // Yatay padding
            .statusBarsPadding() // Durum çubuğu boşluğu
            .navigationBarsPadding() // Navigasyon çubuğu boşluğu
    ) {
        // Geri Butonu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp), // Üst padding
            verticalAlignment = Alignment.CenterVertically // Dikey hizalama
        ) {
            IconButton(onClick = { navController.popBackStack() }) { // Geri tuşu
                Icon( // Material 3 Icon kullanın
                    imageVector = Icons.Default.ArrowBack, // Geri ok ikonu
                    contentDescription = "Go Back", // Erişilebilirlik açıklaması
                    tint = colorScheme.primary, // İkon rengi
                    modifier = Modifier.size(28.dp) // İkon boyutu
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // Boşluk

        // Başlık
        Text(
            text = "Search Recipes",
            fontSize = 28.sp, // Yazı boyutu
            color = colorScheme.primary, // Yazı rengi
            fontWeight = FontWeight.Bold, // Kalın yazı
            fontFamily = FontFamily.Serif, // Yazı tipi
            modifier = Modifier.align(Alignment.CenterHorizontally) // Yatayda ortalama
        )

        Spacer(modifier = Modifier.height(16.dp)) // Boşluk

        // Arama Kutusu - value ve onValueChange ViewModel'ı kullanacak
        OutlinedTextField(
            value = searchText, // YENİ: ViewModel'dan gelen state'i kullan
            onValueChange = { viewModel.updateSearchText(it) }, // YENİ: ViewModel'ın arama metnini güncelleme metodunu çağır
            label = { Text("Search", color = colorScheme.primary) }, // Label metni ve rengi
            modifier = Modifier
                .fillMaxWidth() // Tam genişlik
                .background(colorScheme.background, RoundedCornerShape(12.dp)), // Arka plan ve köşe yuvarlama
            shape = RoundedCornerShape(12.dp), // Alanın şekli
            colors = TextFieldDefaults.colors( // Alanın renkleri
                focusedIndicatorColor = colorScheme.primary, // Odaklanıldığında alt çizgi rengi
                unfocusedIndicatorColor = colorScheme.primary, // Odaklanılmadığında alt çizgi rengi (veya başka renk)
                cursorColor = colorScheme.primary, // İmleç rengi
                focusedContainerColor = colorScheme.background, // Alanın arka plan rengi (odaklanıldığında)
                unfocusedContainerColor = colorScheme.background // Alanın arka plan rengi (odaklanılmadığında)
            )
            // Arama kutusuna temizleme ikonu eklemek isterseniz (isteğe bağlı)
            /*
            trailingIcon = {
                 if (searchText.isNotEmpty()) {
                      IconButton(onClick = { viewModel.updateSearchText("") }) {
                           Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = colorScheme.primary)
                      }
                 }
            }
             */
        )

        Spacer(modifier = Modifier.height(12.dp)) // Boşluk

        // Tarif Listesi - Room'dan gelen filtrelenmiş adlar listesini kullanacak
        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) { // Liste içeriği padding

            // Ana tarif kartları listesi
            // itemsIndexed: Listeyi indeksle birlikte döngüye alır
            itemsIndexed(
                items = filteredRecipeNames, // YENİ: ViewModel'dan gelen filtrelenmiş tarif adları (List<String>)
                key = { index, recipeName -> recipeName } // Her öğe için benzersiz anahtar (performans için önerilir)
            ) { index, recipeName -> // recipeName artık doğrudan String
                Card(
                    modifier = Modifier
                        .fillMaxWidth() // Tam genişlik
                        .padding(vertical = 8.dp) // Dikey boşluk
                        .clip(RoundedCornerShape(12.dp)), // Köşe yuvarlama
                    onClick = {
                        // Navigasyon aynı kalır, detay ekranına tarif adını göndermeye devam edin
                        // recipeName zaten String olduğu için direkt kullanılır
                        navController.navigate("recipeDetail/${recipeName}")
                    },
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant), // Kart rengi
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Gölge
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp) // Kart içi padding
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally // Yatayda ortalama
                    ) {
                        // recipe.name yerine doğrudan recipeName string'ini kullanın
                        Text(
                            text = recipeName, // YENİ: Doğrudan string kullanılır
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Kartlar arasına ek boşluk (isteğe bağlı, padding de yeterli olabilir)
                // if (index != filteredRecipeNames.lastIndex) {
                //     Spacer(modifier = Modifier.height(4.dp))
                // }
            }

            // Liste boşsa veya filtre sonucu yoksa mesaj gösterme
            // item() çağrıları LazyColumn'ın doğrudan scope'u içinde olmalıdır.
            // filteredRecipeNames List<String> olduğu için isEmpty() kontrolü yeterlidir.
            if (filteredRecipeNames.isEmpty()) {
                // Arama yapılıyorsa ve sonuç yoksa VEYA arama boş ve hiç tarif yoksa
                item { // item() çağrısı burada, LazyColumn'ın scope'unda
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        val message = when {
                            searchText.isNotEmpty() -> "No recipes found for \"$searchText\"." // Arama sonucu yoksa
                            // ViewModel'a yükleniyor state'i eklerseniz burayı güncelleyebilirsiniz
                            // !viewModel.isLoading.collectAsState().value -> "No recipes available." // Yüklendi ve boşsa
                            else -> "Loading recipes or no recipes available." // Yükleniyor veya bilinmiyor
                        }
                        Text(
                            text = message,
                            fontSize = 18.sp,
                            color = colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp) // Mesaj çevresine padding
                        )
                        // Eğer ViewModel'da loading state'iniz varsa ve true ise burada gösterge gösterin
                        /*
                        if (viewModel.isLoading.collectAsState().value) {
                             CircularProgressIndicator(color = colorScheme.primary)
                        }
                        */
                    }
                }
            }
        } // LazyColumn Sonu
    } // Column Sonu
}