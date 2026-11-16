package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.UserProfile
import com.example.data.model.World
import com.example.data.preferences.AetherUserSettings
import com.example.data.preferences.UniverseRenderQuality
import com.example.ui.theme.CyanGlassBorder
import com.example.ui.theme.DeepSpaceSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SilverPrimary
import com.example.ui.theme.SilverSecondary
import com.example.ui.theme.StatusError
import com.example.ui.theme.parseColorHexOrDefault

@Composable
fun CreateOrEditWorldDialog(
  existingWorld: World? = null,
  onDismiss: () -> Unit,
  onConfirm: (name: String, description: String, colorHex: String, category: String, orbitRadius: Double) -> Unit,
) {
  var name by remember { mutableStateOf(existingWorld?.name.orEmpty()) }
  var description by remember { mutableStateOf(existingWorld?.description.orEmpty()) }
  var colorHex by remember { mutableStateOf(existingWorld?.colorHex ?: "#22D3EE") }
  var category by remember { mutableStateOf(existingWorld?.category ?: "Architecture") }
  var orbitRadius by remember { mutableDoubleStateOf(existingWorld?.orbitRadius ?: 115.0) }

  val palette = listOf("#22D3EE", "#38BDF8", "#67E8F9", "#06B6D4", "#94A3B8", "#34D399")
  val categories = listOf("Architecture", "Research", "Product", "Creative", "Operations")

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = DeepSpaceSurface,
    title = {
      Text(
        text = if (existingWorld == null) "Materialize New World" else "Edit ${existingWorld.name}",
        color = SilverPrimary
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("World Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("world_name_input")
        )
        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Description") },
          maxLines = 3,
          modifier = Modifier.fillMaxWidth().testTag("world_desc_input")
        )
        Text("Category", style = MaterialTheme.typography.labelSmall, color = SilverSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          categories.take(3).forEach { cat ->
            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { category = cat },
              color = if (category == cat) ElectricCyan else ObsidianVoid,
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = cat,
                style = MaterialTheme.typography.labelSmall,
                color = if (category == cat) ObsidianVoid else SilverPrimary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
              )
            }
          }
        }
        Text("Planet Color", style = MaterialTheme.typography.labelSmall, color = SilverSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          palette.forEach { hex ->
            val color = parseColorHexOrDefault(hex)
            Box(
              modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(color)
                .clickable { colorHex = hex },
              contentAlignment = Alignment.Center
            ) {
              if (colorHex == hex) {
                Box(
                  modifier = Modifier
                    .size(12.dp)
                    .background(ObsidianVoid, CircleShape)
                )
              }
            }
          }
        }
        if (existingWorld == null) {
          Text("Orbit Radius: ${orbitRadius.toInt()}", style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
          Slider(
            value = orbitRadius.toFloat(),
            onValueChange = { orbitRadius = it.toDouble() },
            valueRange = 60f..220f,
            colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(name, description, colorHex, category, orbitRadius) },
        enabled = name.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = ObsidianVoid),
        modifier = Modifier.testTag("confirm_world_button")
      ) {
        Text(if (existingWorld == null) "Launch World" else "Save")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = SilverSecondary) }
    }
  )
}

@Composable
fun SettingsAndProfileDialog(
  profile: UserProfile?,
  fallbackName: String,
  userEmail: String,
  settings: AetherUserSettings,
  onUpdateProfile: (displayName: String, roleTitle: String, bio: String) -> Unit,
  onSetQuality: (UniverseRenderQuality) -> Unit,
  onSetReducedMotion: (Boolean) -> Unit,
  onSetVoiceAutoSpeak: (Boolean) -> Unit,
  onSignOut: () -> Unit,
  onDismiss: () -> Unit,
) {
  var displayName by remember(profile) { mutableStateOf(profile?.displayName ?: fallbackName) }
  var roleTitle by remember(profile) { mutableStateOf(profile?.roleTitle ?: "Cosmic Architect") }
  var bio by remember(profile) { mutableStateOf(profile?.bio.orEmpty()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = DeepSpaceSurface,
    title = { Text("AETHER X Identity & Engine", color = SilverPrimary) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(userEmail, style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
        OutlinedTextField(
          value = displayName,
          onValueChange = { displayName = it },
          label = { Text("Display Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = roleTitle,
          onValueChange = { roleTitle = it },
          label = { Text("Cosmic Role") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = bio,
          onValueChange = { bio = it },
          label = { Text("Bio") },
          maxLines = 2,
          modifier = Modifier.fillMaxWidth()
        )
        Button(
          onClick = { onUpdateProfile(displayName, roleTitle, bio) },
          colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = ObsidianVoid),
          modifier = Modifier.fillMaxWidth()
        ) { Text("Save Profile") }

        HorizontalDivider(color = GlassBorder)

        Text("Universe Graphics Quality", style = MaterialTheme.typography.labelSmall, color = SilverSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          UniverseRenderQuality.entries.forEach { q ->
            Surface(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onSetQuality(q) },
              color = if (settings.renderQuality == q) ElectricCyan else ObsidianVoid,
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, GlassBorder)
            ) {
              Text(
                text = q.label.substringBefore(" "),
                style = MaterialTheme.typography.labelSmall,
                color = if (settings.renderQuality == q) ObsidianVoid else SilverPrimary,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
              )
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Reduced Motion", style = MaterialTheme.typography.bodyMedium, color = SilverPrimary)
          Switch(
            checked = settings.reducedMotion,
            onCheckedChange = onSetReducedMotion,
            colors = SwitchDefaults.colors(checkedThumbColor = ObsidianVoid, checkedTrackColor = ElectricCyan)
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Voice Auto-Speak (TTS)", style = MaterialTheme.typography.bodyMedium, color = SilverPrimary)
          Switch(
            checked = settings.voiceAutoSpeak,
            onCheckedChange = onSetVoiceAutoSpeak,
            colors = SwitchDefaults.colors(checkedThumbColor = ObsidianVoid, checkedTrackColor = ElectricCyan)
          )
        }

        HorizontalDivider(color = GlassBorder)
        OutlinedButton(
          onClick = onSignOut,
          border = BorderStroke(1.dp, StatusError.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth().testTag("sign_out_button")
        ) {
          Text("Sign Out", color = StatusError)
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) { Text("Done", color = ElectricCyan) }
    }
  )
}
