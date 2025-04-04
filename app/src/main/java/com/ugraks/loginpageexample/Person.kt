package com.ugraks.loginpageexample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun PersonPage(navController: NavHostController, name: String) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {

            Column (modifier = Modifier.fillMaxWidth().padding(top = 35.dp)){
                Row() {
                    Button(onClick = {

                        navController.navigateUp()


                    }, colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {

                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Person",
                            modifier = Modifier.size(50.dp),
                            tint = Color.Magenta
                        )

                    }
                    Box(modifier = Modifier.padding(start = 60.dp)) {
                    Icon(Icons.Default.Person, contentDescription = "Person"
                        , modifier = Modifier.size(80.dp), tint = Color.Magenta)}

                }

            }
            Spacer(modifier = Modifier.padding(top = 30.dp))


            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

                Text(text = "Hello ${name}", fontSize = 20.sp)







            }




            }




















}



@Preview
@Composable

fun PersonPagePreview() {



    // PersonPage(navController, args.name, args.surname)


}