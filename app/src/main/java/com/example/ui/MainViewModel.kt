package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AetherAiService
import com.example.data.ai.OrchestrationOutcome
import com.example.data.model.Activity
import com.example.data.model.AgentRole
import com.example.data.model.AgentRun
import com.example.data.model.Conversation
import com.example.data.model.MarketplaceItem
import com.example.data.model.NotificationItem
import com.example.data.model.Project
import com.example.data.model.TaskItem
import com.example.data.model.Team
import com.example.data.model.TeamMembership
import com.example.data.model.UserProfile
import com.example.data.model.World
import com.example.data.preferences.AetherPreferences
import com.example.data.preferences.AetherUserSettings
import com.example.data.preferences.UniverseRenderQuality
import com.example.data.repository.AetherRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DataUiState<out T> {
  data object Loading : DataUiState<Nothing>
  data class Success<T>(val data: T) : DataUiState<T>
  data class Error(val message: String) : DataUiState<Nothing>
}

sealed interface AiStreamUiState {
  data object Idle : AiStreamUiState
  data object Loading : AiStreamUiState
  data class Streaming(val agent: AgentRole, val text: String) : AiStreamUiState
  data class Success(val agent: AgentRole, val prompt: String, val text: String) : AiStreamUiState
  data class Error(val message: String) : AiStreamUiState
}

sealed interface OrchestrationUiState {
  data object Idle : OrchestrationUiState
  data object Running : OrchestrationUiState
  data class Completed(val prompt: String, val outcome: OrchestrationOutcome) : OrchestrationUiState
  data class Error(val message: String) : OrchestrationUiState
}

class MainViewModel(
  private val repository: AetherRepository,
  private val aiService: AetherAiService,
  private val preferences: AetherPreferences,
  val currentUserId: String,
  val userDisplayName: String,
  val userEmail: String,
) : ViewModel() {

  init {
    viewModelScope.launch {
      repository.ensureUserProfile(userDisplayName, userEmail)
    }
  }

  private fun <T> Flow<T>.asUiState(label: String): StateFlow<DataUiState<T>> =
    this.map<T, DataUiState<T>> { DataUiState.Success(it) }
      .catch { error ->
        val fsError = generateSequence(error) { it.cause }
          .filterIsInstance<FirebaseFirestoreException>()
          .firstOrNull()
        if (fsError == null && error is CancellationException) throw error
        val root = fsError ?: error
        Log.w(TAG, "Error observing $label", root)
        emit(DataUiState.Error(root.message ?: "Failed to load $label"))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = DataUiState.Loading
      )

  val userProfile: StateFlow<DataUiState<UserProfile?>> =
    repository.observeUserProfile().asUiState("profile")

  val worlds: StateFlow<DataUiState<List<World>>> =
    repository.observeWorlds().asUiState("worlds")

  val projects: StateFlow<DataUiState<List<Project>>> =
    repository.observeProjects().asUiState("projects")

  val tasks: StateFlow<DataUiState<List<TaskItem>>> =
    repository.observeTasks().asUiState("tasks")

  val teams: StateFlow<DataUiState<List<Team>>> =
    repository.observeTeams().asUiState("teams")

  val memberships: StateFlow<DataUiState<List<TeamMembership>>> =
    repository.observeTeamMemberships().asUiState("memberships")

  val activities: StateFlow<DataUiState<List<Activity>>> =
    repository.observeActivities().asUiState("activities")

  val conversations: StateFlow<DataUiState<List<Conversation>>> =
    repository.observeConversations().asUiState("conversations")

  val agentRuns: StateFlow<DataUiState<List<AgentRun>>> =
    repository.observeAgentRuns().asUiState("agent_runs")

  val marketplaceItems: StateFlow<DataUiState<List<MarketplaceItem>>> =
    repository.observeMarketplaceItems().asUiState("marketplace")

  val notifications: StateFlow<DataUiState<List<NotificationItem>>> =
    repository.observeNotifications().asUiState("notifications")

  val settings: StateFlow<AetherUserSettings> = preferences.settingsFlow
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
      initialValue = AetherUserSettings()
    )

  private val _aiState = MutableStateFlow<AiStreamUiState>(AiStreamUiState.Idle)
  val aiState: StateFlow<AiStreamUiState> = _aiState.asStateFlow()

  private val _orchestrationState = MutableStateFlow<OrchestrationUiState>(OrchestrationUiState.Idle)
  val orchestrationState: StateFlow<OrchestrationUiState> = _orchestrationState.asStateFlow()

  private val _voiceReplyState = MutableStateFlow<AiStreamUiState>(AiStreamUiState.Idle)
  val voiceReplyState: StateFlow<AiStreamUiState> = _voiceReplyState.asStateFlow()

  private val _statusBanner = MutableStateFlow<String?>(null)
  val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

  fun clearStatusBanner() {
    _statusBanner.value = null
  }

  private fun actorName(): String {
    val profile = (userProfile.value as? DataUiState.Success)?.data
    return profile?.displayName?.ifBlank { userDisplayName } ?: userDisplayName.ifBlank { "Explorer" }
  }

  // --- PREFERENCES ---

  fun setReducedMotion(enabled: Boolean) {
    viewModelScope.launch { preferences.setReducedMotion(enabled) }
  }

  fun setRenderQuality(quality: UniverseRenderQuality) {
    viewModelScope.launch { preferences.setRenderQuality(quality) }
  }

  fun setVoiceAutoSpeak(enabled: Boolean) {
    viewModelScope.launch { preferences.setVoiceAutoSpeak(enabled) }
  }

  // --- PROFILE ---

  fun updateProfile(displayName: String, roleTitle: String, bio: String) {
    viewModelScope.launch {
      repository.updateProfile(displayName, roleTitle, bio)
        .onSuccess { _statusBanner.value = "Identity profile updated." }
        .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to update profile." }
    }
  }

  // --- WORLDS ---

  fun initializeGenesisWorld() {
    viewModelScope.launch {
      val coreResult = repository.createWorld(
        name = "Aether Genesis",
        description = "Central command world for architecture, research, and flagship creations.",
        colorHex = "#22D3EE",
        category = "Architecture",
        orbitRadius = 96.0,
        orbitAngle = 0.5,
        planetScale = 1.25
      )
      coreResult.onSuccess { coreId ->
        repository.createProject(
          worldId = coreId,
          title = "Universe Core System",
          summary = "Initial digital architecture and orbital topology.",
          status = "active",
          progress = 35
        )
        repository.createTask(
          worldId = coreId,
          title = "Define core modules with Aether Architect",
          priority = "high",
          assignedAgent = "architect"
        )
        repository.logActivity(actorName(), "Initialized Aether Genesis world", "world", coreId)
        repository.createNotification(
          title = "World Materialized",
          message = "Aether Genesis is now orbiting in your 3D universe.",
          category = "world"
        )
        _statusBanner.value = "Aether Genesis initialized."
      }.onFailure {
        _statusBanner.value = it.localizedMessage ?: "Could not create starter world."
      }
    }
  }

  fun createWorld(
    name: String,
    description: String,
    colorHex: String,
    category: String,
    orbitRadius: Double,
    connectedWorldIds: List<String>,
  ) {
    if (name.isBlank()) {
      _statusBanner.value = "World name cannot be empty."
      return
    }
    viewModelScope.launch {
      val existingCount = (worlds.value as? DataUiState.Success)?.data?.size ?: 0
      val angle = (existingCount * 1.37) % (Math.PI * 2.0)
      repository.createWorld(
        name = name,
        description = description,
        colorHex = colorHex,
        category = category,
        orbitRadius = orbitRadius,
        orbitAngle = angle,
        planetScale = 1.0 + (existingCount % 3) * 0.14,
        connectedWorldIds = connectedWorldIds
      ).onSuccess { newId ->
        repository.logActivity(actorName(), "Created world \"$name\"", "world", newId)
        repository.createNotification("New World Orbiting", "\"$name\" joined the Infinity Engine.", "world")
        _statusBanner.value = "World \"$name\" materialized."
      }.onFailure {
        _statusBanner.value = it.localizedMessage ?: "Failed to create world."
      }
    }
  }

  fun updateWorld(
    worldId: String,
    name: String,
    description: String,
    colorHex: String,
    category: String,
    connectedWorldIds: List<String>,
  ) {
    if (name.isBlank()) return
    viewModelScope.launch {
      repository.updateWorld(worldId, name, description, colorHex, category, connectedWorldIds)
        .onSuccess {
          repository.logActivity(actorName(), "Updated world \"$name\"", "world", worldId)
          _statusBanner.value = "World updated."
        }
        .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to update world." }
    }
  }

  fun toggleWorldConnection(sourceWorld: World, targetWorldId: String) {
    if (sourceWorld.id == targetWorldId) return
    val updatedLinks = sourceWorld.connectedWorldIds.toMutableList()
    val adding = !updatedLinks.contains(targetWorldId)
    if (adding) updatedLinks.add(targetWorldId) else updatedLinks.remove(targetWorldId)
    viewModelScope.launch {
      repository.updateWorld(
        worldId = sourceWorld.id,
        name = sourceWorld.name,
        description = sourceWorld.description,
        colorHex = sourceWorld.colorHex,
        category = sourceWorld.category,
        connectedWorldIds = updatedLinks
      ).onSuccess {
        repository.logActivity(
          actorName(),
          if (adding) "Linked ${sourceWorld.name} to another world" else "Unlinked connection in ${sourceWorld.name}",
          "world",
          sourceWorld.id
        )
      }
    }
  }

  fun deleteWorld(world: World) {
    viewModelScope.launch {
      repository.deleteWorld(world.id)
        .onSuccess {
          repository.logActivity(actorName(), "Decommissioned world \"${world.name}\"", "world")
          _statusBanner.value = "World removed."
        }
        .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to delete world." }
    }
  }

  // --- PROJECTS & TASKS ---

  fun createProject(worldId: String, title: String, summary: String) {
    if (title.isBlank()) return
    viewModelScope.launch {
      repository.createProject(worldId, title, summary)
        .onSuccess {
          repository.logActivity(actorName(), "Launched project \"$title\"", "project", worldId)
          _statusBanner.value = "Project \"$title\" created."
        }
        .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to create project." }
    }
  }

  fun updateProjectProgress(project: Project, status: String, progress: Int) {
    viewModelScope.launch {
      repository.updateProjectProgress(project.id, status, progress)
        .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to update project." }
    }
  }

  fun deleteProject(project: Project) {
    viewModelScope.launch {
      repository.deleteProject(project.id)
        .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to delete project." }
    }
  }

  fun createTask(
    worldId: String,
    projectId: String = "",
    title: String,
    description: String = "",
    priority: String = "medium",
    assignedAgent: String = "",
  ) {
    if (title.isBlank()) return
    viewModelScope.launch {
      repository.createTask(worldId, projectId, title, description, priority, assignedAgent)
        .onSuccess {
          repository.logActivity(actorName(), "Added task \"$title\"", "task", worldId)
        }
        .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to create task." }
    }
  }

  fun cycleTaskStatus(task: TaskItem) {
    val nextStatus = when (task.status) {
      "todo" -> "in_progress"
      "in_progress" -> "done"
      else -> "todo"
    }
    viewModelScope.launch {
      repository.updateTaskStatus(task.id, nextStatus)
        .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to update task." }
    }
  }

  fun deleteTask(task: TaskItem) {
    viewModelScope.launch {
      repository.deleteTask(task.id)
        .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to delete task." }
    }
  }

  // --- AI CIVILIZATION (SINGLE AGENT + MULTI-AGENT ORCHESTRATION) ---

  fun runSingleAgent(role: AgentRole, prompt: String, world: World? = null) {
    val cleanPrompt = prompt.trim()
    if (cleanPrompt.isBlank()) return

    val worldContext = world?.let {
      "World: ${it.name} (${it.category}) - ${it.description}"
    }.orEmpty()

    viewModelScope.launch {
      _aiState.value = AiStreamUiState.Loading
      var finalOutput = ""
      try {
        aiService.streamAgentResponse(role, cleanPrompt, worldContext).collect { partial ->
          finalOutput = partial
          _aiState.value = AiStreamUiState.Streaming(role, partial)
        }
        _aiState.value = AiStreamUiState.Success(role, cleanPrompt, finalOutput)
        repository.saveConversation(
          agentId = role.id,
          title = cleanPrompt.take(72),
          prompt = cleanPrompt,
          responseSummary = finalOutput,
          worldId = world?.id.orEmpty(),
          isVoice = false
        )
        repository.logActivity(actorName(), "Consulted ${role.displayName}", "agent", world?.id.orEmpty())
      } catch (e: TimeoutCancellationException) {
        Log.e(TAG, "Agent call timed out", e)
        _aiState.value = AiStreamUiState.Error("${role.displayName} timed out. Please retry.")
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Log.e(TAG, "Agent call failed", e)
        _aiState.value = AiStreamUiState.Error(e.localizedMessage ?: "AI call failed.")
      }
    }
  }

  fun runMultiAgentCouncil(
    prompt: String,
    preferredAgents: List<AgentRole> = emptyList(),
    world: World? = null,
  ) {
    val cleanPrompt = prompt.trim()
    if (cleanPrompt.isBlank()) return

    val worldContext = world?.let {
      "World: ${it.name} (${it.category}) - ${it.description}"
    }.orEmpty()

    viewModelScope.launch {
      _orchestrationState.value = OrchestrationUiState.Running
      val runId = repository.createAgentRun(
        taskPrompt = cleanPrompt,
        selectedAgents = if (preferredAgents.isNotEmpty()) preferredAgents.map { it.id } else listOf("architect"),
        worldId = world?.id.orEmpty()
      ).getOrNull()

      try {
        val outcome = aiService.runMultiAgentOrchestration(
          taskPrompt = cleanPrompt,
          worldContext = worldContext,
          preferredAgents = preferredAgents
        )
        _orchestrationState.value = OrchestrationUiState.Completed(cleanPrompt, outcome)
        if (runId != null) {
          repository.completeAgentRun(
            runId = runId,
            agentOutputs = outcome.agentOutputs,
            consolidatedResult = outcome.consolidatedResult,
            status = "completed"
          )
        }
        repository.saveConversation(
          agentId = "council",
          title = "Council: ${cleanPrompt.take(64)}",
          prompt = cleanPrompt,
          responseSummary = outcome.consolidatedResult,
          worldId = world?.id.orEmpty(),
          isVoice = false
        )
        repository.createNotification(
          title = "Multi-Agent Council Complete",
          message = "${outcome.plan.selectedAgents.size} agents synthesized insights for your review.",
          category = "agent"
        )
        repository.logActivity(actorName(), "Ran Multi-Agent Council synthesis", "agent", world?.id.orEmpty())
      } catch (e: TimeoutCancellationException) {
        Log.e(TAG, "Orchestration timed out", e)
        if (runId != null) {
          repository.completeAgentRun(runId, emptyMap(), "Timed out", "failed")
        }
        _orchestrationState.value = OrchestrationUiState.Error("Multi-agent council timed out after 60s.")
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Log.e(TAG, "Orchestration failed", e)
        if (runId != null) {
          repository.completeAgentRun(runId, emptyMap(), e.localizedMessage ?: "Error", "failed")
        }
        _orchestrationState.value = OrchestrationUiState.Error(e.localizedMessage ?: "Orchestration failed.")
      }
    }
  }

  fun approveActionableTaskFromAi(worldId: String, title: String, agentId: String) {
    createTask(
      worldId = worldId,
      title = title.take(140),
      description = "Approved from AI Civilization synthesis.",
      priority = "high",
      assignedAgent = agentId
    )
    _statusBanner.value = "Approved & added task to World."
  }

  // --- AETHER VOICE ---

  fun askVoiceAgent(prompt: String, role: AgentRole = AgentRole.ARCHITECT, onReadyToSpeak: (String) -> Unit) {
    val cleanPrompt = prompt.trim()
    if (cleanPrompt.isBlank()) return
    viewModelScope.launch {
      _voiceReplyState.value = AiStreamUiState.Loading
      var finalOutput = ""
      try {
        aiService.streamAgentResponse(
          role = role,
          prompt = "$cleanPrompt\n\n(Respond conversationally in 2 to 4 concise sentences suitable for spoken voice playback.)"
        ).collect { partial ->
          finalOutput = partial
          _voiceReplyState.value = AiStreamUiState.Streaming(role, partial)
        }
        _voiceReplyState.value = AiStreamUiState.Success(role, cleanPrompt, finalOutput)
        repository.saveConversation(
          agentId = "voice",
          title = "Voice: ${cleanPrompt.take(64)}",
          prompt = cleanPrompt,
          responseSummary = finalOutput,
          isVoice = true
        )
        onReadyToSpeak(finalOutput)
      } catch (e: TimeoutCancellationException) {
        _voiceReplyState.value = AiStreamUiState.Error("Voice agent timed out.")
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _voiceReplyState.value = AiStreamUiState.Error(e.localizedMessage ?: "Voice query failed.")
      }
    }
  }

  // --- LIVE WORLDS / TEAMS ---

  fun createTeam(name: String, description: String, worldId: String = "") {
    if (name.isBlank()) return
    viewModelScope.launch {
      repository.createTeam(name, description, worldId, actorName())
        .onSuccess {
          repository.logActivity(actorName(), "Formed team \"$name\"", "team", worldId)
          _statusBanner.value = "Team \"$name\" created."
        }
        .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to create team." }
    }
  }

  fun inviteTeamMember(teamId: String, collaboratorName: String, handle: String, role: String) {
    if (collaboratorName.isBlank()) return
    viewModelScope.launch {
      repository.addTeamMemberInvite(teamId, collaboratorName, handle, role)
        .onSuccess {
          repository.logActivity(actorName(), "Invited $collaboratorName ($role)", "team")
          _statusBanner.value = "Invitation issued for $collaboratorName."
        }
        .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to invite member." }
    }
  }

  fun updateMemberRole(membershipId: String, role: String) {
    viewModelScope.launch {
      repository.updateMemberRole(membershipId, role)
        .onSuccess { _statusBanner.value = "Role updated to $role." }
        .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to update role." }
    }
  }

  fun removeTeamMember(membershipId: String) {
    viewModelScope.launch {
      repository.removeTeamMember(membershipId)
        .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to remove member." }
    }
  }

  // --- NEXUS MARKET ---

  fun publishMarketplaceItem(
    title: String,
    description: String,
    category: String,
    coverKey: String,
    tagsCsv: String,
  ) {
    if (title.isBlank() || description.isBlank()) {
      _statusBanner.value = "Title and description are required."
      return
    }
    val tags = tagsCsv.split(",")
      .map { it.trim() }
      .filter { it.isNotBlank() }
      .take(8)

    viewModelScope.launch {
      repository.publishMarketplaceItem(
        creatorName = actorName(),
        title = title,
        description = description,
        category = category,
        coverKey = coverKey,
        tags = tags
      ).onSuccess {
        repository.logActivity(actorName(), "Published \"$title\" to Nexus Market", "market")
        _statusBanner.value = "Published to Nexus Market."
      }.onFailure {
        _statusBanner.value = it.localizedMessage ?: "Failed to publish item."
      }
    }
  }

  fun toggleFavorite(item: MarketplaceItem) {
    viewModelScope.launch {
      repository.toggleMarketplaceFavorite(item)
        .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to update favorite." }
    }
  }

  fun deployMarketItemAsWorld(item: MarketplaceItem) {
    createWorld(
      name = item.title,
      description = "${item.description} (Deployed from Nexus Market by ${item.creatorName})",
      colorHex = when (item.coverKey) {
        "architect" -> "#38BDF8"
        "neural" -> "#22D3EE"
        "cosmos" -> "#67E8F9"
        else -> "#06B6D4"
      },
      category = item.category,
      orbitRadius = 135.0,
      connectedWorldIds = emptyList()
    )
  }

  fun markNotificationRead(notification: NotificationItem) {
    if (notification.isRead) return
    viewModelScope.launch {
      repository.markNotificationRead(notification.id)
    }
  }

  private companion object {
    const val TAG = "MainViewModel"
    const val STOP_TIMEOUT_MS = 5000L
  }
}
