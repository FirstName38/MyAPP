package com.luma.focus.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luma.focus.ai.AiRepository
import com.luma.focus.data.LumaStore
import com.luma.focus.ui.theme.*
import kotlinx.coroutines.launch

@Composable fun AiScreen(accent:Color){val scope=rememberCoroutineScope();val context=androidx.compose.ui.platform.LocalContext.current;var result by remember{mutableStateOf("")};var loading by remember{mutableStateOf(false)};var imageUri by remember{mutableStateOf<android.net.Uri?>(null)};val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){imageUri=it};val date=LumaStore.today();val tasks=LumaStore.tasksForDate(date);val habits=LumaStore.habitsForDate(date);val journal=LumaStore.journalForDate(date);val focus=LumaStore.focusMinutesForDate(date)
 Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){Text("Luma AI",fontSize=28.sp,fontWeight=FontWeight.ExtraBold,color=accent);Text("Your private productivity coach",color=LumaTextSecondary);Spacer(Modifier.height(16.dp));Card{Column(Modifier.padding(16.dp)){Text("Today at a glance",fontWeight=FontWeight.Bold);Text("Focus: $focus min • Tasks: ${tasks.count{it.done}}/${tasks.size} • Habits: ${if(habits.isEmpty())0 else habits.map{it.second}.average().toInt()}%",color=LumaTextSecondary);Spacer(Modifier.height(8.dp));Button(enabled=!loading,onClick={loading=true;scope.launch{val prompt="You are Luma, a supportive ADHD-friendly productivity coach. Analyze this private day data. Give: 1) wins, 2) unfinished work without shaming, 3) focus pattern, 4) habit pattern, 5) journal/learning insight, 6) a tiny plan for tomorrow. Be specific and practical. Date=$date; focus=$focus minutes; tasks=${tasks.joinToString{it.title+if(it.done)"[done]" else "[open]"}}; habits=${habits.joinToString{it.first.title+"="+it.second+"%"}}; journal=${journal.flatMap{it.sections}.joinToString{it.label+":"+it.content}}";val r=AiRepository.analyze(LumaStore.getApiKey(),LumaStore.getAiModel(),prompt);result=r.getOrElse{it.message?:("AI failed")};loading=false}},colors=ButtonDefaults.buttonColors(containerColor=accent)){Text(if(loading)"Thinking…" else "Analyze my day")}}};if(result.isNotBlank()){Spacer(Modifier.height(16.dp));Card{Text(result,Modifier.padding(16.dp),color=LumaTextPrimary)}};Spacer(Modifier.height(16.dp));Button(onClick={picker.launch("image/*")},colors=ButtonDefaults.buttonColors(containerColor=accent)){Text(if(imageUri==null)"Choose image for AI" else "Image selected")};if(imageUri!=null){Spacer(Modifier.height(6.dp));Button(enabled=!loading,onClick={loading=true;scope.launch{val r=AiRepository.analyzeImage(context,LumaStore.getApiKey(),LumaStore.getAiModel(),imageUri!!);result=r.getOrElse{it.message?:"AI failed"};loading=false}},colors=ButtonDefaults.buttonColors(containerColor=accent)){Text("Analyze image")}};Spacer(Modifier.height(16.dp));Text("AI can use your focus, tasks, habits and journal to create patterns and plans. Add your API key in Settings.",color=LumaTextSecondary,fontSize=12.sp)} }
}
