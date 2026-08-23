package com.luma.focus.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.luma.focus.data.JournalSection
import com.luma.focus.data.LumaStore
import com.luma.focus.ui.theme.LumaSurfaceElevated
import com.luma.focus.ui.theme.LumaTextPrimary
import com.luma.focus.ui.theme.LumaTextSecondary

private data class EditableSection(var label: String, var content: String)

@Composable
fun JournalScreen(accent: Color) {
    var entries by remember { mutableStateOf(LumaStore.getJournalEntries()) }
    var sections by remember {
        mutableStateOf(
            listOf(
                EditableSection("What did you learn today?", ""),
                EditableSection("What did you feel today?", ""),
                EditableSection("What do you want to do?", "")
            )
        )
    }
    var images by remember { mutableStateOf(listOf<Uri>()) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> images = images + uris }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Journal", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = accent)
        Spacer(Modifier.height(12.dp))

        sections.forEachIndexed { index, section ->
            OutlinedTextField(
                value = section.content,
                onValueChange = { newVal ->
                    sections = sections.toMutableList().also {
                        it[index] = it[index].copy(content = newVal)
                    }
                },
                label = { Text(section.label) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
        }

        TextButton(onClick = {
            sections = sections + EditableSection("New section", "")
        }) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Add custom section")
        }

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            TextButton(onClick = { imagePicker.launch("image/*") }) {
                Icon(Icons.Filled.Image, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Add photos")
            }
            Text("${images.size} selected", color = LumaTextSecondary, fontSize = 12.sp)
        }

        if (images.isNotEmpty()) {
            LazyRow {
                items(images) { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .padding(4.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    )
                }
            }
        }

        Button(
            onClick = {
                val hasContent = sections.any { it.content.isNotBlank() }
                if (hasContent) {
                    LumaStore.addJournalEntry(
                        sections.map { JournalSection(it.label, it.content) },
                        images.map { it.toString() }
                    )
                    entries = LumaStore.getJournalEntries()
                    sections = listOf(
                        EditableSection("What did you learn today?", ""),
                        EditableSection("What did you feel today?", ""),
                        EditableSection("What do you want to do?", "")
                    )
                    images = emptyList()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = accent)
        ) { Text("Save entry") }

        Spacer(Modifier.height(16.dp))
        LazyColumn {
            items(entries) { entry ->
                Card(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = LumaSurfaceElevated)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row {
                            Text(entry.dateTime, fontWeight = FontWeight.Bold, color = accent, modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                LumaStore.deleteJournalEntry(entry.id)
                                entries = LumaStore.getJournalEntries()
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                        entry.sections.forEach { s ->
                            if (s.content.isNotBlank()) {
                                Text("${s.label}: ${s.content}", color = LumaTextPrimary, fontSize = 13.sp)
                            }
                        }
                        if (entry.imageUris.isNotEmpty()) {
                            LazyRow {
                                items(entry.imageUris) { uriStr ->
                                    AsyncImage(
                                        model = Uri.parse(uriStr),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .padding(4.dp)
                                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
