package au.com.guidebee.morsetoolkit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import au.com.guidebee.morsetoolkit.activity.R
import au.com.guidebee.morsetoolkit.training.KochOrder
import au.com.guidebee.morsetoolkit.training.KochProgression
import au.com.guidebee.morsetoolkit.training.StreakTracker

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
    onOpenBattleCity: () -> Unit
) {
    val context = LocalContext.current
    val streakTracker = remember { StreakTracker(context) }
    val progression = remember { KochProgression(context) }

    val toolItems = listOf(
        ToolItem(stringResource(R.string.home_tool_send), Icons.Filled.Key, onOpenSend),
        ToolItem(stringResource(R.string.home_tool_library), Icons.Filled.MenuBook, onOpenLibrary),
        ToolItem(stringResource(R.string.transmit), Icons.Filled.Send, onOpenTransmit),
        ToolItem(stringResource(R.string.receive), Icons.Filled.Hearing, onOpenReceive),
        ToolItem(stringResource(R.string.decoder), Icons.Filled.GraphicEq, onOpenDecoder),
        ToolItem(stringResource(R.string.flashcard), Icons.Filled.Style, onOpenFlashcards),
        ToolItem(stringResource(R.string.handbook), Icons.Filled.Book, onOpenHandbook),
        ToolItem(stringResource(R.string.flappybird), Icons.Filled.SportsEsports, onOpenFlappyBird),
        ToolItem(stringResource(R.string.battlecity), Icons.Filled.SportsEsports, onOpenBattleCity)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )

        StreakCard(
            streak = streakTracker.currentStreak,
            xp = streakTracker.totalXp,
            level = streakTracker.level,
            xpIntoLevel = streakTracker.xpIntoLevel
        )

        ContinueTrainingCard(
            unlockedCount = progression.unlockedCharacters.size,
            totalCount = KochOrder.sequence.size,
            onClick = onOpenKoch
        )

        Text(stringResource(R.string.home_section_tools), style = MaterialTheme.typography.titleMedium)

        ToolGrid(toolItems)
    }
}

@Composable
private fun StreakCard(streak: Int, xp: Int, level: Int, xpIntoLevel: Int) {
    Card(shape = RoundedCornerShape(20.dp)) {
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
private fun ContinueTrainingCard(unlockedCount: Int, totalCount: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
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
private fun ToolGrid(items: List<ToolItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
