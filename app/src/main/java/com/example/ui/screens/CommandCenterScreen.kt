package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.Activity
import com.example.data.model.AgentRole
import com.example.data.model.NotificationItem
import com.example.data.model.Project
import com.example.data.model.TaskItem
import com.example.data.model.World
import com.example.ui.theme.AetherGlassCard
import com.example.ui.theme.CyanGlassBorder
import com.example.ui.theme.DeepSpaceSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SilverPrimary
import com.example.ui.theme.SilverSecondary
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.parseColorHexOrDefault

@Composable
fun CommandCenterScreen(
  userName: String,
  worlds: List<World>,
  projects: List<Project>,
  tasks: List<TaskItem>,
  notifications: List<NotificationItem>,
  activities: List<Activity>,
  onOpenGlobalSearch: () -> Unit,
  onOpenVoice: () -> Unit,
  onCreateWorld: () -> Unit,
  onOpenWorld: (World) -> Unit,
  onOpenAgents: (AgentRole?) -> Unit,
  onCycleTaskStatus: (TaskItem) -> Unit,
  onMarkNotificationRead: (NotificationItem) -> Unit,
) {
  val unreadCount = notifications.count { !it.isRead }
  val openTasks = tasks.filter { it.status != "done" }

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
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Header + Universal Search Bar Trigger
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "AETHER COMMAND",
              style = MaterialTheme.typography.labelSmall,
              color = ElectricCyan
            )
            Text(
              text = "Welcome back, ${userName.substringBefore(" ")}",
              style = MaterialTheme.typography.headlineMedium,
              color = SilverPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
          IconButton(
            onClick = onOpenVoice,
            modifier = Modifier
              .size(48.dp)
              .testTag("command_voice_button")
          ) {
            Icon(Icons.Default.Mic, contentDescription = "Aether Voice", tint = ElectricCyan)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onOpenGlobalSearch)
            .testTag("universal_command_palette_trigger"),
          color = DeepSpaceSurface,
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, CyanGlassBorder)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = ElectricCyan)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Search Worlds, Projects, Tasks, Agents, Teams...",
              style = MaterialTheme.typography.bodyMedium,
              color = SilverSecondary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }

      // Universe Overview Hero Card
      item {
        Surface(
          shape = RoundedCornerShape(20.dp),
          border = BorderStroke(1.dp, CyanGlassBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(modifier = Modifier.fillMaxWidth()) {
            Image(
              painter = painterResource(id = R.drawable.img_hero_universe),
              contentDescription = "Universe overview banner",
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .matchParentSize()
                .alpha(0.32f)
            )
            Box(
              modifier = Modifier
                .matchParentSize()
                .background(
                  Brush.horizontalGradient(
                    listOf(
                      ObsidianVoid.copy(alpha = 0.92f),
                      DeepSpaceSurface.copy(alpha = 0.72f)
                    )
                  )
                )
            )
            Column(modifier = Modifier.padding(18.dp)) {
              Text(
                text = "UNIVERSE TELEMETRY",
                style = MaterialTheme.typography.labelSmall,
                color = ElectricCyan
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                TelemetryMetric("${worlds.size}", "Worlds")
                TelemetryMetric("${projects.size}", "Projects")
                TelemetryMetric("${openTasks.size}", "Open Tasks")
                TelemetryMetric("$unreadCount", "Alerts")
              }
            }
          }
        }
      }

      // Quick Actions
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          QuickActionChip(
            icon = Icons.Default.Add,
            label = "New World",
            tag = "quick_new_world",
            modifier = Modifier.weight(1f),
            onClick = onCreateWorld
          )
          QuickActionChip(
            icon = Icons.Default.AutoAwesome,
            label = "AI Council",
            tag = "quick_ai_council",
            modifier = Modifier.weight(1f),
            onClick = { onOpenAgents(null) }
          )
          QuickActionChip(
            icon = Icons.Default.Mic,
            label = "Voice Link",
            tag = "quick_voice",
            modifier = Modifier.weight(1f),
            onClick = onOpenVoice
          )
        }
      }

      // AI Agent Status Roster
      item {
        SectionHeader(title = "AI Civilization Online", subtitle = "5 Specialized Gemini Agents")
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          items(AgentRole.entries) { role ->
            val accent = parseColorHexOrDefault(role.accentHex)
            Surface(
              modifier = Modifier
                .width(156.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onOpenAgents(role) }
                .testTag("agent_status_${role.id}"),
              color = DeepSpaceSurface,
              shape = RoundedCornerShape(16.dp),
              border = BorderStroke(1.dp, GlassBorder)
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(8.dp)
                      .background(StatusSuccess, CircleShape)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "READY",
                    style = MaterialTheme.typography.labelSmall,
                    color = StatusSuccess
                  )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = role.shortTitle,
                  style = MaterialTheme.typography.titleMedium,
                  color = accent
                )
                Spacer(modifier = Modifier.height(2.dp))
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

      // Active Worlds
      item {
        SectionHeader(title = "Active Worlds", subtitle = "${worlds.size} orbiting workspaces")
        Spacer(modifier = Modifier.height(8.dp))
        if (worlds.isEmpty()) {
          AetherGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
              text = "No Worlds created yet. Tap \"New World\" to launch your first workspace.",
              style = MaterialTheme.typography.bodyMedium,
              color = SilverSecondary
            )
          }
        } else {
          LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(worlds, key = { it.id }) { world ->
              val accent = parseColorHexOrDefault(world.colorHex)
              val wProjects = projects.count { it.worldId == world.id }
              Surface(
                modifier = Modifier
                  .width(190.dp)
                  .clip(RoundedCornerShape(16.dp))
                  .clickable { onOpenWorld(world) }
                  .testTag("command_world_${world.id}"),
                color = DeepSpaceSurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.4f))
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Public, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = world.category,
                      style = MaterialTheme.typography.labelSmall,
                      color = accent,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = world.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = SilverPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "$wProjects projects • ${world.connectedWorldIds.size} links",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SilverSecondary
                  )
                }
              }
            }
          }
        }
      }

      // Recent Projects
      if (projects.isNotEmpty()) {
        item {
          SectionHeader(title = "Recent Projects", subtitle = "Tap a World to manage")
        }
        items(projects.take(3), key = { it.id }) { project ->
          val parentWorld = worlds.firstOrNull { it.id == project.worldId }
          AetherGlassCard(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { parentWorld?.let(onOpenWorld) }
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = project.title,
                style = MaterialTheme.typography.titleMedium,
                color = SilverPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "${project.progress}%",
                style = MaterialTheme.typography.labelSmall,
                color = ElectricCyan
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
              progress = { (project.progress / 100f).coerceIn(0f, 1f) },
              color = ElectricCyan,
              trackColor = ObsidianVoid,
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
            )
            if (parentWorld != null) {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "World: ${parentWorld.name} • ${project.status.uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = SilverSecondary
              )
            }
          }
        }
      }

      // Tasks
      if (tasks.isNotEmpty()) {
        item {
          SectionHeader(title = "Priority Tasks", subtitle = "Tap to cycle status")
        }
        items(tasks.take(5), key = { it.id }) { task ->
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
              .clickable { onCycleTaskStatus(task) }
              .testTag("command_task_${task.id}"),
            contentPadding = 12.dp
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(icon, contentDescription = "Toggle task", tint = tint, modifier = Modifier.size(22.dp))
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = task.title,
                  style = MaterialTheme.typography.bodyLarge,
                  color = SilverPrimary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "${task.priority.uppercase()} • ${task.status.replace('_', ' ').uppercase()}",
                  style = MaterialTheme.typography.labelSmall,
                  color = SilverSecondary
                )
              }
            }
          }
        }
      }

      // Notifications & Recent Activity
      if (notifications.isNotEmpty()) {
        item {
          SectionHeader(title = "Notifications", subtitle = "$unreadCount unread")
        }
        items(notifications.take(3), key = { it.id }) { notif ->
          AetherGlassCard(
            highlighted = !notif.isRead,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onMarkNotificationRead(notif) },
            contentPadding = 12.dp
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = if (notif.isRead) SilverSecondary else ElectricCyan,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(notif.title, style = MaterialTheme.typography.titleMedium, color = SilverPrimary)
                Text(notif.message, style = MaterialTheme.typography.bodyMedium, color = SilverSecondary)
              }
            }
          }
        }
      }

      if (activities.isNotEmpty()) {
        item {
          SectionHeader(title = "Recent Activity", subtitle = "Live universe pulse")
        }
        items(activities.take(5), key = { it.id }) { act ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .background(ElectricCyan, CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "${act.actorName}: ${act.action}",
              style = MaterialTheme.typography.bodyMedium,
              color = SilverSecondary
            )
          }
        }
      }
    }
  }
}

@Composable
private fun TelemetryMetric(value: String, label: String) {
  Column(horizontalAlignment = Alignment.Start) {
    Text(
      text = value,
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
      color = SilverPrimary
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = SilverSecondary
    )
  }
}

@Composable
private fun QuickActionChip(
  icon: ImageVector,
  label: String,
  tag: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  Surface(
    modifier = modifier
      .height(48.dp)
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .testTag(tag),
    color = DeepSpaceSurface,
    shape = RoundedCornerShape(14.dp),
    border = BorderStroke(1.dp, GlassBorder)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Icon(icon, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text(label, style = MaterialTheme.typography.labelLarge, color = SilverPrimary, maxLines = 1)
    }
  }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Bottom
  ) {
    Text(title, style = MaterialTheme.typography.titleLarge, color = SilverPrimary)
    Text(subtitle, style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
  }
}
