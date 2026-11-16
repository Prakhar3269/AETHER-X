package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val displayName: String = "",
  val email: String = "",
  val roleTitle: String = "Cosmic Architect",
  val bio: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class World(
  @DocumentId val id: String = "",
  val ownerId: String = "",
  val memberUids: List<String> = emptyList(),
  val name: String = "",
  val description: String = "",
  val colorHex: String = "#22D3EE",
  val category: String = "Architecture",
  val connectedWorldIds: List<String> = emptyList(),
  val orbitRadius: Double = 110.0,
  val orbitAngle: Double = 0.0,
  val planetScale: Double = 1.0,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class Project(
  @DocumentId val id: String = "",
  val ownerId: String = "",
  val memberUids: List<String> = emptyList(),
  val worldId: String = "",
  val title: String = "",
  val summary: String = "",
  val status: String = "active",
  val progress: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class TaskItem(
  @DocumentId val id: String = "",
  val ownerId: String = "",
  val memberUids: List<String> = emptyList(),
  val worldId: String = "",
  val projectId: String = "",
  val title: String = "",
  val description: String = "",
  val status: String = "todo",
  val priority: String = "medium",
  val assignedAgent: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class Team(
  @DocumentId val id: String = "",
  val ownerId: String = "",
  val memberUids: List<String> = emptyList(),
  val name: String = "",
  val description: String = "",
  val worldId: String = "",
  val inviteCode: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class TeamMembership(
  @DocumentId val id: String = "",
  val teamId: String = "",
  val ownerId: String = "",
  val userId: String = "",
  val displayName: String = "",
  val role: String = "viewer",
  val status: String = "active",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class Activity(
  @DocumentId val id: String = "",
  val ownerId: String = "",
  val memberUids: List<String> = emptyList(),
  val worldId: String = "",
  val actorName: String = "",
  val action: String = "",
  val category: String = "world",
  @ServerTimestamp val createdAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class Conversation(
  @DocumentId val id: String = "",
  val ownerId: String = "",
  val worldId: String = "",
  val agentId: String = "architect",
  val title: String = "",
  val prompt: String = "",
  val responseSummary: String = "",
  val isVoice: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class AgentRun(
  @DocumentId val id: String = "",
  val ownerId: String = "",
  val worldId: String = "",
  val taskPrompt: String = "",
  val selectedAgents: List<String> = emptyList(),
  val agentOutputs: Map<String, String> = emptyMap(),
  val consolidatedResult: String = "",
  val status: String = "running",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class MarketplaceItem(
  @DocumentId val id: String = "",
  val ownerId: String = "",
  val creatorName: String = "",
  val title: String = "",
  val description: String = "",
  val category: String = "World Kit",
  val coverKey: String = "cosmos",
  val tags: List<String> = emptyList(),
  val favoriteUids: List<String> = emptyList(),
  val isPublic: Boolean = true,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class NotificationItem(
  @DocumentId val id: String = "",
  val ownerId: String = "",
  val title: String = "",
  val message: String = "",
  val category: String = "system",
  val isRead: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

enum class AgentRole(
  val id: String,
  val displayName: String,
  val shortTitle: String,
  val specialty: String,
  val accentHex: String,
  val systemPrompt: String,
) {
  ARCHITECT(
    id = "architect",
    displayName = "AETHER ARCHITECT",
    shortTitle = "Architect",
    specialty = "Software architecture, project planning, system design & technical blueprints",
    accentHex = "#22D3EE",
    systemPrompt = "You are AETHER ARCHITECT, a principal systems and software architect inside the AETHER X digital universe. Provide structured, precise architectural blueprints, component boundaries, scalability decisions, and phased technical plans. Be concise, crisp, and actionable."
  ),
  RESEARCHER(
    id = "researcher",
    displayName = "AETHER RESEARCHER",
    shortTitle = "Researcher",
    specialty = "Knowledge discovery, source synthesis, information organization & deep research",
    accentHex = "#38BDF8",
    systemPrompt = "You are AETHER RESEARCHER, a knowledge synthesis specialist in AETHER X. Organize complex concepts, compare approaches objectively, highlight key unknowns or assumptions, and deliver structured research briefs."
  ),
  DEVELOPER(
    id = "developer",
    displayName = "AETHER DEVELOPER",
    shortTitle = "Developer",
    specialty = "Code assistance, debugging, implementation plans & clean code explanations",
    accentHex = "#06B6D4",
    systemPrompt = "You are AETHER DEVELOPER, a senior full-stack and mobile systems engineer in AETHER X. Deliver clean implementation steps, edge-case checks, debugging diagnostics, and production-ready code guidance."
  ),
  STRATEGIST(
    id = "strategist",
    displayName = "AETHER STRATEGIST",
    shortTitle = "Strategist",
    specialty = "Product strategy, business brainstorming, prioritization & risk analysis",
    accentHex = "#94A3B8",
    systemPrompt = "You are AETHER STRATEGIST, a visionary product and business strategist in AETHER X. Evaluate market positioning, prioritize MVP vs future milestones, identify risks, and craft clear execution roadmaps."
  ),
  CREATOR(
    id = "creator",
    displayName = "AETHER CREATOR",
    shortTitle = "Creator",
    specialty = "Creative direction, branding, naming, storytelling & content concepts",
    accentHex = "#67E8F9",
    systemPrompt = "You are AETHER CREATOR, a futuristic creative director and brand storyteller in AETHER X. Craft distinctive copy, conceptual narratives, visual world concepts, and refined naming ideas."
  );

  companion object {
    fun fromId(id: String): AgentRole = entries.firstOrNull { it.id == id } ?: ARCHITECT
  }
}
