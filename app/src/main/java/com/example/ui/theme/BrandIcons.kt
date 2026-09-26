package com.example.ui.theme

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object BrandColors {
    val WhatsApp = Color(0xFF25D366)
    val WhatsAppDark = Color(0xFF128C7E)
    val Telegram = Color(0xFF24A1DE)
    val AppleRed = Color(0xFFFF3B30)
}

object BrandIcons {

    val WhatsApp: ImageVector
        get() {
            if (_whatsapp != null) return _whatsapp!!
            _whatsapp = materialIcon(name = "WhatsApp") {
                // Speech bubble with handset contour
                materialPath {
                    moveTo(12.0f, 2.0f)
                    curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                    curveTo(2.0f, 13.82f, 2.49f, 15.53f, 3.35f, 17.01f)
                    lineTo(2.0f, 22.0f)
                    lineTo(7.14f, 20.67f)
                    curveTo(8.58f, 21.52f, 10.24f, 22.0f, 12.0f, 22.0f)
                    curveTo(17.52f, 22.0f, 22.0f, 17.52f, 22.0f, 12.0f)
                    curveTo(22.0f, 6.48f, 17.52f, 2.0f, 12.0f, 2.0f)
                    close()

                    // Telephone handset inside
                    moveTo(16.92f, 14.94f)
                    curveTo(16.71f, 15.53f, 15.89f, 16.03f, 15.24f, 16.17f)
                    curveTo(14.79f, 16.27f, 14.21f, 16.34f, 12.26f, 15.53f)
                    curveTo(9.76f, 14.5f, 8.15f, 11.96f, 8.03f, 11.79f)
                    curveTo(7.91f, 11.63f, 7.02f, 10.45f, 7.02f, 9.23f)
                    curveTo(7.02f, 8.01f, 7.64f, 7.42f, 7.89f, 7.16f)
                    curveTo(8.14f, 6.9f, 8.43f, 6.84f, 8.64f, 6.84f)
                    curveTo(8.85f, 6.84f, 9.06f, 6.84f, 9.24f, 6.85f)
                    curveTo(9.43f, 6.86f, 9.68f, 6.78f, 9.94f, 7.4f)
                    curveTo(10.21f, 8.05f, 10.87f, 9.67f, 10.95f, 9.84f)
                    curveTo(11.03f, 10.01f, 11.07f, 10.22f, 10.95f, 10.46f)
                    curveTo(10.83f, 10.7f, 10.74f, 10.83f, 10.58f, 11.02f)
                    curveTo(10.41f, 11.21f, 10.23f, 11.43f, 10.08f, 11.61f)
                    curveTo(9.91f, 11.8f, 9.74f, 12.01f, 9.94f, 12.35f)
                    curveTo(10.14f, 12.69f, 10.82f, 13.8f, 11.83f, 14.7f)
                    curveTo(13.13f, 15.86f, 14.18f, 16.24f, 14.52f, 16.38f)
                    curveTo(14.86f, 16.52f, 15.06f, 16.49f, 15.26f, 16.25f)
                    curveTo(15.46f, 16.01f, 16.12f, 15.24f, 16.36f, 14.91f)
                    curveTo(16.6f, 14.58f, 16.84f, 14.62f, 17.13f, 14.73f)
                    curveTo(17.42f, 14.84f, 18.97f, 15.61f, 19.29f, 15.77f)
                    curveTo(19.61f, 15.93f, 19.82f, 16.01f, 19.89f, 16.14f)
                    curveTo(19.96f, 16.27f, 19.96f, 16.89f, 16.92f, 14.94f)
                    close()
                }
            }
            return _whatsapp!!
        }
    private var _whatsapp: ImageVector? = null

    val Telegram: ImageVector
        get() {
            if (_telegram != null) return _telegram!!
            _telegram = materialIcon(name = "Telegram") {
                materialPath {
                    moveTo(12.0f, 2.0f)
                    curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                    curveTo(2.0f, 17.52f, 6.48f, 22.0f, 12.0f, 22.0f)
                    curveTo(17.52f, 22.0f, 22.0f, 17.52f, 22.0f, 12.0f)
                    curveTo(22.0f, 6.48f, 17.52f, 2.0f, 12.0f, 2.0f)
                    close()

                    // Paper airplane
                    moveTo(17.0f, 7.0f)
                    lineTo(6.5f, 11.5f)
                    curveTo(5.7f, 11.8f, 5.7f, 12.3f, 6.4f, 12.5f)
                    lineTo(9.1f, 13.4f)
                    lineTo(15.3f, 9.5f)
                    curveTo(15.6f, 9.3f, 15.9f, 9.5f, 15.6f, 9.7f)
                    lineTo(10.6f, 14.2f)
                    lineTo(10.4f, 17.0f)
                    curveTo(10.7f, 17.0f, 10.8f, 16.9f, 11.0f, 16.7f)
                    lineTo(12.4f, 15.3f)
                    lineTo(15.3f, 17.4f)
                    curveTo(15.8f, 17.7f, 16.2f, 17.5f, 16.3f, 16.9f)
                    lineTo(18.2f, 8.0f)
                    curveTo(18.4f, 7.2f, 17.9f, 6.8f, 17.0f, 7.0f)
                    close()
                }
            }
            return _telegram!!
        }
    private var _telegram: ImageVector? = null
}
