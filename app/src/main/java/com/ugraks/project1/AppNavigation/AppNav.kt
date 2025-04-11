package com.ugraks.project1.AppNavigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.ugraks.project1.AppNavigation.Screens.DailyCaloriesPage
import com.ugraks.project1.AppNavigation.Screens.ForgotPassword
import com.ugraks.project1.AppNavigation.Screens.NewPassword
import com.ugraks.project1.AppNavigation.Screens.RegisterPage
import com.ugraks.project1.AppNavigation.Screens.ScreenHomePage
import com.ugraks.project1.AppNavigation.Screens.ScreenLoginPage
import com.ugraks.project1.AppNavigation.Screens.ScreenPersonPage
import com.ugraks.project1.AppNavigation.Screens.ScreenProfileEditPage
import com.ugraks.project1.AppNavigation.Screens.ScreenRatingPage
import com.ugraks.project1.DailyCalories
import com.ugraks.project1.FirstLoginPage
import com.ugraks.project1.ForgotPassword
import com.ugraks.project1.HomePage
import com.ugraks.project1.NewPassword
import com.ugraks.project1.PersonPage
import com.ugraks.project1.ProfileEditPage
import com.ugraks.project1.RatingPage
import com.ugraks.project1.RegisterPage


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

        composable<NewPassword> {

            NewPassword(navController)

        }

        composable<ScreenHomePage> {
            val args = it.toRoute<ScreenHomePage>()
            HomePage(navController,args.username,args.email)



        }
        composable<ScreenPersonPage>{
            val args = it.toRoute<ScreenPersonPage>()
            PersonPage(navController,args.email)


        }
        composable<DailyCaloriesPage> {

            DailyCalories(navController)

        }

        composable<ScreenRatingPage> {

            RatingPage(navController)
        }

        composable<ScreenProfileEditPage>{
            val args = it.toRoute<ScreenProfileEditPage>()
            ProfileEditPage(navController,args.email)


        }




    }





}