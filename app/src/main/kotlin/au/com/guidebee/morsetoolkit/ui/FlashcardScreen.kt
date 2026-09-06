package au.com.guidebee.morsetoolkit.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import au.com.guidebee.morsetoolkit.ConfigInfo
import au.com.guidebee.morsetoolkit.activity.R
import au.com.guidebee.morsetoolkit.helper.MorseEncoder
import au.com.guidebee.morsetoolkit.helper.MorseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * The legacy flip-card deck, unchanged rules: each new card opens on
 * whichever face ConfigInfo.letterOnFront says, tapping flips it to peek
 * the other, next/prev is sequential or random per ConfigInfo.flashCardType.
 * The morse-pattern face is plain monospace text now instead of the
 * dropped custom morse.ttf dingbat font.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(onBack: () -> Unit) {
    val encoder = remember { MorseEncoder(2) }
    DisposableEffect(Unit) { onDispose { encoder.release() } }
    val scope = rememberCoroutineScope()

    val letters = remember {
        MorseHelper.initTestLetters(
            ConfigInfo.TYPE_LETTER_LETTER or ConfigInfo.TYPE_LETTER_NUMBER or ConfigInfo.TYPE_LETTER_PUNCTUATION
        )
    }
    var index by remember { mutableIntStateOf(0) }
    var flipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (flipped) 180f else 0f, label = "flashcardFlip")
    val frontIsLetter = ConfigInfo.letterOnFront
    val currentLetter = letters[index]
    val frontFace = rotation <= 90f
    val currentFaceIsLetter = if (frontFace) frontIsLetter else !frontIsLetter

    fun move(delta: Int) {
        index = if (ConfigInfo.flashCardType) {
            ((index + delta) % letters.size + letters.size) % letters.size
        } else {
            Random.nextInt(letters.size)
        }
        flipped = false
    }

    Scaffold(
        topBar = { MorseTopBar(title = stringResource(R.string.flashcard), onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Card(
                onClick = { flipped = !flipped },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .size(220.dp)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 16f * density
                    }
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (frontFace) {
                        FlashcardFace(frontIsLetter, currentLetter)
                    } else {
                        Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                            FlashcardFace(!frontIsLetter, currentLetter)
                        }
                    }
                }
            }

            IconButton(
                onClick = { scope.launch(Dispatchers.Default) { encoder.playMorseCode(currentLetter.toString()) } },
                enabled = !currentFaceIsLetter
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.play))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                IconButton(onClick = { move(-1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                }
                IconButton(onClick = { move(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun FlashcardFace(isLetter: Boolean, letter: Char) {
    if (isLetter) {
        Text(letter.uppercaseChar().toString(), style = MaterialTheme.typography.displayLarge)
    } else {
        Text(
            text = MorseHelper.morseCodeData[letter] ?: "",
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = FontFamily.Monospace
        )
    }
}
