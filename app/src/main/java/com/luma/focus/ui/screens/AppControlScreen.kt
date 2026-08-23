package com.luma.focus.ui.screens

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luma.focus.data.LumaStore
import com.luma.focus.ui.theme.*

private data class AppUsage(val label:String,val pkg:String,val minutes:Int)
@Composable fun AppControlScreen(accent:Color,onBack:()->Unit){val context=androidx.compose.ui.platform.LocalContext.current;var blocked by remember{mutableStateOf(LumaStore.getBlockedApps())};var apps by remember{mutableStateOf(listOf<AppUsage>())};LaunchedEffect(Unit){apps=loadUsage(context)};Column(Modifier.fillMaxSize().padding(16.dp)){Row{Text("App control",Modifier.weight(1f),fontSize=24.sp,color=accent);TextButton(onClick=onBack){Text("Back")}};Text("Blocked apps are sent home by Luma's Accessibility Service. Usage time comes from Android Usage Access.",color=LumaTextSecondary,fontSize=12.sp);Row{Button(onClick={context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))},colors=ButtonDefaults.buttonColors(containerColor=accent)){Text("Usage access")};Spacer(Modifier.width(8.dp));OutlinedButton(onClick={context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))}){Text("Accessibility")}};Spacer(Modifier.height(10.dp));LazyColumn{items(apps){app->val isBlocked=app.pkg in blocked;ListItem(headlineContent={Text(app.label)},supportingContent={Text("${app.minutes} min • ${app.pkg}",fontSize=11.sp)},trailingContent={Switch(isBlocked,{blocked=if(it)blocked+app.pkg else blocked-app.pkg;LumaStore.setBlockedApps(blocked)})})}}}}
private fun loadUsage(context:Context):List<AppUsage>{val pm=context.packageManager;val now=System.currentTimeMillis();val start=now-24*60*60*1000L;val usm=context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager;val stats=usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,start,now).orEmpty().filter{it.totalTimeInForeground>0};return stats.mapNotNull{try{val ai=pm.getApplicationInfo(it.packageName,0);AppUsage(pm.getApplicationLabel(ai).toString(),it.packageName,(it.totalTimeInForeground/60000).toInt())}catch(_:Exception){null}}.distinctBy{it.pkg}.sortedByDescending{it.minutes}.take(60)}
