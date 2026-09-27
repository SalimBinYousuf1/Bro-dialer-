package com.example.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R

object BrandColors {
    val WhatsApp = Color(0xFF25D366)
    val WhatsAppDark = Color(0xFF128C7E)
    val Telegram = Color(0xFF24A1DE)
    val AppleRed = Color(0xFFFF3B30)
}

object BrandIcons {

    /**
     * Exact Official WhatsApp Logo Composable rendering authentic vector drawable.
     */
    @Composable
    fun WhatsAppLogo(
        modifier: Modifier = Modifier,
        size: Dp = 24.dp
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_whatsapp_logo),
            contentDescription = "WhatsApp",
            modifier = modifier.size(size)
        )
    }

    val WhatsApp: ImageVector
        get() {
            if (_whatsapp != null) return _whatsapp!!
            _whatsapp = materialIcon(name = "WhatsApp") {
                materialPath {
                    // Outer green speech bubble contour
                    moveTo(12.04f, 2.0f)
                    curveTo(6.58f, 2.0f, 2.13f, 6.45f, 2.13f, 11.91f)
                    curveTo(2.13f, 13.66f, 2.59f, 15.36f, 3.45f, 16.86f)
                    lineTo(2.05f, 22.0f)
                    lineTo(7.3f, 20.63f)
                    curveTo(8.75f, 21.41f, 10.38f, 21.83f, 12.04f, 21.83f)
                    curveTo(17.5f, 21.83f, 21.95f, 17.38f, 21.95f, 11.92f)
                    curveTo(21.95f, 9.27f, 20.92f, 6.78f, 19.05f, 4.91f)
                    curveTo(17.18f, 3.03f, 14.69f, 2.0f, 12.04f, 2.0f)
                    close()

                    // Phone receiver silhouette cutout
                    moveTo(17.47f, 14.38f)
                    curveTo(17.17f, 14.23f, 15.71f, 13.51f, 15.44f, 13.41f)
                    curveTo(15.17f, 13.31f, 14.97f, 13.26f, 14.77f, 13.56f)
                    curveTo(14.57f, 13.86f, 14.0f, 14.56f, 13.82f, 14.76f)
                    curveTo(13.65f, 14.96f, 13.47f, 14.99f, 13.17f, 14.84f)
                    curveTo(12.87f, 14.69f, 11.91f, 14.37f, 10.77f, 13.36f)
                    curveTo(9.89f, 12.57f, 9.29f, 11.59f, 9.12f, 11.29f)
                    curveTo(8.94f, 10.99f, 9.1f, 10.83f, 9.25f, 10.68f)
                    curveTo(9.39f, 10.55f, 9.55f, 10.33f, 9.7f, 10.15f)
                    curveTo(9.85f, 9.98f, 9.9f, 9.85f, 10.0f, 9.65f)
                    curveTo(10.1f, 9.45f, 10.05f, 9.28f, 9.97f, 9.13f)
                    curveTo(9.9f, 8.98f, 9.3f, 7.5f, 9.05f, 6.9f)
                    curveTo(8.81f, 6.32f, 8.56f, 6.4f, 8.38f, 6.39f)
                    curveTo(8.21f, 6.38f, 8.01f, 6.38f, 7.81f, 6.38f)
                    curveTo(7.61f, 6.38f, 7.28f, 6.46f, 7.01f, 6.76f)
                    curveTo(6.73f, 7.06f, 5.96f, 7.78f, 5.96f, 9.26f)
                    curveTo(5.96f, 10.74f, 7.04f, 12.16f, 7.19f, 12.36f)
                    curveTo(7.34f, 12.56f, 9.31f, 15.59f, 12.33f, 16.89f)
                    curveTo(13.05f, 17.2f, 13.61f, 17.39f, 14.05f, 17.53f)
                    curveTo(14.77f, 17.76f, 15.43f, 17.73f, 15.95f, 17.65f)
                    curveTo(16.53f, 17.56f, 17.73f, 16.92f, 17.98f, 16.22f)
                    curveTo(18.23f, 15.52f, 18.23f, 14.92f, 18.15f, 14.79f)
                    curveTo(18.08f, 14.67f, 17.88f, 14.53f, 17.47f, 14.38f)
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
