package com.ugraks.project1

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ugraks.project1.Authenticate.getLikeCountForRecipe
import com.ugraks.project1.Authenticate.hasUserLikedRecipe
import com.ugraks.project1.Authenticate.readRecipesFromAssets
import com.ugraks.project1.Authenticate.toggleLike

@Composable
fun RecipeListScreen(navController: NavController, userEmail: String) {
    val context = LocalContext.current
    val allRecipes = remember { readRecipesFromAssets(context) }
    var searchText by remember { mutableStateOf("") }

    val filteredRecipes = allRecipes.filter {
        it.name.contains(searchText, ignoreCase = true)
    }

    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(horizontal = 16.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Geri Butonu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Go Back",
                    tint = colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Başlık
        Text(
            text = "Search Recipes...",
            fontSize = 28.sp,
            color = colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Arama Kutusu
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Search", color = colorScheme.primary) },
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.background, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = colorScheme.primary,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tarif Listesi
        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            itemsIndexed(filteredRecipes) { index, recipe ->
                val likedState = remember {
                    mutableStateOf(hasUserLikedRecipe(context, userEmail, recipe.name))
                }
                val likeCountState = remember {
                    mutableStateOf(getLikeCountForRecipe(context, recipe.name))
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    onClick = {
                        navController.navigate("recipeDetail/${recipe.name}")

                    },
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        Text(
                            text = recipe.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    toggleLike(context, userEmail, recipe.name)
                                    likedState.value = !likedState.value
                                    likeCountState.value = getLikeCountForRecipe(context, recipe.name)
                                }
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = if (likedState.value) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Like",
                                    tint = if (likedState.value) colorScheme.primary else colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Text(
                                text = "${likeCountState.value} Likes",
                                fontSize = 14.sp,
                                color = colorScheme.onSurface,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                // Divider yerine sadece boşluk bırakmak daha sade ve şık olabilir:
                if (index != filteredRecipes.lastIndex) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}