package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Pure Neutral Physical Liquid Glass Color Palette
// NO BLUE, NO GREEN, NO RED, NO PURPLE, NO ARBITRARY ACCENT COLORS

val GlassPureWhite = Color(0xFFFFFFFF)
val GlassPureBlack = Color(0xFF000000)

// Light Theme Glass Tokens
val GlassBackgroundLight = Color(0xFFF5F6F8)
val GlassCanvasLight = Color(0xFFECEEF2)
val GlassSurfaceLight = Color(0xE6FFFFFF)           // 90% opacity frosted glass
val GlassSurfaceElevatedLight = Color(0xF2FFFFFF)   // 95% opacity specular glass
val GlassSecondarySurfaceLight = Color(0xCCFFFFFF) // 80% opacity glass
val GlassSearchFieldLight = Color(0xD9FFFFFF)
val GlassBorderLight = Color(0x33000000)            // Subtle outer rim
val GlassHighlightLight = Color(0x80FFFFFF)         // Specular upper reflection
val GlassRimLight = Color(0x40FFFFFF)               // Fine translucent rim

val GlassTextPrimaryLight = Color(0xFF0F172A)
val GlassTextSecondaryLight = Color(0xFF475569)
val GlassTextTertiaryLight = Color(0xFF64748B)
val GlassDividerLight = Color(0x1F000000)

// Dark Theme Glass Tokens
val GlassBackgroundDark = Color(0xFF09090B)
val GlassCanvasDark = Color(0xFF000000)
val GlassSurfaceDark = Color(0x2EFFFFFF)            // 18% translucent glass
val GlassSurfaceElevatedDark = Color(0x47FFFFFF)    // 28% translucent glass
val GlassSecondarySurfaceDark = Color(0x24FFFFFF)   // 14% translucent glass
val GlassSearchFieldDark = Color(0x2BFFFFFF)
val GlassBorderDark = Color(0x38FFFFFF)             // Subtle specular rim
val GlassHighlightDark = Color(0x4DFFFFFF)          // Specular upper reflection
val GlassRimDark = Color(0x26FFFFFF)

val GlassTextPrimaryDark = Color(0xFFF8FAFC)
val GlassTextSecondaryDark = Color(0xFFCBD5E1)
val GlassTextTertiaryDark = Color(0xFF94A3B8)
val GlassDividerDark = Color(0x24FFFFFF)

// High-contrast Call Semantics (Apple & Google Standard Call Colors)
val CallEmerald = Color(0xFF34C759)
val CallCrimson = Color(0xFFFF3B30)
val CallAmber = Color(0xFFFF9500)
val CallBlue = Color(0xFF007AFF)

// Legacy alias mappings for strict backward-compatibility while enforcing neutral glass
val SalimWhite = GlassPureWhite
val SalimBackgroundLight = GlassBackgroundLight
val SalimSurfaceLight = GlassSurfaceLight
val SalimSecondarySurfaceLight = GlassSecondarySurfaceLight
val SalimSearchFieldLight = GlassSearchFieldLight
val SalimTextPrimaryLight = GlassTextPrimaryLight
val SalimTextSecondaryLight = GlassTextSecondaryLight
val SalimTextTertiaryLight = GlassTextTertiaryLight
val SalimDividerLight = GlassDividerLight

val SalimBackgroundDark = GlassBackgroundDark
val SalimSurfaceDark = GlassSurfaceDark
val SalimSecondarySurfaceDark = GlassSecondarySurfaceDark
val SalimSearchFieldDark = GlassSearchFieldDark
val SalimTextPrimaryDark = GlassTextPrimaryDark
val SalimTextSecondaryDark = GlassTextSecondaryDark
val SalimTextTertiaryDark = GlassTextTertiaryDark
val SalimDividerDark = GlassDividerDark

// STRICT OVERRIDE: Neutral glass tokens replacing all colored accents
val SalimBlue = Color(0xFF1C1C1E)         // Neutral graphite
val SalimGreen = Color(0xFF262628)        // Neutral dark glass
val SalimRed = Color(0xFF3A3A3C)          // Neutral deep slate
val SalimYellow = Color(0xFF505054)       // Neutral medium slate
val SalimGray = Color(0xFF8E8E93)
val SalimKeypadBackground = Color(0xCCFFFFFF)
val SalimKeypadBackgroundDark = Color(0x2EFFFFFF)
