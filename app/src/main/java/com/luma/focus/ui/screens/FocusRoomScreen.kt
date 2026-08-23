package com.luma.focus.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.luma.focus.data.LumaStore
import com.luma.focus.ui.theme.LumaTextPrimary
import com.luma.focus.ui.theme.LumaTextSecondary
import com.luma.focus.ui.theme.wallpaperByName

/**
 * Solo focus room: local camera preview only, nothing is sent anywhere.
 * "Join" is deliberately not multi-user — this app has one user (you) — but the
 * on/off camera and mute UI mirrors what a real room would look like, for your
 * own accountability while you study.
 */
@Composable
fun FocusRoomScreen(accent: Color) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var cameraOn by remember { mutableStateOf(false) }
    var muted by remember { mutableStateOf(true) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var taskLabel by remember { mutableStateOf("Studying") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted; if (granted) cameraOn = true }

    val wallpaper = wallpaperByName(LumaStore.getSelectedWallpaper())

    Column(
        Modifier.fillMaxSize().background(wallpaper.brush).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Focus Room", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = accent)
        Text("Solo session · $taskLabel", color = LumaTextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))

        Box(
            Modifier
                .size(280.dp)
                .background(Color(0xFF000000))
        ) {
            if (cameraOn && hasCameraPermission) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val selector = CameraSelector.DEFAULT_FRONT_CAMERA
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview)
                            } catch (_: Exception) { }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    }
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Camera off", color = LumaTextSecondary)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            FilledIconButton(onClick = {
                if (!cameraOn) {
                    if (hasCameraPermission) cameraOn = true
                    else permissionLauncher.launch(Manifest.permission.CAMERA)
                } else {
                    cameraOn = false
                }
            }) {
                Icon(
                    if (cameraOn) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
                    contentDescription = "Toggle camera"
                )
            }
            FilledIconButton(onClick = { muted = !muted }) {
                Icon(
                    if (muted) Icons.Filled.MicOff else Icons.Filled.Mic,
                    contentDescription = "Toggle mute"
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = { /* UI only: multiplayer is intentionally not implemented */ }, enabled = false) {
            Text("Join a room (coming later)")
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "This is a private, single-user room — nothing is streamed or recorded.",
            color = LumaTextSecondary,
            fontSize = 11.sp
        )
    }
}
