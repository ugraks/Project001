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
import com.ugraks.project1.Authenticate.readRecipesFromAssets


@Composable
fun RecipeDetailScreen(recipeName: String, navController: NavController) {
    val context = LocalContext.current
    val allRecipes = remember { readRecipesFromAssets(context) }
    val recipe = allRecipes.find { it.name == recipeName }

    val colorScheme = MaterialTheme.colorScheme
    val titleFontFamily = FontFamily.Serif
    val contentFontFamily = FontFamily.SansSerif

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
                .padding(start = 10.dp, top = 10.dp) // Tıklanabilirlik için padding ayarladık
                .zIndex(1f) // IconButton'ı üst planda tut
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Go Back",
                tint = colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }

        // Card yapısı + içeriği
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp)) // üst boşluk

            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {
                // Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp) // Resme yer bırakmak için
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(60.dp)) // Resmin altına yer bırak

                        // Yemek ismi
                        Text(
                            text = recipe?.name ?: "Recipe not found",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = titleFontFamily,
                            color = colorScheme.primary,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(bottom = 16.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Ingredients başlığı
                        Text(
                            text = "Ingredients:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = colorScheme.primary,
                            fontFamily = titleFontFamily
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Ingredients list
                        recipe?.ingredients?.forEach {
                            Text(
                                text = "• $it",
                                fontSize = 16.sp,
                                fontFamily = contentFontFamily,
                                color = colorScheme.onSurface,
                                modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                                lineHeight = 22.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Instructions başlığı
                        Text(
                            text = "Instructions:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = colorScheme.primary,
                            fontFamily = titleFontFamily
                        )

                        Text(
                            text = recipe?.instructions ?: "No instructions available.",
                            fontSize = 16.sp,
                            fontFamily = contentFontFamily,
                            color = colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp),
                            lineHeight = 24.sp
                        )
                    }
                }

                // Resim - Burada varsayılan bir resim ekliyoruz
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground), // Varsayılan resim
                    contentDescription = "Recipe Image",
                    modifier = Modifier
                        .size(120.dp) // Card'ın yüksekliğinin yaklaşık 2/5'i
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, colorScheme.primary, RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop // Resmi crop yaparak sığdırıyoruz
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}