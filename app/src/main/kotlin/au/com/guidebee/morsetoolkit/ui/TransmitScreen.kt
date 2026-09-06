package au.com.guidebee.morsetoolkit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import au.com.guidebee.morsetoolkit.helper.MorseHelper
import au.com.guidebee.morsetoolkit.training.LetterDrill
import au.com.guidebee.morsetoolkit.training.LetterDrillResult
import au.com.guidebee.morsetoolkit.training.LetterRoundState
import au.com.guidebee.morsetoolkit.training.TutorialPreference
import au.com.guidebee.morsetoolkit.training.WordDrill
import au.com.guidebee.morsetoolkit.training.WordLetterResult
import au.com.guidebee.morsetoolkit.ui.theme.CorrectGreen
import au.com.guidebee.morsetoolkit.ui.theme.WrongRed
import kotlinx.coroutines.delay

private enum class TransmitTab { LETTER, WORD, FREE }

/**
 * The legacy "Transmit" screens, unified: you're shown a target (no audio
 * hint) and answer by tapping the key — you have to already know the code.
 * Same drill rules as TransmitLetterActivity/TransmitWordActivity, just
 * rehosted in Compose. Free-form sending routes to the existing Send
 * Practice screen instead of a third copy of the same sandbox.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransmitScreen(onBack: () -> Unit, onOpenSendPractice: () -> Unit) {
    val context = LocalContext.current
    var tab by remember { mutableStateOf(TransmitTab.LETTER) }

    var showTutorial by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!TutorialPreference.hasSeen(context, "transmit")) {
            showTutorial = true
            TutorialPreference.markSeen(context, "transmit")
        }
    }

    Scaffold(
        topBar = {
            MorseTopBar(
                title = stringResource(R.string.transmit),
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showTutorial = true }) {
                        Icon(Icons.Filled.HelpOutline, contentDescription = stringResource(R.string.action_help))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                SegmentedButton(
                    selected = tab == TransmitTab.LETTER,
                    onClick = { tab = TransmitTab.LETTER },
                    shape = SegmentedButtonDefaults.itemShape(0, 3)
                ) { Text(stringResource(R.string.letter)) }
                SegmentedButton(
                    selected = tab == TransmitTab.WORD,
                    onClick = { tab = TransmitTab.WORD },
                    shape = SegmentedButtonDefaults.itemShape(1, 3)
                ) { Text(stringResource(R.string.word)) }
                SegmentedButton(
                    selected = tab == TransmitTab.FREE,
                    onClick = { tab = TransmitTab.FREE },
                    shape = SegmentedButtonDefaults.itemShape(2, 3)
                ) { Text(stringResource(R.string.free_text)) }
            }
            when (tab) {
                TransmitTab.LETTER -> TransmitLetterDrill()
                TransmitTab.WORD -> TransmitWordDrill()
                TransmitTab.FREE -> FreeTextRouteCard(onOpenSendPractice)
            }
        }
    }

    if (showTutorial) {
        TutorialDialog(
            title = stringResource(R.string.transmit),
            tips = listOf(
                TutorialTip(Icons.Filled.SwapHoriz, stringResource(R.string.tutorial_transmit_tip1)),
                TutorialTip(Icons.Filled.Key, stringResource(R.string.tutorial_transmit_tip2)),
                TutorialTip(Icons.Filled.Send, stringResource(R.string.tutorial_transmit_tip3))
            ),
            onDismiss = { showTutorial = false }
        )
    }
}

@Composable
private fun TransmitLetterDrill() {
    val drill = remember { LetterDrill({ ConfigInfo.transmitLetterType }) }
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
            LetterRoundState.WAITING -> MaterialTheme.colorScheme.onSurface
        }
        Text(target.uppercaseChar().toString(), style = MaterialTheme.typography.displayLarge, color = color)
        if (uiState == LetterRoundState.REVEAL) {
            Text(
                MorseHelper.morseCodeData[target] ?: "",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily.Monospace,
                color = WrongRed
            )
        }

        MorseKey(decoder = decoder)

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TransmitWordDrill() {
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

        Text(target, style = MaterialTheme.typography.displaySmall)
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

@Composable
private fun FreeTextRouteCard(onOpenSendPractice: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.transmit_free_text_hint), style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onOpenSendPractice) { Text(stringResource(R.string.home_tool_send)) }
    }
}
