package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Activity
import com.example.data.model.AgentRole
import com.example.data.model.Project
import com.example.data.model.TaskItem
import com.example.data.model.Team
import com.example.data.model.World
import com.example.ui.AiStreamUiState
import com.example.ui.theme.AetherGlassCard
import com.example.ui.theme.CyanGlassBorder
import com.example.ui.theme.DeepSpaceSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SilverPrimary
import com.example.ui.theme.SilverSecondary
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.parseColorHexOrDefault

@Composable
fun WorldWorkspaceScreen(
  world: World,
  allWorlds: List<World>,
  projects: List<Project>,
  tasks: List<TaskItem>,
  teams: List<Team>,
  activities: List<Activity>,
  aiState: AiStreamUiState,
  onBack: () -> Unit,
  onEditWorld: () -> Unit,
  onDeleteWorld: () -> Unit,
  onCreateProject: (title: String, summary: String) -> Unit,
  onUpdateProjectProgress: (Project, String, Int) -> Unit,
  onDeleteProject: (Project) -> Unit,
  onCreateTask: (title: String, priority: String) -> Unit,
  onCycleTaskStatus: (TaskItem) -> Unit,
  onDeleteTask: (TaskItem) -> Unit,
  onToggleWorldLink: (targetWorldId: String) -> Unit,
  onAskWorldAi: (AgentRole, String) -> Unit,
) {
  BackHandler(onBack = onBack)

  var selectedTab by remember { mutableIntStateOf(0) }
  var showAddProject by remember { mutableStateOf(false) }
  var showAddTask by remember { mutableStateOf(false) }
  var showDeleteConfirm by remember { mutableStateOf(false) }

  val accent = parseColorHexOrDefault(world.colorHex)
  val worldProjects = projects.filter { it.worldId == world.id }
  val worldTasks = tasks.filter { it.worldId == world.id }
  val worldTeams = teams.filter { it.worldId == world.id }
  val worldActivities = activities.filter { it.worldId == world.id }
  val tabs = listOf("Overview", "Projects", "Tasks", "Links & Team", "AI Assist")

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianVoid),
    contentAlignment = Alignment.TopCenter
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 860.dp)
        .statusBarsPadding()
        .navigationBarsPadding()
    ) {
      // Top Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier.testTag("world_back_button")
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SilverPrimary)
        }
        Box(
          modifier = Modifier
            .size(12.dp)
            .background(accent, CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = world.name,
            style = MaterialTheme.typography.titleLarge,
            color = SilverPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = world.category.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = accent
          )
        }
        IconButton(onClick = onEditWorld, modifier = Modifier.testTag("edit_world_button")) {
          Icon(Icons.Default.Edit, contentDescription = "Edit World", tint = ElectricCyan)
        }
        IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.testTag("delete_world_button")) {
          Icon(Icons.Default.DeleteOutline, contentDescription = "Delete World", tint = StatusError)
        }
      }

      ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = MidnightBlack,
        contentColor = ElectricCyan,
        edgePadding = 12.dp
      ) {
        tabs.forEachIndexed { idx, label ->
          Tab(
            selected = selectedTab == idx,
            onClick = { selectedTab = idx },
            text = {
              Text(
                text = label,
                color = if (selectedTab == idx) ElectricCyan else SilverSecondary
              )
            }
          )
        }
      }

      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        when (selectedTab) {
          0 -> {
            item {
              AetherGlassCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
                Text("World Overview", style = MaterialTheme.typography.titleLarge, color = SilverPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = world.description.ifBlank { "No description provided for this World." },
                  style = MaterialTheme.typography.bodyLarge,
                  color = SilverSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Projects: ${worldProjects.size}", style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
                  Text("Tasks: ${worldTasks.size}", style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
                  Text("Links: ${world.connectedWorldIds.size}", style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
                }
              }
            }
            item {
              Text("Recent Activity", style = MaterialTheme.typography.titleMedium, color = SilverPrimary)
            }
            if (worldActivities.isEmpty()) {
              item {
                Text("No activity logged in this World yet.", style = MaterialTheme.typography.bodyMedium, color = SilverSecondary)
              }
            } else {
              items(worldActivities.take(10), key = { it.id }) { act ->
                AetherGlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 12.dp) {
                  Text("${act.actorName} • ${act.action}", style = MaterialTheme.typography.bodyMedium, color = SilverPrimary)
                }
              }
            }
          }

          1 -> {
            item {
              Button(
                onClick = { showAddProject = true },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = ObsidianVoid),
                modifier = Modifier.fillMaxWidth().testTag("world_add_project_button")
              ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Project")
              }
            }
            if (worldProjects.isEmpty()) {
              item {
                AetherGlassCard(modifier = Modifier.fillMaxWidth()) {
                  Text("No projects inside ${world.name} yet.", color = SilverSecondary)
                }
              }
            } else {
              items(worldProjects, key = { it.id }) { project ->
                AetherGlassCard(modifier = Modifier.fillMaxWidth()) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(project.title, style = MaterialTheme.typography.titleMedium, color = SilverPrimary, modifier = Modifier.weight(1f))
                    IconButton(onClick = { onDeleteProject(project) }) {
                      Icon(Icons.Default.DeleteOutline, contentDescription = "Delete project", tint = SilverSecondary)
                    }
                  }
                  if (project.summary.isNotBlank()) {
                    Text(project.summary, style = MaterialTheme.typography.bodyMedium, color = SilverSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                  }
                  Text("Progress: ${project.progress}% (${project.status})", style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
                  Slider(
                    value = project.progress.toFloat(),
                    onValueChange = {},
                    onValueChangeFinished = { /* handled via quick step buttons below */ },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                  )
                  LinearProgressIndicator(
                    progress = { (project.progress / 100f).coerceIn(0f, 1f) },
                    color = ElectricCyan,
                    trackColor = ObsidianVoid,
                    modifier = Modifier.fillMaxWidth().height(4.dp)
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                      onClick = {
                        val next = (project.progress + 25).coerceAtMost(100).toInt()
                        val st = if (next >= 100) "completed" else "active"
                        onUpdateProjectProgress(project, st, next)
                      },
                      border = BorderStroke(1.dp, CyanGlassBorder)
                    ) {
                      Text("+25%", color = ElectricCyan)
                    }
                    OutlinedButton(
                      onClick = { onUpdateProjectProgress(project, "completed", 100) },
                      border = BorderStroke(1.dp, GlassBorder)
                    ) {
                      Text("Mark Done", color = StatusSuccess)
                    }
                  }
                }
              }
            }
          }

          2 -> {
            item {
              Button(
                onClick = { showAddTask = true },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = ObsidianVoid),
                modifier = Modifier.fillMaxWidth().testTag("world_add_task_button")
              ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Task")
              }
            }
            items(worldTasks, key = { it.id }) { task ->
              val icon = when (task.status) {
                "done" -> Icons.Default.CheckCircle
                "in_progress" -> Icons.Default.Timelapse
                else -> Icons.Default.RadioButtonUnchecked
              }
              val tint = when (task.status) {
                "done" -> StatusSuccess
                "in_progress" -> StatusWarning
                else -> ElectricCyan
              }
              AetherGlassCard(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { onCycleTaskStatus(task) },
                contentPadding = 12.dp
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(icon, contentDescription = "Cycle status", tint = tint)
                  Spacer(modifier = Modifier.width(10.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(task.title, style = MaterialTheme.typography.titleMedium, color = SilverPrimary)
                    Text("${task.priority.uppercase()} • ${task.status}", style = MaterialTheme.typography.labelSmall, color = SilverSecondary)
                  }
                  IconButton(onClick = { onDeleteTask(task) }) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete task", tint = SilverSecondary)
                  }
                }
              }
            }
          }

          3 -> {
            item {
              Text("Connect Related Worlds in 3D Space", style = MaterialTheme.typography.titleMedium, color = SilverPrimary)
            }
            val otherWorlds = allWorlds.filter { it.id != world.id }
            if (otherWorlds.isEmpty()) {
              item {
                Text("Create another World to form celestial connections.", color = SilverSecondary)
              }
            } else {
              items(otherWorlds, key = { it.id }) { other ->
                val linked = world.connectedWorldIds.contains(other.id)
                AetherGlassCard(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleWorldLink(other.id) },
                  highlighted = linked,
                  contentPadding = 12.dp
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(other.name, style = MaterialTheme.typography.titleMedium, color = SilverPrimary)
                      Text(other.category, style = MaterialTheme.typography.labelSmall, color = SilverSecondary)
                    }
                    Icon(
                      imageVector = if (linked) Icons.Default.Link else Icons.Default.LinkOff,
                      contentDescription = if (linked) "Linked" else "Unlinked",
                      tint = if (linked) ElectricCyan else SilverSecondary
                    )
                  }
                }
              }
            }
            item {
              Spacer(modifier = Modifier.height(8.dp))
              Text("Linked Teams (${worldTeams.size})", style = MaterialTheme.typography.titleMedium, color = SilverPrimary)
              if (worldTeams.isEmpty()) {
                Text("Link a team from the Network tab.", style = MaterialTheme.typography.bodyMedium, color = SilverSecondary)
              }
            }
          }

          4 -> {
            item {
              WorldAiAssistPanel(
                world = world,
                aiState = aiState,
                onAsk = onAskWorldAi
              )
            }
          }
        }
      }
    }
  }

  if (showAddProject) {
    var title by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { showAddProject = false },
      containerColor = DeepSpaceSurface,
      title = { Text("New Project in ${world.name}", color = SilverPrimary) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Project Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = summary,
            onValueChange = { summary = it },
            label = { Text("Summary") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onCreateProject(title, summary)
            showAddProject = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = ObsidianVoid)
        ) { Text("Create") }
      },
      dismissButton = {
        TextButton(onClick = { showAddProject = false }) { Text("Cancel", color = SilverSecondary) }
      }
    )
  }

  if (showAddTask) {
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("medium") }
    AlertDialog(
      onDismissRequest = { showAddTask = false },
      containerColor = DeepSpaceSurface,
      title = { Text("New Task", color = SilverPrimary) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Task Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("low", "medium", "high").forEach { p ->
              Surface(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { priority = p },
                color = if (priority == p) ElectricCyan else ObsidianVoid,
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(
                  text = p.uppercase(),
                  color = if (priority == p) ObsidianVoid else SilverSecondary,
                  style = MaterialTheme.typography.labelSmall,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onCreateTask(title, priority)
            showAddTask = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = ObsidianVoid)
        ) { Text("Add Task") }
      },
      dismissButton = {
        TextButton(onClick = { showAddTask = false }) { Text("Cancel", color = SilverSecondary) }
      }
    )
  }

  if (showDeleteConfirm) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirm = false },
      containerColor = DeepSpaceSurface,
      title = { Text("Delete ${world.name}?", color = SilverPrimary) },
      text = { Text("This removes the World planet from your universe.", color = SilverSecondary) },
      confirmButton = {
        Button(
          onClick = {
            showDeleteConfirm = false
            onDeleteWorld()
          },
          colors = ButtonDefaults.buttonColors(containerColor = StatusError, contentColor = ObsidianVoid)
        ) { Text("Delete") }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel", color = SilverSecondary) }
      }
    )
  }
}

@Composable
private fun WorldAiAssistPanel(
  world: World,
  aiState: AiStreamUiState,
  onAsk: (AgentRole, String) -> Unit,
) {
  var selectedRole by remember { mutableStateOf(AgentRole.ARCHITECT) }
  var prompt by remember { mutableStateOf("") }

  AetherGlassCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
    Text("Contextual World AI (${world.name})", style = MaterialTheme.typography.titleMedium, color = SilverPrimary)
    Spacer(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      AgentRole.entries.take(3).forEach { role ->
        Surface(
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { selectedRole = role },
          color = if (selectedRole == role) ElectricCyan else ObsidianVoid,
          shape = RoundedCornerShape(10.dp)
        ) {
          Text(
            text = role.shortTitle,
            style = MaterialTheme.typography.labelSmall,
            color = if (selectedRole == role) ObsidianVoid else SilverPrimary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }
      }
    }
    Spacer(modifier = Modifier.height(10.dp))
    OutlinedTextField(
      value = prompt,
      onValueChange = { prompt = it },
      label = { Text("Ask ${selectedRole.shortTitle} about ${world.name}") },
      modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(10.dp))
    Button(
      onClick = { onAsk(selectedRole, prompt) },
      enabled = prompt.isNotBlank() && aiState !is AiStreamUiState.Loading,
      colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = ObsidianVoid),
      modifier = Modifier.fillMaxWidth()
    ) {
      Icon(Icons.Default.AutoAwesome, contentDescription = null)
      Spacer(modifier = Modifier.width(8.dp))
      Text("Consult ${selectedRole.shortTitle}")
    }

    Spacer(modifier = Modifier.height(12.dp))
    when (aiState) {
      AiStreamUiState.Idle -> Unit
      AiStreamUiState.Loading -> CircularProgressIndicator(color = ElectricCyan, modifier = Modifier.size(24.dp))
      is AiStreamUiState.Streaming -> Text(aiState.text, style = MaterialTheme.typography.bodyMedium, color = SilverPrimary)
      is AiStreamUiState.Success -> Text(aiState.text, style = MaterialTheme.typography.bodyMedium, color = SilverPrimary)
      is AiStreamUiState.Error -> Text(aiState.message, style = MaterialTheme.typography.bodyMedium, color = StatusError)
    }
  }
}
