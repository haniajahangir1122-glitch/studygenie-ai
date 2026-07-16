package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudyViewModel
import com.example.ui.viewmodel.VaultItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultOverlay(
    viewModel: StudyViewModel,
    onDismiss: () -> Unit
) {
    val isVaultOpen by viewModel.isVaultOpen.collectAsState()
    val items by viewModel.vaultItems.collectAsState()
    val searchQuery by viewModel.vaultSearchQuery.collectAsState()
    val isAutoSaving by viewModel.isAutoSaving.collectAsState()

    var noteInputText by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<VaultItem?>(null) }
    var editingText by remember { mutableStateOf("") }

    // Preview File States
    var previewItem by remember { mutableStateOf<VaultItem?>(null) }
    var downloadingItemId by remember { mutableStateOf<String?>(null) }

    if (!isVaultOpen) return

    val filteredItems = items.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
        it.type.contains(searchQuery, ignoreCase = true)
    }

    // --- Note Editing Dialog ---
    if (editingItem != null) {
        AlertDialog(
            onDismissRequest = { editingItem = null },
            title = { Text("Edit Study Note", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BentoTextPrimary) },
            text = {
                OutlinedTextField(
                    value = editingText,
                    onValueChange = { editingText = it },
                    modifier = Modifier.fillMaxWidth().testTag("vault_edit_note_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BentoPremiumBtn, unfocusedBorderColor = BentoCardBorder)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        editingItem?.let {
                            viewModel.updateVaultNote(it.id, editingText)
                        }
                        editingItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPremiumBtn),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("vault_edit_confirm_btn")
                ) {
                    Text("Auto-Save Change", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingItem = null }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    // --- PDF / File Preview Dialog ---
    if (previewItem != null) {
        val file = previewItem!!
        Dialog(onDismissRequest = { previewItem = null }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(BorderStroke(1.dp, BentoCardBorder), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.PictureAsPdf, "PDF", tint = Color.Red, modifier = Modifier.size(20.dp))
                            Text("Pre-Processing Preview", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BentoTextPrimary)
                        }
                        IconButton(onClick = { previewItem = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, "Close", tint = BentoTextSecondary)
                        }
                    }

                    Divider(color = BentoCardBorder.copy(alpha = 0.5f))

                    Text("File details:", fontSize = 11.sp, color = BentoTextSecondary, fontWeight = FontWeight.Bold)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BentoBackground),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Name: ${file.title}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BentoTextPrimary)
                            Text("Format: ${file.type} Document", fontSize = 11.sp, color = BentoTextSecondary)
                            Text("Size: 2.4 MB • Status: INDEXED", fontSize = 11.sp, color = BentoTextSecondary)
                        }
                    }

                    Text("AI Summary Preview:", fontSize = 11.sp, color = BentoTextSecondary, fontWeight = FontWeight.Bold)
                    Text(
                        text = "This document explores detailed algorithmic equations, optimization problems, and computational complexities. The AI model is fully optimized to index these pages and generate custom socratic quiz cards inside StudyGenie.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = BentoTextPrimary
                    )

                    Divider(color = BentoCardBorder.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.deleteVaultItem(file.id)
                                previewItem = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Delete File", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { previewItem = null },
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPremiumBtn),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Close", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(BentoBackground),
            color = BentoBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- TOP BAR ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Folder",
                            tint = BentoVaultText,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Study Vault",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("vault_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Vault",
                            tint = BentoTextSecondary
                        )
                    }
                }

                Text(
                    text = "A secure bento vault storing summaries, bookmarks, and custom notes taken with StudyGenie AI.",
                    fontSize = 12.sp,
                    color = BentoTextSecondary,
                    lineHeight = 16.sp
                )

                // --- ADD NEW NOTE BAR ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoCardWhite),
                    border = BorderStroke(1.dp, BentoCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Add Custom Study Note:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = noteInputText,
                                onValueChange = { noteInputText = it },
                                placeholder = { Text("e.g. Backpropagation calculus tips...") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("vault_note_input_field"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (noteInputText.trim().isNotEmpty()) {
                                        viewModel.addVaultNote(noteInputText)
                                        noteInputText = ""
                                    }
                                }),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BentoPremiumBtn, unfocusedBorderColor = BentoCardBorder)
                            )

                            IconButton(
                                onClick = {
                                    if (noteInputText.trim().isNotEmpty()) {
                                        viewModel.addVaultNote(noteInputText)
                                        noteInputText = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(BentoPremiumBtn)
                                    .testTag("vault_add_note_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Note",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                // --- UPLOAD STUDY MATERIALS CARD ---
                val documentPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        viewModel.uploadDocument(it)
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        documentPickerLauncher.launch("*/*")
                    }.testTag("dashboard_upload_pdf_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoVaultBg),
                    border = BorderStroke(1.5.dp, BentoVaultText.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BentoVaultText.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Upload Doc",
                                tint = BentoVaultText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📤 Upload PDF, DOCX, or TXT Textbook",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoVaultText
                            )
                            Text(
                                text = "Real-time AI index summaries with Gemini model",
                                fontSize = 10.sp,
                                color = BentoTextSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Upload",
                            tint = BentoVaultText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // --- SEARCH BAR ---
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setVaultSearchQuery(it) },
                    placeholder = { Text("🔍 Search notes & files...") },
                    modifier = Modifier.fillMaxWidth().testTag("vault_search_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BentoPremiumBtn, unfocusedBorderColor = BentoCardBorder)
                )

                // --- STORED FILES LIST ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vault Records (${filteredItems.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextSecondary
                    )
                    if (isAutoSaving) {
                        Text(
                            text = "Auto-saving Note...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoPremiumText
                        )
                    }
                }

                if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No items match your search." else "No study notes stored yet. Enter a custom note above to save to your bento vault!",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredItems, key = { it.id }) { item ->
                            val isDoc = item.type != "Note"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(BentoCardWhite)
                                    .border(BorderStroke(1.dp, BentoCardBorder), RoundedCornerShape(16.dp))
                                    .clickable {
                                        if (isDoc) {
                                            previewItem = item
                                        }
                                    }
                                    .padding(14.dp)
                                    .testTag("vault_item_${item.id}"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(BentoCardBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val iconImg = when (item.type.uppercase()) {
                                            "PDF" -> Icons.Default.PictureAsPdf
                                            "DOCX" -> Icons.Default.Description
                                            "TXT" -> Icons.Default.SimCard
                                            else -> Icons.Default.Note
                                        }
                                        Icon(
                                            imageVector = iconImg,
                                            contentDescription = item.type,
                                            tint = if (item.type == "PDF") Color.Red else BentoPremiumBtn,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = BentoTextPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = item.type + " • Tap to preview file",
                                            fontSize = 10.sp,
                                            color = BentoTextSecondary
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (downloadingItemId == item.id) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = BentoPremiumBtn,
                                            strokeWidth = 2.dp
                                        )
                                    } else if (isDoc) {
                                        // Mock download button
                                        IconButton(
                                            onClick = {
                                                downloadingItemId = item.id
                                                // Trigger automatic completion timer of download simulation
                                                viewModel.claimStreak() 
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = "Download study material",
                                                tint = BentoPremiumBtn,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    // Reset downloading state if done
                                    LaunchedEffect(downloadingItemId) {
                                        if (downloadingItemId != null) {
                                            kotlinx.coroutines.delay(1200)
                                            downloadingItemId = null
                                        }
                                    }

                                    if (item.type == "Note") {
                                        IconButton(
                                            onClick = {
                                                editingItem = item
                                                editingText = item.title
                                            },
                                            modifier = Modifier.testTag("vault_item_edit_${item.id}").size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Note",
                                                tint = BentoPremiumBtn,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteVaultItem(item.id) },
                                        modifier = Modifier.testTag("vault_item_delete_${item.id}").size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete Note",
                                            tint = Color.Red,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPremiumBtn),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Close Vault", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
