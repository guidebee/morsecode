package au.com.guidebee.morsetoolkit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import au.com.guidebee.morsetoolkit.activity.R
import au.com.guidebee.morsetoolkit.helper.MorseEncoder
import au.com.guidebee.morsetoolkit.training.CallsignDrill
import au.com.guidebee.morsetoolkit.training.ContentPacks
import au.com.guidebee.morsetoolkit.training.ProcedureEntry
import au.com.guidebee.morsetoolkit.training.QsoScript
import au.com.guidebee.morsetoolkit.training.QsoScripts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private enum class LibraryTab(val labelRes: Int) {
    Q_CODES(R.string.library_tab_qcodes),
    PROSIGNS(R.string.library_tab_prosigns),
    CALLSIGNS(R.string.library_tab_callsigns),
    QSO(R.string.library_tab_qso)
}

@Composable
fun ContentLibraryScreen() {
    val encoder = remember { MorseEncoder(2) }
    DisposableEffect(Unit) { onDispose { encoder.release() } }
    val scope = rememberCoroutineScope()

    fun play(text: String) {
        scope.launch(Dispatchers.Default) { encoder.playMorseCode(text) }
    }

    var selectedTab by remember { mutableStateOf(LibraryTab.Q_CODES) }
    val callsigns = remember { CallsignDrill.generateSet(8) }
    val qsoScripts = remember { QsoScripts.sample() }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.library_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(20.dp)
        )
        TabRow(selectedTabIndex = selectedTab.ordinal) {
            LibraryTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(stringResource(tab.labelRes)) }
                )
            }
        }
        when (selectedTab) {
            LibraryTab.Q_CODES -> ProcedureList(ContentPacks.qCodes, ::play)
            LibraryTab.PROSIGNS -> ProcedureList(ContentPacks.prosigns, ::play)
            LibraryTab.CALLSIGNS -> CallsignList(callsigns, ::play)
            LibraryTab.QSO -> QsoList(qsoScripts, ::play)
        }
    }
}

@Composable
private fun ProcedureList(entries: List<ProcedureEntry>, onPlay: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(entries) { entry ->
            Card {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.code, style = MaterialTheme.typography.titleMedium)
                        Text(entry.meaning, style = MaterialTheme.typography.bodyMedium)
                    }
                    IconButton(onClick = { onPlay(entry.code) }) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.library_play))
                    }
                }
            }
        }
    }
}

@Composable
private fun CallsignList(callsigns: List<String>, onPlay: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(callsigns) { call ->
            Card {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(call, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = { onPlay(call) }) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.library_play))
                    }
                }
            }
        }
    }
}

@Composable
private fun QsoList(scripts: List<QsoScript>, onPlay: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(scripts) { script ->
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(script.title, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    script.lines.forEach { line ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(line.label, style = MaterialTheme.typography.labelMedium)
                                Text(line.morseText, style = MaterialTheme.typography.bodyMedium)
                            }
                            IconButton(onClick = { onPlay(line.morseText) }) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.library_play))
                            }
                        }
                    }
                }
            }
        }
    }
}
