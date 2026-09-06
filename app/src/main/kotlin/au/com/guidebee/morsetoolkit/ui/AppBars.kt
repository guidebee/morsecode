package au.com.guidebee.morsetoolkit.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import au.com.guidebee.morsetoolkit.activity.R

/**
 * The one top-bar look every screen shares. Bottom-tab screens (Home,
 * Library, Settings) and pushed screens (Transmit, Decoder, etc.) previously
 * looked like two different navigation systems - the pushed screens had a
 * Material app bar while the tab screens just had a plain heading baked into
 * the scrolling content. Routing every screen through this composable gives
 * them identical title styling, height and color, with a back arrow as the
 * only difference.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorseTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
    ),
    actions: @Composable RowScope.() -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                }
            }
        },
        actions = actions,
        colors = colors
    )
}
