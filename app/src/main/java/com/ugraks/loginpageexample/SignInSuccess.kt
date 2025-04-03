package com.ugraks.loginpageexample

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ugraks.loginpageexample.AppNavigation.Screens.ScreenHomePage
import kotlinx.coroutines.delay

@Composable
fun SignSuccess(navController: NavController, name: String, surname: String) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {


        Text(
            """
            You Signed In Successfully ${name + " " + surname}...
            Wait Please...
            
            
                
        """.trimIndent(),
            modifier = Modifier.padding(start = 20.dp, top = 80.dp),
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Cursive,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 40.sp,
            color = Color.Magenta
        )



        Column(
            modifier = Modifier
                .fillMaxWidth()
                , verticalArrangement = Arrangement.Center
                , horizontalAlignment = Alignment.CenterHorizontally
                ,
        ) {
            CircularProgressIndicator(color = Color.Magenta) // Basit bir CircularProgressIndicator
            Spacer(modifier = Modifier.height(16.dp))

            LaunchedEffect(Unit) {
                delay(3000) // 3 saniye bekle
                navController.navigate(ScreenHomePage(name,surname))

            }








        }

        Spacer(modifier = Modifier.padding(top = 20.dp))

        Button(onClick = {

            navController.navigateUp()



        }, modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 250.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta))
        {
            Text("Go Back", color = Color.Black)

        }





    }




}




@Preview
@Composable


fun SignSuccessPreview() {

    //SignSuccess()




}