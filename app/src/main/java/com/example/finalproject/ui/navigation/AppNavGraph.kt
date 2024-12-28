package com.example.finalproject.ui.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalproject.ui.screens.*
import com.example.finalproject.viewmodel.ScanViewModel
import com.example.finalproject.viewmodel.UserInfoViewModel
import com.example.finalproject.utils.DatabaseHelper
import androidx.compose.ui.platform.LocalContext
import com.example.finalproject.viewmodel.FoodDetailsViewModel

private const val TAG = "AppNavGraph"

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun AppNavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val databaseHelper = remember { DatabaseHelper(context) }
    val scanViewModel: ScanViewModel = viewModel()

    val sharedViewModel: FoodDetailsViewModel = remember { FoodDetailsViewModel() }

    // Using DisposableEffect to handle database validation
    DisposableEffect(Unit) {
        val isValid = databaseHelper.isDatabaseValid()
        if (!isValid) {
            Log.e(TAG, "Database initialization failed")
        }

        onDispose {
            databaseHelper.close()
        }
    }

    NavHost(navController = navController, startDestination = "splash") {
        // Startup Page
        composable("splash") {
            SplashScreen(onFinish = {
                navController.navigate("onboarding") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }

        // Guide page
        composable("onboarding") {
            OnboardingScreen(onFinish = {
                navController.navigate("login") {
                    popUpTo("onboarding") { inclusive = true }
                }
            })
        }

        // Login Page
        composable("login") {
            LoginScreen(onLoginSuccess = {
                navController.navigate("loginsuccess") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }

        // Login success page
        composable("loginsuccess") {
            LoginSuccessScreen(onNavigateToSetup = {
                navController.navigate("setup_plan_flow") {
                    popUpTo("loginsuccess") { inclusive = true }
                }
            })
        }

        // Setting up the planning process
        composable("setup_plan_flow") {
            val viewModel: UserInfoViewModel = viewModel()
            SetupPlanFlow(
                viewModel = viewModel,
                onFinish = {
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // main
        composable("home") {
            HomeScreen(
                onNavigateToFoodDetails = { navController.navigate("food_details") },
                onNavigateToMoodDetails = { navController.navigate("mood_details") },
                onNavigateToWeight = { navController.navigate("weight") },
                onNavigateToData = { navController.navigate("data") },
                onNavigateToPersonal = { navController.navigate("personal") }
            )
        }

        composable("weight") {
            WeightScreen(
                onNavigateTo = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("data") {
            DataScreen(
                onNavigateTo = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("personal") {
            PersonalScreen(
                onNavigateTo = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("personal_details") {
            PersonalDetailsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("scan") {
            Log.d(TAG, "scan page")
            ScanScreen(
                navController = navController,
                viewModel = scanViewModel,
                databaseHelper = databaseHelper
            )
        }

        composable(
            route = "food_details/{barcode}",
            arguments = listOf(navArgument("barcode") {
                type = NavType.StringType
                nullable = false
            })
        ) { backStackEntry ->
            val barcode = backStackEntry.arguments?.getString("barcode")
            if (barcode != null) {
                Log.d(TAG, "Navigate to the food details page, barcode: $barcode, using ViewModel: ${sharedViewModel.hashCode()}")
                FoodDetailScreen(
                    navController = navController,
                    barcode = barcode,
                    databaseHelper = databaseHelper,
                    viewModel = sharedViewModel
                )
            }
        }

        composable("food_details") {
            Log.d(TAG, "Navigate to the food list page, using ViewModel: ${sharedViewModel.hashCode()}")
            FoodDetailsScreen(
                viewModel = sharedViewModel,  // 使用共享的 ViewModel
                onScanButtonClick = { navController.navigate("scan") },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("mood_details") {
            MoodDetailsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}