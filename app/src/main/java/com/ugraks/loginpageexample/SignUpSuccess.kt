package com.ugraks.loginpageexample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ugraks.loginpageexample.AppNavigation.Screens
import com.ugraks.loginpageexample.AppNavigation.Screens.ScreenSignUpSuccess

@Composable
fun SignUpSuccess(navController: NavController, name: String, surname: String) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {


        Text(
            """
            Welcome ${name + " " + surname}
            You Signed Up Successfully...
            Wait Please...
            
            
                
        """.trimIndent(),
            modifier = Modifier.padding(start = 20.dp, top = 80.dp),
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Cursive,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 40.sp,
            color = Color.Magenta
        )

        var isLoading by remember { mutableStateOf(true) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
            , verticalArrangement = Arrangement.Center
            , horizontalAlignment = Alignment.CenterHorizontally
            ,
        ) {
            CircularProgressIndicator(color = Color.Magenta) // Basit bir CircularProgressIndicator
            Spacer(modifier = Modifier.height(16.dp))

        }

        Column {
            Button(onClick = {

                navController.navigate(Screens.ScreenLoginPage)


            }, modifier = Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 60.dp)
                , colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta)) {

                Text("Sign In", color = Color.Black)

            }





        }








    }




}




@Preview
@Composable


fun SignUpSuccessPreview() {

    //SignSuccess()




}