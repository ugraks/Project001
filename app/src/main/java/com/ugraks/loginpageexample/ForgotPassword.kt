package com.ugraks.loginpageexample

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ugraks.loginpageexample.AppNavigation.Screens
import com.ugraks.loginpageexample.AppNavigation.Screens.ScreenSignInSuccess

@Composable

fun ForgotPassword(navController: NavController) {

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {

        Text("""
            Please Fill In Your Account İnformation :
                
        """.trimIndent()
            , textAlign = TextAlign.Center
            , modifier = Modifier.padding(start = 20.dp, top = 80.dp, end = 20.dp)
            , fontFamily = FontFamily.Cursive
            , fontWeight = FontWeight.ExtraBold
            , fontSize = 40.sp
            , color = Color.Magenta)




        var username = remember { mutableStateOf("") }
        Column(modifier = Modifier.fillMaxWidth()) {



            OutlinedTextField(modifier = Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp)
                .background(Color.White)
                ,shape = RoundedCornerShape(20.dp)
                ,value = username.value
                , onValueChange = {
                    if (it.length <= 30 && it.all { char -> char.isLetterOrDigit() }) {
                        username.value = it
                    }

                }, label = {Text("Username :")}
                ,     keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Unspecified)

            )










        }

        Spacer(modifier = Modifier.padding(top = 10.dp))

        var number = remember { mutableStateOf("") }
        Column(modifier = Modifier.fillMaxWidth()) {



            OutlinedTextField(modifier = Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp)
                .background(Color.White)
                ,shape = RoundedCornerShape(20.dp)
                ,value = number.value
                , onValueChange = {

                    if (it.length <= 30 && it.all { char -> char.isDigit() }) {
                        number.value = it
                    }


                }, label = {Text("Telephone Number :")},keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number))










        }

        Spacer(modifier = Modifier.padding(top = 10.dp))


        var emaill = remember { mutableStateOf("") }
        Column(modifier = Modifier.fillMaxWidth()) {



            OutlinedTextField(modifier = Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp)
                .background(Color.White)
                ,shape = RoundedCornerShape(20.dp)
                ,value = emaill.value
                , onValueChange = {

                    if (it.length <= 30 && it.none { char -> char.isWhitespace() }) {
                        emaill.value = it
                    }


                }, label = {Text("E-Mail :")},keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email))










        }


        val context = LocalContext.current
        Button(onClick = {

            if(username.value == "") {

                Toast.makeText(context, "Please Enter Your Username", Toast.LENGTH_LONG).show()

            }
            else if (number.value == ""){

                Toast.makeText(context, "Please Enter Your Phone Number", Toast.LENGTH_LONG).show()

            }
            else if (emaill.value == ""){

                Toast.makeText(context, "Please Enter Your Email Address", Toast.LENGTH_LONG).show()

            }
            else{

                if (emaill.value.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emaill.value).matches()) {
                    // Eğer geçerli bir email değilse, uyarı göster
                    Toast.makeText(context, "Please Enter a Valid Email Address", Toast.LENGTH_LONG).show()

                }

                else{

                    navController.navigate(Screens.NewPassword)
                }


            }



                



        }, modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 250.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta))
        {
            Text("Reset Password", color = Color.Black)

        }

        Spacer(modifier = Modifier.padding(top = 20.dp))

        Button(onClick = {

            navController.navigateUp()



        }, modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta))
        {
            Text("Go Back", color = Color.Black)

        }














    }
















}

@Preview
@Composable

fun ForgotPasswordPreview() {

    //ForgotPassword()


}