package au.com.guidebee.morsetoolkit.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import au.com.guidebee.morsetoolkit.activity.R
import au.com.guidebee.morsetoolkit.activity.battlecity.BattleCityGameActivity
import au.com.guidebee.morsetoolkit.activity.flappybird.FlappyBirdGameActivity
import au.com.guidebee.morsetoolkit.training.ThemeMode
import au.com.guidebee.morsetoolkit.training.TutorialPreference

private object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val KOCH = "koch"
    const val SEND = "send"
    const val TRANSMIT = "transmit"
    const val RECEIVE = "receive"
    const val DECODER = "decoder"
    const val FLASHCARD = "flashcard"
    const val HANDBOOK = "handbook"
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
}

/**
 * The app's only navigation model now: everything lives in this one
 * NavHost. The two arcade games are the sole exception — they stay real
 * Activities (OpenGL, untouched) launched by plain Intent with no
 * back-stack flags, so finishing a game already returns here correctly.
 */
@Composable
fun MorseApp(
    onLaunchGame: (Class<*>) -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute == Routes.HOME || currentRoute == Routes.LIBRARY || currentRoute == Routes.SETTINGS
    val startDestination = remember {
        if (TutorialPreference.hasSeenOnboarding(context)) Routes.HOME else Routes.ONBOARDING
    }

    Scaffold(
        // This Scaffold has no topBar of its own, so by default it would
        // reserve a full status-bar-height gap for the content on every
        // route - on top of the identical status-bar inset each pushed
        // screen's own Scaffold+TopAppBar already reserves for itself. That
        // double reservation was the "extra empty space above the title" on
        // Transmit/Receive/Decoder. Zeroing it here leaves inset handling to
        // whichever screen-level Scaffold actually owns it.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onFinish = {
                        TutorialPreference.markOnboardingSeen(context)
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenKoch = { navController.navigate(Routes.KOCH) },
                    onOpenSend = { navController.navigate(Routes.SEND) },
                    onOpenLibrary = { navController.navigate(Routes.LIBRARY) { launchSingleTop = true } },
                    onOpenTransmit = { navController.navigate(Routes.TRANSMIT) },
                    onOpenReceive = { navController.navigate(Routes.RECEIVE) },
                    onOpenDecoder = { navController.navigate(Routes.DECODER) },
                    onOpenFlashcards = { navController.navigate(Routes.FLASHCARD) },
                    onOpenHandbook = { navController.navigate(Routes.HANDBOOK) },
                    onOpenFlappyBird = { onLaunchGame(FlappyBirdGameActivity::class.java) },
                    onOpenBattleCity = { onLaunchGame(BattleCityGameActivity::class.java) }
                )
            }
            composable(Routes.KOCH) {
                KochTrainerScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SEND) {
                SendPracticeScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.TRANSMIT) {
                TransmitScreen(
                    onBack = { navController.popBackStack() },
                    onOpenSendPractice = { navController.navigate(Routes.SEND) }
                )
            }
            composable(Routes.RECEIVE) {
                ReceiveScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.DECODER) {
                DecoderScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.FLASHCARD) {
                FlashcardScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.HANDBOOK) {
                HandbookScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.LIBRARY) {
                ContentLibraryScreen()
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange,
                    onReplayTutorials = {
                        TutorialPreference.resetAll(context)
                        navController.navigate(Routes.ONBOARDING) { launchSingleTop = true }
                    }
                )
            }
        }
    }
}
