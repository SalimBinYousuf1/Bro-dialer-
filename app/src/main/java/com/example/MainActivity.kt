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
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.blocked.BlockedNumbersScreen
import com.example.ui.blocked.BlockedNumbersViewModel
import com.example.ui.components.SalimBottomNavigation
import com.example.ui.contacts.ContactDetailScreen
import com.example.ui.contacts.ContactEditScreen
import com.example.ui.contacts.ContactsScreen
import com.example.ui.contacts.ContactsViewModel
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
            val useDark = settings.darkTheme || isSystemInDarkTheme()

            SalimTheme(darkTheme = useDark) {
                MainAppScaffold(
                    dialpadViewModel = dialpadViewModel,
                    contactsViewModel = contactsViewModel,
                    recentsViewModel = recentsViewModel,
                    homeViewModel = homeViewModel,
                    blockedNumbersViewModel = blockedNumbersViewModel,
                    settingsViewModel = settingsViewModel,
                    defaultStartTab = settings.defaultStartTab,
                    onRequestPermissions = {
                        permissionLauncher.launch(PermissionHelper.MANDATORY_PERMISSIONS)
                    },
                    onRequestDefaultDialer = {
                        RoleHelper.requestDefaultDialerIntent(this)?.let {
                            roleLauncher.launch(it)
                        }
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val data: Uri? = intent?.data
        if (data != null && (data.scheme == "tel" || intent.action == Intent.ACTION_DIAL || intent.action == Intent.ACTION_VIEW)) {
            val schemeSpecific = data.schemeSpecificPart ?: ""
            if (schemeSpecific.isNotBlank()) {
                dialpadViewModel.setNumber(schemeSpecific)
            }
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
    defaultStartTab: String = "home",
    onRequestPermissions: () -> Unit,
    onRequestDefaultDialer: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val initialRoute = when (defaultStartTab) {
        "recents" -> Screen.Recents.route
        "contacts" -> Screen.Contacts.route
        "dialpad" -> Screen.Dialpad.route
        "more" -> Screen.More.route
        else -> Screen.Home.route
    }
    val currentRoute = navBackStackEntry?.destination?.route ?: initialRoute

    val bottomBarRoutes = listOf(
        Screen.Home.route,
        Screen.Recents.route,
        Screen.Contacts.route,
        Screen.Dialpad.route,
        Screen.More.route
    )

    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                SalimBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { targetRoute ->
                        navController.navigate(targetRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = initialRoute,
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
                        // If number matches contact, open contact detail, else open edit
                        val contact = contactsViewModel.rawContacts.value.find { c ->
                            c.numbers.any { it.number == number || it.normalizedNumber == number }
                        }
                        if (contact != null) {
                            navController.navigate(Screen.ContactDetail.createRoute(contact.id))
                        } else {
                            navController.navigate(Screen.ContactEdit.createRoute(-1L) + "?number=$number")
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
                        navController.navigate(Screen.ContactEdit.createRoute(-1L) + "?number=$number")
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
                arguments = listOf(navArgument("contactId") { type = NavType.LongType })
            ) { backStackEntry ->
                val contactId = backStackEntry.arguments?.getLong("contactId") ?: -1L
                ContactDetailScreen(
                    contactId = contactId,
                    viewModel = contactsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.ContactEdit.route + "?number={number}",
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
                val initialNum = backStackEntry.arguments?.getString("number") ?: ""
                ContactEditScreen(
                    initialNumber = initialNum,
                    viewModel = contactsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
