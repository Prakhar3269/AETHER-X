package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.core.firebase.OperationType
import com.example.core.firebase.handleFirestoreError
import com.example.data.model.Activity
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
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class AetherRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    ),
    Firebase.auth
  )

  private fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
  }

  private fun <T> safeSnapshotFlow(
    path: String,
    operationType: OperationType,
    upstream: Flow<T>,
  ): Flow<T> = flow {
    val uid = requireUserId()
    emitAll(
      upstream.catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
          .filterIsInstance<FirebaseFirestoreException>()
          .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
          handleFirestoreError(exceptionToLog, operationType, path, auth)
        }
        throw error
      }
    )
  }

  // --- REAL-TIME STREAMS ---

  fun observeUserProfile(): Flow<UserProfile?> = flow {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    emitAll(
      safeSnapshotFlow(docRef.path, OperationType.GET, docRef.snapshots().map { snap ->
        snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
      })
    )
  }

  fun observeWorlds(): Flow<List<World>> = flow {
    val uid = requireUserId()
    val query = db.collection("worlds").whereArrayContains("memberUids", uid)
    emitAll(
      safeSnapshotFlow("worlds", OperationType.LIST, query.snapshots().map { snap ->
        snap.toObjects(World::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          .sortedBy { it.createdAt ?: Timestamp(0, 0) }
      })
    )
  }

  fun observeProjects(): Flow<List<Project>> = flow {
    val uid = requireUserId()
    val query = db.collection("projects").whereArrayContains("memberUids", uid)
    emitAll(
      safeSnapshotFlow("projects", OperationType.LIST, query.snapshots().map { snap ->
        snap.toObjects(Project::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          .sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
      })
    )
  }

  fun observeTasks(): Flow<List<TaskItem>> = flow {
    val uid = requireUserId()
    val query = db.collection("tasks").whereArrayContains("memberUids", uid)
    emitAll(
      safeSnapshotFlow("tasks", OperationType.LIST, query.snapshots().map { snap ->
        snap.toObjects(TaskItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          .sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
      })
    )
  }

  fun observeTeams(): Flow<List<Team>> = flow {
    val uid = requireUserId()
    val query = db.collection("teams").whereArrayContains("memberUids", uid)
    emitAll(
      safeSnapshotFlow("teams", OperationType.LIST, query.snapshots().map { snap ->
        snap.toObjects(Team::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          .sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
      })
    )
  }

  fun observeTeamMemberships(): Flow<List<TeamMembership>> = flow {
    val uid = requireUserId()
    val query = db.collection("team_memberships").whereEqualTo("ownerId", uid)
    emitAll(
      safeSnapshotFlow("team_memberships", OperationType.LIST, query.snapshots().map { snap ->
        snap.toObjects(TeamMembership::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          .sortedBy { it.createdAt ?: Timestamp(0, 0) }
      })
    )
  }

  fun observeActivities(): Flow<List<Activity>> = flow {
    val uid = requireUserId()
    val query = db.collection("activities").whereArrayContains("memberUids", uid)
    emitAll(
      safeSnapshotFlow("activities", OperationType.LIST, query.snapshots().map { snap ->
        snap.toObjects(Activity::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
      })
    )
  }

  fun observeConversations(): Flow<List<Conversation>> = flow {
    val uid = requireUserId()
    val query = db.collection("conversations").whereEqualTo("ownerId", uid)
    emitAll(
      safeSnapshotFlow("conversations", OperationType.LIST, query.snapshots().map { snap ->
        snap.toObjects(Conversation::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
      })
    )
  }

  fun observeAgentRuns(): Flow<List<AgentRun>> = flow {
    val uid = requireUserId()
    val query = db.collection("agent_runs").whereEqualTo("ownerId", uid)
    emitAll(
      safeSnapshotFlow("agent_runs", OperationType.LIST, query.snapshots().map { snap ->
        snap.toObjects(AgentRun::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
      })
    )
  }

  fun observeMarketplaceItems(): Flow<List<MarketplaceItem>> = flow {
    requireUserId()
    val query = db.collection("marketplace_items").whereEqualTo("isPublic", true)
    emitAll(
      safeSnapshotFlow("marketplace_items", OperationType.LIST, query.snapshots().map { snap ->
        snap.toObjects(MarketplaceItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
      })
    )
  }

  fun observeNotifications(): Flow<List<NotificationItem>> = flow {
    val uid = requireUserId()
    val query = db.collection("notifications").whereEqualTo("ownerId", uid)
    emitAll(
      safeSnapshotFlow("notifications", OperationType.LIST, query.snapshots().map { snap ->
        snap.toObjects(NotificationItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
      })
    )
  }

  // --- USER PROFILE ---

  suspend fun ensureUserProfile(displayName: String, email: String): Result<Unit> = runCatching {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    val existing = docRef.get().await()
    if (!existing.exists()) {
      val payload = mapOf(
        "userId" to uid,
        "displayName" to displayName.ifBlank { "Aether Explorer" }.take(80),
        "email" to email.take(120),
        "roleTitle" to "Cosmic Architect",
        "bio" to "Exploring digital worlds in AETHER X.",
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      docRef.set(payload).await()
    }
    Unit
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.WRITE, "users/${auth.currentUser?.uid}", auth)
  }

  suspend fun updateProfile(displayName: String, roleTitle: String, bio: String): Result<Unit> = runCatching {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    docRef.update(
      mapOf(
        "displayName" to displayName.trim().ifBlank { "Aether Explorer" }.take(80),
        "roleTitle" to roleTitle.trim().take(60),
        "bio" to bio.trim().take(300),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    ).await()
    Unit
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.UPDATE, "users/${auth.currentUser?.uid}", auth)
  }

  // --- WORLDS ---

  suspend fun createWorld(
    name: String,
    description: String,
    colorHex: String,
    category: String,
    orbitRadius: Double = 110.0,
    orbitAngle: Double = 0.6,
    planetScale: Double = 1.0,
    connectedWorldIds: List<String> = emptyList(),
  ): Result<String> = runCatching {
    val uid = requireUserId()
    val docRef = db.collection("worlds").document()
    val payload = mapOf(
      "ownerId" to uid,
      "memberUids" to listOf(uid),
      "name" to name.trim().take(80),
      "description" to description.trim().take(500),
      "colorHex" to colorHex.trim().take(9),
      "category" to category.trim().take(40),
      "connectedWorldIds" to connectedWorldIds.take(20),
      "orbitRadius" to orbitRadius.coerceIn(45.0, 260.0),
      "orbitAngle" to orbitAngle,
      "planetScale" to planetScale.coerceIn(0.6, 2.0),
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp(),
    )
    docRef.set(payload).await()
    docRef.id
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.CREATE, "worlds", auth)
  }

  suspend fun updateWorld(
    worldId: String,
    name: String,
    description: String,
    colorHex: String,
    category: String,
    connectedWorldIds: List<String>,
  ): Result<Unit> = runCatching {
    requireUserId()
    val docRef = db.collection("worlds").document(worldId)
    docRef.update(
      mapOf(
        "name" to name.trim().take(80),
        "description" to description.trim().take(500),
        "colorHex" to colorHex.trim().take(9),
        "category" to category.trim().take(40),
        "connectedWorldIds" to connectedWorldIds.take(20),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    ).await()
    Unit
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.UPDATE, "worlds/$worldId", auth)
  }

  suspend fun deleteWorld(worldId: String): Result<Unit> = runCatching {
    requireUserId()
    db.collection("worlds").document(worldId).delete().await()
    Unit
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.DELETE, "worlds/$worldId", auth)
  }

  suspend fun getWorldById(worldId: String): Result<World?> = runCatching {
    requireUserId()
    db.collection("worlds").document(worldId).get().await()
      .toObject(World::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.GET, "worlds/$worldId", auth)
  }

  // --- PROJECTS ---

  suspend fun createProject(
    worldId: String,
    title: String,
    summary: String,
    status: String = "active",
    progress: Int = 15,
  ): Result<String> = runCatching {
    val uid = requireUserId()
    val docRef = db.collection("projects").document()
    val payload = mapOf(
      "ownerId" to uid,
      "memberUids" to listOf(uid),
      "worldId" to worldId,
      "title" to title.trim().take(100),
      "summary" to summary.trim().take(600),
      "status" to status,
      "progress" to progress.coerceIn(0, 100),
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp(),
    )
    docRef.set(payload).await()
    docRef.id
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.CREATE, "projects", auth)
  }

  suspend fun updateProjectProgress(projectId: String, status: String, progress: Int): Result<Unit> = runCatching {
    requireUserId()
    db.collection("projects").document(projectId).update(
      mapOf(
        "status" to status,
        "progress" to progress.coerceIn(0, 100),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    ).await()
    Unit
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.UPDATE, "projects/$projectId", auth)
  }

  suspend fun deleteProject(projectId: String): Result<Unit> = runCatching {
    requireUserId()
    db.collection("projects").document(projectId).delete().await()
    Unit
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.DELETE, "projects/$projectId", auth)
  }

  // --- TASKS ---

  suspend fun createTask(
    worldId: String,
    projectId: String = "",
    title: String,
    description: String = "",
    priority: String = "medium",
    assignedAgent: String = "",
  ): Result<String> = runCatching {
    val uid = requireUserId()
    val docRef = db.collection("tasks").document()
    val payload = mapOf(
      "ownerId" to uid,
      "memberUids" to listOf(uid),
      "worldId" to worldId,
      "projectId" to projectId,
      "title" to title.trim().take(140),
      "description" to description.trim().take(500),
      "status" to "todo",
      "priority" to priority,
      "assignedAgent" to assignedAgent.take(40),
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp(),
    )
    docRef.set(payload).await()
    docRef.id
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.CREATE, "tasks", auth)
  }

  suspend fun updateTaskStatus(taskId: String, status: String): Result<Unit> = runCatching {
    requireUserId()
    db.collection("tasks").document(taskId).update(
      mapOf(
        "status" to status,
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    ).await()
    Unit
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.UPDATE, "tasks/$taskId", auth)
  }

  suspend fun deleteTask(taskId: String): Result<Unit> = runCatching {
    requireUserId()
    db.collection("tasks").document(taskId).delete().await()
    Unit
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.DELETE, "tasks/$taskId", auth)
  }

  // --- TEAMS & MEMBERSHIPS ---

  suspend fun createTeam(
    name: String,
    description: String,
    worldId: String = "",
    ownerDisplayName: String,
  ): Result<String> = runCatching {
    val uid = requireUserId()
    val teamRef = db.collection("teams").document()
    val code = "AX-" + teamRef.id.take(6).uppercase()
    val teamPayload = mapOf(
      "ownerId" to uid,
      "memberUids" to listOf(uid),
      "name" to name.trim().take(80),
      "description" to description.trim().take(400),
      "worldId" to worldId,
      "inviteCode" to code,
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp(),
    )
    teamRef.set(teamPayload).await()

    val memberRef = db.collection("team_memberships").document()
    memberRef.set(
      mapOf(
        "teamId" to teamRef.id,
        "ownerId" to uid,
        "userId" to uid,
        "displayName" to ownerDisplayName.ifBlank { "Owner" }.take(80),
        "role" to "owner",
        "status" to "active",
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    ).await()

    teamRef.id
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.CREATE, "teams", auth)
  }

  suspend fun addTeamMemberInvite(
    teamId: String,
    collaboratorName: String,
    collaboratorHandle: String,
    role: String,
  ): Result<String> = runCatching {
    val uid = requireUserId()
    val memberRef = db.collection("team_memberships").document()
    val sanitizedUserId = collaboratorHandle.trim().replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
      .ifBlank { "invite_${memberRef.id.take(8)}" }
      .take(64)
    memberRef.set(
      mapOf(
        "teamId" to teamId,
        "ownerId" to uid,
        "userId" to sanitizedUserId,
        "displayName" to collaboratorName.trim().take(80),
        "role" to role,
        "status" to "invited",
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    ).await()
    memberRef.id
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.CREATE, "team_memberships", auth)
  }

  suspend fun updateMemberRole(membershipId: String, role: String): Result<Unit> = runCatching {
    requireUserId()
    db.collection("team_memberships").document(membershipId).update(
      mapOf(
        "role" to role,
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    ).await()
    Unit
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.UPDATE, "team_memberships/$membershipId", auth)
  }

  suspend fun removeTeamMember(membershipId: String): Result<Unit> = runCatching {
    requireUserId()
    db.collection("team_memberships").document(membershipId).delete().await()
    Unit
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.DELETE, "team_memberships/$membershipId", auth)
  }

  // --- ACTIVITIES & NOTIFICATIONS ---

  suspend fun logActivity(
    actorName: String,
    action: String,
    category: String,
    worldId: String = "",
  ): Result<String> = runCatching {
    val uid = requireUserId()
    val docRef = db.collection("activities").document()
    docRef.set(
      mapOf(
        "ownerId" to uid,
        "memberUids" to listOf(uid),
        "worldId" to worldId,
        "actorName" to actorName.ifBlank { "Explorer" }.take(80),
        "action" to action.take(160),
        "category" to category,
        "createdAt" to FieldValue.serverTimestamp(),
      )
    ).await()
    docRef.id
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.CREATE, "activities", auth)
  }

  suspend fun createNotification(
    title: String,
    message: String,
    category: String = "system",
  ): Result<String> = runCatching {
    val uid = requireUserId()
    val docRef = db.collection("notifications").document()
    docRef.set(
      mapOf(
        "ownerId" to uid,
        "title" to title.take(100),
        "message" to message.take(300),
        "category" to category,
        "isRead" to false,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    ).await()
    docRef.id
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.CREATE, "notifications", auth)
  }

  suspend fun markNotificationRead(notificationId: String): Result<Unit> = runCatching {
    requireUserId()
    db.collection("notifications").document(notificationId).update(
      mapOf(
        "isRead" to true,
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    ).await()
    Unit
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.UPDATE, "notifications/$notificationId", auth)
  }

  // --- AI CONVERSATIONS & MULTI-AGENT RUNS ---

  suspend fun saveConversation(
    agentId: String,
    title: String,
    prompt: String,
    responseSummary: String,
    worldId: String = "",
    isVoice: Boolean = false,
  ): Result<String> = runCatching {
    val uid = requireUserId()
    val docRef = db.collection("conversations").document()
    docRef.set(
      mapOf(
        "ownerId" to uid,
        "worldId" to worldId,
        "agentId" to agentId,
        "title" to title.trim().ifBlank { "AI Session" }.take(120),
        "prompt" to prompt.trim().take(4000),
        "responseSummary" to responseSummary.trim().take(12000),
        "isVoice" to isVoice,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    ).await()
    docRef.id
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.CREATE, "conversations", auth)
  }

  suspend fun createAgentRun(
    taskPrompt: String,
    selectedAgents: List<String>,
    worldId: String = "",
  ): Result<String> = runCatching {
    val uid = requireUserId()
    val docRef = db.collection("agent_runs").document()
    docRef.set(
      mapOf(
        "ownerId" to uid,
        "worldId" to worldId,
        "taskPrompt" to taskPrompt.trim().take(4000),
        "selectedAgents" to selectedAgents.take(5),
        "agentOutputs" to emptyMap<String, String>(),
        "consolidatedResult" to "",
        "status" to "running",
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    ).await()
    docRef.id
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.CREATE, "agent_runs", auth)
  }

  suspend fun completeAgentRun(
    runId: String,
    agentOutputs: Map<String, String>,
    consolidatedResult: String,
    status: String,
  ): Result<Unit> = runCatching {
    requireUserId()
    db.collection("agent_runs").document(runId).update(
      mapOf(
        "agentOutputs" to agentOutputs,
        "consolidatedResult" to consolidatedResult.take(12000),
        "status" to status,
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    ).await()
    Unit
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.UPDATE, "agent_runs/$runId", auth)
  }

  // --- NEXUS MARKET ---

  suspend fun publishMarketplaceItem(
    creatorName: String,
    title: String,
    description: String,
    category: String,
    coverKey: String,
    tags: List<String>,
  ): Result<String> = runCatching {
    val uid = requireUserId()
    val docRef = db.collection("marketplace_items").document()
    docRef.set(
      mapOf(
        "ownerId" to uid,
        "creatorName" to creatorName.ifBlank { "Aether Creator" }.take(80),
        "title" to title.trim().take(100),
        "description" to description.trim().take(600),
        "category" to category,
        "coverKey" to coverKey,
        "tags" to tags.take(8),
        "favoriteUids" to emptyList<String>(),
        "isPublic" to true,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    ).await()
    docRef.id
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.CREATE, "marketplace_items", auth)
  }

  suspend fun toggleMarketplaceFavorite(item: MarketplaceItem): Result<Unit> = runCatching {
    val uid = requireUserId()
    val currentFavorites = item.favoriteUids.toMutableList()
    if (currentFavorites.contains(uid)) {
      currentFavorites.remove(uid)
    } else {
      currentFavorites.add(uid)
    }
    db.collection("marketplace_items").document(item.id).update(
      mapOf(
        "favoriteUids" to currentFavorites.take(500),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    ).await()
    Unit
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.UPDATE, "marketplace_items/${item.id}", auth)
  }

  suspend fun deleteMarketplaceItem(itemId: String): Result<Unit> = runCatching {
    requireUserId()
    db.collection("marketplace_items").document(itemId).delete().await()
    Unit
  }.onFailure { e ->
    if (e is CancellationException) throw e
    handleFirestoreError(e, OperationType.DELETE, "marketplace_items/$itemId", auth)
  }
}
