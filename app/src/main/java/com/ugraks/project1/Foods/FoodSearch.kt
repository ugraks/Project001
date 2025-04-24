package com.ugraks.project1.Foods // Kendi paket adınız

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue // collectAsState için
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
// YENİ: ViewModel için import
import androidx.hilt.navigation.compose.hiltViewModel
// YENİ: ViewModel'dan gelen Flow'u izlemek için
import androidx.compose.runtime.collectAsState
import com.ugraks.project1.ui.viewmodels.FoodViewModel.FoodSearchViewModel

// YENİ: ViewModel'ınızın paketi ve sınıfı


// ESKİ: readAndParseItemsFromAssets fonksiyonu artık burada kullanılmaz.
// import com.ugraks.project1.Foods.readAndParseItemsFromAssets
// ESKİ: FoodItem data class'ı artık doğrudan UI'da kullanılmaz (Entity kullanılır veya sadece isim)
// import com.ugraks.project1.Foods.FoodItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSearchScreen(
    navController: NavController,
    // YENİ: FoodSearchViewModel'ı Hilt ile inject et
    viewModel: FoodSearchViewModel = hiltViewModel()
) {
    val context = LocalContext.current // Gerekirse (örneğin Toast için)

    // ESKİ: val foodItems = remember { readAndParseItemsFromAssets(context) } // KALDIRILDI
    // ESKİ: var searchText by remember { mutableStateOf("") } // ViewModel'da yönetilecek
    // ESKİ: val filteredItems = foodItems.filter { ... } // ViewModel'da yönetilecek

    // YENİ: ViewModel'dan arama metnini ve filtrelenmiş yemek öğesi adları listesini reaktif olarak izle
    val searchText by viewModel.searchText.collectAsState() // ViewModel'dan gelen arama metni state'i
    // filteredFoodItemNames Flow'u List<String> yayınlıyor
    val filteredFoodItemNames by viewModel.filteredFoodItemNames.collectAsState() // ViewModel'dan gelen filtrelenmiş isimler (List<String>)


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Geri Butonu aynı kalır
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 10.dp, top = 25.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
        }


        Spacer(modifier = Modifier.height(60.dp))
        Text(
            text = "Search Foods Here...",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            fontStyle = FontStyle.Italic,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .wrapContentWidth(Alignment.CenterHorizontally)
        )

        // Arama Kutusu - value ve onValueChange ViewModel'ı kullanacak
        OutlinedTextField(
            value = searchText, // YENİ: ViewModel'dan gelen state'i kullan
            onValueChange = { viewModel.updateSearchText(it) }, // YENİ: ViewModel'ın arama metnini güncelleme metodunu çağır
            label = { Text("Search for food or drink") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            textStyle = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground),
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = Color.Gray,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Yemek öğesi Listesi - ViewModel'dan gelen filtrelenmiş adlar listesini kullanacak
        LazyColumn(
            modifier = Modifier.fillMaxHeight(0.6f)
        ) {
            // YENİ: filteredFoodItemNames List<String> kullanılır
            itemsIndexed(
                items = filteredFoodItemNames,
                key = { index, foodName -> foodName } // Her öğe için benzersiz anahtar
            ) { index, foodName -> // foodName artık doğrudan String
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .clickable {
                            // Navigasyon aynı kalır, detay ekranına yemek adını göndermeye devam edin
                            navController.navigate("FoodCaloriesPage/${foodName}")
                        }
                        .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp))
                        .shadow(4.dp, RoundedCornerShape(10.dp)),
                    verticalAlignment = Alignment.CenterVertically // Dikeyde ortala
                ) {
                    // item.name yerine doğrudan foodName string'ini kullanın
                    Text(
                        text = foodName, // YENİ: Doğrudan string kullanılır
                        color = MaterialTheme.colorScheme.primary,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }
            // Liste boşsa veya filtre sonucu yoksa mesaj gösterme
            if (filteredFoodItemNames.isEmpty()) {
                item { // item() çağrısı LazyColumn'ın doğrudan scope'u içinde olmalıdır.
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        val message = when {
                            searchText.isNotEmpty() -> "No foods found for \"$searchText\"." // Arama sonucu yoksa
                            // ViewModel'a yükleniyor state'i eklerseniz burayı güncelleyebilirsiniz
                            // !viewModel.isLoading.collectAsState().value -> "No foods available." // Yüklendi ve boşsa
                            else -> "Loading foods or no foods available." // Yükleniyor veya bilinmiyor (ilk yükleme veya boş asset)
                        }
                        Text(
                            text = message,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                        // Eğer ViewModel'da loading state'iniz varsa ve true ise burada gösterge gösterin
                        /*
                        if (viewModel.isLoading.collectAsState().value) {
                             CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                        */
                    }
                }
            }
        } // LazyColumn Sonu

        Spacer(modifier = Modifier.height(24.dp)) // Listenin altındaki boşluk
    }
}