package com.luma.focus.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luma.focus.data.LumaStore
import com.luma.focus.ui.theme.*

@Composable
fun SettingsScreen(accent: Color) {
    val context = LocalContext.current
    var api by remember { mutableStateOf(LumaStore.getApiKey()) }
    var model by remember { mutableStateOf(LumaStore.getAiModel()) }
    var sectionWp by remember { mutableStateOf(LumaStore.getSectionWallpaper("settings")) }
    var showApps by remember { mutableStateOf(false) }

    if (showApps) {
        AppControlScreen(accent) { showApps = false }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Settings", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = accent)
        Text("Make Luma fit the way your brain works.", color = LumaTextSecondary)
        Spacer(Modifier.height(18.dp))

        SettingGroup(
            title = "AI agent",
            description = "AI can analyze focus, tasks, habits and journal data. You supply the API key; Luma does not ship one."
        ) {
            OutlinedTextField(
                value = api,
                onValueChange = { api = it },
                label = { Text("Anthropic API key") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("AI model") },
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
            )
            Button(
                onClick = {
                    LumaStore.setApiKey(api)
                    LumaStore.setAiModel(model)
                },
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                modifier = Modifier.padding(top = 6.dp)
            ) { Text("Save AI settings") }
        }

        SettingGroup(
            title = "Focus",
            description = "Choose the visual and audio identity of focus sessions."
        ) {
            Text("Default wallpaper", color = LumaTextSecondary)
            Row {
                LumaWallpapers.take(4).forEach { wallpaper ->
                    FilterChip(
                        selected = LumaStore.getSelectedWallpaper() == wallpaper.name,
                        onClick = { LumaStore.setSelectedWallpaper(wallpaper.name) },
                        label = { Text(wallpaper.name) },
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }
            Text(
                "Current sound: ${LumaStore.getSelectedSound()}",
                color = LumaTextSecondary,
                fontSize = 12.sp
            )
        }

        SettingGroup(
            title = "App blocking & usage",
            description = "Block distracting apps and see Android usage time."
        ) {
            Button(
                onClick = { showApps = true },
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) { Text("Manage apps") }
            OutlinedButton(
                onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                modifier = Modifier.padding(top = 6.dp)
            ) { Text("Enable Luma Accessibility") }
            OutlinedButton(
                onClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) },
                modifier = Modifier.padding(top = 6.dp)
            ) { Text("Enable Usage Access") }
        }

        SettingGroup(
            title = "Reminders",
            description = "Tasks support scheduled alarms with selectable sounds."
        ) {
            Text(
                "Reminder sounds: ${LumaAlarmSounds.joinToString()}",
                color = LumaTextSecondary,
                fontSize = 12.sp
            )
        }

        SettingGroup(
            title = "Section style",
            description = "Each area can have its own wallpaper identity."
        ) {
            LazyStylePicker(
                current = sectionWp,
                onSelect = {
                    sectionWp = it
                    LumaStore.setSectionWallpaper("settings", it)
                }
            )
        }

        SettingGroup(
            title = "Permissions",
            description = "Android requires a few permissions for notifications, camera, alarms, accessibility and usage statistics."
        ) {
            Text(
                "You can change these any time from Android Settings.",
                color = LumaTextSecondary,
                fontSize = 12.sp
            )
        }

        Text(
            "Luma 1.1 • personal focus companion",
            color = LumaTextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 18.dp)
        )
    }
}

@Composable
private fun SettingGroup(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(title, fontWeight = FontWeight.Bold)
        Text(
            description,
            color = LumaTextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 7.dp)
        )
        content()
    }
}

@Composable
private fun LazyStylePicker(
    current: String,
    onSelect: (String) -> Unit
) {
    Row {
        LumaWallpapers.take(5).forEach { wallpaper ->
            FilterChip(
                selected = current == wallpaper.name,
                onClick = { onSelect(wallpaper.name) },
                label = { Text(wallpaper.name) },
                modifier = Modifier.padding(2.dp)
            )
        }
    }
}
