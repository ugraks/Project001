package com.ugraks.loginpageexample

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ugraks.loginpageexample.AppNavigation.Screens
import com.ugraks.loginpageexample.AppNavigation.Screens.ScreenSignUpSuccess

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

                    if (it.length <= 30 && it.all { char -> char.isLetter() }) {
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

                    if (it.length <= 30 && it.all { char -> char.isLetterOrDigit() }) {
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

                    if (it.length <= 30)  {
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

                    if (it.length <= 30 && it.all { char -> char.isLetterOrDigit() }) {
                        password.value = it
                    }



                }, label = {Text("Password :")},keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password))






        }

        val context = LocalContext.current
        Column {
            Button(onClick = {

                if(username.value == "" || businessName.value == "" || phone.value == "" || email.value == "" || password.value == "") {


                    Toast.makeText(context, "Please Enter Your Information Completely", Toast.LENGTH_LONG).show()


                }
                else{

                    if (email.value.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.value).matches()) {
                        // Eğer geçerli bir email değilse, uyarı göster
                        Toast.makeText(context, "Please Enter a Valid Email Address", Toast.LENGTH_LONG).show()

                    }
                    else {
                    navController.navigate(ScreenSignUpSuccess(username.value,password.value))
                    }

                }




            }, modifier = Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp)
                , colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta)) {

                Text("Sign Up", color = Color.Black)

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