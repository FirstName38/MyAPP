package com.luma.focus.ui.screens

import android.content.Intent
import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luma.focus.R
import com.luma.focus.data.LumaStore
import com.luma.focus.service.FocusService
import com.luma.focus.ui.theme.*
import kotlinx.coroutines.delay

private fun soundRes(name:String):Int=when(name){"Rain"->R.raw.rain;"LoFi"->R.raw.lofi;"WhiteNoise"->R.raw.whitenoise;"Forest"->R.raw.forest;"Chime"->R.raw.chime;"Soft"->R.raw.soft;"DeepFocus"->R.raw.deepfocus;else->0}

@Composable
fun FocusScreen(accent: Color) {
    val context=LocalContext.current
    var mode by remember{mutableStateOf("Stopwatch")}; var isRunning by remember{mutableStateOf(false)}; var seconds by remember{mutableStateOf(0)}
    var targetMinutes by remember{mutableStateOf(25)}; var breakMinutes by remember{mutableStateOf(5)}
    var taskLabel by remember{mutableStateOf("General focus")}; var showTaskPicker by remember{mutableStateOf(false)}
    var wallpaperName by remember{mutableStateOf(LumaStore.getSelectedWallpaper())}; var soundName by remember{mutableStateOf(LumaStore.getSelectedSound())}
    var player by remember{mutableStateOf<MediaPlayer?>(null)}
    val wallpaper=wallpaperByName(wallpaperName)

    DisposableEffect(Unit){onDispose{player?.release()}}
    LaunchedEffect(isRunning,mode,targetMinutes){if(isRunning){while(true){delay(1000);if(mode=="Stopwatch")seconds++ else if(seconds<targetMinutes*60)seconds++ else {isRunning=false;break}}}}
    LaunchedEffect(isRunning,soundName){player?.release();player=null;if(isRunning&&soundName!="Silence"){val id=soundRes(soundName);if(id!=0){player=MediaPlayer.create(context,id);player?.isLooping=true;player?.start()}}}

    Column(Modifier.fillMaxSize().background(wallpaper.brush).padding(18.dp),horizontalAlignment=Alignment.CenterHorizontally){
        Text("Focus",fontSize=28.sp,fontWeight=FontWeight.ExtraBold,color=accent);Spacer(Modifier.height(4.dp))
        OutlinedButton(onClick={showTaskPicker=true}){Text("What are you doing?  •  $taskLabel")}
        Spacer(Modifier.height(12.dp))
        LazyRow{items(listOf("Stopwatch","Standard","ADHD","Custom")){m->FilterChip(selected=mode==m,onClick={mode=m;seconds=0;isRunning=false;if(m=="Standard"){targetMinutes=25;breakMinutes=5};if(m=="ADHD"){targetMinutes=10;breakMinutes=3}},label={Text(m)},modifier=Modifier.padding(3.dp))}}
        if(mode=="Custom"){Text("Focus ${targetMinutes}m  •  Break ${breakMinutes}m",color=LumaTextSecondary);Slider(value=targetMinutes.toFloat(),onValueChange={targetMinutes=it.toInt()},valueRange=5f..90f)}
        Spacer(Modifier.height(18.dp));Text(String.format("%02d:%02d",seconds/60,seconds%60),fontSize=62.sp,fontWeight=FontWeight.ExtraBold,color=LumaTextPrimary);Spacer(Modifier.height(18.dp))
        Row{Button(onClick={isRunning=!isRunning;context.startService(Intent(context,FocusService::class.java).apply{action=if(isRunning)FocusService.ACTION_START else FocusService.ACTION_STOP;putExtra(FocusService.EXTRA_LABEL,"$mode — $taskLabel")})},colors=ButtonDefaults.buttonColors(containerColor=accent)){Text(if(isRunning)"Pause" else "Start")};Spacer(Modifier.width(10.dp));OutlinedButton(onClick={if(seconds>0)LumaStore.addFocusSession(seconds/60,taskLabel,wallpaperName,soundName);isRunning=false;seconds=0;context.startService(Intent(context,FocusService::class.java).apply{action=FocusService.ACTION_STOP})}){Text("Finish")}}
        Spacer(Modifier.height(8.dp));Text("Today: ${LumaStore.todayFocusMinutes()} min",color=LumaTextSecondary)
        Spacer(Modifier.height(18.dp));Text("Focus wallpaper",color=LumaTextSecondary,fontSize=12.sp);LazyRow{items(LumaWallpapers){w->FilterChip(selected=wallpaperName==w.name,onClick={wallpaperName=w.name;LumaStore.setSelectedWallpaper(w.name)},label={Text(w.name)},modifier=Modifier.padding(3.dp))}}
        Text("Focus sound",color=LumaTextSecondary,fontSize=12.sp);LazyRow{items(LumaSounds){s->FilterChip(selected=soundName==s,onClick={soundName=s;LumaStore.setSelectedSound(s)},label={Text(s)},modifier=Modifier.padding(3.dp))}}
    }
    if(showTaskPicker)AlertDialog(onDismissRequest={showTaskPicker=false},title={Text("Choose your task")},text={Column{LumaStore.getTasks().forEach{task->TextButton(onClick={taskLabel=task.title;showTaskPicker=false}){Text(if(task.done)"✓ ${task.title}" else task.title)}};TextButton(onClick={taskLabel="Other";showTaskPicker=false}){Text("Other")}}},confirmButton={TextButton(onClick={showTaskPicker=false}){Text("Close")}})
}
