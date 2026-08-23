package com.luma.focus.ai

import android.content.Context
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

object AiRepository {
 private val client=OkHttpClient();private const val ENDPOINT="https://api.anthropic.com/v1/messages"
 private fun request(apiKey:String,model:String,messages:JSONArray):Result<String>{try{val body=JSONObject().apply{put("model",model);put("max_tokens",1000);put("messages",messages)}.toString();val req=Request.Builder().url(ENDPOINT).addHeader("x-api-key",apiKey).addHeader("anthropic-version","2023-06-01").addHeader("content-type","application/json").post(body.toRequestBody("application/json".toMediaType())).build();client.newCall(req).execute().use{r->val b=r.body?.string()?:("");if(!r.isSuccessful)return Result.failure(Exception("AI error ${r.code}: $b"));val blocks=JSONObject(b).getJSONArray("content");val text=buildString{for(i in 0 until blocks.length()){val x=blocks.getJSONObject(i);if(x.optString("type")=="text")append(x.optString("text"))}};return Result.success(text)}}catch(e:Exception){return Result.failure(e)}}
 suspend fun analyze(apiKey:String,model:String,prompt:String):Result<String>=withContext(Dispatchers.IO){if(apiKey.isBlank())Result.failure(IllegalStateException("Add your AI API key in Settings")) else request(apiKey,model,JSONArray().put(JSONObject().apply{put("role","user");put("content",prompt)}))}
 suspend fun analyzeImage(context:Context,apiKey:String,model:String,uri:Uri):Result<String>=withContext(Dispatchers.IO){if(apiKey.isBlank())return@withContext Result.failure(IllegalStateException("Add your AI API key in Settings"));try{val bytes=context.contentResolver.openInputStream(uri)?.use{it.readBytes()}?:return@withContext Result.failure(Exception("Could not read image"));val mime=context.contentResolver.getType(uri)?:"image/jpeg";val encoded=Base64.encodeToString(bytes,Base64.NO_WRAP);val content=JSONArray().put(JSONObject().apply{put("type","image");put("source",JSONObject().apply{put("type","base64");put("media_type",mime);put("data",encoded)})}).put(JSONObject().apply{put("type","text");put("text","Analyze this captured image as my Luma productivity coach. If it is a study page, whiteboard, notes, schedule, or workspace, extract useful information, summarize what I am working on, and suggest one next action. If it is unrelated, simply describe it briefly. Do not invent text that cannot be read.")});request(apiKey,model,JSONArray().put(JSONObject().apply{put("role","user");put("content",content)}))}catch(e:Exception){Result.failure(e)}}
}
