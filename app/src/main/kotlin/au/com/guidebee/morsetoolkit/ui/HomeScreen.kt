package au.com.guidebee.morsetoolkit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import au.com.guidebee.morsetoolkit.activity.R
import au.com.guidebee.morsetoolkit.training.KochOrder
import au.com.guidebee.morsetoolkit.training.KochProgression
import au.com.guidebee.morsetoolkit.training.StreakTracker
import au.com.guidebee.morsetoolkit.training.TutorialPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenKoch: () -> Unit,
    onOpenSend: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenTransmit: () -> Unit,
    onOpenReceive: () -> Unit,
    onOpenDecoder: () -> Unit,
    onOpenFlashcards: () -> Unit,
    onOpenHandbook: () -> Unit,
    onOpenFlappyBird: () -> Unit,
    onOpenBattleCity: () -> Unit,
    onOpenMario: () -> Unit
) {
    val context = LocalContext.current
    val streakTracker = remember { StreakTracker(context) }
    val progression = remember { KochProgression(context) }

    val coachMarkState = remember { CoachMarkState() }
    var showTour by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!TutorialPreference.hasSeen(context, "home_tour")) {
            showTour = true
            TutorialPreference.markSeen(context, "home_tour")
        }
    }

    val toolItems = listOf(
        ToolItem(stringResource(R.string.home_tool_send), Icons.Filled.Key, onOpenSend),
        ToolItem(stringResource(R.string.home_tool_library), Icons.Filled.MenuBook, onOpenLibrary),
        ToolItem(stringResource(R.string.transmit), Icons.Filled.Send, onOpenTransmit),
        ToolItem(stringResource(R.string.receive), Icons.Filled.Hearing, onOpenReceive),
        ToolItem(stringResource(R.string.decoder), Icons.Filled.GraphicEq, onOpenDecoder),
        ToolItem(stringResource(R.string.flashcard), Icons.Filled.Style, onOpenFlashcards),
        ToolItem(stringResource(R.string.handbook), Icons.Filled.Book, onOpenHandbook),
        ToolItem(stringResource(R.string.flappybird), Icons.Filled.SportsEsports, onOpenFlappyBird),
        ToolItem(stringResource(R.string.battlecity), Icons.Filled.SportsEsports, onOpenBattleCity),
        ToolItem(stringResource(R.string.mario), Icons.Filled.SportsEsports, onOpenMario)
    )

    Scaffold(
        topBar = {
            MorseTopBar(
                title = stringResource(R.string.app_name),
                actions = {
                    IconButton(onClick = { showTour = true }) {
                        Icon(Icons.Filled.HelpOutline, contentDescription = stringResource(R.string.action_help))
                    }
                }
            )
        },
        // The bottom tab bar (a sibling Scaffold in MorseApp) already reserves
        // exactly its own height for this content; this screen's own topBar
        // already bakes in the status bar inset. Nothing left for this
        // Scaffold to add on either edge.
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                StreakCard(
                    streak = streakTracker.currentStreak,
                    xp = streakTracker.totalXp,
                    level = streakTracker.level,
                    xpIntoLevel = streakTracker.xpIntoLevel,
                    modifier = Modifier.coachMarkAnchor(coachMarkState, "streak")
                )

                ContinueTrainingCard(
                    unlockedCount = progression.unlockedCharacters.size,
                    totalCount = KochOrder.sequence.size,
                    onClick = onOpenKoch,
                    modifier = Modifier.coachMarkAnchor(coachMarkState, "continue")
                )

                Text(stringResource(R.string.home_section_tools), style = MaterialTheme.typography.titleMedium)

                ToolGrid(toolItems, modifier = Modifier.coachMarkAnchor(coachMarkState, "tools"))
            }

            if (showTour) {
                CoachMarkOverlay(
                    steps = listOf(
                        CoachMarkStep(
                            anchorKey = "streak",
                            title = stringResource(R.string.home_tour_streak_title),
                            description = stringResource(R.string.home_tour_streak_body)
                        ),
                        CoachMarkStep(
                            anchorKey = "continue",
                            title = stringResource(R.string.home_tour_continue_title),
                            description = stringResource(R.string.home_tour_continue_body)
                        ),
                        CoachMarkStep(
                            anchorKey = "tools",
                            title = stringResource(R.string.home_tour_tools_title),
                            description = stringResource(R.string.home_tour_tools_body)
                        )
                    ),
                    state = coachMarkState,
                    onFinish = { showTour = false }
                )
            }
        }
    }
}

@Composable
private fun StreakCard(streak: Int, xp: Int, level: Int, xpIntoLevel: Int, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Text(stringResource(R.string.home_streak_days, streak), style = MaterialTheme.typography.titleMedium)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.home_level, level), style = MaterialTheme.typography.labelLarge)
                LinearProgressIndicator(
                    progress = { xpIntoLevel / 200f },
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                )
                Text(
                    text = stringResource(R.string.home_xp_total, xp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ContinueTrainingCard(unlockedCount: Int, totalCount: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_continue_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(R.string.home_continue_subtitle, unlockedCount, totalCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

private data class ToolItem(val label: String, val icon: ImageVector, val onClick: () -> Unit)

@Composable
private fun ToolGrid(items: List<ToolItem>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(3).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { item ->
                    Box(modifier = Modifier.weight(1f)) { ToolTile(item) }
                }
                repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ToolTile(item: ToolItem) {
    Card(onClick = item.onClick, shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(item.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(text = item.label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
        }
    }
}
