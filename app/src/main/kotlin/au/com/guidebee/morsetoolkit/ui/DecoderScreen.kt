package au.com.guidebee.morsetoolkit.ui

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import au.com.guidebee.morsetoolkit.ConfigInfo
import au.com.guidebee.morsetoolkit.activity.R
import au.com.guidebee.morsetoolkit.decoder.MorseCodePatternMatch
import au.com.guidebee.morsetoolkit.training.AudioDecoderController
import au.com.guidebee.morsetoolkit.ui.theme.WrongRed

private const val WAVEFORM_WIDTH = 800

/**
 * The legacy audio decoder, unchanged underneath: mic capture ->
 * [au.com.guidebee.morsetoolkit.decoder.AudioMorseCodeDecoder]'s FFT tone
 * detection -> decoded text, same as ListenActivity. "Play Sample" plays
 * res/raw/morse.wav out loud and turns the mic pipeline on, so the decoder
 * demonstrates itself acoustically exactly like it used to.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecoderScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }
    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val controller = remember { AudioDecoderController(ConfigInfo.sampleRate, WAVEFORM_WIDTH) }

    var isRecording by remember { mutableStateOf(false) }
    var showAnalog by remember { mutableStateOf(false) }
    var xStep by remember { mutableFloatStateOf(2f) }
    var yScale by remember { mutableFloatStateOf(1f) }
    var outputText by remember { mutableStateOf("") }
    var lastChar by remember { mutableStateOf(' ') }
    var savedData by remember { mutableStateOf(listOf<Int>()) }
    var savedRawData by remember { mutableStateOf(listOf<Int>()) }
    var maxMagnitude by remember { mutableDoubleStateOf(1.0) }
    var currentWpm by remember { mutableFloatStateOf(0f) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlayingSample by remember { mutableStateOf(false) }

    fun setRecording(on: Boolean) {
        if (on && hasPermission) {
            isRecording = controller.start()
        } else {
            controller.stop()
            isRecording = false
            savedData = emptyList()
            savedRawData = emptyList()
        }
    }

    DisposableEffect(controller) {
        controller.decoder.addListener(object : MorseCodePatternMatch.MorseCodeListener {
            override fun onEmit(character: Char) {
                mainHandler.post {
                    if (!(character == ' ' && lastChar == ' ') && character != '^') {
                        outputText = (outputText + character).takeLast(6000)
                    }
                    lastChar = character
                }
            }

            override fun onCharStart() {}

            override fun onCharEnd(dotOrDash: String, length: Int) {
                if (dotOrDash == ".") {
                    mainHandler.post {
                        currentWpm = (((ConfigInfo.sampleRate / length.toFloat()) + 0.5f).toInt()) / 100f
                    }
                }
            }
        })
        controller.decoder.addDataBufferListener { rawData, transformedData ->
            mainHandler.post {
                savedData = transformedData.toList()
                savedRawData = rawData.toList()
                maxMagnitude = controller.decoder.maxMagnitude
            }
        }
        onDispose {
            controller.stop()
            mediaPlayer?.let {
                it.stop()
                it.release()
            }
        }
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
            if (!hasPermission) {
                Text(stringResource(R.string.decoder_permission_denied), color = WrongRed)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.power))
                    Switch(checked = isRecording, enabled = hasPermission, onCheckedChange = { setRecording(it) })
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.analog))
                    Switch(checked = showAnalog, onCheckedChange = { showAnalog = it })
                }
            }

            Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                val h = size.height
                val high = 400f
                if (isRecording && savedData.size > 1) {
                    for (i in 1 until savedData.size) {
                        val x0 = (i - 1) * xStep
                        val x1 = i * xStep
                        if (showAnalog && i < savedRawData.size) {
                            val y0 = h - (savedRawData[i - 1] / (maxMagnitude.toFloat() * high + 0.5f)) * h * yScale
                            val y1 = h - (savedRawData[i] / (maxMagnitude.toFloat() * high + 0.5f)) * h * yScale
                            drawLine(Color.White, Offset(x0, y0), Offset(x1, y1), strokeWidth = 1.5f)
                        }
                        val dy0 = h - (savedData[i - 1] / high) * h * yScale
                        val dy1 = h - (savedData[i] / high) * h * yScale
                        drawLine(Color.Yellow, Offset(x0, dy0), Offset(x1, dy1), strokeWidth = 2.5f)
                    }
                }
            }
            Text(stringResource(R.string.decoder_wpm, currentWpm), style = MaterialTheme.typography.labelMedium)

            Column {
                Text(stringResource(R.string.x_pos), style = MaterialTheme.typography.labelMedium)
                Slider(value = xStep, onValueChange = { xStep = it }, valueRange = 1f..5f)
                Text(stringResource(R.string.y_pos), style = MaterialTheme.typography.labelMedium)
                Slider(value = yScale, onValueChange = { yScale = it }, valueRange = 0.2f..2f)
            }

            OutlinedCard(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Column(Modifier.padding(12.dp).verticalScroll(rememberScrollState())) {
                    Text(
                        text = outputText.ifEmpty { stringResource(R.string.send_placeholder) },
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    enabled = hasPermission,
                    onClick = {
                        val playing = mediaPlayer
                        if (playing == null) {
                            controller.decoder.resetMaxMagnitude()
                            controller.decoder.changeDotLimit(23f)
                            if (!isRecording) setRecording(true)
                            val mp = MediaPlayer.create(context, R.raw.morse)
                            mp.setOnCompletionListener {
                                it.stop()
                                it.release()
                                mediaPlayer = null
                                isPlayingSample = false
                            }
                            mediaPlayer = mp
                            isPlayingSample = true
                            mp.start()
                        } else {
                            playing.stop()
                            playing.release()
                            mediaPlayer = null
                            isPlayingSample = false
                        }
                    }
                ) {
                    Text(stringResource(if (isPlayingSample) R.string.stop_sample else R.string.play_sample))
                }
                OutlinedButton(onClick = {
                    controller.decoder.resetMaxMagnitude()
                    controller.decoder.changeDotLimit(23f)
                }) {
                    Text(stringResource(R.string.reset))
                }
                OutlinedButton(onClick = { outputText = "" }) {
                    Text(stringResource(R.string.clear))
                }
            }
        }
    }
}
