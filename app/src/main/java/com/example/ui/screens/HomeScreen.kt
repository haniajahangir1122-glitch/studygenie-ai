package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudyViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier,
    onOpenQuiz: () -> Unit,
    onOpenVault: () -> Unit
) {
    val streakCount by viewModel.streakCount.collectAsState()
    val streakClaimed by viewModel.streakClaimed.collectAsState()
    val aiTip by viewModel.aiTip.collectAsState()
    val isAiTipLoading by viewModel.isAiTipLoading.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val activeCourseId by viewModel.activeCourseId.collectAsState()

    val activityLogs by viewModel.activityLogs.collectAsState()
    val userXp by viewModel.userXp.collectAsState()
    val userLevel by viewModel.userLevel.collectAsState()
    val quizzesCompletedToday by viewModel.quizzesCompletedToday.collectAsState()
    val dailyGoalQuizzes by viewModel.dailyGoalQuizzes.collectAsState()
    val studyRemindersEnabled by viewModel.studyRemindersEnabled.collectAsState()

    val activeCourse = courses.find { it.id == activeCourseId } ?: courses.firstOrNull() ?: Course("1", "Machine Learning", "AI", 0.1f, "Intro", "1 hr", "Hard")

    var showSearchDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }

    // Streaks animation scaling
    val streakScale by animateFloatAsState(
        targetValue = if (streakClaimed) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "streakScale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBackground)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 88.dp)
    ) {
        // --- 1. APP BAR HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, top = 20.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // User Avatar Circle "JD"
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(BentoPremiumBtn)
                        .clickable { viewModel.togglePremium() }
                        .testTag("profile_avatar"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "JD",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Column {
                    Text(
                        text = "STUDYGENIE SUITE",
                        color = BentoPremiumBtn,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Genie Dashboard",
                        color = BentoTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Search, Goal Config & Notification Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showSearchDialog = true },
                    modifier = Modifier.testTag("search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = BentoTextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box {
                    IconButton(
                        onClick = { showNotificationDialog = true },
                        modifier = Modifier.testTag("notification_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = BentoTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    if (studyRemindersEnabled) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                                .clip(CircleShape)
                                .background(Color.Green)
                                .border(2.dp, BentoBackground, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                                .border(2.dp, BentoBackground, CircleShape)
                        )
                    }
                }
            }
        }

        // --- 2. BENTO GRID BODY ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Bento Block A: Premium Banner (Full Width)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(BentoPremiumBg)
                    .clickable { viewModel.togglePremium() }
                    .padding(16.dp)
                    .testTag("premium_banner")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPremium) "Premium Unlocked! ⭐" else "Upgrade to Premium",
                            color = BentoPremiumText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isPremium) "Full access to Study Vault & AI tutoring models active" else "Unlock AI custom tutoring & study files storage",
                            color = BentoPremiumText.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Button(
                        onClick = { viewModel.togglePremium() },
                        colors = ButtonDefaults.buttonColors(containerColor = BentoPremiumBtn),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = if (isPremium) "Active" else "Explore",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bento Block B: Daily Study Goals Progress Card (Full Width)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(BorderStroke(1.dp, BentoCardBorder), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = BentoCardWhite)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.TrackChanges, contentDescription = "Goal", tint = BentoPremiumBtn, modifier = Modifier.size(18.dp))
                            Text("DAILY LEARNING GOALS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoTextSecondary)
                        }
                        Text("$quizzesCompletedToday / $dailyGoalQuizzes Quizzes", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoPremiumBtn)
                    }
                    val goalProgress = if (dailyGoalQuizzes > 0) (quizzesCompletedToday.toFloat() / dailyGoalQuizzes).coerceIn(0f, 1f) else 0f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(BentoCardBg)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(goalProgress)
                                .clip(CircleShape)
                                .background(BentoPremiumBtn)
                        )
                    }
                    Text(
                        text = if (goalProgress >= 1f) "Daily goals completed! 🌟 You gained bonus XP." else "Complete $dailyGoalQuizzes quizzes to claim today's daily badge.",
                        fontSize = 11.sp,
                        color = BentoTextSecondary
                    )
                }
            }

            // Bento Row: 2-Columns (Day Streak & AI Recommendation)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bento Block C: Day Streak
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(160.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .scale(streakScale)
                        .clickable { viewModel.claimStreak() }
                        .testTag("streak_card"),
                    colors = CardDefaults.cardColors(containerColor = BentoStreakBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Streak Lightning",
                            tint = BentoStreakText,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = streakCount.toString(),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoStreakText,
                                lineHeight = 32.sp
                            )
                            Text(
                                text = if (streakClaimed) "CLAIMED TODAY!" else "DAY STREAK",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoStreakText.copy(alpha = 0.8f),
                                letterSpacing = 0.5.sp
                            )
                            if (!streakClaimed) {
                                Text(
                                    text = "Tap to claim",
                                    fontSize = 9.sp,
                                    color = BentoStreakText.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                // Bento Block D: AI Recommendation / Tip
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(160.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(BorderStroke(1.dp, BentoCardBorder), RoundedCornerShape(24.dp))
                        .testTag("ai_tip_card"),
                    colors = CardDefaults.cardColors(containerColor = BentoCardWhite)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AI TUTOR TIP",
                                color = BentoPremiumBtn,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            if (isAiTipLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), color = BentoPremiumBtn, strokeWidth = 1.5.dp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, "AI", tint = BentoPremiumBtn, modifier = Modifier.size(14.dp))
                            }
                        }

                        Text(
                            text = aiTip,
                            color = BentoTextPrimary.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                        )

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(BentoCardBg)
                                .clickable { viewModel.generateNewAiTip() }
                                .align(Alignment.End),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Regenerate",
                                tint = BentoPremiumBtn,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Bento Block E: Multiple Subjects Selector Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(BorderStroke(1.dp, BentoCardBorder), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = BentoCardWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ACTIVE SUBJECTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoTextSecondary)
                        IconButton(onClick = { showAddSubjectDialog = true }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.AddCircle, "Add", tint = BentoPremiumBtn)
                        }
                    }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(courses) { course ->
                            val isSelected = course.id == activeCourseId
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) BentoPremiumBtn else BentoCardBg)
                                    .clickable { viewModel.selectActiveCourse(course.id) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = course.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else BentoTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Bento Block F: Course Progress Card (Active Subject)
            val animatedProgress by animateFloatAsState(
                targetValue = activeCourse.progress,
                animationSpec = spring(),
                label = "courseProgress"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { viewModel.advanceActiveCourseProgress() }
                    .testTag("active_course_card"),
                colors = CardDefaults.cardColors(containerColor = BentoCardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("STUDY PROGRESS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BentoTextSecondary)
                            Text(activeCourse.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                        }
                        Text("${(animatedProgress * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BentoPremiumBtn)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE6E0E9))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = animatedProgress)
                                .clip(CircleShape)
                                .background(BentoPremiumBtn)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .border(BorderStroke(1.dp, BentoCardBorder), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayCircle, "Play", tint = BentoTextSecondary, modifier = Modifier.size(18.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(activeCourse.currentChapter, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(activeCourse.timeRemaining, fontSize = 10.sp, color = BentoTextSecondary)
                        }
                        Text("Tap to Study", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoPremiumBtn)
                    }
                }
            }

            // Bento Block G: Learning Statistics & Custom Progress Chart Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(BorderStroke(1.dp, BentoCardBorder), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = BentoCardWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("LEARNING STATISTICS & ACTIVITY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoTextSecondary)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatCounter(title = "Level", value = userLevel.toString(), sub = "Rank Up")
                        StatCounter(title = "XP Gained", value = userXp.toString(), sub = "Total XP")
                        StatCounter(title = "Goals Progress", value = "${(goalProgress * 100).toInt()}%", sub = "Quizzes Completed")
                    }

                    Divider(color = BentoCardBorder.copy(alpha = 0.5f))

                    Text("Weekly Study Performance", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                    
                    // Custom Hand-drawn Canvas Progress Line Chart
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(top = 8.dp)
                    ) {
                        val points = listOf(20f, 45f, 35f, 60f, 75f, 90f, 100f)
                        val widthBetweenPoints = size.width / (points.size - 1)
                        val heightMultiplier = size.height / 100f

                        // Draw Grid Lines
                        for (i in 0..4) {
                            val y = size.height - (i * 25f * heightMultiplier)
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.4f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1f
                            )
                        }

                        // Draw Line Path
                        for (i in 0 until points.size - 1) {
                            val startX = i * widthBetweenPoints
                            val startY = size.height - (points[i] * heightMultiplier)
                            val endX = (i + 1) * widthBetweenPoints
                            val endY = size.height - (points[i + 1] * heightMultiplier)

                            drawLine(
                                color = Purple40,
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = 5f
                            )
                            // Draw point circles
                            drawCircle(
                                color = Pink40,
                                radius = 6f,
                                center = Offset(startX, startY)
                            )
                        }
                        // Draw last point
                        drawCircle(
                            color = Pink40,
                            radius = 6f,
                            center = Offset(size.width, size.height - (points.last() * heightMultiplier))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                            Text(it, fontSize = 10.sp, color = BentoTextSecondary)
                        }
                    }
                }
            }

            // Bento Block H: Study Calendar (Streak calendar grid)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(BorderStroke(1.dp, BentoCardBorder), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = BentoCardWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("STUDY CALENDAR (ACTIVE DAYS)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoTextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("July 2026", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                        Text("Current Streak: $streakCount days", fontSize = 11.sp, color = BentoPremiumBtn, fontWeight = FontWeight.Bold)
                    }

                    // Simulated Monthly Calendar showing active days based on streakCount
                    val activeDaysCount = streakCount.coerceIn(0, 31)
                    val daysInMonth = 31
                    val firstDayOffset = 3 // Wed is offset 3
                    
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Weekday headers
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach {
                                Text(it, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BentoTextSecondary, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                            }
                        }

                        // Render Days Grid (5 rows max)
                        for (row in 0 until 5) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                for (col in 0 until 7) {
                                    val dayIndex = row * 7 + col - firstDayOffset + 1
                                    if (dayIndex in 1..daysInMonth) {
                                        // Active days end up to the current day (let's assume current day is 16th and we streak backwards)
                                        val isActive = dayIndex in (17 - activeDaysCount)..16
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(if (isActive) BentoPremiumBtn else Color.Transparent),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = dayIndex.toString(),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isActive) Color.White else BentoTextPrimary
                                            )
                                        }
                                    } else {
                                        Box(modifier = Modifier.size(28.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bento Block I: Achievements and Badges
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(BorderStroke(1.dp, BentoCardBorder), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = BentoCardWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ACHIEVEMENTS & BADGES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoTextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BadgeItem(icon = Icons.Default.School, title = "AI Scholar", unlocked = userLevel >= 1)
                        BadgeItem(icon = Icons.Default.OfflineBolt, title = "Streak Master", unlocked = streakCount >= 7)
                        BadgeItem(icon = Icons.Default.FolderSpecial, title = "File Master", unlocked = true)
                        BadgeItem(icon = Icons.Default.EmojiEvents, title = "Quiz Whiz", unlocked = quizzesCompletedToday >= 1)
                    }
                }
            }

            // Bento Block J: Recent Activity Timeline
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(BorderStroke(1.dp, BentoCardBorder), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = BentoCardWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("RECENT ACTIVITY TIMELINE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoTextSecondary)
                    
                    if (activityLogs.isEmpty()) {
                        Text("No recent activities. Claim your streak or take a quiz!", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            activityLogs.take(5).forEach { log ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(BentoPremiumBtn)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(log.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                                        Text(log.description, fontSize = 10.sp, color = BentoTextSecondary)
                                    }
                                    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                                    Text(timeStr, fontSize = 10.sp, color = BentoTextSecondary)
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Buttons (Quizzes & Vault)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(68.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { onOpenQuiz() }
                        .testTag("quizzes_card"),
                    colors = CardDefaults.cardColors(containerColor = BentoQuizBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Quiz, "Quiz", tint = BentoQuizText, modifier = Modifier.size(24.dp))
                        Text("Quizzes Game", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoQuizText)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(68.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { onOpenVault() }
                        .testTag("vault_card"),
                    colors = CardDefaults.cardColors(containerColor = BentoVaultBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Folder, "Vault", tint = BentoVaultText, modifier = Modifier.size(24.dp))
                        Text("Study Vault", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoVaultText)
                    }
                }
            }
        }
    }

    // --- Search Interactive Dialog ---
    if (showSearchDialog) {
        Dialog(onDismissRequest = { showSearchDialog = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = BentoCardWhite,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(BorderStroke(1.dp, BentoCardBorder), RoundedCornerShape(28.dp))
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Search Suite", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BentoTextPrimary)
                        IconButton(onClick = { showSearchDialog = false }) {
                            Icon(Icons.Default.Close, "Close", tint = BentoTextSecondary)
                        }
                    }

                    var query by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search subjects, AI tips, summaries...") },
                        modifier = Modifier.fillMaxWidth().testTag("search_input_field"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BentoPremiumBtn, unfocusedBorderColor = BentoCardBorder)
                    )

                    val filtered = if (query.trim().isEmpty()) emptyList() else courses.filter {
                        it.title.contains(query, ignoreCase = true) || it.currentChapter.contains(query, ignoreCase = true)
                    }

                    if (query.trim().isNotEmpty()) {
                        Text("Results (${filtered.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoTextSecondary)
                        if (filtered.isEmpty()) {
                            Text("No items found. Try 'Kotlin' or 'Machine'.", fontSize = 12.sp, color = Color.Gray)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                filtered.forEach { course ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(BentoCardBg)
                                            .clickable {
                                                viewModel.selectActiveCourse(course.id)
                                                showSearchDialog = false
                                            }
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(course.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoTextPrimary)
                                            Text(course.currentChapter, fontSize = 11.sp, color = BentoTextSecondary)
                                        }
                                        Text("Go", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = BentoPremiumBtn)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Notifications Inbox Dialog ---
    if (showNotificationDialog) {
        Dialog(onDismissRequest = { showNotificationDialog = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = BentoCardWhite,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(BorderStroke(1.dp, BentoCardBorder), RoundedCornerShape(28.dp))
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Study Reminders", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BentoTextPrimary)
                        IconButton(onClick = { showNotificationDialog = false }) {
                            Icon(Icons.Default.Close, "Close", tint = BentoTextSecondary)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("In-App Study Alerts", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoTextPrimary)
                                Text("Remind me to study daily", fontSize = 11.sp, color = BentoTextSecondary)
                            }
                            Switch(checked = studyRemindersEnabled, onCheckedChange = { viewModel.toggleReminders() })
                        }
                        NotificationItem(title = "Daily study reminder ⏰", desc = "Stay consistent! Claim your streak or take a quiz to complete today's goals.", time = "1m ago")
                    }
                }
            }
        }
    }

    // --- Add Custom Subject Dialog ---
    if (showAddSubjectDialog) {
        Dialog(onDismissRequest = { showAddSubjectDialog = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = BentoCardWhite,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(BorderStroke(1.dp, BentoCardBorder), RoundedCornerShape(28.dp))
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Add Learning Subject", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BentoTextPrimary)
                    var title by remember { mutableStateOf("") }
                    var category by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Subject Title (e.g. Computer Vision)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        placeholder = { Text("Category (e.g. AI & ML)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(onClick = { showAddSubjectDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (title.trim().isNotEmpty()) {
                                    viewModel.addNewCourse(title, category)
                                    showAddSubjectDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPremiumBtn),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Add", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCounter(title: String, value: String, sub: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(title.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BentoTextSecondary)
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
        Text(sub, fontSize = 9.sp, color = BentoTextSecondary)
    }
}

@Composable
fun BadgeItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, unlocked: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.width(76.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (unlocked) BentoPremiumBg else BentoCardBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (unlocked) BentoPremiumText else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = title,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (unlocked) BentoTextPrimary else Color.Gray,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
