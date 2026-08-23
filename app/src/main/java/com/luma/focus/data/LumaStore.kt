package com.luma.focus.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class Task(
    val id: String,
    val title: String,
    val done: Boolean,
    val date: String,
    val reminderTime: Long? = null,
    val reminderSound: String = "Chime"
)
data class Habit(val id: String, val title: String, val doneDates: Map<String, Int>)
data class JournalSection(val label: String, val content: String)
data class JournalEntry(val id: String, val dateTime: String, val date: String, val sections: List<JournalSection>, val imageUris: List<String>)
data class FocusSession(val id: String, val date: String, val minutes: Int, val label: String, val wallpaper: String, val sound: String)

object LumaStore {
    private lateinit var prefs: SharedPreferences
    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val dateTimeFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
    fun init(context: Context) { if (!::prefs.isInitialized) prefs = context.applicationContext.getSharedPreferences("luma_prefs", Context.MODE_PRIVATE) }
    fun today() = dateFmt.format(Date())
    fun now() = dateTimeFmt.format(Date())

    fun getTasks(): List<Task> {
        val arr = JSONArray(prefs.getString("tasks", "[]"))
        return (0 until arr.length()).map { val o=arr.getJSONObject(it); Task(o.getString("id"),o.getString("title"),o.getBoolean("done"),o.optString("date",today()),if(o.has("reminderTime")&&!o.isNull("reminderTime"))o.getLong("reminderTime") else null,o.optString("reminderSound","Chime")) }
    }
    private fun saveTasks(tasks: List<Task>) { val arr=JSONArray(); tasks.forEach{val o=JSONObject();o.put("id",it.id);o.put("title",it.title);o.put("done",it.done);o.put("date",it.date);if(it.reminderTime!=null)o.put("reminderTime",it.reminderTime);o.put("reminderSound",it.reminderSound);arr.put(o)};prefs.edit().putString("tasks",arr.toString()).apply() }
    fun addTask(title:String, date:String=today(), reminderTime:Long?=null, reminderSound:String="Chime") { saveTasks(getTasks()+Task(UUID.randomUUID().toString(),title,false,date,reminderTime,reminderSound)) }
    fun updateTaskReminder(id:String,time:Long?,sound:String){saveTasks(getTasks().map{if(it.id==id)it.copy(reminderTime=time,reminderSound=sound)else it})}
    fun toggleTask(id:String){saveTasks(getTasks().map{if(it.id==id)it.copy(done=!it.done)else it})}
    fun deleteTask(id:String){saveTasks(getTasks().filterNot{it.id==id})}
    fun tasksForDate(date:String)=getTasks().filter{it.date==date}

    fun getHabits():List<Habit>{val arr=JSONArray(prefs.getString("habits","[]"));return(0 until arr.length()).map{val o=arr.getJSONObject(it);val map=mutableMapOf<String,Int>();val d=o.optJSONObject("doneDates");if(d!=null)d.keys().forEach{k->map[k]=d.getInt(k)};Habit(o.getString("id"),o.getString("title"),map)}}
    private fun saveHabits(h:List<Habit>){val arr=JSONArray();h.forEach{val o=JSONObject();o.put("id",it.id);o.put("title",it.title);val d=JSONObject();it.doneDates.forEach{k,v->d.put(k,v)};o.put("doneDates",d);arr.put(o)};prefs.edit().putString("habits",arr.toString()).apply()}
    fun addHabit(title:String){saveHabits(getHabits()+Habit(UUID.randomUUID().toString(),title,emptyMap()))}
    fun setHabitPercent(id:String,date:String,percent:Int){saveHabits(getHabits().map{if(it.id==id)it.copy(doneDates=it.doneDates+(date to percent.coerceIn(0,100)))else it})}
    fun setHabitPercentToday(id:String,percent:Int)=setHabitPercent(id,today(),percent)
    fun deleteHabit(id:String){saveHabits(getHabits().filterNot{it.id==id})}
    fun habitsForDate(date:String)=getHabits().map{it to(it.doneDates[date]?:0)}

    fun getJournalEntries():List<JournalEntry>{val arr=JSONArray(prefs.getString("journal","[]"));return(0 until arr.length()).map{val o=arr.getJSONObject(it);val s=mutableListOf<JournalSection>();val sa=o.optJSONArray("sections")?:JSONArray();for(i in 0 until sa.length()){val x=sa.getJSONObject(i);s.add(JournalSection(x.getString("label"),x.getString("content")))};val imgs=mutableListOf<String>();val ia=o.optJSONArray("imageUris")?:JSONArray();for(i in 0 until ia.length())imgs.add(ia.getString(i));JournalEntry(o.getString("id"),o.getString("dateTime"),o.optString("date",o.getString("dateTime").take(10)),s,imgs)}.sortedByDescending{it.dateTime}}
    private fun saveJournal(e:List<JournalEntry>){val arr=JSONArray();e.forEach{val o=JSONObject();o.put("id",it.id);o.put("dateTime",it.dateTime);o.put("date",it.date);val sa=JSONArray();it.sections.forEach{s->val x=JSONObject();x.put("label",s.label);x.put("content",s.content);sa.put(x)};o.put("sections",sa);val ia=JSONArray();it.imageUris.forEach{ia.put(it)};o.put("imageUris",ia);arr.put(o)};prefs.edit().putString("journal",arr.toString()).apply()}
    fun addJournalEntry(sections:List<JournalSection>,imageUris:List<String>){saveJournal(getJournalEntries()+JournalEntry(UUID.randomUUID().toString(),now(),today(),sections,imageUris))}
    fun deleteJournalEntry(id:String){saveJournal(getJournalEntries().filterNot{it.id==id})}
    fun journalForDate(date:String)=getJournalEntries().filter{it.date==date}

    fun getFocusSessions():List<FocusSession>{val arr=JSONArray(prefs.getString("focus_sessions","[]"));return(0 until arr.length()).map{val o=arr.getJSONObject(it);FocusSession(o.getString("id"),o.getString("date"),o.getInt("minutes"),o.getString("label"),o.optString("wallpaper","Nebula"),o.optString("sound","Silence"))}}
    private fun saveFocusSessions(s:List<FocusSession>){val arr=JSONArray();s.forEach{val o=JSONObject();o.put("id",it.id);o.put("date",it.date);o.put("minutes",it.minutes);o.put("label",it.label);o.put("wallpaper",it.wallpaper);o.put("sound",it.sound);arr.put(o)};prefs.edit().putString("focus_sessions",arr.toString()).apply()}
    fun addFocusSession(minutes:Int,label:String,wallpaper:String,sound:String,date:String=today()){if(minutes<=0)return;saveFocusSessions(getFocusSessions()+FocusSession(UUID.randomUUID().toString(),date,minutes,label,wallpaper,sound))}
    fun todayFocusMinutes()=focusMinutesForDate(today())
    fun focusMinutesForDate(date:String)=getFocusSessions().filter{it.date==date}.sumOf{it.minutes}
    fun focusSessionsForDate(date:String)=getFocusSessions().filter{it.date==date}

    fun getBlockedApps(): Set<String> = prefs.getStringSet("blocked_apps", emptySet()) ?: emptySet()
    fun setBlockedApps(p:Set<String>){prefs.edit().putStringSet("blocked_apps",p).apply()}
    fun getApiKey()=prefs.getString("ai_api_key","")?:""
    fun setApiKey(k:String){prefs.edit().putString("ai_api_key",k).apply()}
    fun getAiModel()=prefs.getString("ai_model","claude-sonnet-4-20250514")?:"claude-sonnet-4-20250514"
    fun setAiModel(v:String){prefs.edit().putString("ai_model",v).apply()}
    fun getSelectedWallpaper()=prefs.getString("selected_wallpaper","Nebula")?:"Nebula"
    fun setSelectedWallpaper(n:String){prefs.edit().putString("selected_wallpaper",n).apply()}
    fun getSelectedSound()=prefs.getString("selected_sound","Silence")?:"Silence"
    fun setSelectedSound(n:String){prefs.edit().putString("selected_sound",n).apply()}
    fun getSectionWallpaper(section:String)=prefs.getString("wallpaper_$section","")?:""
    fun setSectionWallpaper(section:String,name:String){prefs.edit().putString("wallpaper_$section",name).apply()}
}
