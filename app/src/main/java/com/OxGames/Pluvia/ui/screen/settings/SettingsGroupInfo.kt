package com.OxGames.Pluvia.ui.screen.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import com.OxGames.Pluvia.PrefManager
import com.OxGames.Pluvia.ui.component.dialog.MessageDialog
import com.OxGames.Pluvia.utils.Constants
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch

@Composable
fun SettingsGroupInfo() {
    var showTechnologyDialog by remember { mutableStateOf(false) }

    MessageDialog(
        visible = showTechnologyDialog,
        onDismissRequest = {
            showTechnologyDialog = false
        },
        onConfirmClick = {
            showTechnologyDialog = false
        },
        confirmBtnText = "Close",
        icon = Icons.Default.Info,
        title = "Libraries Used",
        message = """
                JavaSteam - github.com/Longi94/JavaSteam
                Winlator - github.com/brunodev85/winlator
                Ubuntu RootFs - releases.ubuntu.com/focal
                Wine - winehq.org
                Box86/Box64 - box86.org
                PRoot - proot-me.github.io
                Mesa (Turnip/Zink/VirGL) - mesa3d.org
                DXVK - github.com/doitsujin/dxvk
                VKD3D - gitlab.winehq.org/wine/vkd3d
                D8VK - github.com/AlpyneDreams/d8vk
                CNC DDraw - github.com/FunkyFr3sh/cnc-ddraw
        """.trimIndent(),
    )

    SettingsGroup(title = { Text(text = "Info") }) {
        val uriHandler = LocalUriHandler.current
        var askForTip by remember { mutableStateOf(!PrefManager.tipped) }

        SettingsMenuLink(
            title = { Text(text = "Source code") },
            subtitle = { Text(text = "View the source code of this project") },
            onClick = { uriHandler.openUri(Constants.Misc.GITHUB_LINK) },
        )

        SettingsMenuLink(
            title = { Text(text = "Libraries Used") },
            subtitle = { Text(text = "See what technologies make Pluvia possible") },
            onClick = { showTechnologyDialog = true },
        )

        SettingsMenuLink(
            title = { Text("Send tip") },
            subtitle = { Text(text = "Contribute to ongoing development") },
            icon = { Icon(imageVector = Icons.Filled.MonetizationOn, contentDescription = "Tip") },
            onClick = {
                uriHandler.openUri(Constants.Misc.DONATION_LINK)
                askForTip = false
                PrefManager.tipped = !askForTip
            },
        )

        SettingsSwitch(
            state = askForTip,
            title = { Text("Ask for tip on startup") },
            subtitle = { Text(text = "Stops the tip message from appearing") },
            onCheckedChange = {
                askForTip = it
                PrefManager.tipped = !askForTip
            },
        )

        SettingsMenuLink(
            title = { Text(text = "Privacy Policy") },
            subtitle = { Text(text = "Opens a link to Pluvia's privacy policy") },
            onClick = {
                uriHandler.openUri(Constants.Misc.PRIVACY_LINK)
            },
        )
    }
}
