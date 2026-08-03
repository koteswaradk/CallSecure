package com.akshaglobal.smartcallshield.presentation.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akshaglobal.smartcallshield.R
import androidx.core.content.edit

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun IntroScreen(context: Context, onFinish: () -> Unit) {
    // Use only fallback drawable icons for intro pages
    val pages: List<Triple<String, String, Int>> = listOf(
        Triple("Stay Focused While Driving", "Automatically respond to calls with custom SMS messages while you drive.", R.drawable.ic_mode_driving),
        Triple("Smart Call Modes", "Select who can reach you. Choose between Normal, Family, and Emergency modes.", R.drawable.ic_mode_family),
        Triple("AI Spam Protection", "Secondary layer of defense. Keep unwanted robocalls away with AI detection.", R.drawable.ic_spam_block),
        Triple("Detailed Analytics", "Track your protection history and auto-reply activity in real-time.", R.drawable.ic_blocked_call)
    )
    var currentPage by remember { mutableStateOf(0) }

    // Gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) }
                ) { page ->
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Only fallback image
                        Image(
                            painter = painterResource(id = pages[page].third),
                            contentDescription = pages[page].first,
                            modifier = Modifier.size(160.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            pages[page].first,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            pages[page].second,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            // Pager indicator
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth()
            ) {
                repeat(pages.size) { i ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (i == currentPage) 14.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (i == currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentPage > 0) {
                    Button(onClick = { currentPage-- }, modifier = Modifier.width(120.dp)) { Text("Previous") }
                } else {
                    Spacer(modifier = Modifier.width(120.dp))
                }
                if (currentPage < pages.size - 1) {
                    Button(onClick = { currentPage++ }, modifier = Modifier.width(120.dp)) { Text("Next") }
                } else {
                    Button(onClick = {
                        val prefs = context.getSharedPreferences("smartcallshield_prefs", Context.MODE_PRIVATE)
                        prefs.edit { putBoolean("intro_shown", true) }
                        onFinish()
                    }, modifier = Modifier.width(120.dp)) { Text("Finish") }
                }
            }
        }
    }
}

// NOTE: To add Lottie animations in the future, place intro_*.json in res/raw and update the code to use them.
