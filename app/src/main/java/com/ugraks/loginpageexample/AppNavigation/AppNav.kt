package com.ugraks.loginpageexample.AppNavigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.ugraks.loginpageexample.AppNavigation.Screens.ForgotPassword
import com.ugraks.loginpageexample.AppNavigation.Screens.NewPassword
import com.ugraks.loginpageexample.AppNavigation.Screens.RegisterPage
import com.ugraks.loginpageexample.AppNavigation.Screens.ScreenHomePage
import com.ugraks.loginpageexample.AppNavigation.Screens.ScreenLoginPage
import com.ugraks.loginpageexample.AppNavigation.Screens.ScreenPersonPage
import com.ugraks.loginpageexample.AppNavigation.Screens.ScreenSignInSuccess
import com.ugraks.loginpageexample.AppNavigation.Screens.ScreenSignUpSuccess
import com.ugraks.loginpageexample.FirstLoginPage
import com.ugraks.loginpageexample.ForgotPassword
import com.ugraks.loginpageexample.HomePage
import com.ugraks.loginpageexample.NewPassword
import com.ugraks.loginpageexample.PersonPage
import com.ugraks.loginpageexample.RegisterPage
import com.ugraks.loginpageexample.SignSuccess
import com.ugraks.loginpageexample.SignUpSuccess

@Composable
fun SayfaGecisleri() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ScreenLoginPage) {
        composable<ScreenLoginPage> {

            FirstLoginPage(navController)
        }

        composable<RegisterPage> {

            RegisterPage(navController)
        }
        composable<ForgotPassword> {
            ForgotPassword(navController)

        }
        composable<ScreenSignInSuccess> {

            val args = it.toRoute<ScreenSignInSuccess>()
            SignSuccess(navController,args.name,args.surname)

        }
        composable<NewPassword> {

            NewPassword(navController)

        }
        composable<ScreenSignUpSuccess> {

            val args = it.toRoute<ScreenSignUpSuccess>()
            SignUpSuccess(navController,args.name,args.surname)

        }
        composable<ScreenHomePage> {
            val args = it.toRoute<ScreenHomePage>()
            HomePage(navController,args.name,args.surname)



        }
        composable<ScreenPersonPage>{
            val args = it.toRoute<ScreenPersonPage>()
            PersonPage(navController,args.name,args.surname)


        }


    }





}