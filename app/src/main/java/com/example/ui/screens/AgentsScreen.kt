package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AgentRole
import com.example.data.model.Conversation
import com.example.data.model.World
import com.example.ui.AiStreamUiState
import com.example.ui.OrchestrationUiState
import com.example.ui.theme.AetherGlassCard
import com.example.ui.theme.CyanGlassBorder
import com.example.ui.theme.DeepSpaceSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SilverPrimary
import com.example.ui.theme.SilverSecondary
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.parseColorHexOrDefault

@Composable
fun AgentsScreen(
  initialSelectedAgent: AgentRole?,
  worlds: List<World>,
  conversations: List<Conversation>,
  aiState: AiStreamUiState,
  orchestrationState: OrchestrationUiState,
  onRunSingleAgent: (AgentRole, String, World?) -> Unit,
  onRunMultiAgentCouncil: (String, List<AgentRole>, World?) -> Unit,
  onApproveTaskToWorld: (worldId: String, title: String, agentId: String) -> Unit,
  onOpenVoice: () -> Unit,
) {
  var isCouncilMode by remember { mutableStateOf(false) }
  var selectedAgent by remember(initialSelectedAgent) {
    mutableStateOf(initialSelectedAgent ?: AgentRole.ARCHITECT)
  }
  val councilAgents = remember { mutableStateListOf<AgentRole>() }
  var selectedWorld by remember { mutableStateOf<World?>(worlds.firstOrNull()) }
  var taskPrompt by remember { mutableStateOf("") }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianVoid),
    contentAlignment = Alignment.TopCenter
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 860.dp)
        .statusBarsPadding(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("AI CIVILIZATION", style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
            Text("Multi-Agent Orchestrator", style = MaterialTheme.typography.headlineMedium, color = SilverPrimary)
          }
          IconButton(onClick = onOpenVoice, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Default.Mic, contentDescription = "Aether Voice", tint = ElectricCyan)
          }
        }
      }

      // Mode Toggle: Single Agent vs Multi-Agent Council
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          ModeTogglePill(
            label = "Single Specialist",
            selected = !isCouncilMode,
            tag = "mode_single_agent",
            modifier = Modifier.weight(1f),
            onClick = { isCouncilMode = false }
          )
          ModeTogglePill(
            label = "Multi-Agent Council",
            selected = isCouncilMode,
            tag = "mode_multi_agent_council",
            modifier = Modifier.weight(1f),
            onClick = { isCouncilMode = true }
          )
        }
      }

      // Agent Selector Cards
      item {
        Text(
          text = if (isCouncilMode) "Select 2+ agents or leave empty for Auto-Routing" else "Select Specialized Agent",
          style = MaterialTheme.typography.labelSmall,
          color = SilverSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          items(AgentRole.entries) { role ->
            val accent = parseColorHexOrDefault(role.accentHex)
            val active = if (isCouncilMode) councilAgents.contains(role) else selectedAgent == role
            Surface(
              modifier = Modifier
                .width(156.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                  if (isCouncilMode) {
                    if (councilAgents.contains(role)) councilAgents.remove(role) else councilAgents.add(role)
                  } else {
                    selectedAgent = role
                  }
                }
                .testTag("select_agent_${role.id}"),
              color = if (active) accent.copy(alpha = 0.18f) else DeepSpaceSurface,
              shape = RoundedCornerShape(16.dp),
              border = BorderStroke(1.dp, if (active) accent else GlassBorder)
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(role.shortTitle, style = MaterialTheme.typography.titleMedium, color = accent)
                  if (active) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                  }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = role.specialty,
                  style = MaterialTheme.typography.bodyMedium,
                  color = SilverSecondary,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }
      }

      // Optional World Context Selector
      if (worlds.isNotEmpty()) {
        item {
          Text("World Context (Optional)", style = MaterialTheme.typography.labelSmall, color = SilverSecondary)
          Spacer(modifier = Modifier.height(6.dp))
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
              ContextChip(
                label = "Global Universe",
                selected = selectedWorld == null,
                onClick = { selectedWorld = null }
              )
            }
            items(worlds, key = { it.id }) { world ->
              ContextChip(
                label = world.name,
                selected = selectedWorld?.id == world.id,
                onClick = { selectedWorld = world }
              )
            }
          }
        }
      }

      // Prompt Composer
      item {
        AetherGlassCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
          OutlinedTextField(
            value = taskPrompt,
            onValueChange = { taskPrompt = it },
            label = {
              Text(
                if (isCouncilMode) "Describe complex mission for Multi-Agent Council"
                else "Ask ${selectedAgent.displayName}"
              )
            },
            minLines = 3,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("agent_prompt_input")
          )
          Spacer(modifier = Modifier.height(12.dp))
          Button(
            onClick = {
              if (isCouncilMode) {
                onRunMultiAgentCouncil(taskPrompt, councilAgents.toList(), selectedWorld)
              } else {
                onRunSingleAgent(selectedAgent, taskPrompt, selectedWorld)
              }
            },
            enabled = taskPrompt.isNotBlank() &&
              aiState !is AiStreamUiState.Loading &&
              orchestrationState !is OrchestrationUiState.Running,
            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = ObsidianVoid),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("submit_agent_task_button")
          ) {
            Icon(
              imageVector = if (isCouncilMode) Icons.Default.Hub else Icons.AutoMirrored.Filled.Send,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isCouncilMode) "Orchestrate Council" else "Transmit to ${selectedAgent.shortTitle}",
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      // Live Output Section
      if (!isCouncilMode) {
        item {
          when (aiState) {
            AiStreamUiState.Idle -> Unit
            AiStreamUiState.Loading -> {
              AetherGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  CircularProgressIndicator(color = ElectricCyan, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                  Spacer(modifier = Modifier.width(12.dp))
                  Text("Connecting to ${selectedAgent.displayName} via Firebase AI Logic...", color = SilverSecondary)
                }
              }
            }
            is AiStreamUiState.Streaming -> {
              AetherGlassCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
                Text("${aiState.agent.displayName} (Streaming)", style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
                Spacer(modifier = Modifier.height(8.dp))
                Text(aiState.text, style = MaterialTheme.typography.bodyLarge, color = SilverPrimary)
              }
            }
            is AiStreamUiState.Success -> {
              AetherGlassCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
                Text(aiState.agent.displayName, style = MaterialTheme.typography.labelSmall, color = StatusSuccess)
                Spacer(modifier = Modifier.height(8.dp))
                Text(aiState.text, style = MaterialTheme.typography.bodyLarge, color = SilverPrimary)
                if (selectedWorld != null) {
                  Spacer(modifier = Modifier.height(12.dp))
                  OutlinedButton(
                    onClick = {
                      onApproveTaskToWorld(
                        selectedWorld!!.id,
                        "Review: ${aiState.prompt.take(60)}",
                        aiState.agent.id
                      )
                    },
                    border = BorderStroke(1.dp, CyanGlassBorder)
                  ) {
                    Icon(Icons.Default.AddTask, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Approve & Save Action Task to ${selectedWorld!!.name}", color = ElectricCyan)
                  }
                }
              }
            }
            is AiStreamUiState.Error -> {
              AetherGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(aiState.message, style = MaterialTheme.typography.bodyMedium, color = StatusError)
              }
            }
          }
        }
      } else {
        item {
          when (orchestrationState) {
            OrchestrationUiState.Idle -> Unit
            OrchestrationUiState.Running -> {
              AetherGlassCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  CircularProgressIndicator(color = ElectricCyan, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text("Multi-Agent Council Active", style = MaterialTheme.typography.titleMedium, color = SilverPrimary)
                    Text("Routing scoped sub-tasks, collecting agent briefs & synthesizing...", style = MaterialTheme.typography.bodyMedium, color = SilverSecondary)
                  }
                }
              }
            }
            is OrchestrationUiState.Completed -> {
              val outcome = orchestrationState.outcome
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AetherGlassCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CONSOLIDATED SYNTHESIS", style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
                  }
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(outcome.plan.rationale, style = MaterialTheme.typography.labelSmall, color = SilverSecondary)
                  Spacer(modifier = Modifier.height(10.dp))
                  Text(outcome.consolidatedResult, style = MaterialTheme.typography.bodyLarge, color = SilverPrimary)
                  if (selectedWorld != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                      onClick = {
                        onApproveTaskToWorld(
                          selectedWorld!!.id,
                          "Execute Council Plan: ${orchestrationState.prompt.take(50)}",
                          "council"
                        )
                      },
                      colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = ObsidianVoid)
                    ) {
                      Icon(Icons.Default.AddTask, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text("Approve & Materialize Task")
                    }
                  }
                }

                Text("Individual Agent Breakdowns", style = MaterialTheme.typography.titleMedium, color = SilverPrimary)
                outcome.plan.selectedAgents.forEach { role ->
                  val out = outcome.agentOutputs[role.id].orEmpty()
                  val accent = parseColorHexOrDefault(role.accentHex)
                  AetherGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(role.displayName, style = MaterialTheme.typography.labelSmall, color = accent)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(out, style = MaterialTheme.typography.bodyMedium, color = SilverSecondary)
                  }
                }
              }
            }
            is OrchestrationUiState.Error -> {
              AetherGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(orchestrationState.message, style = MaterialTheme.typography.bodyMedium, color = StatusError)
              }
            }
          }
        }
      }

      // Saved Conversation History
      item {
        Spacer(modifier = Modifier.height(6.dp))
        Text("Conversation Archive (${conversations.size})", style = MaterialTheme.typography.titleLarge, color = SilverPrimary)
      }
      if (conversations.isEmpty()) {
        item {
          Text("Your AI and Voice conversations will appear here.", style = MaterialTheme.typography.bodyMedium, color = SilverSecondary)
        }
      } else {
        items(conversations.take(12), key = { it.id }) { conv ->
          AetherGlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 12.dp) {
            Text(
              text = "${conv.agentId.uppercase()} • ${conv.title}",
              style = MaterialTheme.typography.labelSmall,
              color = ElectricCyan,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = conv.responseSummary,
              style = MaterialTheme.typography.bodyMedium,
              color = SilverSecondary,
              maxLines = 3,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ModeTogglePill(
  label: String,
  selected: Boolean,
  tag: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  Surface(
    modifier = modifier
      .height(44.dp)
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .testTag(tag),
    color = if (selected) ElectricCyan else DeepSpaceSurface,
    shape = RoundedCornerShape(12.dp),
    border = BorderStroke(1.dp, if (selected) ElectricCyan else GlassBorder)
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) ObsidianVoid else SilverPrimary
      )
    }
  }
}

@Composable
private fun ContextChip(label: String, selected: Boolean, onClick: () -> Unit) {
  Surface(
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .clickable(onClick = onClick),
    color = if (selected) ElectricCyan.copy(alpha = 0.2f) else DeepSpaceSurface,
    shape = RoundedCornerShape(10.dp),
    border = BorderStroke(1.dp, if (selected) ElectricCyan else GlassBorder)
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = if (selected) ElectricCyan else SilverSecondary,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
    )
  }
}
