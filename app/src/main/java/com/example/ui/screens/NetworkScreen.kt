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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.Activity
import com.example.data.model.Team
import com.example.data.model.TeamMembership
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

@Composable
fun NetworkScreen(
  teams: List<Team>,
  memberships: List<TeamMembership>,
  worlds: List<World>,
  activities: List<Activity>,
  onCreateTeam: (name: String, description: String, worldId: String) -> Unit,
  onInviteMember: (teamId: String, name: String, handle: String, role: String) -> Unit,
  onUpdateMemberRole: (membershipId: String, role: String) -> Unit,
  onRemoveMember: (membershipId: String) -> Unit,
) {
  var showCreateTeam by remember { mutableStateOf(false) }
  var inviteTargetTeam by remember { mutableStateOf<Team?>(null) }

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
            Text("LIVE WORLDS", style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
            Text("Teams & Collaboration", style = MaterialTheme.typography.headlineMedium, color = SilverPrimary)
          }
          Button(
            onClick = { showCreateTeam = true },
            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = ObsidianVoid),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("create_team_button")
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Team")
          }
        }
      }

      if (teams.isEmpty()) {
        item {
          AetherGlassCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Groups, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("No Teams Formed Yet", style = MaterialTheme.typography.titleLarge, color = SilverPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Create a team to share Digital Worlds, issue invitation codes, and assign Owner, Administrator, Editor, or Viewer permissions.",
              style = MaterialTheme.typography.bodyMedium,
              color = SilverSecondary
            )
          }
        }
      } else {
        items(teams, key = { it.id }) { team ->
          val teamMembers = memberships.filter { it.teamId == team.id }
          val linkedWorld = worlds.firstOrNull { it.id == team.worldId }

          AetherGlassCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(team.name, style = MaterialTheme.typography.titleLarge, color = SilverPrimary)
                Text(
                  text = "INVITE CODE: ${team.inviteCode}" + (linkedWorld?.let { " • WORLD: ${it.name}" } ?: ""),
                  style = MaterialTheme.typography.labelSmall,
                  color = ElectricCyan
                )
              }
              OutlinedButton(
                onClick = { inviteTargetTeam = team },
                border = BorderStroke(1.dp, CyanGlassBorder),
                modifier = Modifier.testTag("invite_member_${team.id}")
              ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Invite", color = ElectricCyan)
              }
            }

            if (team.description.isNotBlank()) {
              Spacer(modifier = Modifier.height(6.dp))
              Text(team.description, style = MaterialTheme.typography.bodyMedium, color = SilverSecondary)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Members & RBAC (${teamMembers.size})", style = MaterialTheme.typography.labelSmall, color = SilverSecondary)
            Spacer(modifier = Modifier.height(6.dp))

            teamMembers.forEach { member ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .background(ElectricCyan.copy(alpha = 0.16f), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = member.displayName.take(1).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricCyan
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(member.displayName, style = MaterialTheme.typography.titleMedium, color = SilverPrimary)
                  Text(
                    text = "${member.role.uppercase()} • ${member.status.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (member.status == "active") StatusSuccess else SilverSecondary
                  )
                }
                if (member.role != "owner") {
                  Surface(
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .clickable {
                        val nextRole = when (member.role) {
                          "viewer" -> "editor"
                          "editor" -> "administrator"
                          else -> "viewer"
                        }
                        onUpdateMemberRole(member.id, nextRole)
                      },
                    color = ObsidianVoid,
                    border = BorderStroke(1.dp, GlassBorder),
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Text(
                      text = "Cycle Role",
                      style = MaterialTheme.typography.labelSmall,
                      color = ElectricCyan,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                  }
                  IconButton(onClick = { onRemoveMember(member.id) }) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Remove member", tint = SilverSecondary)
                  }
                }
              }
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(6.dp))
        Text("Collaboration Activity Feed", style = MaterialTheme.typography.titleLarge, color = SilverPrimary)
      }
      if (activities.isEmpty()) {
        item {
          Text("Team and workspace activity appears here in real time.", style = MaterialTheme.typography.bodyMedium, color = SilverSecondary)
        }
      } else {
        items(activities.take(15), key = { it.id }) { act ->
          AetherGlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 12.dp) {
            Text("${act.actorName} • ${act.action}", style = MaterialTheme.typography.bodyMedium, color = SilverPrimary)
            Text(act.category.uppercase(), style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
          }
        }
      }
    }
  }

  if (showCreateTeam) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedWorldId by remember { mutableStateOf(worlds.firstOrNull()?.id.orEmpty()) }
    AlertDialog(
      onDismissRequest = { showCreateTeam = false },
      containerColor = DeepSpaceSurface,
      title = { Text("Create Collaboration Team", color = SilverPrimary) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Team Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Purpose") },
            modifier = Modifier.fillMaxWidth()
          )
          if (worlds.isNotEmpty()) {
            Text("Shared World", style = MaterialTheme.typography.labelSmall, color = SilverSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              worlds.take(3).forEach { w ->
                Surface(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { selectedWorldId = w.id },
                  color = if (selectedWorldId == w.id) ElectricCyan else ObsidianVoid,
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text(
                    text = w.name.take(12),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selectedWorldId == w.id) ObsidianVoid else SilverPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                  )
                }
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onCreateTeam(name, description, selectedWorldId)
            showCreateTeam = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = ObsidianVoid)
        ) { Text("Create Team") }
      },
      dismissButton = {
        TextButton(onClick = { showCreateTeam = false }) { Text("Cancel", color = SilverSecondary) }
      }
    )
  }

  inviteTargetTeam?.let { team ->
    var collabName by remember { mutableStateOf("") }
    var collabHandle by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("editor") }
    AlertDialog(
      onDismissRequest = { inviteTargetTeam = null },
      containerColor = DeepSpaceSurface,
      title = { Text("Invite to ${team.name}", color = SilverPrimary) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = collabName,
            onValueChange = { collabName = it },
            label = { Text("Member Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = collabHandle,
            onValueChange = { collabHandle = it },
            label = { Text("Handle or UID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          Text("Assign Role", style = MaterialTheme.typography.labelSmall, color = SilverSecondary)
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("administrator", "editor", "viewer").forEach { r ->
              Surface(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { role = r },
                color = if (role == r) ElectricCyan else ObsidianVoid,
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(
                  text = r.take(5).uppercase(),
                  style = MaterialTheme.typography.labelSmall,
                  color = if (role == r) ObsidianVoid else SilverPrimary,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onInviteMember(team.id, collabName, collabHandle, role)
            inviteTargetTeam = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = ObsidianVoid)
        ) { Text("Send Invite") }
      },
      dismissButton = {
        TextButton(onClick = { inviteTargetTeam = null }) { Text("Cancel", color = SilverSecondary) }
      }
    )
  }
}
