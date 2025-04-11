package com.ugraks.project1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay

@Composable
fun RatingPage(navController: NavHostController) {
    var rating by remember { mutableStateOf(0) }
    var showThankYouMessage by remember { mutableStateOf(false) }
    var hideStarsAndButton by remember { mutableStateOf(false) }

    // Geri dönme işlemi için delay
    LaunchedEffect(showThankYouMessage) {
        if (showThankYouMessage) {
            delay(2000)
            navController.navigateUp()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // 🔙 IconButton - sol üstte sabit
        if (!showThankYouMessage) {
            IconButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 12.dp, top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Go Back",
                    tint = Color.Magenta,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // 🌟 Ortalanmış Yıldızlar ve Submit Butonu
        if (!showThankYouMessage) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.align(Alignment.Center) // 💡 EKRANIN TAM ORTASI
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        IconButton(onClick = {
                            rating = i
                        }) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "Star $i",
                                tint = if (i <= rating) Color.Magenta else Color.Gray,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(48.dp)
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        showThankYouMessage = true
                        hideStarsAndButton = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta),
                    modifier = Modifier.width(200.dp)
                ) {
                    Text(text = "Submit", color = Color.White)
                }
            }
        }

        // 🎉 Teşekkür ekranı
        if (showThankYouMessage) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Thank You!",
                    color = Color.Magenta,
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(20.dp))
                CircularProgressIndicator(
                    color = Color.Magenta,
                    modifier = Modifier.size(60.dp)
                )
            }
        }
    }
}



@Preview
@Composable

fun RatingPagePreview(){


    //RatingPage(navController)



}