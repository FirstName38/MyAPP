package com.luma.focus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luma.focus.data.Habit
import com.luma.focus.data.LumaStore
import com.luma.focus.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable fun HabitsScreen(accent:Color){var habits by remember{mutableStateOf(LumaStore.getHabits())};var title by remember{mutableStateOf("")};var editing by remember{mutableStateOf<Habit?>(null)};val dates=(0..6).map{Calendar.getInstance().apply{add(Calendar.DAY_OF_YEAR,-it)}};val key=SimpleDateFormat("yyyy-MM-dd",Locale.US);val label=SimpleDateFormat("dd MMM",Locale.US)
 Column(Modifier.fillMaxSize().padding(16.dp)){Text("Habits",fontSize=24.sp,color=accent);Text("Every day is a percentage, not an all-or-nothing test.",color=LumaTextSecondary,fontSize=12.sp);Spacer(Modifier.height(10.dp));Row{OutlinedTextField(title,{title=it},label={Text("Add habit")},modifier=Modifier.weight(1f));Spacer(Modifier.width(8.dp));Button({if(title.isNotBlank()){LumaStore.addHabit(title);habits=LumaStore.getHabits();title=""}},colors=ButtonDefaults.buttonColors(containerColor=accent)){Text("Add")}};Spacer(Modifier.height(12.dp));LazyColumn{items(habits){h->Column(Modifier.fillMaxWidth().padding(vertical=8.dp)){Row{Text(h.title,Modifier.weight(1f));TextButton({editing=h}){Text("Edit")}};Row{dates.reversed().forEach{d->val date=key.format(d.time);val pct=h.doneDates[date]?:0;Column(Modifier.weight(1f)){Text(label.format(d.time),fontSize=8.sp,color=LumaTextSecondary);Box(Modifier.fillMaxWidth().height(22.dp).padding(2.dp).background(if(pct==0)HabitMissed.copy(alpha=.35f) else HabitDone.copy(alpha=pct/100f))){Text("${pct}%",fontSize=8.sp,color=LumaTextPrimary)}}}}}}}}
 }
 editing?.let{habit->HabitEditor(habit,accent,{editing=null;habits=LumaStore.getHabits()})}
}
@Composable private fun HabitEditor(habit:Habit,accent:Color,onClose:()->Unit){var pct by remember{mutableStateOf((habit.doneDates[LumaStore.today()]?:0).toFloat())};AlertDialog(onDismissRequest=onClose,title={Text(habit.title)},text={Column{Text("Today: ${pct.toInt()}%",color=LumaTextSecondary);Slider(pct,{pct=it},valueRange=0f..100f);Row{listOf(0,25,50,75,100).forEach{Button(onClick={pct=it.toFloat()},modifier=Modifier.padding(2.dp)){Text("$it")}}}}},confirmButton={Button({LumaStore.setHabitPercentToday(habit.id,pct.toInt());onClose()},colors=ButtonDefaults.buttonColors(containerColor=accent)){Text("Save")}},dismissButton={TextButton(onClick=onClose){Text("Cancel")}})}
