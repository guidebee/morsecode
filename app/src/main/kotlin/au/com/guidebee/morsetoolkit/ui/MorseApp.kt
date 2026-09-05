package au.com.guidebee.morsetoolkit.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.padding
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import au.com.guidebee.morsetoolkit.activity.R
import au.com.guidebee.morsetoolkit.activity.FlashcardActivity
import au.com.guidebee.morsetoolkit.activity.HandbookActivity
import au.com.guidebee.morsetoolkit.activity.ListenActivity
import au.com.guidebee.morsetoolkit.activity.OptionActivity
import au.com.guidebee.morsetoolkit.activity.ReceiveLetterActivity
import au.com.guidebee.morsetoolkit.activity.TransmitLetterActivity
import au.com.guidebee.morsetoolkit.activity.battlecity.BattleCityGameActivity
import au.com.guidebee.morsetoolkit.activity.flappybird.FlappyBirdGameActivity
import au.com.guidebee.morsetoolkit.training.ThemeMode

private object Routes {
    const val HOME = "home"
    const val KOCH = "koch"
    const val SEND = "send"
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
}

@Composable
fun MorseApp(
    onLaunchLegacy: (Class<*>) -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute == Routes.HOME || currentRoute == Routes.LIBRARY || currentRoute == Routes.SETTINGS

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Routes.HOME,
                        onClick = { navController.navigate(Routes.HOME) { launchSingleTop = true } },
                        icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_home)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.LIBRARY,
                        onClick = { navController.navigate(Routes.LIBRARY) { launchSingleTop = true } },
                        icon = { Icon(Icons.Filled.LibraryBooks, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_library)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.SETTINGS,
                        onClick = { navController.navigate(Routes.SETTINGS) { launchSingleTop = true } },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_settings)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenKoch = { navController.navigate(Routes.KOCH) },
                    onOpenSend = { navController.navigate(Routes.SEND) },
                    onOpenLibrary = { navController.navigate(Routes.LIBRARY) { launchSingleTop = true } },
                    onOpenTransmit = { onLaunchLegacy(TransmitLetterActivity::class.java) },
                    onOpenReceive = { onLaunchLegacy(ReceiveLetterActivity::class.java) },
                    onOpenDecoder = { onLaunchLegacy(ListenActivity::class.java) },
                    onOpenFlashcards = { onLaunchLegacy(FlashcardActivity::class.java) },
                    onOpenHandbook = { onLaunchLegacy(HandbookActivity::class.java) },
                    onOpenFlappyBird = { onLaunchLegacy(FlappyBirdGameActivity::class.java) },
                    onOpenBattleCity = { onLaunchLegacy(BattleCityGameActivity::class.java) },
                    onOpenClassicSettings = { onLaunchLegacy(OptionActivity::class.java) }
                )
            }
            composable(Routes.KOCH) {
                KochTrainerScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SEND) {
                SendPracticeScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.LIBRARY) {
                ContentLibraryScreen()
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange,
                    onOpenClassicSettings = { onLaunchLegacy(OptionActivity::class.java) }
                )
            }
        }
    }
}
