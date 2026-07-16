package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.BuildConfig
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChatMessage
import com.example.ui.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isChatLoading.collectAsState()
    
    val apiKey = BuildConfig.GEMINI_API_KEY
    val isDemoMode = apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY"

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val chatFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.uploadDocument(it)
        }
    }

    // Tutor modes from ViewModel
    val tutorMode by viewModel.tutorMode.collectAsState()

    // Voice simulation states
    var isRecordingVoice by remember { mutableStateOf(false) }
    var voiceTextSimulationIndex by remember { mutableStateOf(0) }
    val simulatedVoicePhrases = listOf(
        "Explain deep learning loss curves",
        "Explain backpropagation gradient descent",
        "Give me exam prep questions on neural networks"
    )

    // Upload & file processing states
    val isFileUploading by viewModel.isFileUploading.collectAsState()
    val uploadedFileName by viewModel.uploadedFileName.collectAsState()
    val fileProcessingProgress by viewModel.fileProcessingProgress.collectAsState()

    var showFilePicker by remember { mutableStateOf(false) }

    // Scroll to the latest message whenever messages change
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val suggestionPrompts = listOf(
        "Explain Gradient Descent simply",
        "Give me a 5-step Neural Network roadmap",
        "How do I manage study stress?"
    )

    // --- File Attachment Dialog ---
    if (showFilePicker) {
        Dialog(onDismissRequest = { showFilePicker = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(BorderStroke(1.dp, BentoCardBorder), RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Document to Index", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BentoTextPrimary)
                    Text("Select a local document to process and index into your bento vault:", fontSize = 11.sp, color = BentoTextSecondary)
                    
                    listOf(
                        "Machine_Learning_A1.pdf" to "PDF",
                        "Backpropagation_Calculus.docx" to "DOCX",
                        "Kotlin_Flows_Reference.txt" to "TXT"
                    ).forEach { (fileName, fileType) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(BentoBackground)
                                .clickable {
                                    viewModel.simulateDocumentUpload(fileName, fileType)
                                    showFilePicker = false
                                }
                                .padding(12.dp)
                                .testTag("file_picker_${fileType}"),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = fileType,
                                tint = BentoPremiumBtn,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(fileName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                                Text("$fileType Document File", fontSize = 10.sp, color = BentoTextSecondary)
                            }
                        }
                    }

                    // --- CHOOSE REAL DEVICE FILE ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BentoPremiumBg)
                            .clickable {
                                chatFilePickerLauncher.launch("*/*")
                                showFilePicker = false
                            }
                            .padding(12.dp)
                            .testTag("chat_choose_real_file_btn"),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = "Real File Picker",
                            tint = BentoPremiumText,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text("Choose File from Device...", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoPremiumText)
                            Text("PDF, DOCX, or TXT (Max 5MB)", fontSize = 10.sp, color = BentoPremiumText.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBackground)
            .padding(horizontal = 16.dp, top = 20.dp)
    ) {
        // --- CHAT HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "StudyGenie AI Tutor",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )
                Text(
                    text = if (isDemoMode) "AI Tutor Active (Offline Sandbox)" else "Powered by Gemini 3.5",
                    fontSize = 12.sp,
                    color = BentoPremiumBtn,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            IconButton(
                onClick = { viewModel.clearChat() },
                modifier = Modifier.testTag("clear_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Clear Chat",
                    tint = BentoTextSecondary
                )
            }
        }

        // --- EXPLANATION & TUTORING MODES SELECTOR (Material Bento Tabs) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .border(BorderStroke(1.dp, BentoCardBorder), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = BentoCardWhite)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "AI EXPLANATION TUTORING MODE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Default", "Socratic", "Exam Prep").forEach { mode ->
                        val isSelected = tutorMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) BentoPremiumBg else BentoBackground)
                                .clickable { viewModel.setTutorMode(mode) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isSelected) BentoPremiumText else BentoTextPrimary
                            )
                        }
                    }
                }
            }
        }

        // --- GEMINI WARNING BANNER ---
        if (isDemoMode) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BentoPremiumBg),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BentoCardBorder.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .testTag("api_warning_banner")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info Key",
                        tint = BentoPremiumText,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "💡 StudyGenie AI is running in offline sandbox. Set GEMINI_API_KEY in the AI Studio Secrets panel for real-time Socratic responses!",
                        color = BentoPremiumText,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // --- FILE PROCESSING STATUS CARD ---
        if (isFileUploading) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BentoPremiumBg),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, BentoPremiumText.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("file_processing_status_panel")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { fileProcessingProgress },
                        color = BentoPremiumBtn,
                        trackColor = Color(0xFFE6E0E9),
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Analyzing & Indexing $uploadedFileName...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                        Text(
                            text = "${(fileProcessingProgress * 100).toInt()}% parsed • Saving to Study Vault",
                            fontSize = 10.sp,
                            color = BentoTextSecondary
                        )
                    }
                }
            }
        }

        // --- VOICE RECORDING SIMULATOR HUD ---
        if (isRecordingVoice) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFECEFF1)),
                border = BorderStroke(1.dp, BentoPremiumBtn)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Recording Voice input...", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoTextPrimary)
                        Text("Converting spoken speech to text...", fontSize = 11.sp, color = BentoTextSecondary)
                    }
                    Button(
                        onClick = {
                            val phrase = simulatedVoicePhrases[voiceTextSimulationIndex]
                            inputText = phrase
                            voiceTextSimulationIndex = (voiceTextSimulationIndex + 1) % simulatedVoicePhrases.size
                            isRecordingVoice = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BentoPremiumBtn),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Finish", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }

        // --- MESSAGES STREAM ---
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.5f)
        ) {
            items(messages) { message ->
                MessageBubble(message)
            }
            
            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BentoPremiumBtn),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Thinking",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Thinking of a Socratic answer...",
                            fontSize = 12.sp,
                            color = BentoTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = BentoPremiumBtn,
                            strokeWidth = 1.5.dp
                        )
                    }
                }
            }
        }

        // --- QUICK SUGGESTIONS CAROUSEL ---
        if (messages.size <= 1 && !isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Suggested Questions:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextSecondary
                )
                suggestionPrompts.forEach { prompt ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(BentoCardWhite)
                            .border(BorderStroke(1.dp, BentoCardBorder), RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.sendChatMessage(prompt)
                             }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "robot query",
                                tint = BentoPremiumBtn,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = prompt,
                                fontSize = 12.sp,
                                color = BentoTextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // --- CHAT INPUT BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 96.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Attachment File Button
            IconButton(
                onClick = { showFilePicker = true },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(BentoCardWhite)
                    .border(BorderStroke(1.dp, BentoCardBorder), CircleShape)
                    .testTag("chat_attach_file_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.UploadFile,
                    contentDescription = "Attach File",
                    tint = BentoPremiumBtn
                )
            }

            // Voice Mic Button
            IconButton(
                onClick = { isRecordingVoice = !isRecordingVoice },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isRecordingVoice) BentoPremiumBg else BentoCardWhite)
                    .border(BorderStroke(1.dp, if (isRecordingVoice) BentoPremiumBtn else BentoCardBorder), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Speak Input",
                    tint = if (isRecordingVoice) BentoPremiumText else BentoPremiumBtn
                )
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask anything...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_text_field"),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputText.trim().isNotEmpty()) {
                        viewModel.sendChatMessage(inputText)
                        inputText = ""
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BentoCardWhite,
                    unfocusedContainerColor = BentoCardWhite,
                    focusedBorderColor = BentoPremiumBtn,
                    unfocusedBorderColor = BentoCardBorder
                )
            )

            IconButton(
                onClick = {
                    if (inputText.trim().isNotEmpty()) {
                        viewModel.sendChatMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(BentoPremiumBtn)
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val bubbleShape = if (message.isUser) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)
    }

    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bg = if (message.isUser) BentoPremiumBtn else BentoCardWhite
    val textCol = if (message.isUser) Color.White else BentoTextPrimary
    val borderStroke = if (message.isUser) null else BorderStroke(1.dp, BentoCardBorder)

    var isPlayingAudio by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth(0.85f).align(alignment)
        ) {
            if (!message.isUser) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(BentoCardBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Genie",
                        tint = BentoPremiumBtn,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Surface(
                color = bg,
                shape = bubbleShape,
                border = borderStroke,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Column {
                    Text(
                        text = message.text,
                        color = textCol,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                    
                    // Voice playback simulation option for AI responses
                    if (!message.isUser) {
                        Divider(color = BentoCardBorder.copy(alpha = 0.5f))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isPlayingAudio = !isPlayingAudio }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isPlayingAudio) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                contentDescription = "Play voice audio reply",
                                tint = BentoPremiumBtn,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isPlayingAudio) "Playing spoken voice audio..." else "Simulate Speak Aloud Voice AI",
                                fontSize = 10.sp,
                                color = BentoPremiumBtn,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (message.isUser) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(BentoPremiumBtn),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "JD",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
