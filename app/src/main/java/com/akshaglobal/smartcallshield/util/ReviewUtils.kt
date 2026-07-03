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

    /**
     * Attempts to launch the In-App Review flow.
     * Note: This API has strict quotas and often does nothing in debug builds.
     * If the request fails, it falls back to opening the Play Store listing.
     */
    fun launchReviewFlow(context: Context) {
        val activity = context.findActivity()
        if (activity == null) {
            Log.e(TAG, "Could not find Activity context. Redirecting to Play Store.")
            openPlayStoreListing(context)
            return
        }

        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()
        
        // Give feedback to the user
        Toast.makeText(context, "Opening Play Store...", Toast.LENGTH_SHORT).show()

        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener { _ ->
                    // Flow finished. Note: We don't know if the dialog actually showed.
                    Log.d(TAG, "In-app review flow execution finished.")
                    
                    // If this is a debug build, the dialog likely didn't show.
                    // To ensure the user can actually review, we can provide a fallback 
                    // if they click again, or just always open the listing if we suspect failure.
                    // For now, let's just log.
                }
            } else {
                Log.e(TAG, "In-app review request failed. Fallback to Store listing.", task.exception)
                openPlayStoreListing(context)
            }
        }
        
        // If the task takes too long, it might be stuck. 
        // In a real app, you might want to open the listing directly if this happens.
    }

    /**
     * Directly opens the app's listing page on the Google Play Store app or browser.
     */
    fun openPlayStoreListing(context: Context) {
        val packageName = context.packageName
        
        // Try opening with the Market URI (Direct to Play Store App)
        val marketUri = Uri.parse("market://details?id=$packageName")
        val marketIntent = Intent(Intent.ACTION_VIEW, marketUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or 
                     Intent.FLAG_ACTIVITY_NO_HISTORY or 
                     Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        }

        // Try opening with the HTTPS URL (Fallback to Browser/Play Store)
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
