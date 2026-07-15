package com.akshaglobal.smartcallshield.presentation.ui.components

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun BannerAdView(modifier: Modifier = Modifier, isCollapsible: Boolean = true) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            AdView(ctx).apply {
                // Determine the adaptive banner size
                val displayMetrics = ctx.resources.displayMetrics
                val widthPixels = displayMetrics.widthPixels
                val density = displayMetrics.density
                val adWidth = (widthPixels / density).toInt()
                setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, adWidth))
                
                // Test ad unit ID for banners
                adUnitId = "ca-app-pub-7949207340789864/5113719707"
                
                val adRequestBuilder = AdRequest.Builder()
                
                if (isCollapsible) {
                    // Add extras for collapsible banner (expanded initially)
                    val extras = Bundle()
                    extras.putString("collapsible", "bottom")
                    adRequestBuilder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                }
                
                loadAd(adRequestBuilder.build())
            }
        }
    )
}
