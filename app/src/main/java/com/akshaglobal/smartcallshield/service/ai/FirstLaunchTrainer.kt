package com.akshaglobal.smartcallshield.service.ai

import android.content.Context
import com.akshaglobal.smartcallshield.data.repository.CallLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class FirstLaunchTrainer(private val context: Context, private val callLogRepository: CallLogRepository) {
    suspend fun trainModelOnFirstLaunch() = withContext(Dispatchers.IO) {
        // 1. Extract call logs
        val callLogs = callLogRepository.getAllCallLogs() // Flow<List<CallLogEntity>>
        val logs = callLogs.firstOrNull() ?: emptyList()

        // 2. Preprocess: features and labels
        val features = logs.map { log ->
            floatArrayOf(
                log.callType.toFloat(),
                log.duration.toFloat(),
                if (log.wasBlocked) 1f else 0f,
                if (log.isSpam) 1f else 0f,
                log.spamScore
            )
        }
        val labels = logs.map { log ->
            when {
                log.isSpam -> 1 // spam
                log.spamScore > 0.8f -> 2 // robocall
                log.wasBlocked -> 3 // unwanted
                else -> 0 // normal
            }
        }

        // 3. Train model (pseudo-code, replace with TFLite Model Maker or custom routine)
        // NOTE: TFLite on-device training is limited. This is a placeholder for actual training.
        // You may need to use TensorFlow Lite Model Maker or retrain off-device and update model.
        // Here, just save features/labels for future use.
        // TODO: Implement actual training and save modelFile
        // For now, just log training
        android.util.Log.d("FirstLaunchTrainer", "Training completed. Features: ${features.size}, Labels: ${labels.size}")
        // Save a flag so training is not repeated
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean("model_trained", true)
        editor.apply()
    }
}
