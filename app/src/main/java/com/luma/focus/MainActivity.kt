package com.luma.focus

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.luma.focus.data.LumaStore
import com.luma.focus.ui.screens.*
import com.luma.focus.ui.theme.*

enum class Screen { FOCUS, TASKS, HABITS, JOURNAL, CALENDAR, FOCUS_ROOM, AI, SETTINGS }

class MainActivity : ComponentActivity() {

    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LumaStore.init(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            LumaApp()
        }
    }
}

@Composable
fun LumaApp() {
    var screen by remember { mutableStateOf(Screen.FOCUS) }

    val accent = when (screen) {
        Screen.FOCUS -> FocusAccent
        Screen.TASKS -> TasksAccent
        Screen.HABITS -> HabitsAccent
        Screen.JOURNAL -> JournalAccent
        Screen.CALENDAR -> CalendarAccent
        Screen.FOCUS_ROOM -> FocusRoomAccent
        Screen.AI -> FocusAccent
        Screen.SETTINGS -> SettingsAccent
    }

    LumaTheme(accentColor = accent) {
        Scaffold(
            containerColor = LumaBackground,
            bottomBar = {
                NavigationBar(containerColor = LumaSurface) {
                    NavigationBarItem(
                        selected = screen == Screen.FOCUS,
                        onClick = { screen = Screen.FOCUS },
                        icon = { Icon(Icons.Filled.PlayArrow, contentDescription = "Focus") },
                        label = { Text("Focus") }
                    )
                    NavigationBarItem(
                        selected = screen == Screen.TASKS,
                        onClick = { screen = Screen.TASKS },
                        icon = { Icon(Icons.Filled.Check, contentDescription = "Tasks") },
                        label = { Text("Tasks") }
                    )
                    NavigationBarItem(
                        selected = screen == Screen.HABITS,
                        onClick = { screen = Screen.HABITS },
                        icon = { Icon(Icons.Filled.Star, contentDescription = "Habits") },
                        label = { Text("Habits") }
                    )
                    NavigationBarItem(
                        selected = screen == Screen.JOURNAL,
                        onClick = { screen = Screen.JOURNAL },
                        icon = { Icon(Icons.Filled.Edit, contentDescription = "Journal") },
                        label = { Text("Journal") }
                    )
                    NavigationBarItem(
                        selected = screen == Screen.CALENDAR,
                        onClick = { screen = Screen.CALENDAR },
                        icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = "Calendar") },
                        label = { Text("Calendar") }
                    )
                    NavigationBarItem(
                        selected = screen == Screen.FOCUS_ROOM,
                        onClick = { screen = Screen.FOCUS_ROOM },
                        icon = { Icon(Icons.Filled.Videocam, contentDescription = "Focus Room") },
                        label = { Text("Room") }
                    )
                    NavigationBarItem(
                        selected = screen == Screen.AI,
                        onClick = { screen = Screen.AI },
                        icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = "AI") },
                        label = { Text("AI") }
                    )
                    NavigationBarItem(
                        selected = screen == Screen.SETTINGS,
                        onClick = { screen = Screen.SETTINGS },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") }
                    )
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                when (screen) {
                    Screen.FOCUS -> FocusScreen(accent)
                    Screen.TASKS -> TasksScreen(accent)
                    Screen.HABITS -> HabitsScreen(accent)
                    Screen.JOURNAL -> JournalScreen(accent)
                    Screen.CALENDAR -> CalendarScreen(accent)
                    Screen.FOCUS_ROOM -> FocusRoomScreen(accent)
                    Screen.AI -> AiScreen(accent)
                    Screen.SETTINGS -> SettingsScreen(accent)
                }
            }
        }
    }
}
