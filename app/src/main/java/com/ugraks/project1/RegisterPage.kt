package com.ugraks.project1

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ugraks.project1.AppNavigation.Screens.ScreenLoginPage
import com.ugraks.project1.Authenticate.saveUserToFile
import kotlinx.coroutines.delay

@Composable
fun RegisterPage(navController: NavController){
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {

        Text(
            """
            Let's Register Account
                
        """.trimIndent(),
            modifier = Modifier.padding(start = 20.dp, top = 80.dp),
            fontFamily = FontFamily.Cursive,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 40.sp,
            color = Color.Magenta
        )

        Text(
            """
            Hello user, you have
            a greatful journey
      
        """.trimIndent(),
            modifier = Modifier.padding(start = 20.dp),
            fontFamily = FontFamily.Cursive,
            fontWeight = FontWeight.Black,
            fontSize = 25.sp
        )

        var username = remember { mutableStateOf("") }
        Column() {




            OutlinedTextField(modifier = Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp)
                .background(Color.White)
                ,shape = RoundedCornerShape(20.dp)

                ,value = username.value
                , onValueChange = {

                    if (it.length <= 30 && it.all { char -> char.isLetter() || char.isWhitespace() }) {
                        username.value = it
                    }


                }, label = {Text("Name :")},keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Unspecified))






        }

        var businessName = remember { mutableStateOf("") }
        Column(modifier = Modifier.padding(top = 4.dp)) {




            OutlinedTextField(modifier = Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp)
                .background(Color.White)
                ,shape = RoundedCornerShape(20.dp)

                ,value = businessName.value
                , onValueChange = {

                    if (it.length <= 30 && it.all { char -> char.isLetterOrDigit() || char.isWhitespace() }) {
                        businessName.value = it
                    }


                }, label = {Text("Business Name :")},keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Unspecified))






        }

        var phone = remember { mutableStateOf("") }
        Column(modifier = Modifier.padding(top = 4.dp)) {




            OutlinedTextField(modifier = Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp)
                .background(Color.White)
                ,shape = RoundedCornerShape(20.dp)

                ,value = phone.value
                , onValueChange = {

                    if (it.length <= 30 && it.all { char -> char.isDigit() }) {
                        phone.value = it
                    }


                }, label = {Text("Phone :")},keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Phone))






        }

        var email = remember { mutableStateOf("") }

        Column(modifier = Modifier.padding(top = 4.dp)) {




            OutlinedTextField(modifier = Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp)
                .background(Color.White)
                ,shape = RoundedCornerShape(20.dp)

                ,value = email.value
                , onValueChange = {

                    if (it.length <= 30 && it.none { char -> char.isWhitespace() })  {
                        email.value = it
                    }


                }, label = {Text("Email :")},keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email))





        }

        var password = remember { mutableStateOf("") }
        Column(modifier = Modifier.padding(top = 4.dp)) {




            OutlinedTextField(modifier = Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp)
                .background(Color.White)
                ,shape = RoundedCornerShape(20.dp)

                ,value = password.value
                , onValueChange = {

                    if (it.length <= 30 && it.none { char -> char.isWhitespace() }) {
                        password.value = it
                    }



                }, label = {Text("Password :")},
                visualTransformation = PasswordVisualTransformation()
                ,keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password))








        }

        val context = LocalContext.current
        var isLoading = remember { mutableStateOf(false) }

        Column {
            if (!isLoading.value) {  // Eğer yükleme durumu false ise, butonu göster
                Button(
                    onClick = {
                        if (username.value == "" || businessName.value == "" || phone.value == "" || email.value == "" || password.value == "") {
                            Toast.makeText(context, "Please Enter Your Information Completely", Toast.LENGTH_LONG).show()
                        } else {
                            if (email.value.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.value).matches()) {
                                // Eğer geçerli bir email değilse, uyarı göster
                                Toast.makeText(context, "Please Enter a Valid Email Address", Toast.LENGTH_LONG).show()
                            } else {
                                // Yükleme durumu açılır
                                isLoading.value = true  // isLoading'i true yapıyoruz
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta)
                ) {
                    Text("Sign Up", color = Color.Black)
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
                    // Kullanıcı bilgilerini kaydetme işlemi
                    saveUserToFile(context, username.value, businessName.value, phone.value, email.value, password.value)
                    // Kaydetme işleminden sonra login sayfasına yönlendir
                    navController.navigate(ScreenLoginPage)
                    isLoading.value = false  // Yükleme durumu sonlanır
                }
            }
        }

        Column(modifier = Modifier.padding(top = 10.dp)) {
            Row (horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ){

                Text(text = "Already have an account ? ", modifier = Modifier.padding(start = 50.dp))

                Button(onClick = {
                    navController.navigateUp()


                }, colors = ButtonDefaults.buttonColors(Color.White)
                    , modifier = Modifier.padding(start = 30.dp)) {

                    Text(text = "Login", color = Color.Blue)

                }





            }







        }













    }



}

@Preview
@Composable

fun PreviewRegister(){

    //RegisterPage()



}