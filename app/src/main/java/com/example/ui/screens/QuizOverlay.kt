package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudyViewModel

@Composable
fun QuizOverlay(
    viewModel: StudyViewModel,
    onDismiss: () -> Unit
) {
    val isQuizActive by viewModel.isQuizModeActive.collectAsState()
    val questions = viewModel.getQuizQuestions()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val score by viewModel.quizScore.collectAsState()
    val selectedIndex by viewModel.selectedAnswerIndex.collectAsState()
    val isSubmitted by viewModel.isAnswerSubmitted.collectAsState()
    val completed by viewModel.quizCompleted.collectAsState()

    // Flashcard states
    val flashcards by viewModel.flashcards.collectAsState()
    val currentFlashcardIndex by viewModel.currentFlashcardIndex.collectAsState()
    val isFlashcardFlipped by viewModel.isFlashcardFlipped.collectAsState()
    val activeCourseId by viewModel.activeCourseId.collectAsState()
    val courses by viewModel.courses.collectAsState()

    var studyMode by remember { mutableStateOf("Quiz") } // "Quiz" or "Flashcards"

    // Locally track choices to display wrong answer reviews
    val answersLog = remember { mutableStateListOf<Pair<Int, Int>>() }

    // Reset log when quiz starts or completes
    LaunchedEffect(isQuizActive, completed) {
        if (!completed) {
            answersLog.clear()
        }
    }

    if (!isQuizActive) return

    val currentQuestion = questions.getOrNull(currentIndex) ?: questions.first()
    val activeCourse = courses.find { it.id == activeCourseId } ?: courses.firstOrNull() ?: Course("1", "Machine Learning", "AI", 0.1f, "Intro", "1 hr", "Hard")

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
                    Text(
                        text = if (studyMode == "Quiz") "Genie Study Quiz" else "AI Flashcard Deck",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("quiz_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close overlay",
                            tint = BentoTextSecondary
                        )
                    }
                }

                // --- MODE TABS ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("Quiz", "Flashcards").forEach { mode ->
                        val isSelected = studyMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) BentoPremiumBg else BentoCardWhite)
                                .border(
                                    BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) BentoPremiumBtn else BentoCardBorder
                                    ),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { studyMode = mode }
                                .padding(vertical = 10.dp)
                                .testTag("study_mode_tab_$mode"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) BentoPremiumText else BentoTextPrimary
                            )
                        }
                    }
                }

                Divider(color = BentoCardBorder)

                if (studyMode == "Quiz") {
                    // ==========================================
                    //               QUIZ SCREEN
                    // ==========================================
                    if (!completed) {
                        // --- PROGRESS HEADER ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Question ${currentIndex + 1} of ${questions.size}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoPremiumBtn
                            )
                            Text(
                                text = "Score: $score",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextSecondary
                            )
                        }

                        // Progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE6E0E9))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = (currentIndex + 1).toFloat() / questions.size)
                                    .clip(CircleShape)
                                    .background(BentoPremiumBtn)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // --- QUESTION CARD ---
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = BentoCardWhite),
                            border = BorderStroke(1.dp, BentoCardBorder)
                        ) {
                            Text(
                                text = currentQuestion.text,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimary,
                                lineHeight = 22.sp,
                                modifier = Modifier.padding(20.dp)
                            )
                        }

                        // --- OPTIONS LIST ---
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            currentQuestion.options.forEachIndexed { index, option ->
                                val isSelected = selectedIndex == index
                                val isCorrectAnswer = index == currentQuestion.correctAnswerIndex

                                val optionBg = when {
                                    isSubmitted && isSelected && isCorrectAnswer -> Color(0xFFD4EDDA)
                                    isSubmitted && isSelected && !isCorrectAnswer -> Color(0xFFF8D7DA)
                                    isSubmitted && isCorrectAnswer -> Color(0xFFD4EDDA)
                                    isSelected -> BentoPremiumBg
                                    else -> BentoCardWhite
                                }

                                val optionTextCol = when {
                                    isSubmitted && isSelected && isCorrectAnswer -> Color(0xFF155724)
                                    isSubmitted && isSelected && !isCorrectAnswer -> Color(0xFF721C24)
                                    isSubmitted && isCorrectAnswer -> Color(0xFF155724)
                                    isSelected -> BentoPremiumText
                                    else -> BentoTextPrimary
                                }

                                val optionBorder = when {
                                    isSubmitted && isCorrectAnswer -> BorderStroke(1.5.dp, Color(0xFF28A745))
                                    isSubmitted && isSelected && !isCorrectAnswer -> BorderStroke(1.5.dp, Color(0xFFDC3545))
                                    isSelected -> BorderStroke(1.5.dp, BentoPremiumBtn)
                                    else -> BorderStroke(1.dp, BentoCardBorder)
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(optionBg)
                                        .border(optionBorder, RoundedCornerShape(16.dp))
                                        .clickable(enabled = !isSubmitted) {
                                            viewModel.selectQuizAnswer(index)
                                        }
                                        .padding(16.dp)
                                        .testTag("quiz_option_$index"),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = optionTextCol,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    if (isSubmitted) {
                                        if (isCorrectAnswer) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Correct",
                                                tint = Color(0xFF28A745),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Error,
                                                contentDescription = "Incorrect",
                                                tint = Color(0xFFDC3545),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // --- EXPLANATION PANEL ---
                        AnimatedVisibility(visible = isSubmitted) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedIndex == currentQuestion.correctAnswerIndex) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = if (selectedIndex == currentQuestion.correctAnswerIndex) "Correct Answer!" else "Incorrect",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (selectedIndex == currentQuestion.correctAnswerIndex) Color(0xFF2E7D32) else Color(0xFFE65100)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentQuestion.explanation,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        color = BentoTextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // --- SUBMIT / NEXT ACTION FOOTER ---
                        Button(
                            onClick = {
                                if (!isSubmitted) {
                                    selectedIndex?.let { sel ->
                                        answersLog.add(currentIndex to sel)
                                    }
                                    viewModel.submitQuizAnswer()
                                } else {
                                    viewModel.nextQuizQuestion()
                                }
                            },
                            enabled = selectedIndex != null,
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPremiumBtn),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("quiz_action_button")
                        ) {
                            Text(
                                text = if (!isSubmitted) "Submit Answer" else "Next Question",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                    } else {
                        // --- QUIZ COMPLETED VIEW WITH WRONG ANSWER REVIEW SYSTEM ---
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(BentoPremiumBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Trophy,
                                    contentDescription = "Trophy",
                                    tint = BentoPremiumText,
                                    modifier = Modifier.size(44.dp)
                                )
                            }

                            Text(
                                text = "Quiz Finished!",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimary
                            )

                            Text(
                                text = "You scored $score out of ${questions.size} correct answers.",
                                fontSize = 14.sp,
                                color = BentoTextSecondary,
                                textAlign = TextAlign.Center
                            )

                            val percentage = (score.toFloat() / questions.size) * 100
                            Text(
                                text = "${percentage.toInt()}% Accuracy",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoPremiumBtn
                            )

                            // --- WRONG ANSWER REVIEW SECTION ---
                            val wrongAnswers = answersLog.filter { (qIdx, selIdx) ->
                                val q = questions.getOrNull(qIdx)
                                q != null && selIdx != q.correctAnswerIndex
                            }

                            if (wrongAnswers.isNotEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = BentoCardWhite),
                                    border = BorderStroke(1.dp, BentoCardBorder)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.Dangerous, "Wrong", tint = Color.Red, modifier = Modifier.size(18.dp))
                                            Text(
                                                text = "WRONG ANSWER REVIEW (${wrongAnswers.size})",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Red
                                            )
                                        }

                                        wrongAnswers.forEach { (qIdx, selIdx) ->
                                            val q = questions[qIdx]
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(BentoBackground, RoundedCornerShape(12.dp))
                                                    .padding(12.dp)
                                            ) {
                                                Text(
                                                    text = "Q: ${q.text}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = BentoTextPrimary
                                                )
                                                Text(
                                                    text = "Your Answer: ${q.options.getOrNull(selIdx) ?: "None"}",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFFDC3545),
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = "Correct Answer: ${q.options[q.correctAnswerIndex]}",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF28A745),
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Explanation: ${q.explanation}",
                                                    fontSize = 11.sp,
                                                    color = BentoTextSecondary,
                                                    lineHeight = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "Perfect! No wrong answers to review. 🌟",
                                    fontSize = 13.sp,
                                    color = Color(0xFF28A745),
                                    fontWeight = FontWeight.Bold
                                )
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
                            Text("Return to Dashboard", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // ==========================================
                    //             FLASHCARDS SCREEN
                    // ==========================================
                    if (flashcards.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No flashcards found. Create one with AI below!", color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    } else {
                        val currentCard = flashcards.getOrNull(currentFlashcardIndex) ?: flashcards.first()

                        Text(
                            text = "Card ${currentFlashcardIndex + 1} of ${flashcards.size}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextSecondary
                        )

                        // --- INTERACTIVE FLIPPABLE FLASHCARD CARD ---
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clickable { viewModel.flipFlashcard() }
                                .testTag("flashcard_interactive_surface"),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isFlashcardFlipped) BentoPremiumBg else BentoCardWhite
                            ),
                            border = BorderStroke(1.5.dp, if (isFlashcardFlipped) BentoPremiumBtn else BentoCardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flip,
                                    contentDescription = "Flip",
                                    tint = if (isFlashcardFlipped) BentoPremiumText else BentoTextSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = if (isFlashcardFlipped) "ANSWER:" else "QUESTION:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFlashcardFlipped) BentoPremiumText else BentoTextSecondary,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isFlashcardFlipped) currentCard.back else currentCard.front,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFlashcardFlipped) BentoPremiumText else BentoTextPrimary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                            }
                        }

                        // --- FLASHCARD NAVIGATION CONTROLS ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.prevFlashcard() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(BentoCardWhite)
                                    .border(BorderStroke(1.dp, BentoCardBorder), RoundedCornerShape(16.dp))
                                    .testTag("flashcard_prev_btn")
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ArrowBack, "Prev", tint = BentoTextPrimary)
                                    Text("Previous", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                                }
                            }

                            IconButton(
                                onClick = { viewModel.nextFlashcard() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(BentoCardWhite)
                                    .border(BorderStroke(1.dp, BentoCardBorder), RoundedCornerShape(16.dp))
                                    .testTag("flashcard_next_btn")
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("Next", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                                    Icon(Icons.Default.ArrowForward, "Next", tint = BentoTextPrimary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // --- GENERATE AI FLASHCARD CARD ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.generateFlashcardFromAi(activeCourse.title) }
                            .testTag("generate_ai_flashcard_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoCardWhite),
                        border = BorderStroke(1.dp, BentoPremiumBtn.copy(alpha = 0.3f))
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
                                    .background(BentoPremiumBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI",
                                    tint = BentoPremiumBtn,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "✨ Generate Flashcard with AI",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoTextPrimary
                                )
                                Text(
                                    text = "Based on course: ${activeCourse.title}",
                                    fontSize = 11.sp,
                                    color = BentoTextSecondary
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Generate",
                                tint = BentoPremiumBtn,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
