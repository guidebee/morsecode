package au.com.guidebee.morsetoolkit.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import au.com.guidebee.morsetoolkit.activity.R

/** One highlighted step of a [CoachMarkOverlay] tour: which anchor to spotlight, and what to say about it. */
data class CoachMarkStep(val anchorKey: String, val title: String, val description: String)

/** Holds the on-screen bounds of every anchor registered via [coachMarkAnchor], keyed by name. */
class CoachMarkState {
    private val anchors = mutableStateMapOf<String, Rect>()
    internal fun register(key: String, rect: Rect) {
        anchors[key] = rect
    }
    internal fun boundsFor(key: String): Rect? = anchors[key]
}

/** Marks a composable as a spotlight target: its bounds become available to a [CoachMarkOverlay] under [key]. */
fun Modifier.coachMarkAnchor(state: CoachMarkState, key: String): Modifier =
    this.onGloballyPositioned { coordinates -> state.register(key, coordinates.boundsInRoot()) }

/**
 * A guided tour: dims the whole screen except a cutout around each step's
 * anchor in turn, with a fixed instruction card pinned to the bottom. Must be
 * composed as a sibling, later in the same [Box], of every anchor registered
 * into [state] so root coordinates line up between target and overlay.
 */
@Composable
fun CoachMarkOverlay(steps: List<CoachMarkStep>, state: CoachMarkState, onFinish: () -> Unit) {
    var stepIndex by remember { mutableIntStateOf(0) }
    if (stepIndex >= steps.size) {
        LaunchedEffect(Unit) { onFinish() }
        return
    }
    val step = steps[stepIndex]
    val rect = state.boundsFor(step.anchorKey)

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // Forces off-screen compositing so BlendMode.Clear actually
                // punches a transparent hole instead of blending into black.
                .graphicsLayer(alpha = 0.995f, compositingStrategy = CompositingStrategy.Offscreen)
        ) {
            drawRect(color = Color.Black.copy(alpha = 0.7f))
            rect?.let {
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(it.left - 10f, it.top - 10f),
                    size = Size(it.width + 20f, it.height + 20f),
                    cornerRadius = CornerRadius(24f, 24f),
                    blendMode = BlendMode.Clear
                )
            }
        }

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(step.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(step.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { stepIndex = steps.size }) {
                        Text(stringResource(R.string.onboarding_skip))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${stepIndex + 1}/${steps.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        TextButton(onClick = { stepIndex += 1 }) {
                            Text(stringResource(if (stepIndex == steps.size - 1) R.string.tutorial_got_it else R.string.next))
                        }
                    }
                }
            }
        }
    }
}
