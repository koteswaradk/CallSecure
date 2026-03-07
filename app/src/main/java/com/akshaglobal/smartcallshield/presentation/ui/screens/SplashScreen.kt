package com.akshaglobal.smartcallshield.presentation.ui.screens

import android.content.Context
import android.provider.CallLog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.akshaglobal.smartcallshield.service.TFLiteSpamDetector
import com.akshaglobal.smartcallshield.data.database.SmartCallShieldDatabase

@Composable
fun SplashScreen(context: Context, onComplete: () -> Unit) {
    var loading by remember { mutableStateOf(true) }
    var progressText by remember { mutableStateOf("Initializing...") }

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            progressText = "Reading call history..."
            val callLogUri = CallLog.Calls.CONTENT_URI
            val projection = arrayOf(CallLog.Calls.NUMBER)
            val cursor = context.contentResolver.query(callLogUri, projection, null, null, null)
            val numbers = mutableListOf<String>()
            cursor?.let {
                val numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                while (cursor.moveToNext()) {
                    val number = cursor.getString(numberIndex)
                    numbers.add(number)
                }
                cursor.close()
            }
            progressText = "Running spam detection..."
            val detector = TFLiteSpamDetector(context)
            val db = SmartCallShieldDatabase.getDatabase(context)
            numbers.forEach { number ->
                val prediction = detector.predict(number)
                // TODO: Replace with actual DB insert logic for call log and prediction
                // Example: db.callLogDao().insert(CallLogEntity(number = number, prediction = prediction))
            }
            detector.close()
            progressText = "Done!"
            loading = false
            onComplete()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (loading) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(progressText, fontSize = 18.sp)
            }
        }
    }
}
