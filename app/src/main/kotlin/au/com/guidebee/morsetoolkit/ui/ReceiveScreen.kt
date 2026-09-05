package au.com.guidebee.morsetoolkit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import au.com.guidebee.morsetoolkit.ConfigInfo
import au.com.guidebee.morsetoolkit.activity.R
import au.com.guidebee.morsetoolkit.helper.MorseEncoder
import au.com.guidebee.morsetoolkit.helper.MorseHelper
import au.com.guidebee.morsetoolkit.training.LetterDrill
import au.com.guidebee.morsetoolkit.training.LetterDrillResult
import au.com.guidebee.morsetoolkit.training.LetterRoundState
import au.com.guidebee.morsetoolkit.training.WordDrill
import au.com.guidebee.morsetoolkit.training.WordLetterResult
import au.com.guidebee.morsetoolkit.ui.theme.CorrectGreen
import au.com.guidebee.morsetoolkit.ui.theme.WrongRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class ReceiveTab { LETTER, WORD, FREE }

/**
 * The legacy "Receive" screens, unified: same drills as [TransmitScreen],
 * but the target is auto-played first via [MorseEncoder] at the legacy
 * fixed ConfigInfo.morseReceiveWPM speed — deliberately not the Koch
 * trainer's Farnsworth timing, this mode's sound shouldn't change.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveScreen(onBack: () -> Unit) {
    var tab by remember { mutableStateOf(ReceiveTab.LETTER) }
    val encoder = remember { MorseEncoder(ConfigInfo.morseReceiveWPM) }
    DisposableEffect(Unit) { onDispose { encoder.release() } }
    val scope = rememberCoroutineScope()
    fun play(text: String) {
        scope.launch(Dispatchers.Default) { encoder.playMorseCode(text) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.receive)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                SegmentedButton(
                    selected = tab == ReceiveTab.LETTER,
                    onClick = { tab = ReceiveTab.LETTER },
                    shape = SegmentedButtonDefaults.itemShape(0, 3)
                ) { Text(stringResource(R.string.letter)) }
                SegmentedButton(
                    selected = tab == ReceiveTab.WORD,
                    onClick = { tab = ReceiveTab.WORD },
                    shape = SegmentedButtonDefaults.itemShape(1, 3)
                ) { Text(stringResource(R.string.word)) }
                SegmentedButton(
                    selected = tab == ReceiveTab.FREE,
                    onClick = { tab = ReceiveTab.FREE },
                    shape = SegmentedButtonDefaults.itemShape(2, 3)
                ) { Text(stringResource(R.string.free_text)) }
            }
            when (tab) {
                ReceiveTab.LETTER -> ReceiveLetterDrill(onPlay = ::play)
                ReceiveTab.WORD -> ReceiveWordDrill(onPlay = ::play)
                ReceiveTab.FREE -> ReceiveFreeTextPlayback(onPlay = ::play)
            }
        }
    }
}

@Composable
private fun ReceiveLetterDrill(onPlay: (String) -> Unit) {
    val drill = remember { LetterDrill({ ConfigInfo.receiveLetterType }) }
    var target by remember { mutableStateOf(drill.next()) }
    var uiState by remember { mutableStateOf(LetterRoundState.WAITING) }
    var triesLeft by remember { mutableIntStateOf(LetterDrill.TRY_LIMIT) }

    val decoder = rememberMorseKeyDecoder { emitted ->
        if (uiState == LetterRoundState.WAITING) {
            when (drill.answer(emitted)) {
                LetterDrillResult.CORRECT -> uiState = LetterRoundState.CORRECT
                LetterDrillResult.RETRY -> triesLeft = LetterDrill.TRY_LIMIT - drill.tries
                LetterDrillResult.REVEAL -> uiState = LetterRoundState.REVEAL
            }
        }
    }

    LaunchedEffect(target) { onPlay(target.toString()) }

    LaunchedEffect(uiState) {
        when (uiState) {
            LetterRoundState.CORRECT -> delay(1500L)
            LetterRoundState.REVEAL -> delay(3000L)
            LetterRoundState.WAITING -> return@LaunchedEffect
        }
        target = drill.next()
        triesLeft = LetterDrill.TRY_LIMIT
        uiState = LetterRoundState.WAITING
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            stringResource(R.string.drill_score, drill.totalCorrect, drill.totalAttempts),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            stringResource(R.string.drill_tries_left, triesLeft),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))

        val color = when (uiState) {
            LetterRoundState.CORRECT -> CorrectGreen
            LetterRoundState.REVEAL -> WrongRed
            LetterRoundState.WAITING -> MaterialTheme.colorScheme.onSurfaceVariant
        }
        Text(
            if (uiState == LetterRoundState.WAITING) "?" else target.uppercaseChar().toString(),
            style = MaterialTheme.typography.displayLarge,
            color = color
        )
        if (uiState == LetterRoundState.REVEAL) {
            Text(
                MorseHelper.morseCodeData[target] ?: "",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily.Monospace,
                color = WrongRed
            )
        }

        TextButton(onClick = { onPlay(target.toString()) }) {
            Icon(Icons.Filled.VolumeUp, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(stringResource(R.string.koch_replay))
        }

        MorseKey(decoder = decoder)

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ReceiveWordDrill(onPlay: (String) -> Unit) {
    val drill = remember { WordDrill() }
    var target by remember { mutableStateOf(drill.next()) }
    var typed by remember { mutableStateOf("") }
    var lastWrong by remember { mutableStateOf<Char?>(null) }
    var completed by remember { mutableStateOf(false) }

    val decoder = rememberMorseKeyDecoder { emitted ->
        if (!completed) {
            when (drill.answer(emitted)) {
                WordLetterResult.CORRECT -> {
                    typed = drill.typedSoFar
                    lastWrong = null
                }
                WordLetterResult.WORD_COMPLETE -> {
                    typed = drill.typedSoFar
                    lastWrong = null
                    completed = true
                }
                WordLetterResult.WRONG -> lastWrong = emitted
            }
        }
    }

    LaunchedEffect(target) { onPlay(target) }

    LaunchedEffect(completed) {
        if (completed) {
            delay(1500L)
            target = drill.next()
            typed = ""
            lastWrong = null
            completed = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            stringResource(R.string.drill_score, drill.totalCorrect, drill.totalAttempts),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.weight(1f))

        TextButton(onClick = { onPlay(target) }) {
            Icon(Icons.Filled.VolumeUp, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(stringResource(R.string.koch_replay))
        }
        Row {
            Text(typed, color = CorrectGreen, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            lastWrong?.let {
                Text(it.uppercaseChar().toString(), color = WrongRed, style = MaterialTheme.typography.headlineMedium)
            }
        }

        MorseKey(decoder = decoder)

        Spacer(modifier = Modifier.weight(1f))
    }
}

/** Type arbitrary text, hear it played back — no key involved. Mirrors ReceiveFreePadActivity exactly. */
@Composable
private fun ReceiveFreeTextPlayback(onPlay: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            label = { Text(stringResource(R.string.receive_free_text_label)) }
        )
        Button(onClick = { if (text.isNotBlank()) onPlay(text) }, enabled = text.isNotBlank()) {
            Text(stringResource(R.string.play))
        }
    }
}
