package com.akshaglobal.smartcallshield.service

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.FileInputStream

class TFLiteSpamDetector(context: Context) {
    private var interpreter: Interpreter? = null

    init {
        try {
            val model = loadModelFile(context)
            interpreter = Interpreter(model)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd("spam_detection.tflite")
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // Improved: Extract real features from phone number for the model
    private fun extractFeatures(phoneNumber: String): FloatArray {
        val digits = phoneNumber.filter { it.isDigit() }
        return FloatArray(10) { index ->
            when (index) {
                0 -> if (phoneNumber.startsWith("+1")) 1.0f else 0.0f // US number
                1 -> digits.length / 15f // normalized length predict and use with the
                2 -> digits.take(3).toFloatOrNull() ?: 0f // area code as float
                3 -> digits.takeLast(4).toFloatOrNull() ?: 0f // last 4 digits
                4 -> if (digits.toCharArray().toSet().size < 4) 1.0f else 0.0f // repeated digits
                5 -> if (digits.startsWith("800")) 1.0f else 0.0f // toll-free
                6 -> if (digits.startsWith("900")) 1.0f else 0.0f // premium
                7 -> if (digits.length == 10) 1.0f else 0.0f // standard US
                8 -> if (digits.length < 7) 1.0f else 0.0f // short/unknown
                9 -> 0.0f // reserved for future
                else -> 0.0f
            }
        }
    }

    // Example: input is a float array of features, output is a float array of probabilities
    fun predict(phoneNumber: String): Int {
        val features = extractFeatures(phoneNumber)
        val input = arrayOf(features)
        val output = Array(1) { FloatArray(4) } // 4 classes: safe, spam, robocall, unknown
        interpreter?.run(input, output)
        val prediction = output[0].indices.maxByOrNull { output[0][it] } ?: 0
        android.util.Log.d("TFLiteSpamDetector", "Prediction for $phoneNumber: $prediction, probs=${output[0].joinToString()}")
        return prediction
    }

    fun close() {
        interpreter?.close()
    }
}
