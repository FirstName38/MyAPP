package com.luma.focus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luma.focus.data.LumaStore
import com.luma.focus.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable fun CalendarScreen(accent:Color){var month by remember{mutableStateOf(Calendar.getInstance())};var selected by remember{mutableStateOf(LumaStore.today())};val fmt=SimpleDateFormat("MMMM yyyy",Locale.US);val keyFmt=SimpleDateFormat("yyyy-MM-dd",Locale.US);val days=month.getActualMaximum(Calendar.DAY_OF_MONTH);val first=Calendar.getInstance().apply{set(month.get(Calendar.YEAR),month.get(Calendar.MONTH),1)};val offset=first.get(Calendar.DAY_OF_WEEK)-1
 Column(Modifier.fillMaxSize().padding(14.dp)){Row(verticalAlignment=Alignment.CenterVertically){IconButton({month=(month.clone() as Calendar).apply{add(Calendar.MONTH,-1)}}){Icon(Icons.Filled.ChevronLeft,"Previous month")};Text(fmt.format(month.time),Modifier.weight(1f),fontSize=23.sp,fontWeight=FontWeight.Bold,color=accent);IconButton({month=(month.clone() as Calendar).apply{add(Calendar.MONTH,1)}}){Icon(Icons.Filled.ChevronRight,"Next month")}}
 Row(Modifier.fillMaxWidth()){listOf("S","M","T","W","T","F","S").forEach{Text(it,Modifier.weight(1f),color=LumaTextSecondary,fontSize=11.sp)}}
 LazyVerticalGrid(columns=GridCells.Fixed(7),modifier=Modifier.height(270.dp)){items(offset){Box(Modifier.height(42.dp))};items(days){i->val d=i+1;val c=Calendar.getInstance().apply{set(month.get(Calendar.YEAR),month.get(Calendar.MONTH),d)};val k=keyFmt.format(c.time);val focus=LumaStore.focusMinutesForDate(k);val task=LumaStore.tasksForDate(k);val pct=LumaStore.habitsForDate(k).map{it.second}.averageOrZero();Box(Modifier.height(48.dp).padding(2.dp).background(if(k==selected)accent else LumaSurfaceElevated).clickable{selected=k},contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Text("$d",color=if(k==selected)Color.Black else LumaTextPrimary);Text(if(focus>0)"${focus}m" else if(task.any{it.done})"✓" else if(pct>0)"${pct.toInt()}%" else "",fontSize=8.sp,color=if(k==selected)Color.Black else LumaTextSecondary)}}}}
 Spacer(Modifier.height(10.dp));DayAnalysis(selected,accent)
 }
}
private fun List<Int>.averageOrZero():Double=if(isEmpty())0.0 else average()

@Composable private fun DayAnalysis(date:String,accent:Color){val tasks=LumaStore.tasksForDate(date);val done=tasks.count{it.done};val habits=LumaStore.habitsForDate(date);val journal=LumaStore.journalForDate(date);val focus=LumaStore.focusMinutesForDate(date);Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())){Text(date,fontSize=20.sp,fontWeight=FontWeight.Bold,color=accent);Spacer(Modifier.height(8.dp));Card{Column(Modifier.padding(14.dp)){Text("Day overview",fontWeight=FontWeight.Bold);Text("Focus ${focus} min • Tasks $done/${tasks.size} • Habits ${(habits.map{it.second}.averageOrZero()).toInt()}%",color=LumaTextSecondary);Spacer(Modifier.height(10.dp));Text("Focus sessions",fontWeight=FontWeight.Bold);LumaStore.focusSessionsForDate(date).forEach{Text("• ${it.minutes} min — ${it.label} — ${it.sound}",color=LumaTextSecondary,fontSize=12.sp)};Text("Tasks",fontWeight=FontWeight.Bold);if(tasks.isEmpty())Text("No tasks",color=LumaTextSecondary) else tasks.forEach{Text(if(it.done)"✓ ${it.title}" else "✗ ${it.title}",color=if(it.done)HabitDone else HabitMissed,fontSize=12.sp)};Text("Habits",fontWeight=FontWeight.Bold);habits.forEach{Text("• ${it.first.title}: ${it.second}%",color=LumaTextSecondary,fontSize=12.sp)};Text("What you learned / journal",fontWeight=FontWeight.Bold);if(journal.isEmpty())Text("No journal entry",color=LumaTextSecondary,fontSize=12.sp) else journal.flatMap{it.sections}.filter{it.content.isNotBlank()}.forEach{Text("${it.label}: ${it.content}",color=LumaTextSecondary,fontSize=12.sp)} }}}
}
