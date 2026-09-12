package com.akshaglobal.smartcallshield.service.ai

import android.content.Context
import com.akshaglobal.smartcallshield.data.model.SpamDetectionResult
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpamDetectionModel @Inject constructor(
    private val context: Context
) {
    @Volatile
    private var interpreter: Interpreter? = null
    private val modelFileName = "spam_detection_model.tflite"
    private val initLock = Any()

    suspend fun detectSpam(phoneNumber: String): SpamDetectionResult {
       ensureInitialized()

       return try {
           val features = extractFeatures(phoneNumber)
           val confidence = runInference(features)

           val isSpam = confidence > 0.7f
           val category = categorizeSpam(phoneNumber, confidence)

           SpamDetectionResult(
               phoneNumber = phoneNumber,
               isSpam = isSpam,
               confidence = confidence,
               category = category,
               aiModel = "TensorFlow Lite"
           )
       } catch (_: Exception) {
           SpamDetectionResult(
               phoneNumber = phoneNumber,
               isSpam = false,
               confidence = 0.5f,
               category = "UNKNOWN"
           )
       }
    }

    private fun extractFeatures(phoneNumber: String): FloatArray {
        return FloatArray(10) { index ->
            when (index) {
                0 -> if (phoneNumber.startsWith("+1")) 1.0f else 0.0f
                1 -> (phoneNumber.filter { it.isDigit() }.length / 15f) // normalized length
                2 -> phoneNumber.filter { it == phoneNumber[0] }.length / phoneNumber.length.toFloat()
                3 -> 0.0f // Replace with real feature if available
                4 -> 0.0f
                5 -> 0.0f
                6 -> 0.0f
                7 -> 0.0f
                8 -> 0.0f
                9 -> 0.0f
                else -> 0.0f
            }
        }
    }

    private suspend fun runInference(features: FloatArray): Float {
       val interpreter = interpreter ?: return 0.5f
        val inputBuffer = ByteBuffer.allocateDirect(4 * features.size).order(ByteOrder.nativeOrder())
        features.forEach { inputBuffer.putFloat(it) }
        inputBuffer.rewind()
        val outputBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder())
        outputBuffer.rewind()
        interpreter.run(inputBuffer, outputBuffer)
        outputBuffer.rewind()
        return outputBuffer.float
    }

    private fun categorizeSpam(phoneNumber: String, confidence: Float): String {
        return when {
            confidence > 0.9f -> "ROBOCALL"
            confidence > 0.7f -> "LIKELY_SPAM"
            confidence > 0.5f -> "SUSPICIOUS"
            else -> "CLEAN"
        }
    }

    fun ensureInitialized() {
        if (interpreter != null) return

        synchronized(initLock) {
            if (interpreter != null) return
            try {
                val model = loadModelFile(modelFileName)
                interpreter = Interpreter(model)
            } catch (_: Exception) {
                interpreter = null
            }
        }
    }

    fun initialize() = ensureInitialized()

    private fun loadModelFile(fileName: String): ByteBuffer {
        val assetFileDescriptor = context.assets.openFd(fileName)
        val inputStream = assetFileDescriptor.createInputStream()
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun cleanup() {
        interpreter?.close()
        interpreter = null
    }
}
