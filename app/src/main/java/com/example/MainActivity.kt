package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudyViewModel
import com.example.ui.viewmodel.Tab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                StudyAppMain()
            }
        }
    }
}

@Composable
fun StudyAppMain() {
    val viewModel: StudyViewModel = viewModel()
    val currentTab by viewModel.currentTab.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    if (!isLoggedIn) {
        AuthScreen(viewModel = viewModel)
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            StudyBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                Tab.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onOpenQuiz = { viewModel.startQuiz() },
                    onOpenVault = { viewModel.openVault() }
                )
                Tab.COURSES -> CoursesScreen(viewModel = viewModel)
                Tab.CHAT -> ChatScreen(viewModel = viewModel)
                Tab.PROFILE -> ProfileScreen(viewModel = viewModel)
            }

            // --- QUIZ OVERLAY GAME ---
            QuizOverlay(
                viewModel = viewModel,
                onDismiss = { viewModel.closeQuiz() }
            )

            // --- STUDY VAULT OVERLAY ---
            VaultOverlay(
                viewModel = viewModel,
                onDismiss = { viewModel.closeVault() }
            )
        }
    }
}

@Composable
fun StudyBottomNavigation(
    currentTab: Tab,
    onTabSelected: (Tab) -> Unit
) {
    NavigationBar(
        containerColor = BentoCardBg, // matches #F3EDF7
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("study_bottom_navigation")
    ) {
        NavigationBarItem(
            selected = currentTab == Tab.HOME,
            onClick = { onTabSelected(Tab.HOME) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BentoPremiumText,
                unselectedIconColor = BentoTextSecondary,
                selectedTextColor = BentoPremiumText,
                unselectedTextColor = BentoTextSecondary,
                indicatorColor = Color(0xFFE8DEF8) // active pill colour matching M3
            ),
            modifier = Modifier.testTag("nav_tab_home")
        )

        NavigationBarItem(
            selected = currentTab == Tab.COURSES,
            onClick = { onTabSelected(Tab.COURSES) },
            icon = {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "Courses"
                )
            },
            label = { Text("Courses", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BentoPremiumText,
                unselectedIconColor = BentoTextSecondary,
                selectedTextColor = BentoPremiumText,
                unselectedTextColor = BentoTextSecondary,
                indicatorColor = Color(0xFFE8DEF8)
            ),
            modifier = Modifier.testTag("nav_tab_courses")
        )

        NavigationBarItem(
            selected = currentTab == Tab.CHAT,
            onClick = { onTabSelected(Tab.CHAT) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "AI Chat"
                )
            },
            label = { Text("AI Chat", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BentoPremiumText,
                unselectedIconColor = BentoTextSecondary,
                selectedTextColor = BentoPremiumText,
                unselectedTextColor = BentoTextSecondary,
                indicatorColor = Color(0xFFE8DEF8)
            ),
            modifier = Modifier.testTag("nav_tab_chat")
        )

        NavigationBarItem(
            selected = currentTab == Tab.PROFILE,
            onClick = { onTabSelected(Tab.PROFILE) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BentoPremiumText,
                unselectedIconColor = BentoTextSecondary,
                selectedTextColor = BentoPremiumText,
                unselectedTextColor = BentoTextSecondary,
                indicatorColor = Color(0xFFE8DEF8)
            ),
            modifier = Modifier.testTag("nav_tab_profile")
        )
    }
}
