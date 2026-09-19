package jo.aliftaa.prayertimes

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import jo.aliftaa.prayertimes.data.repository.PreferencesRepository
import jo.aliftaa.prayertimes.ui.screens.HomeScreen
import jo.aliftaa.prayertimes.ui.screens.SettingsContent
import jo.aliftaa.prayertimes.ui.theme.PrayerTimeTheme
import jo.aliftaa.prayertimes.ui.viewmodel.PrayerViewModel
import jo.aliftaa.prayertimes.ui.viewmodel.SettingsViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val prayerViewModel: PrayerViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        val lang = try {
            PreferencesRepository.getLanguageSync(newBase)
        } catch (e: Throwable) {
            "ar"
        }
        val locale = if (lang == "ar") {
            Locale.forLanguageTag("ar-JO-u-nu-latn")
        } else {
            Locale(lang)
        }
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val prayerState by prayerViewModel.uiState.collectAsState()

            var hasNotificationPermission by remember {
                mutableStateOf(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    } else {
                        true
                    }
                )
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                hasNotificationPermission = isGranted
            }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            PrayerTimeTheme(
                themeName = prayerState.theme,
                darkMode = prayerState.darkMode,
                language = if (prayerState.isArabic) "ar" else "en",
                fontScale = prayerState.fontScale
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showSettingsSheet by remember { mutableStateOf(false) }
                    var showQiblaDialog by remember { mutableStateOf(false) }
                    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                    HomeScreen(
                        viewModel = prayerViewModel,
                        hasNotificationPermission = hasNotificationPermission,
                        onRequestNotificationPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onNavigateToSettings = {
                            showSettingsSheet = true
                        },
                        onNavigateToQibla = {
                            showQiblaDialog = true
                        }
                    )

                    if (showQiblaDialog) {
                        jo.aliftaa.prayertimes.qibla.QiblaCompassDialog(
                            onDismiss = { showQiblaDialog = false }
                        )
                    }

                    if (showSettingsSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showSettingsSheet = false },
                            sheetState = sheetState,
                            shape = MaterialTheme.shapes.extraLarge,
                            tonalElevation = 6.dp, // MD3 level 3 elevation
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            SettingsContent(
                                viewModel = settingsViewModel,
                                onNavigateBack = { showSettingsSheet = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
