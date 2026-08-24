package com.luma.focus.ui.screens

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luma.focus.data.LumaStore
import com.luma.focus.data.Task
import com.luma.focus.service.ReminderScheduler
import com.luma.focus.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun dayShift(days:Int):String{val c=Calendar.getInstance();c.add(Calendar.DAY_OF_YEAR,days);return SimpleDateFormat("yyyy-MM-dd",Locale.US).format(c.time)}
private fun humanDay(days:Int)=when(days){-1->"Yesterday";0->"Today";1->"Tomorrow";else->dayShift(days)}

@Composable fun TasksScreen(accent:Color){
 val context=androidx.compose.ui.platform.LocalContext.current;var day by remember{mutableStateOf(0)};var tasks by remember{mutableStateOf(LumaStore.tasksForDate(dayShift(0)))};var title by remember{mutableStateOf("")};var priority by remember{mutableStateOf("Normal")};var label by remember{mutableStateOf("General")};var description by remember{mutableStateOf("")};var detail by remember{mutableStateOf<Task?>(null)};var reminderTask by remember{mutableStateOf<Task?>(null)}
 fun refresh(){tasks=LumaStore.tasksForDate(dayShift(day)).sortedByDescending{when(it.priority){"Ultra urgent"->4;"Urgent"->3;"High"->2;else->1}}}
 LaunchedEffect(day){refresh()}
 Column(Modifier.fillMaxSize().padding(16.dp)){
  Text("Tasks",fontSize=26.sp,fontWeight=FontWeight.Bold,color=accent);Text("Plan • prioritize • review your focus history",color=LumaTextSecondary,fontSize=12.sp)
  Spacer(Modifier.height(8.dp));Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf(-1,0,1).forEach{d->FilterChip(selected=day==d,onClick={day=d},label={Text(humanDay(d))})}}
  Spacer(Modifier.height(10.dp));OutlinedTextField(title,{title=it},label={Text("New task")},modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(5.dp));Row{FilterChip(selected=priority=="Ultra urgent",onClick={priority="Ultra urgent"},label={Text("Ultra urgent")},modifier=Modifier.padding(2.dp));FilterChip(selected=priority=="Urgent",onClick={priority="Urgent"},label={Text("Urgent")},modifier=Modifier.padding(2.dp));FilterChip(selected=priority=="High",onClick={priority="High"},label={Text("High")},modifier=Modifier.padding(2.dp));FilterChip(selected=priority=="Normal",onClick={priority="Normal"},label={Text("Normal")},modifier=Modifier.padding(2.dp))}
  OutlinedTextField(value=label,onValueChange={label=it},label={Text("Label (Study, Work, Personal…)")},modifier=Modifier.fillMaxWidth().padding(top=4.dp));OutlinedTextField(description,{description=it},label={Text("Description (optional)")},modifier=Modifier.fillMaxWidth().padding(top=4.dp),maxLines=2)
  Button(onClick={if(title.isNotBlank()){LumaStore.addTask(title,dayShift(day),priority=priority,label=label,description=description);title="";description="";refresh()}},colors=ButtonDefaults.buttonColors(containerColor=accent),modifier=Modifier.padding(top=5.dp)){Text("Add task")};Spacer(Modifier.height(8.dp))
  Text("${tasks.size} tasks • priority order",fontSize=12.sp,color=LumaTextSecondary)
  LazyColumn{items(tasks){task->Row(Modifier.fillMaxWidth().padding(vertical=5.dp),verticalAlignment=Alignment.CenterVertically){Checkbox(task.done,{LumaStore.toggleTask(task.id);refresh()},colors=CheckboxDefaults.colors(checkedColor=accent));Column(Modifier.weight(1f)){Text(task.title,fontWeight=FontWeight.SemiBold,color=LumaTextPrimary);Text("${task.priority} • ${task.label}",fontSize=11.sp,color=accent);if(task.description.isNotBlank())Text(task.description,fontSize=11.sp,color=LumaTextSecondary)};TextButton(onClick={detail=task}){Text("Details")};IconButton({reminderTask=task}){Icon(Icons.Filled.Notifications,"Reminder")};IconButton({ReminderScheduler.cancel(context,task.id);LumaStore.deleteTask(task.id);refresh()}){Icon(Icons.Filled.Delete,"Delete")}}}}
 }
 detail?.let{task->AlertDialog(onDismissRequest={detail=null},title={Text(task.title)},text={Column{Text("${task.priority} • ${task.label}");Text(task.description.ifBlank{"No description"},modifier=Modifier.padding(top=8.dp));Text("Date: ${task.date}",modifier=Modifier.padding(top=8.dp));val sessions=LumaStore.focusSessionsForDate(task.date).filter{it.label==task.title};Text("Focus linked: ${sessions.sumOf{it.activeSeconds/60}} min • ${sessions.sumOf{it.pausedSeconds}} sec paused",fontSize=12.sp,color=LumaTextSecondary,modifier=Modifier.padding(top=8.dp))}},confirmButton={TextButton(onClick={detail=null}){Text("Close")}})}
 reminderTask?.let{task->ReminderDialog(context,task,{reminderTask=null;refresh()},accent)}
}

@Composable private fun ReminderDialog(context:Context,task:Task,onClose:()->Unit,accent:Color){var sound by remember{mutableStateOf(task.reminderSound)};AlertDialog(onDismissRequest=onClose,title={Text("Reminder: ${task.title}")},text={Column{Text("Choose a reminder sound",color=LumaTextSecondary);LumaAlarmSounds.forEach{s->FilterChip(selected=sound==s,onClick={sound=s},label={Text(s)},modifier=Modifier.padding(2.dp))}}},confirmButton={Button(onClick={val c=Calendar.getInstance();TimePickerDialog(context,{_,h,m->c.set(Calendar.HOUR_OF_DAY,h);c.set(Calendar.MINUTE,m);c.set(Calendar.SECOND,0);LumaStore.updateTaskReminder(task.id,c.timeInMillis,sound);ReminderScheduler.schedule(context,task.id,task.title,c.timeInMillis,sound);onClose()},c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),true).show()},colors=ButtonDefaults.buttonColors(containerColor=accent)){Text("Set reminder")}},dismissButton={TextButton(onClick=onClose){Text("Cancel")}})}
