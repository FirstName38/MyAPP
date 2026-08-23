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
import java.util.Calendar

@Composable fun TasksScreen(accent:Color){
 val context=androidx.compose.ui.platform.LocalContext.current;var tasks by remember{mutableStateOf(LumaStore.getTasks())};var title by remember{mutableStateOf("")};var reminderTask by remember{mutableStateOf<Task?>(null)}
 Column(Modifier.fillMaxSize().padding(16.dp)){Text("Tasks",fontSize=24.sp,fontWeight=FontWeight.Bold,color=accent);Text("Plan it. Do it. See it again in Calendar.",color=LumaTextSecondary,fontSize=12.sp);Spacer(Modifier.height(10.dp));Row{OutlinedTextField(title,{title=it},label={Text("New task")},modifier=Modifier.weight(1f));Spacer(Modifier.width(8.dp));Button(onClick={if(title.isNotBlank()){LumaStore.addTask(title);tasks=LumaStore.getTasks();title=""}},colors=ButtonDefaults.buttonColors(containerColor=accent)){Text("Add")}}
 Spacer(Modifier.height(12.dp));LazyColumn{items(tasks){task->Row(Modifier.fillMaxWidth().padding(vertical=5.dp),verticalAlignment=Alignment.CenterVertically){Checkbox(task.done,{LumaStore.toggleTask(task.id);tasks=LumaStore.getTasks()},colors=CheckboxDefaults.colors(checkedColor=accent));Column(Modifier.weight(1f)){Text(task.title,color=LumaTextPrimary);task.reminderTime?.let{Text("Reminder ${java.text.SimpleDateFormat("HH:mm").format(java.util.Date(it))} • ${task.reminderSound}",color=LumaTextSecondary,fontSize=11.sp)}};IconButton({reminderTask=task}){Icon(Icons.Filled.Notifications,"Reminder")};IconButton({ReminderScheduler.cancel(context,task.id);LumaStore.deleteTask(task.id);tasks=LumaStore.getTasks()}){Icon(Icons.Filled.Delete,"Delete")}}}}
 }
 reminderTask?.let{task->ReminderDialog(context,task,{reminderTask=null;tasks=LumaStore.getTasks()},accent)}
}

@Composable private fun ReminderDialog(context:Context,task:Task,onClose:()->Unit,accent:Color){var sound by remember{mutableStateOf(task.reminderSound)};AlertDialog(onDismissRequest=onClose,title={Text("Reminder: ${task.title}")},text={Column{Text("Choose a reminder sound",color=LumaTextSecondary);LazyColumn(Modifier.heightIn(max=180.dp)){items(LumaAlarmSounds){s->FilterChip(selected=sound==s,onClick={sound=s},label={Text(s)},modifier=Modifier.padding(2.dp))}}}},confirmButton={Button(onClick={val c=Calendar.getInstance();TimePickerDialog(context,{_,h,m->c.set(Calendar.HOUR_OF_DAY,h);c.set(Calendar.MINUTE,m);c.set(Calendar.SECOND,0);val trigger=if(c.timeInMillis<System.currentTimeMillis())c.timeInMillis+24*60*60*1000L else c.timeInMillis;LumaStore.updateTaskReminder(task.id,trigger,sound);ReminderScheduler.schedule(context,task.id,task.title,trigger,sound);onClose()},c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),false).show()},colors=ButtonDefaults.buttonColors(containerColor=accent)){Text("Set time")}},dismissButton={TextButton(onClick=onClose){Text("Cancel")}})}
