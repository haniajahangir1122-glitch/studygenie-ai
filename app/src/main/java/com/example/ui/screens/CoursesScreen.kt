package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Bookmarked
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudyViewModel

@Composable
fun CoursesScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val courses by viewModel.courses.collectAsState()
    val activeCourseId by viewModel.activeCourseId.collectAsState()
    
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "AI & ML", "Mobile Dev")

    val filteredCourses = if (selectedCategory == "All") {
        courses
    } else {
        courses.filter { it.category == selectedCategory }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBackground)
            .padding(horizontal = 16.dp, top = 20.dp)
    ) {
        // --- HEADER ---
        Text(
            text = "Courses",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = BentoTextPrimary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Select a course to set as active or tap to increase study progress.",
            fontSize = 13.sp,
            color = BentoTextSecondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // --- FILTER CHIPS ---
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = { Text(text = category, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BentoPremiumBtn,
                        selectedLabelColor = Color.White,
                        containerColor = BentoCardWhite,
                        labelColor = BentoTextPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = BentoCardBorder,
                        borderWidth = 1.dp,
                        enabled = true,
                        selected = isSelected
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("filter_chip_$category")
                )
            }
        }

        // --- COURSES LIST ---
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 88.dp) // Offset for bottom navigation bar
        ) {
            items(filteredCourses, key = { it.id }) { course ->
                val isActive = course.id == activeCourseId
                val animatedProgress by animateFloatAsState(targetValue = course.progress, label = "courseProgress")

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .border(
                            BorderStroke(
                                width = if (isActive) 2.dp else 1.dp,
                                color = if (isActive) BentoPremiumBtn else BentoCardBorder
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clickable { viewModel.selectActiveCourse(course.id) }
                        .testTag("course_item_${course.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) BentoCardBg else BentoCardWhite
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(BentoPremiumBg)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = course.category,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoPremiumText
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = course.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            // Active indicator
                            if (isActive) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(BentoPremiumBtn),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active Course",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progress: ${(course.progress * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoTextSecondary
                            )
                            Text(
                                text = "Difficulty: ${course.difficulty}",
                                fontSize = 11.sp,
                                color = if (course.difficulty == "Hard") Color(0xFFC00000) else BentoPremiumBtn
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Progress Bar
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
                                    .fillMaxWidth(fraction = animatedProgress)
                                    .clip(CircleShape)
                                    .background(BentoPremiumBtn)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = course.currentChapter,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = course.timeRemaining,
                                    fontSize = 10.sp,
                                    color = BentoTextSecondary
                                )
                            }
                            
                            if (isActive) {
                                Button(
                                    onClick = { viewModel.advanceActiveCourseProgress() },
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoPremiumBtn),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Study", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
