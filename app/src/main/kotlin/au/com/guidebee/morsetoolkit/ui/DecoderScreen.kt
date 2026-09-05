package au.com.guidebee.morsetoolkit.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import au.com.guidebee.morsetoolkit.activity.R
import au.com.guidebee.morsetoolkit.decoder.AudioMorseCodeDecoder
import au.com.guidebee.morsetoolkit.training.DecoderViewModel
import au.com.guidebee.morsetoolkit.ui.theme.WrongRed

/**
 * The audio decoder screen: mic capture ->
 * [au.com.guidebee.morsetoolkit.decoder.AudioMorseCodeDecoder] -> decoded
 * text, same flow as the legacy ListenActivity. All state lives in
 * [DecoderViewModel], which survives rotation/navigation instead of being
 * torn down and rebuilt with the composition. Two detection algorithms are
 * selectable (see [AudioMorseCodeDecoder.DetectionMode]) - Classic (the
 * original, frequency-agnostic broadband detector, and the default) and
 * Narrowband (a Goertzel-filter detector tuned to one tracked frequency,
 * better at rejecting broadband noise but only within its tracked band).
 * "Play Sample" plays res/raw/morse.wav out loud and turns the mic pipeline
 * on, so the decoder demonstrates itself acoustically exactly like it used to.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecoderScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: DecoderViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.setPermissionGranted(granted)
    }
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        viewModel.setPermissionGranted(granted)
        if (!granted) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.decoder)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!state.hasPermission) {
                Text(stringResource(R.string.decoder_permission_denied), color = WrongRed)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.power))
                    Switch(
                        checked = state.isRecording,
                        enabled = state.hasPermission,
                        onCheckedChange = { viewModel.setRecording(it) }
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.analog))
                    Switch(checked = state.showAnalog, onCheckedChange = { viewModel.setShowAnalog(it) })
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.decoder_algorithm), style = MaterialTheme.typography.labelMedium)
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = state.detectionMode == AudioMorseCodeDecoder.DetectionMode.BROADBAND,
                        onClick = { viewModel.switchDetectionMode(AudioMorseCodeDecoder.DetectionMode.BROADBAND) },
                        shape = SegmentedButtonDefaults.itemShape(0, 2)
                    ) { Text(stringResource(R.string.decoder_mode_classic)) }
                    SegmentedButton(
                        selected = state.detectionMode == AudioMorseCodeDecoder.DetectionMode.NARROWBAND,
                        onClick = { viewModel.switchDetectionMode(AudioMorseCodeDecoder.DetectionMode.NARROWBAND) },
                        shape = SegmentedButtonDefaults.itemShape(1, 2)
                    ) { Text(stringResource(R.string.decoder_mode_narrowband)) }
                }
            }

            Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                val h = size.height
                val high = 400f
                val savedData = state.savedData
                val savedRawData = state.savedRawData
                if (state.isRecording && savedData.size > 1) {
                    for (i in 1 until savedData.size) {
                        val x0 = (i - 1) * state.xStep
                        val x1 = i * state.xStep
                        if (state.showAnalog && i < savedRawData.size) {
                            val y0 = h - (savedRawData[i - 1] / (state.maxMagnitude.toFloat() * high + 0.5f)) * h * state.yScale
                            val y1 = h - (savedRawData[i] / (state.maxMagnitude.toFloat() * high + 0.5f)) * h * state.yScale
                            drawLine(Color.White, Offset(x0, y0), Offset(x1, y1), strokeWidth = 1.5f)
                        }
                        val dy0 = h - (savedData[i - 1] / high) * h * state.yScale
                        val dy1 = h - (savedData[i] / high) * h * state.yScale
                        drawLine(Color.Yellow, Offset(x0, dy0), Offset(x1, dy1), strokeWidth = 2.5f)
                    }
                }
            }
            Text(stringResource(R.string.decoder_wpm, state.currentWpm), style = MaterialTheme.typography.labelMedium)

            Column {
                Text(stringResource(R.string.x_pos), style = MaterialTheme.typography.labelMedium)
                Slider(value = state.xStep, onValueChange = { viewModel.setXStep(it) }, valueRange = 1f..5f)
                Text(stringResource(R.string.y_pos), style = MaterialTheme.typography.labelMedium)
                Slider(value = state.yScale, onValueChange = { viewModel.setYScale(it) }, valueRange = 0.2f..2f)
            }

            OutlinedCard(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Column(Modifier.padding(12.dp).verticalScroll(rememberScrollState())) {
                    Text(
                        text = state.outputText.ifEmpty { stringResource(R.string.send_placeholder) },
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    enabled = state.hasPermission,
                    onClick = { if (state.isPlayingSample) viewModel.stopSample() else viewModel.playSample() }
                ) {
                    Text(stringResource(if (state.isPlayingSample) R.string.stop_sample else R.string.play_sample))
                }
                OutlinedButton(onClick = { viewModel.resetDecoder() }) {
                    Text(stringResource(R.string.reset))
                }
                OutlinedButton(onClick = { viewModel.clearOutput() }) {
                    Text(stringResource(R.string.clear))
                }
            }
        }
    }
}
