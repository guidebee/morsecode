package au.com.guidebee.morsetoolkit.ui

import android.view.MotionEvent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import au.com.guidebee.morsetoolkit.activity.R
import au.com.guidebee.morsetoolkit.decoder.KeyboardMorseCodeDecoder
import au.com.guidebee.morsetoolkit.decoder.MorseCodePatternMatch
import au.com.guidebee.morsetoolkit.training.KeyClickSounds

/**
 * Builds and drives a [KeyboardMorseCodeDecoder] for the life of the
 * composition — the same class [au.com.guidebee.morsetoolkit.activity.MorseActivity]
 * already uses to let a learner answer a Receive drill by tapping.
 */
@Composable
fun rememberMorseKeyDecoder(onEmit: (Char) -> Unit): KeyboardMorseCodeDecoder {
    val decoder = remember {
        KeyboardMorseCodeDecoder().apply {
            addListener(object : MorseCodePatternMatch.MorseCodeListener {
                override fun onEmit(character: Char) {
                    if (character != '^') onEmit(character)
                }

                override fun onCharStart() {}
                override fun onCharEnd(dotOrDash: String, length: Int) {}
            })
        }
    }
    DisposableEffect(decoder) {
        decoder.startTimer()
        onDispose { decoder.cancelTimer() }
    }
    return decoder
}

/**
 * An on-screen telegraph key. Timing thresholds (< 250ms = dot, < 1000ms =
 * dash) mirror MorseActivity's physical `imageButtonMorse` exactly, and so
 * does the feedback: a dit/dah click sound on release (via [KeyClickSounds],
 * the same res/raw clips and Options-screen volume/mute setting) plus a
 * press animation, since a key with no feedback at all is what this screen
 * used to ship.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MorseKey(decoder: KeyboardMorseCodeDecoder, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val clickSounds = remember { KeyClickSounds(context) }
    DisposableEffect(Unit) { onDispose { clickSounds.release() } }

    var pressStartMs by remember { mutableLongStateOf(0L) }
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, label = "morseKeyScale")
    val keyColor by animateColorAsState(
        if (isPressed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
        label = "morseKeyColor"
    )
    val iconTint = if (isPressed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = modifier
            .size(96.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(keyColor, CircleShape)
            .pointerInteropFilter { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        pressStartMs = System.currentTimeMillis()
                        isPressed = true
                        decoder.setOnOff(true)
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        val diff = System.currentTimeMillis() - pressStartMs
                        if (diff < 250) {
                            clickSounds.playDit()
                            decoder.processKey(false)
                        } else if (diff < 1000) {
                            clickSounds.playDah()
                            decoder.processKey(true)
                        }
                        isPressed = false
                        decoder.setOnOff(false)
                        true
                    }
                    else -> false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.RadioButtonChecked,
            contentDescription = stringResource(R.string.send_key_content_description),
            tint = iconTint
        )
    }
}
