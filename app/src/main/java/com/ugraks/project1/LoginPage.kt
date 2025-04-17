package com.ugraks.project1

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isNewPasswordVisibleLogin by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {

        Text(
            "Lets Sign You In",
            modifier = Modifier.padding(start = 20.dp, top = 80.dp),
            fontFamily = FontFamily.Cursive,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 40.sp,
            color = MaterialTheme.colorScheme.primary
        )


        Text(
            "Welcome Back,\nYou have been missed !",
            modifier = Modifier.padding(start = 20.dp, top = 40.dp),
            fontFamily = FontFamily.Cursive,
            fontWeight = FontWeight.Black,
            fontSize = 25.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(50.dp))

        // Email Field
        OutlinedTextField(
            value = email.value,
            onValueChange = {
                if (it.length <= 30 && it.none { char -> char.isWhitespace() }) {
                    email.value = it
                }
            },
            label = { Text("Email", color = MaterialTheme.colorScheme.onSurface) },
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface


            )
        )

        // Password Field
        OutlinedTextField(
            value = password.value,
            onValueChange = {
                if (it.length <= 30 && it.none { char -> char.isWhitespace() }) {
                    password.value = it
                }
            },
            label = { Text("Password", color = MaterialTheme.colorScheme.onSurface) },
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp),
            shape = RoundedCornerShape(20.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = if (isNewPasswordVisibleLogin) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (isNewPasswordVisibleLogin) Icons.Filled.Close else Icons.Filled.Info
                IconButton(onClick = { isNewPasswordVisibleLogin = !isNewPasswordVisibleLogin }) {
                    Icon(
                        imageVector = icon,
                        contentDescription = if (isNewPasswordVisibleLogin) "Hide" else "Show"
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface
            )
        )

        // Forgot Password
        Text(
            "Forgot Password ?",
            modifier = Modifier
                .padding(top = 10.dp, start = 20.dp)
                .clickable {
                    navController.navigate(Screens.ForgotPassword)
                },
            color = MaterialTheme.colorScheme.primary
        )

        // Sign In Button or Loading
        if (!isLoading) {
            Button(
                onClick = {
                    if (email.value.isEmpty() || password.value.isEmpty()) {
                        Toast.makeText(context, "Please Enter Your Email and Password", Toast.LENGTH_LONG).show()
                    } else {
                        isLoading = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Sign In", color = MaterialTheme.colorScheme.onPrimary)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .height(50.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            LaunchedEffect(true) {
                delay(3000)
                val isAuthenticated = checkUserCredentials(context, email.value, password.value)
                if (isAuthenticated) {
                    val username = getUsernameByEmail(context, email.value)
                    if (username != null) {
                        navController.navigate(ScreenHomePage(username, email.value))
                    } else {
                        Toast.makeText(context, "Username not found", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "Invalid Credentials", Toast.LENGTH_LONG).show()
                }
                isLoading = false
            }
        }

        // OR line
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .weight(1f)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.onBackground)
            )

            Text(
                text = "Or",
                modifier = Modifier.padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            Box(
                modifier = Modifier
                    .padding(end = 20.dp)
                    .weight(1f)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.onBackground)
            )
        }

        // Social Icons
        val handler = LocalUriHandler.current
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            IconButton(onClick = { handler.openUri("https://workspace.google.com/intl/tr/gmail/") }) {
                Icon(
                    painter = painterResource(id = R.drawable.icons8_google),
                    contentDescription = "Google",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = { handler.openUri("https://www.facebook.com/?locale=tr_TR") }) {
                Icon(
                    painter = painterResource(id = R.drawable.icons8_facebook),
                    contentDescription = "Facebook",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = { handler.openUri("https://www.instagram.com/") }) {
                Icon(
                    painter = painterResource(id = R.drawable.icons8_instagram),
                    contentDescription = "Instagram",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Register Text
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp)
        ) {
            Text(
                text = "Don't have an account? Register",
                modifier = Modifier
                    .clickable { navController.navigate(Screens.RegisterPage) },
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
@Preview
@Composable

fun FirstPage() {

//FirstLoginPage()






}