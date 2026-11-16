package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.core.auth.authStateFlow
import com.example.core.auth.signOutAether
import com.example.data.ai.AetherAiService
import com.example.data.model.AgentRole
import com.example.data.model.World
import com.example.data.preferences.AetherPreferences
import com.example.data.preferences.UniverseRenderQuality
import com.example.data.repository.AetherRepository
import com.example.ui.DataUiState
import com.example.ui.MainViewModel
import com.example.ui.auth.AuthScreen
import com.example.ui.components.CommandPaletteDialog
import com.example.ui.components.CreateOrEditWorldDialog
import com.example.ui.components.SettingsAndProfileDialog
import com.example.ui.screens.AgentsScreen
import com.example.ui.screens.CommandCenterScreen
import com.example.ui.screens.NetworkScreen
import com.example.ui.screens.NexusMarketScreen
import com.example.ui.screens.WorldWorkspaceScreen
import com.example.ui.theme.DeepSpaceSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SilverPrimary
import com.example.ui.theme.SilverSecondary
import com.example.ui.universe.UniverseScreen
import com.example.ui.voice.AetherVoiceSheet
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

const val ROUTE_UNIVERSE = "universe"
const val ROUTE_COMMAND = "command"
const val ROUTE_AGENTS = "agents"
const val ROUTE_NETWORK = "network"
const val ROUTE_NEXUS = "nexus"
const val ROUTE_WORLD_DETAIL = "world/{worldId}"

private data class NavTab(
  val route: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
)

private val PrimaryTabs = listOf(
  NavTab(ROUTE_UNIVERSE, "Universe", Icons.Filled.Public, Icons.Outlined.Public),
  NavTab(ROUTE_COMMAND, "Command", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
  NavTab(ROUTE_AGENTS, "Agents", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
  NavTab(ROUTE_NETWORK, "Network", Icons.Filled.Groups, Icons.Outlined.Groups),
  NavTab(ROUTE_NEXUS, "Nexus", Icons.Filled.Storefront, Icons.Outlined.Storefront),
)

@Composable
fun AetherApp(auth: FirebaseAuth = Firebase.auth) {
  val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
  val user = currentUser

  if (user == null) {
    AuthScreen(onAuthSuccess = { /* AuthStateListener updates currentUser reactively */ })
  } else {
    key(user.uid) {
      AetherAuthenticatedShell(
        currentUserId = user.uid,
        displayName = user.displayName ?: "Aether Explorer",
        email = user.email.orEmpty(),
      )
    }
  }
}

@Composable
private fun AetherAuthenticatedShell(
  currentUserId: String,
  displayName: String,
  email: String,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  val viewModel: MainViewModel = viewModel(
    key = currentUserId,
    factory = viewModelFactory {
      initializer {
        val app = checkNotNull(this[APPLICATION_KEY])
        val dbId = app.getString(R.string.firestore_database_id)
        val firestore = FirebaseFirestore.getInstance(dbId)
        MainViewModel(
          repository = AetherRepository(firestore, Firebase.auth),
          aiService = AetherAiService(),
          preferences = AetherPreferences(app),
          currentUserId = currentUserId,
          userDisplayName = displayName,
          userEmail = email,
        )
      }
    }
  )

  val worldsState by viewModel.worlds.collectAsStateWithLifecycle()
  val projectsState by viewModel.projects.collectAsStateWithLifecycle()
  val tasksState by viewModel.tasks.collectAsStateWithLifecycle()
  val teamsState by viewModel.teams.collectAsStateWithLifecycle()
  val membershipsState by viewModel.memberships.collectAsStateWithLifecycle()
  val activitiesState by viewModel.activities.collectAsStateWithLifecycle()
  val conversationsState by viewModel.conversations.collectAsStateWithLifecycle()
  val marketplaceState by viewModel.marketplaceItems.collectAsStateWithLifecycle()
  val notificationsState by viewModel.notifications.collectAsStateWithLifecycle()
  val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val aiState by viewModel.aiState.collectAsStateWithLifecycle()
  val orchestrationState by viewModel.orchestrationState.collectAsStateWithLifecycle()
  val voiceReplyState by viewModel.voiceReplyState.collectAsStateWithLifecycle()
  val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()

  val worlds = (worldsState as? DataUiState.Success)?.data.orEmpty()
  val projects = (projectsState as? DataUiState.Success)?.data.orEmpty()
  val tasks = (tasksState as? DataUiState.Success)?.data.orEmpty()
  val teams = (teamsState as? DataUiState.Success)?.data.orEmpty()
  val memberships = (membershipsState as? DataUiState.Success)?.data.orEmpty()
  val activities = (activitiesState as? DataUiState.Success)?.data.orEmpty()
  val conversations = (conversationsState as? DataUiState.Success)?.data.orEmpty()
  val marketplaceItems = (marketplaceState as? DataUiState.Success)?.data.orEmpty()
  val notifications = (notificationsState as? DataUiState.Success)?.data.orEmpty()
  val userProfile = (profileState as? DataUiState.Success)?.data

  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  var showOpeningOverlay by remember { mutableStateOf(true) }
  var showCreateWorldDialog by remember { mutableStateOf(false) }
  var editingWorld by remember { mutableStateOf<World?>(null) }
  var showCommandPalette by remember { mutableStateOf(false) }
  var showSettingsDialog by remember { mutableStateOf(false) }
  var showVoiceSheet by remember { mutableStateOf(false) }
  var preselectedAgent by remember { mutableStateOf<AgentRole?>(null) }

  val snackbarHostState = remember { SnackbarHostState() }
  LaunchedEffect(statusBanner) {
    statusBanner?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      viewModel.clearStatusBanner()
    }
  }

  fun navigateToTab(route: String) {
    navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  fun openWorldWorkspace(world: World) {
    navController.navigate("world/${world.id}")
  }

  val isTopLevelRoute = PrimaryTabs.any { it.route == currentRoute }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianVoid)
  ) {
    val useNavigationRail = maxWidth >= 600.dp

    Scaffold(
      containerColor = ObsidianVoid,
      snackbarHost = { SnackbarHost(snackbarHostState) },
      contentWindowInsets = WindowInsets(0, 0, 0, 0),
      bottomBar = {
        if (!useNavigationRail && isTopLevelRoute && !showOpeningOverlay) {
          NavigationBar(
            containerColor = MidnightBlack.copy(alpha = 0.96f),
            contentColor = SilverPrimary,
            windowInsets = WindowInsets.navigationBars
          ) {
            PrimaryTabs.forEach { tab ->
              val selected = currentRoute == tab.route
              NavigationBarItem(
                selected = selected,
                onClick = { navigateToTab(tab.route) },
                icon = {
                  Icon(
                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                    contentDescription = tab.label
                  )
                },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = ElectricCyan,
                  selectedTextColor = ElectricCyan,
                  indicatorColor = ElectricCyan.copy(alpha = 0.16f),
                  unselectedIconColor = SilverSecondary,
                  unselectedTextColor = SilverSecondary
                ),
                modifier = Modifier.testTag("nav_tab_${tab.route}")
              )
            }
          }
        }
      }
    ) { innerPadding ->
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        if (useNavigationRail && isTopLevelRoute && !showOpeningOverlay) {
          NavigationRail(
            containerColor = MidnightBlack,
            contentColor = SilverPrimary
          ) {
            PrimaryTabs.forEach { tab ->
              val selected = currentRoute == tab.route
              NavigationRailItem(
                selected = selected,
                onClick = { navigateToTab(tab.route) },
                icon = {
                  Icon(
                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                    contentDescription = tab.label
                  )
                },
                label = { Text(tab.label) },
                colors = NavigationRailItemDefaults.colors(
                  selectedIconColor = ElectricCyan,
                  selectedTextColor = ElectricCyan,
                  indicatorColor = ElectricCyan.copy(alpha = 0.16f),
                  unselectedIconColor = SilverSecondary,
                  unselectedTextColor = SilverSecondary
                ),
                modifier = Modifier.testTag("rail_tab_${tab.route}")
              )
            }
          }
        }

        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
          NavHost(
            navController = navController,
            startDestination = ROUTE_UNIVERSE
          ) {
            composable(ROUTE_UNIVERSE) {
              UniverseScreen(
                worlds = worlds,
                projects = projects,
                tasks = tasks,
                settings = settings,
                showOpeningOverlay = showOpeningOverlay,
                onDismissOpening = { showOpeningOverlay = false },
                onOpenWorld = ::openWorldWorkspace,
                onCreateWorldClick = { showCreateWorldDialog = true },
                onInitializeGenesis = viewModel::initializeGenesisWorld,
                onToggle2DFallback = {
                  val next = if (settings.renderQuality == UniverseRenderQuality.TWO_D_FALLBACK) {
                    UniverseRenderQuality.HIGH
                  } else {
                    UniverseRenderQuality.TWO_D_FALLBACK
                  }
                  viewModel.setRenderQuality(next)
                },
                onOpenSearch = { showCommandPalette = true },
                onOpenSettings = { showSettingsDialog = true },
                onOpenVoice = { showVoiceSheet = true },
              )
            }

            composable(ROUTE_COMMAND) {
              CommandCenterScreen(
                userName = userProfile?.displayName ?: displayName,
                worlds = worlds,
                projects = projects,
                tasks = tasks,
                notifications = notifications,
                activities = activities,
                onOpenGlobalSearch = { showCommandPalette = true },
                onOpenVoice = { showVoiceSheet = true },
                onCreateWorld = { showCreateWorldDialog = true },
                onOpenWorld = ::openWorldWorkspace,
                onOpenAgents = { role ->
                  preselectedAgent = role
                  navigateToTab(ROUTE_AGENTS)
                },
                onCycleTaskStatus = viewModel::cycleTaskStatus,
                onMarkNotificationRead = viewModel::markNotificationRead,
              )
            }

            composable(ROUTE_AGENTS) {
              AgentsScreen(
                initialSelectedAgent = preselectedAgent,
                worlds = worlds,
                conversations = conversations,
                aiState = aiState,
                orchestrationState = orchestrationState,
                onRunSingleAgent = viewModel::runSingleAgent,
                onRunMultiAgentCouncil = viewModel::runMultiAgentCouncil,
                onApproveTaskToWorld = viewModel::approveActionableTaskFromAi,
                onOpenVoice = { showVoiceSheet = true },
              )
            }

            composable(ROUTE_NETWORK) {
              NetworkScreen(
                teams = teams,
                memberships = memberships,
                worlds = worlds,
                activities = activities,
                onCreateTeam = viewModel::createTeam,
                onInviteMember = viewModel::inviteTeamMember,
                onUpdateMemberRole = viewModel::updateMemberRole,
                onRemoveMember = viewModel::removeTeamMember,
              )
            }

            composable(ROUTE_NEXUS) {
              NexusMarketScreen(
                currentUserId = currentUserId,
                items = marketplaceItems,
                onToggleFavorite = viewModel::toggleFavorite,
                onDeployAsWorld = viewModel::deployMarketItemAsWorld,
                onPublishItem = viewModel::publishMarketplaceItem,
              )
            }

            composable(ROUTE_WORLD_DETAIL) { backStack ->
              val worldId = backStack.arguments?.getString("worldId").orEmpty()
              val world = worlds.firstOrNull { it.id == worldId }
              if (world == null) {
                LaunchedEffect(worldId) { navController.popBackStack() }
              } else {
                WorldWorkspaceScreen(
                  world = world,
                  allWorlds = worlds,
                  projects = projects,
                  tasks = tasks,
                  teams = teams,
                  activities = activities,
                  aiState = aiState,
                  onBack = { navController.popBackStack() },
                  onEditWorld = { editingWorld = world },
                  onDeleteWorld = {
                    viewModel.deleteWorld(world)
                    navController.popBackStack()
                  },
                  onCreateProject = { title, summary -> viewModel.createProject(world.id, title, summary) },
                  onUpdateProjectProgress = viewModel::updateProjectProgress,
                  onDeleteProject = viewModel::deleteProject,
                  onCreateTask = { title, priority -> viewModel.createTask(world.id, title = title, priority = priority) },
                  onCycleTaskStatus = viewModel::cycleTaskStatus,
                  onDeleteTask = viewModel::deleteTask,
                  onToggleWorldLink = { targetId -> viewModel.toggleWorldConnection(world, targetId) },
                  onAskWorldAi = { role, prompt -> viewModel.runSingleAgent(role, prompt, world) },
                )
              }
            }
          }
        }
      }
    }
  }

  if (showCreateWorldDialog) {
    CreateOrEditWorldDialog(
      onDismiss = { showCreateWorldDialog = false },
      onConfirm = { name, desc, colorHex, category, orbitRadius ->
        viewModel.createWorld(name, desc, colorHex, category, orbitRadius, emptyList())
        showCreateWorldDialog = false
      }
    )
  }

  editingWorld?.let { target ->
    CreateOrEditWorldDialog(
      existingWorld = target,
      onDismiss = { editingWorld = null },
      onConfirm = { name, desc, colorHex, category, _ ->
        viewModel.updateWorld(target.id, name, desc, colorHex, category, target.connectedWorldIds)
        editingWorld = null
      }
    )
  }

  if (showCommandPalette) {
    CommandPaletteDialog(
      worlds = worlds,
      projects = projects,
      tasks = tasks,
      conversations = conversations,
      memberships = memberships,
      onOpenWorld = ::openWorldWorkspace,
      onNavigateTab = ::navigateToTab,
      onDismiss = { showCommandPalette = false }
    )
  }

  if (showSettingsDialog) {
    SettingsAndProfileDialog(
      profile = userProfile,
      fallbackName = displayName,
      userEmail = email,
      settings = settings,
      onUpdateProfile = viewModel::updateProfile,
      onSetQuality = viewModel::setRenderQuality,
      onSetReducedMotion = viewModel::setReducedMotion,
      onSetVoiceAutoSpeak = viewModel::setVoiceAutoSpeak,
      onSignOut = {
        showSettingsDialog = false
        signOutAether(credentialManager, scope)
      },
      onDismiss = { showSettingsDialog = false }
    )
  }

  if (showVoiceSheet) {
    AetherVoiceSheet(
      voiceReplyState = voiceReplyState,
      voiceHistory = conversations.filter { it.isVoice },
      autoSpeak = settings.voiceAutoSpeak,
      onAskVoice = viewModel::askVoiceAgent,
      onDismiss = { showVoiceSheet = false }
    )
  }
}
