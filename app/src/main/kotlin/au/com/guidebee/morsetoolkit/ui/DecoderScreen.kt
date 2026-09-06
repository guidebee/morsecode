package au.com.guidebee.morsetoolkit.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import au.com.guidebee.morsetoolkit.activity.R
import au.com.guidebee.morsetoolkit.decoder.AudioMorseCodeDecoder
import au.com.guidebee.morsetoolkit.training.DecoderViewModel
import au.com.guidebee.morsetoolkit.ui.theme.ScopeAmber
import au.com.guidebee.morsetoolkit.ui.theme.ScopeBezel
import au.com.guidebee.morsetoolkit.ui.theme.ScopeBezelDark
import au.com.guidebee.morsetoolkit.ui.theme.ScopeBezelHighlight
import au.com.guidebee.morsetoolkit.ui.theme.ScopeGrid
import au.com.guidebee.morsetoolkit.ui.theme.ScopeLedOff
import au.com.guidebee.morsetoolkit.ui.theme.ScopePanelText
import au.com.guidebee.morsetoolkit.ui.theme.ScopePanelTextDim
import au.com.guidebee.morsetoolkit.ui.theme.ScopePhosphor
import au.com.guidebee.morsetoolkit.ui.theme.ScopeScreen
import au.com.guidebee.morsetoolkit.ui.theme.WrongRed
import kotlin.math.cos
import kotlin.math.sin

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
 *
 * The panel below the app bar is styled as a classic bench oscilloscope -
 * a dark instrument bezel, a phosphor-green graticule screen, and a single
 * compact control bank above the screen holding X-POS/Y-POS (rotary-look
 * dial + horizontal fader) and POWER/ANALOG (LED + switch), all stacked in
 * one tight column, so most of the panel's height still goes to the scope
 * screen and the decoded-text terminal beneath. The screen locks itself to
 * portrait for the sake of this compact layout - landscape isn't handled
 * yet.
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

    val activity = context.findActivity()
    DisposableEffect(activity) {
        val previousOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            activity?.requestedOrientation = previousOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Scaffold(
        topBar = {
            MorseTopBar(
                title = stringResource(R.string.decoder),
                onBack = onBack,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ScopeBezelDark,
                    titleContentColor = ScopePanelText,
                    navigationIconContentColor = ScopePanelText
                )
            )
        },
        containerColor = ScopeBezelDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!state.hasPermission) {
                Text(
                    stringResource(R.string.decoder_permission_denied),
                    color = WrongRed,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            ReadoutRow(state = state, viewModel = viewModel)
            ScopeDisplay(state = state, modifier = Modifier.fillMaxWidth().height(170.dp))
            ControlPanel(state = state, viewModel = viewModel)
            OutputTerminal(text = state.outputText, modifier = Modifier.fillMaxWidth().weight(1f))
            ActionButtonRow(state = state, viewModel = viewModel)
        }
    }
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

/** The scope's screen: dark phosphor graticule with the analog/digital traces. */
@Composable
private fun ScopeDisplay(state: DecoderViewModel.UiState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Brush.linearGradient(listOf(ScopeBezelHighlight, ScopeBezel, ScopeBezelDark)))
            .padding(8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(2.dp))
                .background(ScopeScreen)
        ) {
            val w = size.width
            val h = size.height

            // Graticule: a faint 10x8 grid, like a real scope's screen.
            val cols = 10
            val rows = 8
            for (c in 0..cols) {
                val x = w * c / cols
                drawLine(ScopeGrid.copy(alpha = if (c == cols / 2) 0.55f else 0.25f), Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
            }
            for (r in 0..rows) {
                val y = h * r / rows
                drawLine(ScopeGrid.copy(alpha = if (r == rows / 2) 0.55f else 0.25f), Offset(0f, y), Offset(w, y), strokeWidth = 1f)
            }

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
                        // Faint wide stroke under the sharp one fakes a phosphor glow.
                        drawLine(ScopePhosphor.copy(alpha = 0.25f), Offset(x0, y0), Offset(x1, y1), strokeWidth = 4f)
                        drawLine(ScopePhosphor.copy(alpha = 0.85f), Offset(x0, y0), Offset(x1, y1), strokeWidth = 1.5f)
                    }
                    val dy0 = h - (savedData[i - 1] / high) * h * state.yScale
                    val dy1 = h - (savedData[i] / high) * h * state.yScale
                    drawLine(ScopeAmber.copy(alpha = 0.35f), Offset(x0, dy0), Offset(x1, dy1), strokeWidth = 5f)
                    drawLine(ScopeAmber, Offset(x0, dy0), Offset(x1, dy1), strokeWidth = 2.2f)
                }
            }
        }
    }
}

@Composable
private fun ReadoutRow(state: DecoderViewModel.UiState, viewModel: DecoderViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(ScopeBezel)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(ScopeScreen)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.decoder_wpm, state.currentWpm),
                color = ScopeAmber,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
        }
        Text(
            stringResource(R.string.decoder_algorithm),
            style = MaterialTheme.typography.labelSmall,
            color = ScopePanelTextDim
        )
        SingleChoiceSegmentedButtonRow {
            SegmentedButton(
                selected = state.detectionMode == AudioMorseCodeDecoder.DetectionMode.BROADBAND,
                onClick = { viewModel.switchDetectionMode(AudioMorseCodeDecoder.DetectionMode.BROADBAND) },
                shape = SegmentedButtonDefaults.itemShape(0, 2),
                colors = scopeSegmentedColors(),
                icon = {}
            ) { Text(stringResource(R.string.decoder_mode_classic), style = MaterialTheme.typography.labelSmall) }
            SegmentedButton(
                selected = state.detectionMode == AudioMorseCodeDecoder.DetectionMode.NARROWBAND,
                onClick = { viewModel.switchDetectionMode(AudioMorseCodeDecoder.DetectionMode.NARROWBAND) },
                shape = SegmentedButtonDefaults.itemShape(1, 2),
                colors = scopeSegmentedColors(),
                icon = {}
            ) { Text(stringResource(R.string.decoder_mode_narrowband), style = MaterialTheme.typography.labelSmall) }
        }
    }
}

@Composable
private fun scopeSegmentedColors() = SegmentedButtonDefaults.colors(
    activeContainerColor = ScopeAmber,
    activeContentColor = Color.Black,
    activeBorderColor = ScopeBezelHighlight,
    inactiveContainerColor = ScopeScreen,
    inactiveContentColor = ScopePanelTextDim,
    inactiveBorderColor = ScopeBezelHighlight
)

/**
 * The instrument's control bank, three tight rows: POWER and ANALOG toggles
 * side by side on top, then X-POS and Y-POS each on their own full-width
 * row below.
 */
@Composable
private fun ControlPanel(
    state: DecoderViewModel.UiState,
    viewModel: DecoderViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(ScopeBezel)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ScopeToggleRow(
                label = stringResource(R.string.power),
                checked = state.isRecording,
                enabled = state.hasPermission,
                onCheckedChange = { viewModel.setRecording(it) },
                modifier = Modifier.weight(1f)
            )
            ScopeToggleRow(
                label = stringResource(R.string.analog),
                checked = state.showAnalog,
                onCheckedChange = { viewModel.setShowAnalog(it) },
                modifier = Modifier.weight(1f)
            )
        }
        ScopeKnobRow(
            label = stringResource(R.string.x_pos),
            value = state.xStep,
            valueRange = 1f..5f,
            onValueChange = { viewModel.setXStep(it) }
        )
        ScopeKnobRow(
            label = stringResource(R.string.y_pos),
            value = state.yScale,
            valueRange = 0.2f..2f,
            onValueChange = { viewModel.setYScale(it) }
        )
    }
}

private val ControlLabelWidth = 50.dp

/** One row of the control bank: a fixed-width label, a small rotary-look dial, and a horizontal fader for input. */
@Composable
private fun ScopeKnobRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = ScopePanelTextDim,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.width(ControlLabelWidth)
        )
        Canvas(modifier = Modifier.size(26.dp)) {
            val fraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
            val startDeg = 135f
            val sweepDeg = 270f
            val angleRad = Math.toRadians((startDeg + sweepDeg * fraction).toDouble())
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                brush = Brush.radialGradient(listOf(ScopeBezelHighlight, ScopeBezelDark), center = center, radius = radius),
                radius = radius,
                center = center
            )
            drawCircle(color = Color.Black.copy(alpha = 0.5f), radius = radius, center = center, style = Stroke(width = 1f))

            val pointerLen = radius * 0.75f
            drawLine(
                ScopeAmber,
                center,
                Offset(center.x + pointerLen * cos(angleRad).toFloat(), center.y + pointerLen * sin(angleRad).toFloat()),
                strokeWidth = 2f
            )
            drawCircle(ScopeBezelDark, radius = radius * 0.18f, center = center)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.weight(1f).height(20.dp),
            colors = SliderDefaults.colors(
                thumbColor = ScopeAmber,
                activeTrackColor = ScopeAmber,
                inactiveTrackColor = ScopeBezelDark
            )
        )
    }
}

/** One row of the control bank: a fixed-width label, an LED indicator, and a switch, matching the scope's POWER/ANALOG rockers. */
@Composable
private fun ScopeToggleRow(
    label: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = ScopePanelText,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.width(ControlLabelWidth)
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (checked) ScopePhosphor else ScopeLedOff)
        )
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ScopeAmber,
                checkedTrackColor = ScopeBezelDark,
                checkedBorderColor = ScopeAmber,
                uncheckedThumbColor = ScopePanelTextDim,
                uncheckedTrackColor = ScopeBezelDark,
                uncheckedBorderColor = ScopePanelTextDim
            )
        )
    }
}

/**
 * The decoded-text readout, styled as a small terminal screen. Scrolls
 * internally once the text overflows its height, and auto-follows the
 * bottom as new characters are decoded so the latest output is always
 * in view without the user having to scroll manually.
 */
@Composable
private fun OutputTerminal(text: String, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    LaunchedEffect(text) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Brush.linearGradient(listOf(ScopeBezelHighlight, ScopeBezel, ScopeBezelDark)))
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(3.dp))
                .background(ScopeScreen)
                .border(1.dp, ScopeGrid.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(12.dp)
            ) {
                Text(
                    text = text.ifEmpty { stringResource(R.string.send_placeholder) },
                    fontFamily = FontFamily.Monospace,
                    color = if (text.isEmpty()) ScopePanelTextDim else ScopePhosphor
                )
            }
        }
    }
}

@Composable
private fun ActionButtonRow(state: DecoderViewModel.UiState, viewModel: DecoderViewModel) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            enabled = state.hasPermission,
            onClick = { if (state.isPlayingSample) viewModel.stopSample() else viewModel.playSample() },
            colors = ButtonDefaults.buttonColors(containerColor = ScopeAmber, contentColor = Color.Black)
        ) {
            Text(stringResource(if (state.isPlayingSample) R.string.stop_sample else R.string.play_sample))
        }
        OutlinedButton(
            onClick = { viewModel.resetDecoder() },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ScopePanelText)
        ) {
            Text(stringResource(R.string.reset))
        }
        OutlinedButton(
            onClick = { viewModel.clearOutput() },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ScopePanelText)
        ) {
            Text(stringResource(R.string.clear))
        }
    }
}
