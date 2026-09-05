@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package au.com.guidebee.morsetoolkit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import au.com.guidebee.morsetoolkit.ConfigInfo
import au.com.guidebee.morsetoolkit.activity.R
import au.com.guidebee.morsetoolkit.training.KochProgression
import au.com.guidebee.morsetoolkit.training.ThemeMode

/**
 * Every setting the app has, in one place: the new UI's own preferences
 * (theme, training reset) plus every control the legacy OptionActivity had
 * (transmit/receive mode, WPM, input speed, play-sound + volume, flashcard
 * order, sample rate) — same ConfigInfo static fields, same
 * ConfigInfo.saveConfiguration persistence, just one screen instead of two.
 *
 * Choice groups (WPM, sample rate, mode, etc.) render as FlowRow chips
 * rather than a single fixed Row, so they wrap onto extra lines instead of
 * overflowing off-screen on narrow devices.
 */
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val context = LocalContext.current
    var showResetConfirm by remember { mutableStateOf(false) }
    fun persist() = ConfigInfo.saveConfiguration(context)

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )

        SettingsCard(stringResource(R.string.settings_theme), Icons.Filled.Palette) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ThemeMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = themeMode == mode,
                        onClick = { onThemeModeChange(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = ThemeMode.entries.size)
                    ) {
                        Text(
                            when (mode) {
                                ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
                                ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
                                ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
                            }
                        )
                    }
                }
            }
        }

        SettingsCard(stringResource(R.string.transmit), Icons.Filled.Send) {
            ModeAndTypeControls(
                mode = ConfigInfo.transmitType,
                onModeChange = { ConfigInfo.transmitType = it; persist() },
                letterType = ConfigInfo.transmitLetterType,
                onLetterTypeChange = { ConfigInfo.transmitLetterType = it; persist() }
            )
        }

        SettingsCard(stringResource(R.string.receive), Icons.Filled.Hearing) {
            ModeAndTypeControls(
                mode = ConfigInfo.receiveType,
                onModeChange = { ConfigInfo.receiveType = it; persist() },
                letterType = ConfigInfo.receiveLetterType,
                onLetterTypeChange = { ConfigInfo.receiveLetterType = it; persist() }
            )
        }

        SettingsCard(stringResource(R.string.morse_code_keypad), Icons.Filled.Dialpad) {
            var wpmIndex by remember { mutableIntStateOf(ConfigInfo.morseReceiveWPM) }
            SettingLabel(stringResource(R.string.wpm))
            SingleChoiceChipGroup(
                options = listOf(
                    stringResource(R.string.wpm_10), stringResource(R.string.wpm_15),
                    stringResource(R.string.wpm_20), stringResource(R.string.wpm_25), stringResource(R.string.wpm_40)
                ),
                selectedIndex = wpmIndex,
                onSelect = { wpmIndex = it; ConfigInfo.morseReceiveWPM = it; persist() }
            )

            var inputSpeed by remember { mutableIntStateOf(ConfigInfo.morseInputSpeed) }
            val speedValues = listOf(1, 2, 4)
            SettingLabel(stringResource(R.string.input_speed))
            SingleChoiceChipGroup(
                options = listOf(
                    stringResource(R.string.speed_slow), stringResource(R.string.speed_normal), stringResource(R.string.speed_fast)
                ),
                selectedIndex = speedValues.indexOf(inputSpeed).coerceAtLeast(0),
                onSelect = { inputSpeed = speedValues[it]; ConfigInfo.morseInputSpeed = speedValues[it]; persist() }
            )

            var playAudio by remember { mutableStateOf(ConfigInfo.playAudio) }
            var volume by remember { mutableFloatStateOf(ConfigInfo.audioVolume.toFloat()) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.play_sound), modifier = Modifier.weight(1f))
                Switch(checked = playAudio, onCheckedChange = { playAudio = it; ConfigInfo.playAudio = it; persist() })
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    imageVector = if (volume < 50f) Icons.Filled.VolumeDown else Icons.Filled.VolumeUp,
                    contentDescription = null,
                    tint = if (playAudio) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline
                )
                Slider(
                    value = volume,
                    onValueChange = { volume = it; ConfigInfo.audioVolume = it.toInt(); persist() },
                    valueRange = 0f..99f,
                    enabled = playAudio,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        SettingsCard(stringResource(R.string.flash_card_section_title), Icons.Filled.Style) {
            var letterOnFront by remember { mutableStateOf(ConfigInfo.letterOnFront) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.letter_on_front), modifier = Modifier.weight(1f))
                Switch(checked = letterOnFront, onCheckedChange = { letterOnFront = it; ConfigInfo.letterOnFront = it; persist() })
            }
            var sequential by remember { mutableStateOf(ConfigInfo.flashCardType) }
            SingleChoiceChipGroup(
                options = listOf(stringResource(R.string.order), stringResource(R.string.random)),
                selectedIndex = if (sequential) 0 else 1,
                onSelect = { sequential = it == 0; ConfigInfo.flashCardType = sequential; persist() }
            )
        }

        SettingsCard(stringResource(R.string.decoder), Icons.Filled.GraphicEq) {
            SettingLabel(stringResource(R.string.sample_rate))
            val rates = listOf(88200, 44100, 22050, 11025)
            var sampleRate by remember { mutableIntStateOf(ConfigInfo.sampleRate) }
            SingleChoiceChipGroup(
                options = listOf(
                    stringResource(R.string.sample_rate_88200), stringResource(R.string.sample_rate_44100),
                    stringResource(R.string.sample_rate_22050), stringResource(R.string.sample_rate_11025)
                ).map { "$it Hz" },
                selectedIndex = rates.indexOf(sampleRate).coerceAtLeast(0),
                onSelect = { sampleRate = rates[it]; ConfigInfo.sampleRate = rates[it]; persist() }
            )
        }

        SettingsCard(stringResource(R.string.settings_reset_title), Icons.Filled.RestartAlt) {
            OutlinedButton(onClick = { showResetConfirm = true }) {
                Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(stringResource(R.string.settings_reset_button))
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text(stringResource(R.string.settings_reset_confirm_title)) },
            text = { Text(stringResource(R.string.settings_reset_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    KochProgression(context).reset()
                    showResetConfirm = false
                }) { Text(stringResource(R.string.settings_reset_confirm_action)) }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun ModeAndTypeControls(
    mode: Int,
    onModeChange: (Int) -> Unit,
    letterType: Int,
    onLetterTypeChange: (Int) -> Unit
) {
    var currentMode by remember { mutableIntStateOf(mode) }
    val modeValues = listOf(ConfigInfo.TYPE_LETTER, ConfigInfo.TYPE_WORD, ConfigInfo.TYPE_FREE_TEXT)
    SingleChoiceChipGroup(
        options = listOf(stringResource(R.string.letter), stringResource(R.string.word), stringResource(R.string.free_text)),
        selectedIndex = modeValues.indexOf(currentMode).coerceAtLeast(0),
        onSelect = { currentMode = modeValues[it]; onModeChange(modeValues[it]) }
    )

    var letters by remember { mutableStateOf(letterType and ConfigInfo.TYPE_LETTER_LETTER != 0) }
    var numbers by remember { mutableStateOf(letterType and ConfigInfo.TYPE_LETTER_NUMBER != 0) }
    var punctuation by remember { mutableStateOf(letterType and ConfigInfo.TYPE_LETTER_PUNCTUATION != 0) }
    fun updateLetterType() {
        var type = 0
        if (letters) type = type or ConfigInfo.TYPE_LETTER_LETTER
        if (numbers) type = type or ConfigInfo.TYPE_LETTER_NUMBER
        if (punctuation) type = type or ConfigInfo.TYPE_LETTER_PUNCTUATION
        onLetterTypeChange(type)
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = letters, onClick = { letters = !letters; updateLetterType() }, label = { Text(stringResource(R.string.letter)) })
        FilterChip(selected = numbers, onClick = { numbers = !numbers; updateLetterType() }, label = { Text(stringResource(R.string.number)) })
        FilterChip(selected = punctuation, onClick = { punctuation = !punctuation; updateLetterType() }, label = { Text(stringResource(R.string.punctuation)) })
    }
}

/** Single-choice picker that wraps onto extra lines instead of overflowing the screen width. */
@Composable
private fun SingleChoiceChipGroup(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEachIndexed { index, label ->
            FilterChip(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun SettingLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun SettingsCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            content()
        }
    }
}
