package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AdStudioScreen
import com.example.ui.screens.CaptionStudioScreen
import com.example.ui.screens.ChatAssistantScreen
import com.example.ui.screens.ContentPlanScreen
import com.example.ui.screens.CreatePromoScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PhotoAnalysisScreen
import com.example.ui.screens.ProUpgradeScreen
import com.example.ui.screens.PromoStudioScreen
import com.example.ui.screens.ResultDetailScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.JualinAIProTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JualinAIProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    JualinAiApp()
                }
            }
        }
    }
}

@Composable
fun JualinAiApp(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Listen to global toast events
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    NavHost(
        navController = navController,
        startDestination = "home",
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToCreate = { navController.navigate("create_promo") },
                onNavigateToPhotoAnalysis = { navController.navigate("photo_analysis") },
                onNavigateToCaptionStudio = { navController.navigate("caption_studio") },
                onNavigateToAdStudio = { navController.navigate("ad_studio") },
                onNavigateToContentPlan = { navController.navigate("content_plan") },
                onNavigateToChatAssistant = { navController.navigate("chat_assistant") },
                onNavigateToPromoStudio = { navController.navigate("promo_studio") },
                onNavigateToHistory = { navController.navigate("history") },
                onNavigateToProUpgrade = { navController.navigate("pro_upgrade") },
                onNavigateToSettings = { navController.navigate("settings") },
                onOpenPromoDetail = { promo ->
                    viewModel.setSelectedResultDetail(promo)
                    navController.navigate("result_detail")
                }
            )
        }

        composable("create_promo") {
            CreatePromoScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSuccess = { promo ->
                    navController.navigate("result_detail")
                }
            )
        }

        composable("photo_analysis") {
            PhotoAnalysisScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onContinueToCreatePromo = { navController.navigate("create_promo") }
            )
        }

        composable("result_detail") {
            val selected = uiState.selectedResultDetail ?: uiState.latestResult
            if (selected != null) {
                ResultDetailScreen(
                    viewModel = viewModel,
                    promo = selected,
                    onBack = { navController.popBackStack() }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }

        composable("caption_studio") {
            CaptionStudioScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenResult = { promo ->
                    navController.navigate("result_detail")
                }
            )
        }

        composable("ad_studio") {
            AdStudioScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenResult = { promo ->
                    navController.navigate("result_detail")
                }
            )
        }

        composable("content_plan") {
            ContentPlanScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenResult = { promo ->
                    navController.navigate("result_detail")
                }
            )
        }

        composable("chat_assistant") {
            ChatAssistantScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenResult = { promo ->
                    navController.navigate("result_detail")
                }
            )
        }

        composable("promo_studio") {
            PromoStudioScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenResult = { promo ->
                    navController.navigate("result_detail")
                }
            )
        }

        composable("history") {
            HistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenDetail = { promo ->
                    navController.navigate("result_detail")
                },
                onNavigateToCreate = { navController.navigate("create_promo") }
            )
        }

        composable("pro_upgrade") {
            ProUpgradeScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
