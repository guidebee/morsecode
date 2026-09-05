package au.com.guidebee.morsetoolkit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
 */
@OptIn(ExperimentalMaterial3Api::class)
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
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)

        SettingsSection(stringResource(R.string.settings_theme)) {
            SingleChoiceSegmentedButtonRow {
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

        HorizontalDivider()

        SettingsSection(stringResource(R.string.transmit)) {
            ModeAndTypeControls(
                mode = ConfigInfo.transmitType,
                onModeChange = { ConfigInfo.transmitType = it; persist() },
                letterType = ConfigInfo.transmitLetterType,
                onLetterTypeChange = { ConfigInfo.transmitLetterType = it; persist() }
            )
        }

        HorizontalDivider()

        SettingsSection(stringResource(R.string.receive)) {
            ModeAndTypeControls(
                mode = ConfigInfo.receiveType,
                onModeChange = { ConfigInfo.receiveType = it; persist() },
                letterType = ConfigInfo.receiveLetterType,
                onLetterTypeChange = { ConfigInfo.receiveLetterType = it; persist() }
            )
        }

        HorizontalDivider()

        SettingsSection(stringResource(R.string.morse_code_keypad)) {
            Text(stringResource(R.string.wpm), style = MaterialTheme.typography.labelLarge)
            var wpmIndex by remember { mutableIntStateOf(ConfigInfo.morseReceiveWPM) }
            RadioOptionRow(
                options = listOf(
                    stringResource(R.string.wpm_10), stringResource(R.string.wpm_15),
                    stringResource(R.string.wpm_20), stringResource(R.string.wpm_25), stringResource(R.string.wpm_40)
                ),
                selectedIndex = wpmIndex,
                onSelect = { wpmIndex = it; ConfigInfo.morseReceiveWPM = it; persist() }
            )

            Text(stringResource(R.string.input_speed), style = MaterialTheme.typography.labelLarge)
            var inputSpeed by remember { mutableIntStateOf(ConfigInfo.morseInputSpeed) }
            val speedValues = listOf(1, 2, 4)
            RadioOptionRow(
                options = listOf(
                    stringResource(R.string.speed_slow), stringResource(R.string.speed_normal), stringResource(R.string.speed_fast)
                ),
                selectedIndex = speedValues.indexOf(inputSpeed).coerceAtLeast(0),
                onSelect = { inputSpeed = speedValues[it]; ConfigInfo.morseInputSpeed = speedValues[it]; persist() }
            )

            var playAudio by remember { mutableStateOf(ConfigInfo.playAudio) }
            var volume by remember { mutableFloatStateOf(ConfigInfo.audioVolume.toFloat()) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = playAudio, onCheckedChange = { playAudio = it; ConfigInfo.playAudio = it; persist() })
                Text(stringResource(R.string.play_sound))
            }
            Slider(
                value = volume,
                onValueChange = { volume = it; ConfigInfo.audioVolume = it.toInt(); persist() },
                valueRange = 0f..99f
            )
        }

        HorizontalDivider()

        SettingsSection(stringResource(R.string.flash_card_section_title)) {
            var letterOnFront by remember { mutableStateOf(ConfigInfo.letterOnFront) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = letterOnFront, onCheckedChange = { letterOnFront = it; ConfigInfo.letterOnFront = it; persist() })
                Text(stringResource(R.string.letter_on_front))
            }
            var sequential by remember { mutableStateOf(ConfigInfo.flashCardType) }
            RadioOptionRow(
                options = listOf(stringResource(R.string.order), stringResource(R.string.random)),
                selectedIndex = if (sequential) 0 else 1,
                onSelect = { sequential = it == 0; ConfigInfo.flashCardType = sequential; persist() }
            )
        }

        HorizontalDivider()

        SettingsSection(stringResource(R.string.decoder)) {
            Text(stringResource(R.string.sample_rate), style = MaterialTheme.typography.labelLarge)
            val rates = listOf(88200, 44100, 22050, 11025)
            var sampleRate by remember { mutableIntStateOf(ConfigInfo.sampleRate) }
            RadioOptionRow(
                options = listOf(
                    stringResource(R.string.sample_rate_88200), stringResource(R.string.sample_rate_44100),
                    stringResource(R.string.sample_rate_22050), stringResource(R.string.sample_rate_11025)
                ),
                selectedIndex = rates.indexOf(sampleRate).coerceAtLeast(0),
                onSelect = { sampleRate = rates[it]; ConfigInfo.sampleRate = rates[it]; persist() }
            )
        }

        HorizontalDivider()

        SettingsSection(stringResource(R.string.settings_reset_title)) {
            OutlinedButton(onClick = { showResetConfirm = true }) {
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
    RadioOptionRow(
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
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = letters, onCheckedChange = { letters = it; updateLetterType() })
        Text(stringResource(R.string.letter))
        Checkbox(checked = numbers, onCheckedChange = { numbers = it; updateLetterType() })
        Text(stringResource(R.string.number))
        Checkbox(checked = punctuation, onCheckedChange = { punctuation = it; updateLetterType() })
        Text(stringResource(R.string.punctuation))
    }
}

@Composable
private fun RadioOptionRow(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        options.forEachIndexed { index, label ->
            RadioButton(selected = index == selectedIndex, onClick = { onSelect(index) })
            Text(label)
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}
