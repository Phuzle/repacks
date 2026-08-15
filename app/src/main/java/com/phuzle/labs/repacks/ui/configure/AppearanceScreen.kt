package com.phuzle.labs.repacks.ui.configure

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phuzle.labs.repacks.data.prefs.ThemeMode
import com.phuzle.labs.repacks.ui.components.HudBackdrop
import com.phuzle.labs.repacks.ui.components.HudTopBar
import com.phuzle.labs.repacks.ui.components.NeonPanel
import com.phuzle.labs.repacks.ui.theme.NeonAcid

@Composable
fun AppearanceScreen(viewModel: ConfigureViewModel, onBack: () -> Unit) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        HudBackdrop(modifier = Modifier.padding(padding)) {
            Column(Modifier.fillMaxSize()) {
                HudTopBar(title = "Appearance", onBack = onBack, accent = NeonAcid)
                NeonPanel(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), accent = NeonAcid, glow = false) {
                    Text("Theme", style = MaterialTheme.typography.bodyLarge)
                    listOf(
                        ThemeMode.SYSTEM to "System",
                        ThemeMode.LIGHT to "Light",
                        ThemeMode.DARK_AMOLED to "Dark / AMOLED",
                    ).forEach { (mode, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = mode == prefs.themeMode, onClick = { viewModel.setThemeMode(mode) })
                            Text(label)
                        }
                    }
                }
            }
        }
    }
}
