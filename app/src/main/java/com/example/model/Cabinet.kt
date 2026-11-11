package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.R

enum class Cabinet(
    val id: String,
    val title: String,
    val subtitle: String,
    val primaryColor: Color,
    val accentColor: Color,
    val surfaceColor: Color,
    val borderColor: Color,
    val drawableRes: Int
) {
    SUDO_HERO(
        id = "sudo-hero",
        title = "SUDO HERO",
        subtitle = "RISE OF THE ROOT USER",
        primaryColor = Color(0xFFB26BFF),
        accentColor = Color(0xFFFF7AD9),
        surfaceColor = Color(0xFF1D1038),
        borderColor = Color(0xFF8A3DFF),
        drawableRes = R.drawable.scene_sudo_hero
    ),
    WIFI_WIZARDS(
        id = "wifi-wizards",
        title = "WIFI WIZARDS",
        subtitle = "SIGNAL SORCERY",
        primaryColor = Color(0xFFFF3EA0),
        accentColor = Color(0xFF4EE2FF),
        surfaceColor = Color(0xFF230E36),
        borderColor = Color(0xFFFF3EA0),
        drawableRes = R.drawable.scene_wifi_wizards
    ),
    HONKERS_HACKERS(
        id = "honkers-hackers",
        title = "HONKERS & HACKERS",
        subtitle = "OPERATION SKOGAI",
        primaryColor = Color(0xFF4EE2FF),
        accentColor = Color(0xFFFFD23F),
        surfaceColor = Color(0xFF0B2138),
        borderColor = Color(0xFF2FA8C8),
        drawableRes = R.drawable.scene_honkers_hackers
    ),
    SKOGNET_SYNDICATE(
        id = "skognet-syndicate",
        title = "SKOG-NET SYNDICATE",
        subtitle = "CITY-FOREST LINK",
        primaryColor = Color(0xFF3DFF9E),
        accentColor = Color(0xFFFF3EA0),
        surfaceColor = Color(0xFF0B241B),
        borderColor = Color(0xFF2FB87A),
        drawableRes = R.drawable.scene_skognet_syndicate
    );

    companion object {
        fun fromId(id: String): Cabinet =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: SUDO_HERO
    }
}
