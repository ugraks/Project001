package com.ugraks.project1.Foods

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSearchScreen(navController: NavController) {
    val context = LocalContext.current

    val foodItems = readAndParseItemsFromAssets(context)

    var searchText by remember { mutableStateOf("") }


    val filteredItems = foodItems.filter {

        it.name.contains(searchText, ignoreCase = true)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp) // İlk tasarım padding'i
            .background(MaterialTheme.colorScheme.background) // Tema uyumlu arka plan
    ) {

        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.TopStart)  // Sol üstte hizalama
                    .padding(start = 10.dp, top = 25.dp) // İlk tasarım padding'i
            ) {

                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
        }


        Spacer(modifier = Modifier.height(60.dp)) // İlk tasarım boşluğu
        Text(
            text = "Search Foods Here...",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            fontStyle = FontStyle.Italic,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp) // İlk tasarım padding'i
                .wrapContentWidth(Alignment.CenterHorizontally) // İlk tasarım ortalama stili
        )


        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            label = { Text("Search for food or drink") }, // İlk tasarım label'ı (renk yoktu)
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), // İlk tasarım padding'i
            textStyle = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground),
            shape = RoundedCornerShape(20.dp), // İlk tasarım şekli
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = Color.Gray,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(24.dp)) // İlk tasarım boşluğu


        LazyColumn(
            modifier = Modifier.fillMaxHeight(0.6f) // İlk tasarım yüksekliği
        ) {
            itemsIndexed(filteredItems) { index, item ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp) //
                        .clickable {

                            navController.navigate("FoodCaloriesPage/${item.name}")
                        }

                        .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp)) // İlk tasarım background'ı
                        .shadow(4.dp, RoundedCornerShape(10.dp)) // İlk tasarım shadow'u
                ) {

                    Text(
                        text = item.name,
                        color = MaterialTheme.colorScheme.primary,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.align(Alignment.CenterVertically)
                            .padding(start = 12.dp)
                    )
                }



            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}