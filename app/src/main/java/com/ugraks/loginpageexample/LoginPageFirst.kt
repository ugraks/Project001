package com.ugraks.loginpageexample

import android.net.Uri
import android.R.id.bold
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType.Companion.Uri
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.nio.file.WatchEvent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.ugraks.loginpageexample.AppNavigation.Screens

import com.ugraks.loginpageexample.AppNavigation.Screens.ScreenSignInSuccess




@Composable

fun FirstLoginPage(navController: NavController) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {

        Text("""
            Lets Sign You In
                
        """.trimIndent()
            , modifier = Modifier.padding(start = 20.dp, top = 80.dp)
            , fontFamily = FontFamily.Cursive
            , fontWeight = FontWeight.ExtraBold
            , fontSize = 40.sp
            , color = Color.Magenta)

        Text("""
            Welcome Back,
            You have been missed !
      
      
        """.trimIndent()
            ,modifier = Modifier.padding(start = 20.dp)
            , fontFamily = FontFamily.Cursive
            , fontWeight = FontWeight.Black
            , fontSize = 25.sp)

            var username = remember { mutableStateOf("") }
        Column() {




            OutlinedTextField(modifier = Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp)
                .background(Color.White)
                ,shape = RoundedCornerShape(20.dp)

                ,value = username.value
                , onValueChange = {

                    if (it.length <= 30 && it.all { char -> char.isLetterOrDigit() }) {
                        username.value = it
                    }



                }, label = {Text("Phone or Username :")}
                   ,keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Unspecified))






        }

            var password = remember { mutableStateOf("") }
        Column() {




            OutlinedTextField(modifier = Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp)
                .background(Color.White)
                ,shape = RoundedCornerShape(20.dp)
                ,value = password.value
                , onValueChange = {

                    if (it.length <= 30 && it.all { char -> char.isDigit() }) {
                        password.value = it
                    }


                }, label = {Text("Password :")},
                    keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number))






        }

        Column(modifier = Modifier.padding(top = 20.dp)) {



            var clicked by remember { mutableStateOf(false) }


                Text(
                    """ Forgot Password ? """
                    ,modifier = Modifier.padding(top = 10.dp, start = 250.dp, end = 20.dp)
                        .clickable{

                                navController.navigate(Screens.ForgotPassword)


                        })










        }

        val context = LocalContext.current
        Column {
            Button(onClick = {
                if(username.value == "") {

                    Toast.makeText(context, "Please Enter Your Username", Toast.LENGTH_LONG).show()

                }
                else if (password.value == ""){

                    Toast.makeText(context, "Please Enter Your Password", Toast.LENGTH_LONG).show()

                }
                else{

                    navController.navigate(ScreenSignInSuccess(username.value,password.value))
                }




            }, modifier = Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp)
                , colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta)) {

                Text("Sign In", color = Color.Black)

            }





        }








        Column() {
            Row(verticalAlignment = Alignment.CenterVertically
            , modifier = Modifier.fillMaxWidth().padding(top = 10.dp))
            {
                Box(modifier = Modifier.padding(start = 20.dp).weight(1f)
                    .height(1.dp).background(Color.Black))

                Text(text = "Or", modifier = Modifier
                    .padding(horizontal = 8.dp))

                Box(modifier = Modifier.padding(end = 20.dp).weight(1f)
                    .height(1.dp).background(Color.Black))




            }








        }


        Column {

        Box(modifier = Modifier.fillMaxWidth()
            .padding(16.dp)
            .background(Color.White)){

            Row(horizontalArrangement = Arrangement.SpaceEvenly
                , verticalAlignment = Alignment.CenterVertically
                , modifier = Modifier.fillMaxWidth()) {

                val handler = LocalUriHandler.current



                IconButton(
                    onClick = {
                        handler.openUri("https://workspace.google.com/intl/tr/gmail/")


                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icons8_google), // SVG ikonu
                        contentDescription = "Google Icon",
                        tint = Color.Black
                    )
                }

                IconButton(
                    onClick = {

                        handler.openUri("https://www.facebook.com/?locale=tr_TR")


                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icons8_facebook), // SVG ikonu
                        contentDescription = "Google Icon",
                        tint = Color.Black
                    )
                }

                IconButton(
                    onClick = {
                        handler.openUri("https://www.instagram.com/")

                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icons8_instagram), // SVG ikonu
                        contentDescription = "Google Icon",
                        tint = Color.Black
                    )
                }









            }




        }




        }

        Column(modifier = Modifier.padding(top = 10.dp)) {
            Row (horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
                ){

                Text(text = "Don't have an account ? ", modifier = Modifier.padding(start = 50.dp))

                Button(onClick = {
                        navController.navigate(Screens.RegisterPage)
                    

                }, colors = ButtonDefaults.buttonColors(Color.White)
                    , modifier = Modifier.padding(start = 30.dp)) {

                    Text(text = "Register Now", color = Color.Blue)

                }





            }

            
            
            
            


        }





    }







}
@Preview
@Composable

fun FirstPage() {

//FirstLoginPage()






}