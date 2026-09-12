package com.akshaglobal.smartcallshield.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.android.play.core.review.ReviewManagerFactory

object ReviewUtils {
    private const val TAG = "ReviewUtils"

    fun launchReviewFlow(context: Context) {
        val activity = context.findActivity()
        if (activity == null) {
            Log.e(TAG, "Could not find Activity context. Redirecting to Play Store.")
            openPlayStoreListing(context)
            return
        }

        val manager = ReviewManagerFactory.create(activity)
       val request = manager.requestReviewFlow()

       Toast.makeText(context, "Opening Play Store...", Toast.LENGTH_SHORT).show()

       request.addOnCompleteListener { task ->
           if (task.isSuccessful) {
               val reviewInfo = task.result
               val flow = manager.launchReviewFlow(activity, reviewInfo)
               flow.addOnCompleteListener { _ ->
                   Log.d(TAG, "In-app review flow execution finished.")
               }
           } else {
               Log.e(TAG, "In-app review request failed. Fallback to Store listing.", task.exception)
               openPlayStoreListing(context)
           }
       }
    }

    fun openPlayStoreListing(context: Context) {
        val packageName = context.packageName
        
        // Try opening with the Market URI (Direct to Play Store App)
        val marketUri = Uri.parse("market://details?id=$packageName")
        val marketIntent = Intent(Intent.ACTION_VIEW, marketUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        }

        val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(marketIntent)
        } catch (e: Exception) {
            Log.w(TAG, "Market intent failed, trying web intent.")
            try {
                context.startActivity(webIntent)
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to open Play Store link.", e2)
                Toast.makeText(context, "Could not open Play Store", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun Context.findActivity(): Activity? {
        var currentContext = this
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) return currentContext
            currentContext = currentContext.baseContext
        }
        return null
    }
}
