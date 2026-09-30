package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.SalimApplication
import com.example.ui.blocked.BlockedNumbersScreen
import com.example.ui.blocked.BlockedNumbersViewModel
import com.example.ui.components.SalimBottomNavigation
import com.example.ui.contacts.ContactDetailScreen
import com.example.ui.contacts.ContactEditScreen
import com.example.ui.contacts.ContactsScreen
import com.example.ui.contacts.ContactsViewModel
import com.example.ui.contacts.settings.ContactsSettingsScreen
import com.example.ui.contacts.settings.ContactsSettingsViewModel
import com.example.ui.dialpad.DialpadScreen
import com.example.ui.dialpad.DialpadViewModel
import com.example.ui.favorites.FavoritesScreen
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.more.MoreScreen
import com.example.ui.navigation.Screen
import com.example.ui.permissions.PermissionsScreen
import com.example.ui.recents.RecentsScreen
import com.example.ui.recents.RecentsViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.theme.SalimTheme
import com.example.ui.voicemail.VoicemailScreen
import com.example.util.PermissionHelper
import com.example.util.RoleHelper

class MainActivity : ComponentActivity() {

    private val dialpadViewModel: DialpadViewModel by viewModels()
    private val contactsViewModel: ContactsViewModel by viewModels()
    private val recentsViewModel: RecentsViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val blockedNumbersViewModel: BlockedNumbersViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        contactsViewModel.loadContacts()
        recentsViewModel.loadCallLogs()
        homeViewModel.refresh()
    }

    private val roleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        homeViewModel.refresh()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check & request core permissions on launch if not granted
        if (!PermissionHelper.hasPermissions(this, PermissionHelper.MANDATORY_PERMISSIONS)) {
            permissionLauncher.launch(PermissionHelper.MANDATORY_PERMISSIONS)
        }

        handleIncomingIntent(intent)

        setContent {
            val settings by settingsViewModel.settings.collectAsState()
            val themeMode = settings.themeMode
            val isAppLocked by com.example.security.AppLockManager.isLocked.collectAsState()

            SalimTheme(themeMode = themeMode) {
                if (isAppLocked) {
                    com.example.ui.security.AppLockScreen(
                        onUnlocked = { com.example.security.AppLockManager.recordUnlock(this@MainActivity) },
                        onEmergencyCall = {
                            dialpadViewModel.setNumber("911")
                            dialpadViewModel.makeCall("911")
                        }
                    )
                } else if (themeMode == com.example.data.model.ThemeMode.SALIM) {
                    com.example.ui.components.LiquidGlassSystem {
                        MainAppScaffold(
                            dialpadViewModel = dialpadViewModel,
                            contactsViewModel = contactsViewModel,
                            recentsViewModel = recentsViewModel,
                            homeViewModel = homeViewModel,
                            blockedNumbersViewModel = blockedNumbersViewModel,
                            settingsViewModel = settingsViewModel,
                            themeMode = themeMode,
                            defaultStartTab = settings.defaultStartTab,
                            onRequestPermissions = {
                                permissionLauncher.launch(PermissionHelper.MANDATORY_PERMISSIONS)
                            },
                            onRequestDefaultDialer = {
                                RoleHelper.requestDefaultDialerIntent(this@MainActivity)?.let {
                                    roleLauncher.launch(it)
                                }
                            }
                        )
                    }
                } else {
                    MainAppScaffold(
                        dialpadViewModel = dialpadViewModel,
                        contactsViewModel = contactsViewModel,
                        recentsViewModel = recentsViewModel,
                        homeViewModel = homeViewModel,
                        blockedNumbersViewModel = blockedNumbersViewModel,
                        settingsViewModel = settingsViewModel,
                        themeMode = themeMode,
                        defaultStartTab = settings.defaultStartTab,
                        onRequestPermissions = {
                            permissionLauncher.launch(PermissionHelper.MANDATORY_PERMISSIONS)
                        },
                        onRequestDefaultDialer = {
                            RoleHelper.requestDefaultDialerIntent(this@MainActivity)?.let {
                                roleLauncher.launch(it)
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        com.example.security.AppLockManager.onAppResume(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val data: Uri? = intent.data
        var number = ""
        if (data != null && (data.scheme == "tel" || intent.action == Intent.ACTION_DIAL || intent.action == Intent.ACTION_VIEW || intent.action == Intent.ACTION_CALL)) {
            number = data.schemeSpecificPart ?: data.toString().removePrefix("tel:")
        }
        if (number.isBlank()) {
            number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
                ?: intent.getStringExtra("android.telecom.extra.PHONE_NUMBER")
                ?: ""
        }
        if (number.isNotBlank()) {
            dialpadViewModel.setNumber(number)
        }
    }
}

@Composable
fun MainAppScaffold(
    dialpadViewModel: DialpadViewModel,
    contactsViewModel: ContactsViewModel,
    recentsViewModel: RecentsViewModel,
    homeViewModel: HomeViewModel,
    blockedNumbersViewModel: BlockedNumbersViewModel,
    settingsViewModel: SettingsViewModel,
    themeMode: com.example.data.model.ThemeMode = com.example.data.model.ThemeMode.SYSTEM,
    defaultStartTab: String = "dialpad",
    onRequestPermissions: () -> Unit,
    onRequestDefaultDialer: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dialpad.route

    var navigatedInitialTab by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(defaultStartTab) {
        if (!navigatedInitialTab && defaultStartTab.isNotBlank() && defaultStartTab != "dialpad") {
            navigatedInitialTab = true
            val target = when (defaultStartTab) {
                "home" -> Screen.Home.route
                "recents" -> Screen.Recents.route
                "contacts" -> Screen.Contacts.route
                "more" -> Screen.More.route
                else -> null
            }
            target?.let {
                navController.navigate(it) {
                    popUpTo(Screen.Dialpad.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    val bottomBarRoutes = listOf(
        Screen.Dialpad.route,
        Screen.Recents.route,
        Screen.Contacts.route,
        Screen.Home.route,
        Screen.More.route
    )

    val showBottomBar = currentRoute in bottomBarRoutes

    val isDark = isSystemInDarkTheme()
    val containerBg = when (themeMode) {
        com.example.data.model.ThemeMode.SALIM -> androidx.compose.ui.graphics.Color.Transparent
        com.example.data.model.ThemeMode.DARK -> com.example.ui.theme.GlassBackgroundDark
        com.example.data.model.ThemeMode.LIGHT -> com.example.ui.theme.GlassBackgroundLight
        com.example.data.model.ThemeMode.SYSTEM -> if (isDark) com.example.ui.theme.GlassBackgroundDark else com.example.ui.theme.GlassBackgroundLight
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = containerBg,
        bottomBar = {
            if (showBottomBar) {
                SalimBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { targetRoute ->
                        if (targetRoute == Screen.Dialpad.route) {
                            navController.navigate(Screen.Dialpad.route) {
                                popUpTo(Screen.Dialpad.route) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        } else {
                            navController.navigate(targetRoute) {
                                popUpTo(Screen.Dialpad.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dialpad.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigate = { route -> navController.navigate(route) },
                    onRequestDefaultDialer = onRequestDefaultDialer
                )
            }

            composable(Screen.Recents.route) {
                RecentsScreen(
                    viewModel = recentsViewModel,
                    onContactDetailClick = { number ->
                        // Open contact interface for numbers in recents irrespective of saved or unsaved
                        val contact = com.example.domain.usecase.PhoneNumberHelper.findContactForNumber(
                            contactsViewModel.rawContacts.value,
                            number
                        )
                        if (contact != null) {
                            navController.navigate(Screen.ContactDetail.createRoute(contact.id, number))
                        } else {
                            navController.navigate(Screen.ContactDetail.createRoute(-1L, number))
                        }
                    },
                    onEditBeforeCall = { number ->
                        dialpadViewModel.setNumber(number)
                        navController.navigate(Screen.Dialpad.route) {
                            popUpTo(Screen.Dialpad.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.Contacts.route) {
                ContactsScreen(
                    viewModel = contactsViewModel,
                    onContactClick = { id ->
                        navController.navigate(Screen.ContactDetail.createRoute(id))
                    },
                    onAddContactClick = {
                        navController.navigate(Screen.ContactEdit.createRoute(-1L))
                    }
                )
            }

            composable(Screen.Dialpad.route) {
                DialpadScreen(
                    viewModel = dialpadViewModel,
                    onAddContact = { number ->
                        navController.navigate(Screen.ContactEdit.createRoute(-1L, number))
                    },
                    onViewContact = { contactId ->
                        navController.navigate(Screen.ContactDetail.createRoute(contactId))
                    }
                )
            }

            composable(Screen.More.route) {
                MoreScreen(
                    onNavigate = { route -> navController.navigate(route) },
                    onRequestDefaultDialer = onRequestDefaultDialer
                )
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    viewModel = contactsViewModel,
                    onContactClick = { id ->
                        navController.navigate(Screen.ContactDetail.createRoute(id))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Voicemail.route) {
                VoicemailScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.BlockedNumbers.route) {
                BlockedNumbersScreen(
                    viewModel = blockedNumbersViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CallRecordings.route) {
                com.example.ui.recordings.CallRecordingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Analytics.route) {
                com.example.ui.analytics.CallAnalyticsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigate = { route -> navController.navigate(route) },
                    onRequestDefaultDialer = onRequestDefaultDialer,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Permissions.route) {
                PermissionsScreen(
                    onRequestPermissions = onRequestPermissions,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.ContactDetail.route,
                arguments = listOf(
                    navArgument("contactId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument("number") {
                        type = NavType.StringType
                        defaultValue = ""
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val contactId = backStackEntry.arguments?.getLong("contactId") ?: -1L
                val number = backStackEntry.arguments?.getString("number") ?: ""
                ContactDetailScreen(
                    contactId = contactId,
                    initialPhoneNumber = number,
                    viewModel = contactsViewModel,
                    onBack = { navController.popBackStack() },
                    onEditContact = { id ->
                        navController.navigate(Screen.ContactEdit.createRoute(id))
                    },
                    onCreateContact = { num ->
                        navController.navigate(Screen.ContactEdit.createRoute(-1L, num))
                    }
                )
            }

            composable(Screen.ContactsSettings.route) {
                val contactsSettingsViewModel: ContactsSettingsViewModel = viewModel(
                    factory = ContactsSettingsViewModel.Factory(
                        preferencesManager = SalimApplication.instance.preferencesManager,
                        contactsRepository = SalimApplication.instance.contactsRepository,
                        recentlyDeletedRepository = SalimApplication.instance.recentlyDeletedRepository
                    )
                )
                ContactsSettingsScreen(
                    viewModel = contactsSettingsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.ContactEdit.route,
                arguments = listOf(
                    navArgument("contactId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument("number") {
                        type = NavType.StringType
                        defaultValue = ""
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val contactId = backStackEntry.arguments?.getLong("contactId") ?: -1L
                val initialNum = backStackEntry.arguments?.getString("number") ?: ""
                ContactEditScreen(
                    contactId = contactId,
                    initialNumber = initialNum,
                    viewModel = contactsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
