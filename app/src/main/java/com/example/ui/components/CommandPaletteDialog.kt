package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Conversation
import com.example.data.model.Project
import com.example.data.model.TaskItem
import com.example.data.model.TeamMembership
import com.example.data.model.World
import com.example.ui.theme.AetherGlassCard
import com.example.ui.theme.DeepSpaceSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SilverPrimary
import com.example.ui.theme.SilverSecondary

private data class SearchEntry(
  val id: String,
  val kind: String,
  val title: String,
  val subtitle: String,
  val icon: ImageVector,
  val onSelect: () -> Unit,
)

@Composable
fun CommandPaletteDialog(
  worlds: List<World>,
  projects: List<Project>,
  tasks: List<TaskItem>,
  conversations: List<Conversation>,
  memberships: List<TeamMembership>,
  onOpenWorld: (World) -> Unit,
  onNavigateTab: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  var query by remember { mutableStateOf("") }

  val allEntries = remember(worlds, projects, tasks, conversations, memberships) {
    buildList {
      worlds.forEach { w ->
        add(SearchEntry("w_${w.id}", "WORLD", w.name, w.category, Icons.Default.Public) {
          onOpenWorld(w)
          onDismiss()
        })
      }
      projects.forEach { p ->
        val w = worlds.firstOrNull { it.id == p.worldId }
        add(SearchEntry("p_${p.id}", "PROJECT", p.title, "${p.status} • ${p.progress}%", Icons.Default.FolderOpen) {
          if (w != null) onOpenWorld(w) else onNavigateTab("command")
          onDismiss()
        })
      }
      tasks.forEach { t ->
        val w = worlds.firstOrNull { it.id == t.worldId }
        add(SearchEntry("t_${t.id}", "TASK", t.title, "${t.priority} • ${t.status}", Icons.Default.CheckCircleOutline) {
          if (w != null) onOpenWorld(w) else onNavigateTab("command")
          onDismiss()
        })
      }
      conversations.forEach { c ->
        add(SearchEntry("c_${c.id}", "CONVERSATION", c.title, c.agentId.uppercase(), Icons.Default.AutoAwesome) {
          onNavigateTab("agents")
          onDismiss()
        })
      }
      memberships.forEach { m ->
        add(SearchEntry("m_${m.id}", "MEMBER", m.displayName, "${m.role.uppercase()} • ${m.status}", Icons.Default.Groups) {
          onNavigateTab("network")
          onDismiss()
        })
      }
    }
  }

  val results = remember(allEntries, query) {
    if (query.isBlank()) allEntries.take(12)
    else allEntries.filter {
      it.title.contains(query, ignoreCase = true) ||
        it.subtitle.contains(query, ignoreCase = true) ||
        it.kind.contains(query, ignoreCase = true)
    }.take(20)
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = DeepSpaceSurface,
    title = { Text("Universal Command Palette", color = SilverPrimary) },
    text = {
      Column {
        OutlinedTextField(
          value = query,
          onValueChange = { query = it },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ElectricCyan) },
          placeholder = { Text("Search Worlds, Projects, Tasks, Chats, Members") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("command_palette_search_input")
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .height(290.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (results.isEmpty()) {
            item {
              Text("No matching items across your universe.", color = SilverSecondary)
            }
          } else {
            items(results, key = { it.id }) { entry ->
              AetherGlassCard(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable(onClick = entry.onSelect),
                contentPadding = 10.dp
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(entry.icon, contentDescription = null, tint = ElectricCyan)
                  Spacer(modifier = Modifier.width(10.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(entry.kind, style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
                    Text(
                      text = entry.title,
                      style = MaterialTheme.typography.titleMedium,
                      color = SilverPrimary,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Text(entry.subtitle, style = MaterialTheme.typography.bodyMedium, color = SilverSecondary, maxLines = 1)
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) { Text("Close", color = ElectricCyan) }
    }
  )
}
