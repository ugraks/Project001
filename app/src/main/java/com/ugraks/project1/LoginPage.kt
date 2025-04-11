package com.ugraks.project1

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ugraks.project1.AppNavigation.Screens
import com.ugraks.project1.AppNavigation.Screens.ScreenHomePage
import com.ugraks.project1.Authenticate.checkUserCredentials
import com.ugraks.project1.Authenticate.getUsernameByEmail
import kotlinx.coroutines.delay


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

        var email = remember { mutableStateOf("") }
        Column() {




            OutlinedTextField(modifier = Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp)
                .background(Color.White)
                ,shape = RoundedCornerShape(20.dp)

                ,value = email.value
                , onValueChange = {

                    if (it.length <= 30 && it.none { char -> char.isWhitespace() }) {
                        email.value = it
                    }



                }, label = {Text("Email :")}
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

                    if (it.length <= 30 && it.none { char -> char.isWhitespace() }) {
                        password.value = it
                    }


                }, label = {Text("Password :")},
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number))






        }

        Column(modifier = Modifier.padding(top = 20.dp)) {



            var clicked by remember { mutableStateOf(false) }


            Text(
                """ Forgot Password ? """
                ,modifier = Modifier.padding(top = 10.dp, start = 20.dp).align(Alignment.Start)
                    .clickable{

                        navController.navigate(Screens.ForgotPassword)


                    })










        }

        val context = LocalContext.current

        var isLoading = remember { mutableStateOf(false) }

        Column {
            if (!isLoading.value) {  // Eğer yükleme durumu false ise, butonu göster
                Button(
                    onClick = {
                        if (email.value.isEmpty() || password.value.isEmpty()) {
                            Toast.makeText(context, "Please Enter Your Email and Password", Toast.LENGTH_LONG).show()
                        } else {
                            // Yükleme durumu açılır
                            isLoading.value = true  // isLoading'i true yapıyoruz
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta)
                ) {
                    Text("Sign In", color = Color.Black)
                }
            } else {
                // Buton kaybolur ve sadece CircularProgressIndicator görünür
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 20.dp)
                        .height(50.dp),  // Yükleme çarkının yüksekliğini sınırlıyoruz
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.Magenta,
                        modifier = Modifier.size(40.dp)  // Yükleme çarkını istediğiniz boyutta ayarlayabilirsiniz
                    )
                }
            }

            LaunchedEffect(isLoading.value) {  // isLoading'in değerine erişiyoruz
                if (isLoading.value) {
                    delay(3000) // 3 saniye bekle

                    // Giriş doğrulama işlemi
                    val isAuthenticated = checkUserCredentials(context, email.value, password.value)
                    if (isAuthenticated) {
                        // Giriş başarılıysa, email ile kullanıcı adını alalım
                        val username = getUsernameByEmail(context, email.value)

                        // Eğer kullanıcı adı bulunmuşsa, ana sayfaya geçiş yapalım
                        if (username != null) {
                            navController.navigate(ScreenHomePage(username,email.value))  // Giriş başarılıysa ana sayfaya git
                        } else {
                            Toast.makeText(context, "Username not found", Toast.LENGTH_LONG).show()  // Kullanıcı adı bulunamadı
                        }
                    } else {
                        Toast.makeText(context, "Invalid Credentials", Toast.LENGTH_LONG).show()  // Hatalı giriş
                    }

                    isLoading.value = false  // Yükleme durumu sonlanır
                }
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