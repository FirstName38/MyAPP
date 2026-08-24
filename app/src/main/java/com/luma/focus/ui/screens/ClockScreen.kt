package com.luma.focus.ui.screens

import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luma.focus.data.LumaStore
import com.luma.focus.service.ReminderScheduler
import com.luma.focus.ui.theme.LumaTextPrimary
import com.luma.focus.ui.theme.LumaTextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ClockScreen(accent: Color) {
    val context=LocalContext.current
    var tab by remember { mutableStateOf("Timer") }
    var timerMinutes by remember { mutableStateOf(5) }
    var timerSeconds by remember { mutableStateOf(timerMinutes*60L) }
    var timerRunning by remember { mutableStateOf(false) }
    var stopwatch by remember { mutableStateOf(0L) }
    var stopwatchRunning by remember { mutableStateOf(false) }
    var alarmLabel by remember { mutableStateOf("Luma alarm") }
    var alarmSet by remember { mutableStateOf(false) }
    var alarmText by remember { mutableStateOf("") }

    LaunchedEffect(timerRunning){ while(timerRunning){ delay(1000); if(timerSeconds>0) timerSeconds-- else timerRunning=false } }
    LaunchedEffect(stopwatchRunning){ while(stopwatchRunning){ delay(1000); stopwatch++ } }

    fun fmt(total:Long)=String.format(Locale.US,"%02d:%02d:%02d",total/3600,(total/60)%60,total%60)
    fun scheduleAlarm(){
        val c=Calendar.getInstance()
        TimePickerDialog(context,{_,h,m->
            c.set(Calendar.HOUR_OF_DAY,h); c.set(Calendar.MINUTE,m); c.set(Calendar.SECOND,0); c.set(Calendar.MILLISECOND,0)
            if(c.timeInMillis<=System.currentTimeMillis()) c.add(Calendar.DAY_OF_YEAR,1)
            ReminderScheduler.schedule(context,"clock_alarm",alarmLabel,c.timeInMillis,LumaStore.getAlarmSound())
            alarmSet=true; alarmText=SimpleDateFormat("EEE, HH:mm",Locale.US).format(c.time)
        },c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),true).show()
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Clock",fontSize=28.sp,fontWeight=FontWeight.ExtraBold,color=accent)
        Text("Timer • Stopwatch • Alarm",color=LumaTextSecondary,fontSize=12.sp)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) { listOf("Timer","Stopwatch","Alarm").forEach{t->FilterChip(selected=tab==t,onClick={tab=t},label={Text(t)})} }
        Spacer(Modifier.height(16.dp))
        when(tab){
            "Timer" -> {
                Text("Countdown",fontWeight=FontWeight.Bold,color=LumaTextPrimary)
                Text(fmt(timerSeconds),fontSize=58.sp,fontWeight=FontWeight.ExtraBold,color=accent,modifier=Modifier.align(Alignment.CenterHorizontally))
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.align(Alignment.CenterHorizontally)){
                    Button(onClick={timerRunning=!timerRunning}){Text(if(timerRunning)"Pause" else if(timerSeconds<timerMinutes*60)"Resume" else "Start")}
                    OutlinedButton(onClick={timerRunning=false;timerSeconds=timerMinutes*60L}){Text("Reset")}
                }
                Spacer(Modifier.height(12.dp));Text("Minutes: $timerMinutes",color=LumaTextSecondary)
                Slider(value=timerMinutes.toFloat(),onValueChange={timerMinutes=it.toInt().coerceIn(1,180);if(!timerRunning)timerSeconds=timerMinutes*60L},valueRange=1f..180f)
            }
            "Stopwatch" -> {
                Text("Stopwatch",fontWeight=FontWeight.Bold,color=LumaTextPrimary)
                Text(fmt(stopwatch),fontSize=58.sp,fontWeight=FontWeight.ExtraBold,color=accent,modifier=Modifier.align(Alignment.CenterHorizontally))
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.align(Alignment.CenterHorizontally)){
                    Button(onClick={stopwatchRunning=!stopwatchRunning}){Text(if(stopwatchRunning)"Pause" else if(stopwatch>0)"Resume" else "Start")}
                    OutlinedButton(onClick={stopwatchRunning=false;stopwatch=0}){Text("Reset")}
                }
            }
            else -> {
                Text("Alarm",fontWeight=FontWeight.Bold,color=LumaTextPrimary)
                OutlinedTextField(alarmLabel,{alarmLabel=it},label={Text("Alarm label")},modifier=Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Button(onClick={scheduleAlarm()},modifier=Modifier.align(Alignment.CenterHorizontally)){Text("Set alarm")}
                if(alarmSet){Text("Next alarm: $alarmText",color=accent,modifier=Modifier.padding(top=10.dp));TextButton(onClick={ReminderScheduler.cancel(context,"clock_alarm");alarmSet=false;alarmText=""}){Text("Cancel alarm")}}
                Spacer(Modifier.height(10.dp))
                val am=context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if(android.os.Build.VERSION.SDK_INT>=31 && !am.canScheduleExactAlarms()) TextButton(onClick={context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))}){Text("Allow exact alarms in Android Settings")}
                Text("Alarm uses the sound selected in Settings.",color=LumaTextSecondary,fontSize=11.sp)
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Clock tools are local to this device; no account is required.",color=LumaTextSecondary,fontSize=11.sp)
    }
}
