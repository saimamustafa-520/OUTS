package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.state.OutsViewModel
import com.example.ui.theme.MyApplicationTheme

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    OutsApp()
                }
            }
        }
    }
}

@Composable
fun OutsApp() {
    val viewModel: OutsViewModel = viewModel()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val consumerScreen by viewModel.consumerScreen.collectAsStateWithLifecycle()
    val hostScreen by viewModel.hostScreen.collectAsStateWithLifecycle()
    val adminScreen by viewModel.adminScreen.collectAsStateWithLifecycle()
    val selectedEventId by viewModel.selectedEventId.collectAsStateWithLifecycle()
    val selectedBookingId by viewModel.selectedBookingId.collectAsStateWithLifecycle()

    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val toastType by viewModel.toastType.collectAsStateWithLifecycle()
    val isScreenLoading by viewModel.isScreenLoading.collectAsStateWithLifecycle()

    val canGoBack by viewModel.canGoBack.collectAsStateWithLifecycle()
    BackHandler(enabled = canGoBack) {
        viewModel.goBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isScreenLoading) {
            when (currentScreen) {
                "consumer" -> {
                    if (consumerScreen == "home") {
                        com.example.ui.components.SkeletonFeedLoader()
                    } else {
                        com.example.ui.components.SkeletonDashboardLoader()
                    }
                }
                "host" -> {
                    com.example.ui.components.SkeletonDashboardLoader()
                }
                "admin" -> {
                    com.example.ui.components.SkeletonDashboardLoader()
                }
                else -> {
                    com.example.ui.components.SkeletonFeedLoader()
                }
            }
        } else {
            when (currentScreen) {
                "splash" -> SplashScreen(onLoadComplete = {
                    val role = viewModel.currentRole
                    if (viewModel.currentUserId != null && role != "guest") {
                        when (role) {
                            "admin" -> viewModel.navigateAdminTo("dashboard")
                            "host" -> viewModel.navigateHostTo("dashboard")
                            else -> viewModel.navigateConsumerTo("home")
                        }
                    } else {
                        viewModel.navigateTo("role_chooser")
                    }
                })
                "role_chooser" -> RoleChooserScreen(
                    onSelectConsumer = { viewModel.navigateTo("consumer") },
                    onSelectHost = { viewModel.navigateTo("host") },
                    onSelectAdmin = { viewModel.navigateTo("admin") }
                )
                "consumer" -> {
                    when (consumerScreen) {
                        "login" -> ConsumerLoginScreen(viewModel, onBack = { viewModel.navigateTo("role_chooser") })
                        "home" -> ConsumerHomeScreen(viewModel, onTabSelected = {})
                        "event_details" -> selectedEventId?.let { id ->
                            ConsumerEventDetailsScreen(viewModel, id)
                        }
                        "tickets" -> ConsumerTicketSelectionScreen(viewModel)
                        "success" -> ConsumerSuccessScreen(viewModel)
                        "my_tickets" -> ConsumerMyTicketsScreen(viewModel)
                        "ticket_detail" -> selectedBookingId?.let { id ->
                            ConsumerTicketDetailScreen(viewModel, id)
                        }
                        "profile" -> ConsumerProfileScreen(viewModel, onBackToHome = { viewModel.navigateConsumerTo("home") })
                    }
                }
                "host" -> {
                    when (hostScreen) {
                        "login" -> HostLoginScreen(viewModel, onBack = { viewModel.navigateTo("role_chooser") })
                        "dashboard" -> HostMainDashboard(viewModel)
                    }
                }
                "admin" -> {
                    when (adminScreen) {
                        "login" -> AdminLoginScreen(viewModel, onBack = { viewModel.navigateTo("role_chooser") })
                        "dashboard" -> AdminMainDashboard(viewModel)
                    }
                }
            }
        }

        com.example.ui.components.OutsToastOverlay(
            message = toastMessage,
            type = toastType,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
