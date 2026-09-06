package au.com.guidebee.morsetoolkit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import au.com.guidebee.morsetoolkit.activity.R
import au.com.guidebee.morsetoolkit.training.CallsignDrill
import au.com.guidebee.morsetoolkit.training.TutorialPreference
import au.com.guidebee.morsetoolkit.ui.theme.CorrectGreen

private val practiceTargets = listOf("cq", "sos", "de", "73", "rst")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendPracticeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var target by remember { mutableStateOf(practiceTargets.random()) }
    var typed by remember { mutableStateOf("") }

    val decoder = rememberMorseKeyDecoder { emitted ->
        typed += if (emitted == ' ') " " else emitted.uppercaseChar()
    }

    var showTutorial by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!TutorialPreference.hasSeen(context, "send")) {
            showTutorial = true
            TutorialPreference.markSeen(context, "send")
        }
    }

    Scaffold(
        topBar = {
            MorseTopBar(
                title = stringResource(R.string.send_title),
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showTutorial = true }) {
                        Icon(Icons.Filled.HelpOutline, contentDescription = stringResource(R.string.action_help))
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
            Text(stringResource(R.string.send_instructions), style = MaterialTheme.typography.bodyMedium)

            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.send_target_label), style = MaterialTheme.typography.labelMedium)
                    Text(target.uppercase(), style = MaterialTheme.typography.headlineMedium)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = typed.ifEmpty { stringResource(R.string.send_placeholder) },
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily.Monospace
            )

            if (typed.trim().equals(target, ignoreCase = true)) {
                Text(
                    text = stringResource(R.string.send_match),
                    color = CorrectGreen,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            MorseKey(decoder = decoder)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { typed = "" }) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.send_clear))
                }
                OutlinedButton(onClick = {
                    typed = ""
                    target = if (kotlin.random.Random.nextInt(10) < 3) {
                        CallsignDrill.generate().lowercase()
                    } else {
                        practiceTargets.random()
                    }
                }) {
                    Text(stringResource(R.string.send_new_target))
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }

    if (showTutorial) {
        TutorialDialog(
            title = stringResource(R.string.send_title),
            tips = listOf(
                TutorialTip(Icons.Filled.Flag, stringResource(R.string.tutorial_send_tip1)),
                TutorialTip(Icons.Filled.Visibility, stringResource(R.string.tutorial_send_tip2)),
                TutorialTip(Icons.Filled.Refresh, stringResource(R.string.tutorial_send_tip3))
            ),
            onDismiss = { showTutorial = false }
        )
    }
}
