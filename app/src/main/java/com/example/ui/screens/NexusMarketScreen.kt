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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.MarketplaceItem
import com.example.ui.theme.AetherGlassCard
import com.example.ui.theme.CyanGlassBorder
import com.example.ui.theme.DeepSpaceSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SilverPrimary
import com.example.ui.theme.SilverSecondary

@Composable
fun NexusMarketScreen(
  currentUserId: String,
  items: List<MarketplaceItem>,
  onToggleFavorite: (MarketplaceItem) -> Unit,
  onDeployAsWorld: (MarketplaceItem) -> Unit,
  onPublishItem: (title: String, description: String, category: String, coverKey: String, tagsCsv: String) -> Unit,
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("All") }
  var onlyFavorites by remember { mutableStateOf(false) }
  var showPublishDialog by remember { mutableStateOf(false) }
  var inspectedCreator by remember { mutableStateOf<MarketplaceItem?>(null) }

  val categories = listOf("All", "World Kit", "Architecture", "AI Prompt", "UI System", "Workflow")

  val filteredItems = remember(items, searchQuery, selectedCategory, onlyFavorites, currentUserId) {
    items.filter { item ->
      val matchesCat = selectedCategory == "All" || item.category == selectedCategory
      val matchesFav = !onlyFavorites || item.favoriteUids.contains(currentUserId)
      val matchesQuery = searchQuery.isBlank() ||
        item.title.contains(searchQuery, ignoreCase = true) ||
        item.description.contains(searchQuery, ignoreCase = true) ||
        item.creatorName.contains(searchQuery, ignoreCase = true)
      matchesCat && matchesFav && matchesQuery
    }
  }

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
            Text("NEXUS MARKET", style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
            Text("Digital Creations & Templates", style = MaterialTheme.typography.headlineMedium, color = SilverPrimary)
          }
          Button(
            onClick = { showPublishDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = ObsidianVoid),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("publish_market_item_button")
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Submit")
          }
        }
      }

      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ElectricCyan) },
          placeholder = { Text("Search templates, kits, creators...") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("nexus_search_input")
        )
      }

      item {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          item {
            FilterPill(
              label = "Saved",
              selected = onlyFavorites,
              onClick = { onlyFavorites = !onlyFavorites }
            )
          }
          items(categories) { cat ->
            FilterPill(
              label = cat,
              selected = selectedCategory == cat,
              onClick = { selectedCategory = cat }
            )
          }
        }
      }

      if (filteredItems.isEmpty()) {
        item {
          AetherGlassCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
            Text("Nexus Market Catalog", style = MaterialTheme.typography.titleLarge, color = SilverPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = if (items.isEmpty()) "No digital creations published yet. Be the first creator to submit a template or World kit."
              else "No matching creations found for your active filters.",
              style = MaterialTheme.typography.bodyMedium,
              color = SilverSecondary
            )
          }
        }
      } else {
        items(filteredItems, key = { it.id }) { item ->
          val isFav = item.favoriteUids.contains(currentUserId)
          val coverRes = when (item.coverKey) {
            "architect" -> R.drawable.img_nexus_architect
            "neural" -> R.drawable.img_nexus_neural
            "cosmos" -> R.drawable.img_nexus_cosmos
            else -> R.drawable.img_hero_universe
          }

          AetherGlassCard(highlighted = isFav, modifier = Modifier.fillMaxWidth(), contentPadding = 0.dp) {
            Image(
              painter = painterResource(id = coverRes),
              contentDescription = item.title,
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .fillMaxWidth()
                .height(146.dp)
            )
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(item.category.uppercase(), style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
                  Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = SilverPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
                IconButton(
                  onClick = { onToggleFavorite(item) },
                  modifier = Modifier.testTag("favorite_market_${item.id}")
                ) {
                  Icon(
                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Save favorite",
                    tint = if (isFav) ElectricCyan else SilverSecondary
                  )
                }
              }

              Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = SilverSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
              )

              Spacer(modifier = Modifier.height(10.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "By ${item.creatorName} • ${item.favoriteUids.size} saves",
                  style = MaterialTheme.typography.labelSmall,
                  color = SilverSecondary,
                  modifier = Modifier.clickable { inspectedCreator = item }
                )
                OutlinedButton(
                  onClick = { onDeployAsWorld(item) },
                  border = BorderStroke(1.dp, CyanGlassBorder)
                ) {
                  Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Launch World", color = ElectricCyan)
                }
              }
            }
          }
        }
      }
    }
  }

  if (showPublishDialog) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("World Kit") }
    var coverKey by remember { mutableStateOf("cosmos") }
    var tagsCsv by remember { mutableStateOf("3d, cyan, blueprint") }

    AlertDialog(
      onDismissRequest = { showPublishDialog = false },
      containerColor = DeepSpaceSurface,
      title = { Text("Submit to Nexus Market", color = SilverPrimary) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Creation Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
          )
          Text("Category", style = MaterialTheme.typography.labelSmall, color = SilverSecondary)
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(categories.drop(1)) { cat ->
              FilterPill(label = cat, selected = category == cat, onClick = { category = cat })
            }
          }
          Text("Visual Cover", style = MaterialTheme.typography.labelSmall, color = SilverSecondary)
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("cosmos", "architect", "neural", "universe").forEach { key ->
              FilterPill(label = key, selected = coverKey == key, onClick = { coverKey = key })
            }
          }
          OutlinedTextField(
            value = tagsCsv,
            onValueChange = { tagsCsv = it },
            label = { Text("Tags (comma separated)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onPublishItem(title, description, category, coverKey, tagsCsv)
            showPublishDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = ObsidianVoid)
        ) { Text("Publish") }
      },
      dismissButton = {
        TextButton(onClick = { showPublishDialog = false }) { Text("Cancel", color = SilverSecondary) }
      }
    )
  }

  inspectedCreator?.let { creatorItem ->
    AlertDialog(
      onDismissRequest = { inspectedCreator = null },
      containerColor = DeepSpaceSurface,
      title = { Text(creatorItem.creatorName, color = SilverPrimary) },
      text = {
        Column {
          Text("Creator in AETHER X Nexus Market", style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
          Spacer(modifier = Modifier.height(8.dp))
          Text("Featured Creation: ${creatorItem.title}", style = MaterialTheme.typography.titleMedium, color = SilverPrimary)
          Spacer(modifier = Modifier.height(4.dp))
          Text(creatorItem.description, style = MaterialTheme.typography.bodyMedium, color = SilverSecondary)
        }
      },
      confirmButton = {
        TextButton(onClick = { inspectedCreator = null }) { Text("Close", color = ElectricCyan) }
      }
    )
  }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
  Surface(
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .clickable(onClick = onClick),
    color = if (selected) ElectricCyan else DeepSpaceSurface,
    shape = RoundedCornerShape(10.dp),
    border = BorderStroke(1.dp, if (selected) ElectricCyan else GlassBorder)
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = if (selected) ObsidianVoid else SilverPrimary,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
    )
  }
}
