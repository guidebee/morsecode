package au.com.guidebee.morsetoolkit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import au.com.guidebee.morsetoolkit.activity.R
import au.com.guidebee.morsetoolkit.helper.MorseHelper
import au.com.guidebee.morsetoolkit.training.FarnsworthTone
import au.com.guidebee.morsetoolkit.training.KochProgression
import au.com.guidebee.morsetoolkit.training.SessionScheduler
import au.com.guidebee.morsetoolkit.training.StreakTracker
import kotlinx.coroutines.delay

private enum class RoundFeedback { NONE, CORRECT, WRONG }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KochTrainerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val progression = remember { KochProgression(context) }
    val scheduler = remember { SessionScheduler(progression) }
    val streakTracker = remember { StreakTracker(context) }
    val tone = remember { FarnsworthTone() }
    DisposableEffect(Unit) { onDispose { tone.stop() } }

    var characterWpm by remember { mutableFloatStateOf(20f) }
    var effectiveWpm by remember { mutableFloatStateOf(10f) }
    var target by remember { mutableStateOf(scheduler.nextCharacter()) }
    var feedback by remember { mutableStateOf(RoundFeedback.NONE) }
    var unlockedBanner by remember { mutableStateOf<Char?>(null) }
    var correctCount by remember { mutableIntStateOf(0) }
    var roundCount by remember { mutableIntStateOf(0) }
    var unlockedSize by remember { mutableIntStateOf(progression.unlockedCharacters.size) }

    fun playTarget() {
        tone.play(target.toString(), characterWpm.toInt(), effectiveWpm.toInt().coerceAtMost(characterWpm.toInt()))
    }

    val decoder = rememberMorseKeyDecoder { emitted ->
        if (feedback == RoundFeedback.NONE) {
            val isCorrect = emitted == target
            feedback = if (isCorrect) RoundFeedback.CORRECT else RoundFeedback.WRONG
            roundCount += 1
            if (isCorrect) correctCount += 1
            val newlyUnlocked = progression.recordAnswer(target, isCorrect)
            streakTracker.recordSession(if (isCorrect) 10 else 2)
            unlockedBanner = newlyUnlocked
            unlockedSize = progression.unlockedCharacters.size
        }
    }

    fun nextRound() {
        feedback = RoundFeedback.NONE
        unlockedBanner = null
        target = scheduler.nextCharacter()
        playTarget()
    }

    LaunchedEffect(Unit) { playTarget() }

    LaunchedEffect(feedback) {
        if (feedback != RoundFeedback.NONE) {
            delay(if (feedback == RoundFeedback.CORRECT) 900L else 1800L)
            nextRound()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.koch_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = stringResource(R.string.koch_unlocked_count, unlockedSize),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(stringResource(R.string.koch_score, correctCount, roundCount), style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.weight(1f))

            unlockedBanner?.let { unlocked ->
                AssistChip(
                    onClick = {},
                    label = { Text(stringResource(R.string.koch_unlocked_banner, unlocked.uppercaseChar())) }
                )
            }

            val feedbackColor = when (feedback) {
                RoundFeedback.CORRECT -> MaterialTheme.colorScheme.secondary
                RoundFeedback.WRONG -> MaterialTheme.colorScheme.error
                RoundFeedback.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Text(
                text = when (feedback) {
                    RoundFeedback.NONE -> stringResource(R.string.koch_prompt)
                    RoundFeedback.CORRECT -> stringResource(R.string.koch_correct)
                    RoundFeedback.WRONG -> stringResource(
                        R.string.koch_wrong,
                        target.uppercaseChar(),
                        MorseHelper.morseCodeData[target] ?: ""
                    )
                },
                style = MaterialTheme.typography.headlineSmall,
                color = feedbackColor
            )

            MorseKey(decoder = decoder)

            TextButton(onClick = { playTarget() }) {
                Icon(Icons.Filled.VolumeUp, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.koch_replay))
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.koch_character_speed, characterWpm.toInt()))
                Slider(value = characterWpm, onValueChange = { characterWpm = it }, valueRange = 15f..30f, steps = 14)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.koch_effective_speed, effectiveWpm.toInt()))
                Slider(
                    value = effectiveWpm,
                    onValueChange = { effectiveWpm = it.coerceAtMost(characterWpm) },
                    valueRange = 5f..30f,
                    steps = 24
                )
            }
        }
    }
}
