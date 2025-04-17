package com.ugraks.project1

import android.R.attr.query
import android.content.Context
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSearchScreen(navController: NavController) {
    val context = LocalContext.current
    val itemsList = readItemsFromAssets(context) // items.txt verisini oku
    val foodItems = parseItemData(itemsList) // Veriyi FoodItem listesine dönüştür

    var searchText by remember { mutableStateOf("") }

    // Arama işlemi
    val filteredItems = foodItems.filter {
        it.name.contains(searchText, ignoreCase = true)
    }

    // UI tasarımı
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background) // Tema uyumlu arka plan
    ) {
        // Geri butonu sol üstte sabit
        Box(modifier = Modifier.fillMaxWidth()) {
            // Geri Butonu (sol üstte sabit)
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.TopStart)  // Sol üstte hizalama
                    .padding(start = 10.dp, top = 25.dp) // Geri butonunun yerini ayarlıyoruz
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary, // Tema rengi uyumu
                    modifier = Modifier.size(30.dp)  // Icon boyutunu ayarlıyoruz
                )
            }
        }

        // Başlık "Food Search" kısmı
        Spacer(modifier = Modifier.height(60.dp)) // Başlık ile geri butonunun arasında daha fazla boşluk ekliyoruz
        Text(
            text = "Search Foods Here...",
            color = MaterialTheme.colorScheme.primary, // Tema rengi
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif, // Modern font ailesi
            fontStyle = FontStyle.Italic,  // Başlığı italik yapıyoruz
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp) // Başlık ile diğer elemanlar arasında boşluk ekliyoruz
                .wrapContentWidth(Alignment.CenterHorizontally) // Başlığı yatayda ortalıyoruz
        )

        // Search bar
        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            label = { Text("Search for food or drink") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            textStyle = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground), // Tema uyumlu yazı rengi
            shape = RoundedCornerShape(20.dp)
        )

        Spacer(modifier = Modifier.height(24.dp)) // Search bar ile sonuçlar arasında boşluk

        // Arama sonuçlarını listeleme
        LazyColumn(
            modifier = Modifier.fillMaxHeight(0.6f) // Ekranın ortasına kadar yer kaplasın
        ) {
            itemsIndexed(filteredItems) { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .clickable {
                            navController.navigate("FoodCaloriesPage/${item.name}") // Detay sayfasına yönlendirme
                        }
                        .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp)) // Tema uyumlu arka plan
                        .shadow(4.dp, RoundedCornerShape(10.dp)) // Kartlara gölge ekleyelim
                ) {
                    // Yiyecek ismini ekliyoruz
                    Text(
                        text = item.name,
                        color = MaterialTheme.colorScheme.primary, // Tema uyumlu renk
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.align(Alignment.CenterVertically)
                            .padding(start = 12.dp) // Yiyecek ismi ile kenarlardan boşluk
                    )
                }

                if (index != filteredItems.size - 1) {

                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

