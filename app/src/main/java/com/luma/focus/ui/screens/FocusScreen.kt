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
import com.luma.focus.data.FocusSession
import com.luma.focus.data.LumaStore
import com.luma.focus.data.PauseRecord
import com.luma.focus.service.FocusService
import com.luma.focus.ui.theme.*
import kotlinx.coroutines.delay

private fun soundRes(name:String):Int=when(name){"Rain"->R.raw.rain;"LoFi"->R.raw.lofi;"WhiteNoise"->R.raw.whitenoise;"Forest"->R.raw.forest;"Chime"->R.raw.chime;"Soft"->R.raw.soft;"DeepFocus"->R.raw.deepfocus;else->0}
private fun fmt(s:Long)=String.format("%02d:%02d",s/60,s%60)

@Composable
fun FocusScreen(accent: Color) {
    val context=LocalContext.current
    var mode by remember{mutableStateOf("Standard")}
    var phase by remember{mutableStateOf("Focus")}
    var running by remember{mutableStateOf(false)}
    var elapsed by remember{mutableStateOf(0L)}
    var target by remember{mutableStateOf(25*60L)}
    var breakTarget by remember{mutableStateOf(5*60L)}
    var taskLabel by remember{mutableStateOf("General focus")}
    var showTaskPicker by remember{mutableStateOf(false)}
    var wallpaperName by remember{mutableStateOf(LumaStore.getSelectedWallpaper())}
    var focusSound by remember{mutableStateOf(LumaStore.getSelectedSound())}
    var breakSound by remember{mutableStateOf(LumaStore.getBreakSound())}
    var player by remember{mutableStateOf<MediaPlayer?>(null)}
    var sessionStartedAt by remember{mutableStateOf(0L)}
    var pauseStartedAt by remember{mutableStateOf<Long?>(null)}
    var pauses by remember{mutableStateOf(emptyList<PauseRecord>())}
    var showHistory by remember{mutableStateOf(false)}
    val wallpaper=wallpaperByName(wallpaperName)
    val limit=if(phase=="Focus")target else breakTarget

    DisposableEffect(Unit){onDispose{player?.release()}}
    LaunchedEffect(running,phase,target,breakTarget){
        while(running){
            delay(1000)
            elapsed += 1
            if(phase!="Stopwatch" && elapsed>=limit){
                val now=System.currentTimeMillis()
                val finalPauses=if(pauseStartedAt!=null) pauses+(PauseRecord(pauseStartedAt!!,now,(now-pauseStartedAt!!)/1000)) else pauses
                if(phase=="Focus" && sessionStartedAt>0L){
                    LumaStore.addFocusSession(FocusSession(java.util.UUID.randomUUID().toString(),LumaStore.today(),0,taskLabel,wallpaperName,focusSound,mode,phase,sessionStartedAt,now,limit,finalPauses.sumOf{it.durationSeconds},finalPauses.size,finalPauses,target))
                }
                running=false
                if(phase=="Focus"){phase="Short Break";elapsed=0;sessionStartedAt=0L;pauseStartedAt=null;pauses=emptyList()} else {phase="Focus";elapsed=0}
            }
        }
    }
    LaunchedEffect(running,phase,focusSound,breakSound){
        player?.release();player=null
        if(running){val name=if(phase=="Focus")focusSound else breakSound;val id=soundRes(name);if(id!=0){player=MediaPlayer.create(context,id);player?.isLooping=true;player?.start()}}
    }

    fun begin(){
        if(!running){
            if(sessionStartedAt==0L) sessionStartedAt=System.currentTimeMillis()
            pauseStartedAt?.let{start->pauses=pauses+(PauseRecord(start,System.currentTimeMillis(),(System.currentTimeMillis()-start)/1000));pauseStartedAt=null}
            running=true
            context.startService(Intent(context,FocusService::class.java).apply{action=FocusService.ACTION_START;putExtra(FocusService.EXTRA_LABEL,"$phase • $taskLabel")})
        }
    }
    fun pause(){
        if(running){running=false;pauseStartedAt=System.currentTimeMillis();player?.pause();context.startService(Intent(context,FocusService::class.java).apply{action=FocusService.ACTION_STOP})}
    }
    fun finish(){
        val now=System.currentTimeMillis();val finalPauses=if(pauseStartedAt!=null)pauses+(PauseRecord(pauseStartedAt!!,now,(now-pauseStartedAt!!)/1000)) else pauses
        if(sessionStartedAt>0 && elapsed>0 && phase=="Focus") LumaStore.addFocusSession(FocusSession(java.util.UUID.randomUUID().toString(),LumaStore.today(),0,taskLabel,wallpaperName,focusSound,mode,phase,sessionStartedAt,now,elapsed,finalPauses.sumOf{it.durationSeconds},finalPauses.size,finalPauses,target))
        running=false;elapsed=0;sessionStartedAt=0;pauseStartedAt=null;pauses=emptyList();phase="Focus";context.startService(Intent(context,FocusService::class.java).apply{action=FocusService.ACTION_STOP})
    }

    Column(Modifier.fillMaxSize().background(wallpaper.brush).padding(18.dp),horizontalAlignment=Alignment.CenterHorizontally){
        Text("Focus",fontSize=29.sp,fontWeight=FontWeight.ExtraBold,color=accent)
        Text("$phase • $taskLabel",fontSize=13.sp,color=LumaTextSecondary)
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick={showTaskPicker=true}){Text("Task  •  $taskLabel")}
        Spacer(Modifier.height(10.dp))
        LazyRow{items(listOf("Standard","ADHD","Custom","Stopwatch")){m->FilterChip(selected=mode==m,onClick={if(!running){mode=m;phase=if(m=="Stopwatch")"Stopwatch" else "Focus";elapsed=0;when(m){"Standard"->{target=25*60;breakTarget=5*60};"ADHD"->{target=10*60;breakTarget=3*60}}}},label={Text(m)},modifier=Modifier.padding(3.dp))}}
        if(mode=="Custom"&&!running){Text("Focus ${target/60}m  •  Break ${breakTarget/60}m",color=LumaTextSecondary);Slider(value=target/60f,onValueChange={target=it.toLong()*60},valueRange=5f..120f);Slider(value=breakTarget/60f,onValueChange={breakTarget=it.toLong()*60},valueRange=1f..30f)}
        Spacer(Modifier.height(12.dp))
        Text(if(phase=="Stopwatch")fmt(elapsed) else fmt((limit-elapsed).coerceAtLeast(0)),fontSize=62.sp,fontWeight=FontWeight.ExtraBold,color=LumaTextPrimary)
        if(!running&&pauseStartedAt!=null)Text("Paused • ${pauses.sumOf{it.durationSeconds} + ((System.currentTimeMillis()-pauseStartedAt!!)/1000)}s total",color=LumaTextSecondary,fontSize=12.sp)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
            Button(onClick={if(running)pause() else begin()},colors=ButtonDefaults.buttonColors(containerColor=accent)){Text(if(running)"Pause" else if(sessionStartedAt>0)"Resume" else "Start")}
            OutlinedButton(onClick={if(sessionStartedAt>0)finish()}){Text("Finish")}
            if(mode!="Stopwatch")OutlinedButton(onClick={if(!running){phase=if(phase=="Focus")"Short Break" else "Focus";elapsed=0}}){Text(if(phase=="Focus")"Break" else "Focus")}
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Text("Focus sound",fontSize=11.sp,color=LumaTextSecondary);Text(focusSound,fontSize=11.sp);Text("• Break",fontSize=11.sp,color=LumaTextSecondary);Text(breakSound,fontSize=11.sp)}
        LazyRow{items(LumaSounds){s->FilterChip(selected=focusSound==s,onClick={focusSound=s;LumaStore.setSelectedSound(s)},label={Text(s)},modifier=Modifier.padding(2.dp))}}
        LazyRow{items(LumaBreakSounds){s->FilterChip(selected=breakSound==s,onClick={breakSound=s;LumaStore.setBreakSound(s)},label={Text("B: $s")},modifier=Modifier.padding(2.dp))}}
        Spacer(Modifier.height(6.dp));Text("Today: ${LumaStore.todayFocusMinutes()} min",color=LumaTextSecondary)
        TextButton(onClick={showHistory=true}){Text("View session & pause history")}
        LazyRow{items(LumaWallpapers){w->FilterChip(selected=wallpaperName==w.name,onClick={wallpaperName=w.name;LumaStore.setSelectedWallpaper(w.name)},label={Text(w.name)},modifier=Modifier.padding(2.dp))}}
    }
    if(showTaskPicker)AlertDialog(onDismissRequest={showTaskPicker=false},title={Text("Choose your task")},text={Column{LumaStore.getTasks().filter{it.date==LumaStore.today()}.forEach{task->TextButton(onClick={taskLabel=task.title;showTaskPicker=false}){Text("${task.priority} • ${task.title}")}}}},confirmButton={TextButton(onClick={showTaskPicker=false}){Text("Close")}})
    if(showHistory)AlertDialog(onDismissRequest={showHistory=false},title={Text("Focus history")},text={Column{LumaStore.focusSessionsForDate(LumaStore.today()).takeLast(8).reversed().forEach{s->Text("${s.label} • ${s.activeSeconds/60}m active • ${s.pausedSeconds}s paused • ${s.pauseCount} pauses",fontSize=12.sp,modifier=Modifier.padding(vertical=4.dp))}}},confirmButton={TextButton(onClick={showHistory=false}){Text("Close")}})
}
