package com.akshaglobal.smartcallshield.presentation.ui.theme

import androidx.compose.ui.graphics.Color

// --- PRANIX Brand Palette ---
val PranixNavy = Color(0xFF04101A)
val PranixDeepNavy = Color(0xFF02080D)
val PranixTeal = Color(0xFF00D2FF)
val PranixDarkTeal = Color(0xFF007B9E)
val PranixGreen = Color(0xFF4ADE80)
val PranixDarkGreen = Color(0xFF133F27)
val PranixGrey = Color(0xFF94A3B8)
val PranixDarkGrey = Color(0xFF475569)
val PranixLightGrey = Color(0xFFF8FAFC)

// --- Light Theme ---
val Primary = PranixDarkTeal
val OnPrimary = Color.White
val PrimaryContainer = Color(0xFFB2F5FF)
val OnPrimaryContainer = Color(0xFF001F29)

val Secondary = PranixDarkGreen
val OnSecondary = Color.White
val SecondaryContainer = Color(0xFFB9F1CD)
val OnSecondaryContainer = Color(0xFF00210E)

val Tertiary = PranixDarkGrey
val OnTertiary = Color.White
val TertiaryContainer = Color(0xFFE2E8F0)
val OnTertiaryContainer = Color(0xFF1E293B)

val Error = Color(0xFFBA1A1A)
val OnError = Color.White
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF410002)

val Background = Color(0xFF0A1929) // Dark Navy for "Light" theme (Dark mode focused app)
val OnBackground = Color.White
val Surface = Color(0xFF132333)
val OnSurface = Color.White
val SurfaceVariant = Color(0xFF1E293B)
val OnSurfaceVariant = PranixGrey
val Outline = PranixDarkGrey

// --- Dark Theme ---
val PrimaryDark = PranixTeal
val OnPrimaryDark = PranixDeepNavy
val PrimaryContainerDark = Color(0xFF004D5D)
val OnPrimaryContainerDark = Color(0xFFB2F5FF)

val SecondaryDark = PranixGreen
val OnSecondaryDark = Color(0xFF003917)
val SecondaryContainerDark = Color(0xFF005224)
val OnSecondaryContainerDark = Color(0xFF8DFF9F)

val TertiaryDark = PranixGrey
val OnTertiaryDark = PranixDeepNavy
val TertiaryContainerDark = PranixDarkGrey
val OnTertiaryContainerDark = PranixLightGrey

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = PranixNavy // Very Dark Navy (0xFF04101A)
val OnBackgroundDark = Color.White
val SurfaceDark = Color(0xFF0A1929)
val OnSurfaceDark = Color.White
val SurfaceVariantDark = Color(0xFF1E293B)
val OnSurfaceVariantDark = PranixGrey
val OutlineDark = PranixDarkGrey

// Branded helpers
val Success = PranixGreen
val Warning = Color(0xFFFBBF24)
val Info = PranixTeal
