package com.example.ui.universe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Project
import com.example.data.model.TaskItem
import com.example.data.model.World
import com.example.data.preferences.AetherUserSettings
import com.example.data.preferences.UniverseRenderQuality
import com.example.ui.theme.AetherBrandEmblem
import com.example.ui.theme.AetherGlassCard
import com.example.ui.theme.CyanGlassBorder
import com.example.ui.theme.DeepSpaceSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.IceCyan
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SilverPrimary
import com.example.ui.theme.SilverSecondary
import com.example.ui.theme.SoftCyanGlow
import com.example.ui.theme.SpaceGroteskFamily
import com.example.ui.theme.parseColorHexOrDefault
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class StarParticle(
  val position: Vec3,
  val baseRadius: Float,
  val alpha: Float,
  val isCyanNode: Boolean,
)

private data class ProjectedPlanet(
  val world: World,
  val projected: ProjectedPoint,
  val screenRadius: Float,
  val color: Color,
)

@Composable
fun UniverseScreen(
  worlds: List<World>,
  projects: List<Project>,
  tasks: List<TaskItem>,
  settings: AetherUserSettings,
  showOpeningOverlay: Boolean,
  onDismissOpening: () -> Unit,
  onOpenWorld: (World) -> Unit,
  onCreateWorldClick: () -> Unit,
  onInitializeGenesis: () -> Unit,
  onToggle2DFallback: () -> Unit,
  onOpenSearch: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenVoice: () -> Unit,
) {
  var yawRad by remember { mutableFloatStateOf(0.42f) }
  var pitchRad by remember { mutableFloatStateOf(-0.34f) }
  var zoom by remember { mutableFloatStateOf(1.05f) }
  var panX by remember { mutableFloatStateOf(0f) }
  var panY by remember { mutableFloatStateOf(0f) }
  var selectedWorldId by remember { mutableStateOf<String?>(null) }
  var canvasSize by remember { mutableStateOf(IntSize.Zero) }
  var timeSec by remember { mutableFloatStateOf(0f) }

  // Frame loop respecting Reduced Motion & 2D fallback
  LaunchedEffect(settings.reducedMotion, settings.renderQuality) {
    if (settings.reducedMotion || settings.renderQuality == UniverseRenderQuality.TWO_D_FALLBACK) {
      return@LaunchedEffect
    }
    var lastNanos = 0L
    while (true) {
      withFrameNanos { frameNanos ->
        if (lastNanos != 0L) {
          val dt = ((frameNanos - lastNanos) / 1_000_000_000f).coerceAtMost(0.05f)
          timeSec += dt
        }
        lastNanos = frameNanos
      }
    }
  }

  val starCount = when (settings.renderQuality) {
    UniverseRenderQuality.HIGH -> 220
    UniverseRenderQuality.BALANCED -> 110
    UniverseRenderQuality.TWO_D_FALLBACK -> 60
  }

  val stars = remember(starCount) {
    val rng = Random(42_077)
    List(starCount) { idx ->
      val r = 180f + rng.nextFloat() * 340f
      val theta = rng.nextFloat() * (2f * PI.toFloat())
      val phi = (rng.nextFloat() - 0.5f) * PI.toFloat() * 0.85f
      StarParticle(
        position = Vec3(
          x = r * cos(phi) * cos(theta),
          y = r * sin(phi),
          z = r * cos(phi) * sin(theta),
        ),
        baseRadius = 0.9f + rng.nextFloat() * 2.0f,
        alpha = 0.25f + rng.nextFloat() * 0.65f,
        isCyanNode = idx % 5 == 0,
      )
    }
  }

  val textMeasurer = rememberTextMeasurer()
  val selectedWorld = remember(worlds, selectedWorldId) {
    worlds.firstOrNull { it.id == selectedWorldId }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianVoid)
  ) {
    if (settings.renderQuality == UniverseRenderQuality.TWO_D_FALLBACK) {
      Universe2DFallbackCanvas(
        worlds = worlds,
        selectedWorldId = selectedWorldId,
        stars = stars,
        textMeasurer = textMeasurer,
        onSelectWorld = { selectedWorldId = it?.id },
        modifier = Modifier.fillMaxSize()
      )
    } else {
      Universe3DCanvas(
        worlds = worlds,
        stars = stars,
        yawRad = yawRad,
        pitchRad = pitchRad,
        zoom = zoom,
        panX = panX,
        panY = panY,
        timeSec = timeSec,
        selectedWorldId = selectedWorldId,
        textMeasurer = textMeasurer,
        onSizeMeasured = { canvasSize = it },
        onTransform = { panDelta, zoomChange, isSingleFinger ->
          if (isSingleFinger) {
            yawRad += panDelta.x * 0.0065f
            pitchRad = (pitchRad + panDelta.y * 0.0065f).coerceIn(-1.25f, 1.25f)
          } else {
            zoom = (zoom * zoomChange).coerceIn(0.55f, 2.75f)
            panX = (panX + panDelta.x).coerceIn(-400f, 400f)
            panY = (panY + panDelta.y).coerceIn(-400f, 400f)
          }
        },
        onTapCanvas = { tapOffset ->
          val w = canvasSize.width.toFloat()
          val h = canvasSize.height.toFloat()
          if (w > 0f && h > 0f) {
            val hit = findTappedWorld(
              worlds = worlds,
              tapOffset = tapOffset,
              yawRad = yawRad,
              pitchRad = pitchRad,
              zoom = zoom,
              panX = panX,
              panY = panY,
              timeSec = timeSec,
              width = w,
              height = h
            )
            selectedWorldId = hit?.id
          }
        },
        modifier = Modifier
          .fillMaxSize()
          .testTag("universe_3d_canvas")
      )
    }

    // Top HUD
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          color = MidnightBlack.copy(alpha = 0.78f),
          shape = RoundedCornerShape(16.dp),
          border = BorderStroke(1.dp, CyanGlassBorder)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            AetherBrandEmblem(size = 28.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "AETHER X",
                style = MaterialTheme.typography.labelLarge,
                color = SilverPrimary,
                letterSpacing = 1.6.sp
              )
              Text(
                text = "${worlds.size} WORLDS • ${settings.renderQuality.label.uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = ElectricCyan,
              )
            }
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          HudIconPill(
            icon = Icons.Default.Search,
            contentDescription = "Global Search",
            tag = "universe_search_button",
            onClick = onOpenSearch
          )
          HudIconPill(
            icon = Icons.Default.AutoAwesome,
            contentDescription = "Aether Voice",
            tag = "universe_voice_button",
            onClick = onOpenVoice
          )
          HudIconPill(
            icon = Icons.Default.Settings,
            contentDescription = "Universe Settings",
            tag = "universe_settings_button",
            onClick = onOpenSettings
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Quick Mode & Camera Controls
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onToggle2DFallback)
            .testTag("toggle_2d_3d_button"),
          color = DeepSpaceSurface.copy(alpha = 0.8f),
          border = BorderStroke(1.dp, GlassBorder),
          shape = RoundedCornerShape(12.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Layers,
              contentDescription = null,
              tint = ElectricCyan,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (settings.renderQuality == UniverseRenderQuality.TWO_D_FALLBACK) "Switch to 3D" else "2D Map",
              style = MaterialTheme.typography.labelSmall,
              color = SilverPrimary
            )
          }
        }

        Surface(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable {
              yawRad = 0.42f
              pitchRad = -0.34f
              zoom = 1.05f
              panX = 0f
              panY = 0f
              selectedWorldId = null
            }
            .testTag("reset_universe_camera_button"),
          color = DeepSpaceSurface.copy(alpha = 0.8f),
          border = BorderStroke(1.dp, GlassBorder),
          shape = RoundedCornerShape(12.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.CenterFocusStrong,
              contentDescription = null,
              tint = SilverSecondary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Center",
              style = MaterialTheme.typography.labelSmall,
              color = SilverSecondary
            )
          }
        }
      }
    }

    // Empty universe onboarding card when user has 0 worlds yet
    if (worlds.isEmpty() && !showOpeningOverlay) {
      AetherGlassCard(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(horizontal = 16.dp, vertical = 20.dp)
          .fillMaxWidth(),
        highlighted = true
      ) {
        Text(
          text = "Your Universe Awaits",
          style = MaterialTheme.typography.titleLarge,
          color = SilverPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Create your first Digital World planet or initialize Aether Genesis with a starter workspace.",
          style = MaterialTheme.typography.bodyMedium,
          color = SilverSecondary
        )
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Button(
            onClick = onInitializeGenesis,
            colors = ButtonDefaults.buttonColors(
              containerColor = ElectricCyan,
              contentColor = ObsidianVoid
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("init_genesis_world_button")
          ) {
            Text("Initialize Genesis")
          }
          OutlinedButton(
            onClick = onCreateWorldClick,
            border = BorderStroke(1.dp, CyanGlassBorder),
            modifier = Modifier
              .weight(1f)
              .testTag("empty_create_world_button")
          ) {
            Text("Custom World", color = ElectricCyan)
          }
        }
      }
    }

    // Selected Planet Floating Inspector
    AnimatedVisibility(
      visible = selectedWorld != null && !showOpeningOverlay,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(start = 16.dp, end = 88.dp, bottom = 18.dp)
    ) {
      selectedWorld?.let { world ->
        val worldProjects = projects.count { it.worldId == world.id }
        val worldTasks = tasks.count { it.worldId == world.id && it.status != "done" }
        val accent = parseColorHexOrDefault(world.colorHex)

        AetherGlassCard(
          modifier = Modifier.fillMaxWidth(),
          highlighted = true,
          contentPadding = 14.dp
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Box(
                modifier = Modifier
                  .size(14.dp)
                  .background(accent, CircleShape)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = world.name,
                style = MaterialTheme.typography.titleLarge,
                color = SilverPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
            IconButton(
              onClick = { selectedWorldId = null },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close planet inspector",
                tint = SilverSecondary,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          Text(
            text = "${world.category} • $worldProjects projects • $worldTasks open tasks • ${world.connectedWorldIds.size} links",
            style = MaterialTheme.typography.labelSmall,
            color = accent
          )

          if (world.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = world.description,
              style = MaterialTheme.typography.bodyMedium,
              color = SilverSecondary,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
          }

          Spacer(modifier = Modifier.height(12.dp))
          Button(
            onClick = { onOpenWorld(world) },
            colors = ButtonDefaults.buttonColors(
              containerColor = ElectricCyan,
              contentColor = ObsidianVoid
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("open_selected_world_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.OpenInNew,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Enter World Workspace", fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }

    // Create World FAB
    if (!showOpeningOverlay) {
      FloatingActionButton(
        onClick = onCreateWorldClick,
        containerColor = ElectricCyan,
        contentColor = ObsidianVoid,
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(16.dp)
          .testTag("fab_create_world")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Create World")
      }
    }

    // Non-blocking Cinematic Opening Reveal with ENTER AETHER CTA
    AnimatedVisibility(
      visible = showOpeningOverlay,
      enter = fadeIn(tween(300)),
      exit = fadeOut(tween(450)),
      modifier = Modifier.fillMaxSize()
    ) {
      UniverseOpeningOverlay(onEnter = onDismissOpening)
    }
  }
}

@Composable
private fun UniverseOpeningOverlay(
  onEnter: () -> Unit,
) {
  val transition = rememberInfiniteTransition(label = "openingPulse")
  val pulse by transition.animateFloat(
    initialValue = 0.7f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.radialGradient(
          colors = listOf(
            MidnightBlack.copy(alpha = 0.72f),
            ObsidianVoid.copy(alpha = 0.94f)
          )
        )
      ),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(32.dp)
    ) {
      AetherBrandEmblem(size = 112.dp, pulseAlpha = pulse)
      Spacer(modifier = Modifier.height(22.dp))
      Text(
        text = "AETHER X",
        style = MaterialTheme.typography.displayLarge,
        color = SilverPrimary,
        letterSpacing = 6.sp
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "BEYOND THE INTERFACE.",
        style = MaterialTheme.typography.labelSmall,
        color = ElectricCyan,
        letterSpacing = 2.6.sp
      )
      Spacer(modifier = Modifier.height(14.dp))
      Text(
        text = "Orbit Digital Worlds. Command Five AI Agents.",
        style = MaterialTheme.typography.bodyLarge,
        color = SilverSecondary
      )
      Spacer(modifier = Modifier.height(28.dp))
      Button(
        onClick = onEnter,
        colors = ButtonDefaults.buttonColors(
          containerColor = ElectricCyan,
          contentColor = ObsidianVoid
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .height(52.dp)
          .testTag("enter_aether_button")
      ) {
        Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "ENTER AETHER",
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
          letterSpacing = 1.6.sp
        )
      }
    }
  }
}

@Composable
private fun HudIconPill(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  contentDescription: String,
  tag: String,
  onClick: () -> Unit,
) {
  Surface(
    modifier = Modifier
      .size(44.dp)
      .clip(CircleShape)
      .clickable(onClick = onClick)
      .testTag(tag),
    shape = CircleShape,
    color = MidnightBlack.copy(alpha = 0.82f),
    border = BorderStroke(1.dp, GlassBorder)
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = SilverPrimary,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}

@Composable
private fun Universe3DCanvas(
  worlds: List<World>,
  stars: List<StarParticle>,
  yawRad: Float,
  pitchRad: Float,
  zoom: Float,
  panX: Float,
  panY: Float,
  timeSec: Float,
  selectedWorldId: String?,
  textMeasurer: TextMeasurer,
  onSizeMeasured: (IntSize) -> Unit,
  onTransform: (panDelta: Offset, zoomChange: Float, isSingleFinger: Boolean) -> Unit,
  onTapCanvas: (Offset) -> Unit,
  modifier: Modifier = Modifier,
) {
  Canvas(
    modifier = modifier
      .onSizeChanged(onSizeMeasured)
      .pointerInput(Unit) {
        detectTapGestures { offset -> onTapCanvas(offset) }
      }
      .pointerInput(Unit) {
        detectTransformGestures { _, pan, zoomChange, _ ->
          val isSingleFingerOrbit = kotlin.math.abs(zoomChange - 1f) < 0.01f
          onTransform(pan, zoomChange, isSingleFingerOrbit)
        }
      }
  ) {
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return@Canvas

    // Atmospheric deep space nebula backdrop
    drawRect(
      brush = Brush.radialGradient(
        colors = listOf(
          Color(0xFF0A192F),
          MidnightBlack,
          ObsidianVoid
        ),
        center = Offset(w * 0.5f + panX * 0.25f, h * 0.5f + panY * 0.25f),
        radius = maxOf(w, h) * 0.82f
      )
    )

    // 1. 3D Starfield & cyan neural dust
    for (star in stars) {
      val proj = UniverseMath.projectPoint3D(
        point = star.position,
        yawRad = yawRad * 0.6f + timeSec * 0.015f,
        pitchRad = pitchRad * 0.6f,
        zoom = zoom,
        panX = panX,
        panY = panY,
        viewportWidth = w,
        viewportHeight = h
      ) ?: continue

      if (proj.screenX < -20f || proj.screenX > w + 20f || proj.screenY < -20f || proj.screenY > h + 20f) {
        continue
      }
      val radius = (star.baseRadius * proj.scale * 0.85f).coerceIn(0.8f, 4.2f)
      val color = if (star.isCyanNode) ElectricCyan.copy(alpha = star.alpha * 0.75f)
      else SilverPrimary.copy(alpha = star.alpha * 0.55f)
      drawCircle(color = color, radius = radius, center = Offset(proj.screenX, proj.screenY))
    }

    // 2. Central AETHER CORE projected point
    val centerProj = UniverseMath.projectPoint3D(
      point = Vec3(0f, 0f, 0f),
      yawRad = yawRad,
      pitchRad = pitchRad,
      zoom = zoom,
      panX = panX,
      panY = panY,
      viewportWidth = w,
      viewportHeight = h
    )

    // 3. Project all Worlds
    val projectedPlanets = worlds.mapIndexedNotNull { index, world ->
      val pos3D = UniverseMath.computeWorldPosition(
        orbitRadius = world.orbitRadius.toFloat(),
        orbitAngle = world.orbitAngle.toFloat(),
        timeSec = timeSec,
        index = index
      )
      val proj = UniverseMath.projectPoint3D(
        point = pos3D,
        yawRad = yawRad,
        pitchRad = pitchRad,
        zoom = zoom,
        panX = panX,
        panY = panY,
        viewportWidth = w,
        viewportHeight = h
      ) ?: return@mapIndexedNotNull null

      val radius = (18f * world.planetScale.toFloat() * proj.scale).coerceIn(11f, 52f)
      ProjectedPlanet(
        world = world,
        projected = proj,
        screenRadius = radius,
        color = parseColorHexOrDefault(world.colorHex)
      )
    }
    val planetById = projectedPlanets.associateBy { it.world.id }

    // 4. Orbital rings & glowing cyan neural links
    if (centerProj != null) {
      val coreOffset = Offset(centerProj.screenX, centerProj.screenY)
      projectedPlanets.forEach { planet ->
        // Radial tether to Aether Core
        drawLine(
          color = ElectricCyan.copy(alpha = 0.16f),
          start = coreOffset,
          end = Offset(planet.projected.screenX, planet.projected.screenY),
          strokeWidth = 1.2f,
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 10f), timeSec * 18f)
        )
      }
    }

    // Inter-world connections
    projectedPlanets.forEach { src ->
      src.world.connectedWorldIds.forEach { targetId ->
        val dst = planetById[targetId] ?: return@forEach
        val isHighlighted = selectedWorldId == src.world.id || selectedWorldId == dst.world.id
        drawLine(
          brush = Brush.linearGradient(
            colors = listOf(
              src.color.copy(alpha = if (isHighlighted) 0.9f else 0.45f),
              ElectricCyan.copy(alpha = if (isHighlighted) 0.95f else 0.5f),
              dst.color.copy(alpha = if (isHighlighted) 0.9f else 0.45f),
            ),
            start = Offset(src.projected.screenX, src.projected.screenY),
            end = Offset(dst.projected.screenX, dst.projected.screenY)
          ),
          start = Offset(src.projected.screenX, src.projected.screenY),
          end = Offset(dst.projected.screenX, dst.projected.screenY),
          strokeWidth = if (isHighlighted) 3.4f else 1.8f,
          cap = StrokeCap.Round
        )
      }
    }

    // 5. Depth-sorted rendering (far to near) including Aether Core
    val coreDepth = centerProj?.depth ?: Float.MAX_VALUE
    var coreDrawn = false
    val sortedPlanets = projectedPlanets.sortedByDescending { it.projected.depth }

    fun drawAetherCore() {
      val cp = centerProj ?: return
      val coreCenter = Offset(cp.screenX, cp.screenY)
      val coreRadius = (24f * cp.scale).coerceIn(14f, 54f)
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            IceCyan.copy(alpha = 0.95f),
            ElectricCyan.copy(alpha = 0.55f),
            SoftCyanGlow.copy(alpha = 0.14f),
            Color.Transparent
          ),
          center = coreCenter,
          radius = coreRadius * 2.6f
        ),
        radius = coreRadius * 2.6f,
        center = coreCenter
      )
      drawCircle(
        color = ElectricCyan.copy(alpha = 0.45f),
        radius = coreRadius * 1.45f,
        center = coreCenter,
        style = Stroke(width = 1.6f)
      )
      drawCircle(
        color = IceCyan,
        radius = coreRadius * 0.58f,
        center = coreCenter
      )
    }

    for (planet in sortedPlanets) {
      if (!coreDrawn && planet.projected.depth < coreDepth) {
        drawAetherCore()
        coreDrawn = true
      }

      val pCenter = Offset(planet.projected.screenX, planet.projected.screenY)
      val r = planet.screenRadius
      val isSelected = planet.world.id == selectedWorldId

      // Atmospheric halo
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            planet.color.copy(alpha = if (isSelected) 0.52f else 0.28f),
            Color.Transparent
          ),
          center = pCenter,
          radius = r * 2.2f
        ),
        radius = r * 2.2f,
        center = pCenter
      )

      // Planetary sphere shaded from top-left cyan rim to obsidian shadow
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            IceCyan.copy(alpha = 0.92f),
            planet.color,
            MidnightBlack.copy(alpha = 0.96f)
          ),
          center = Offset(pCenter.x - r * 0.32f, pCenter.y - r * 0.32f),
          radius = r * 1.45f
        ),
        radius = r,
        center = pCenter
      )

      // Orbital planetary ring
      drawOval(
        color = planet.color.copy(alpha = if (isSelected) 0.85f else 0.4f),
        topLeft = Offset(pCenter.x - r * 1.55f, pCenter.y - r * 0.38f),
        size = androidx.compose.ui.geometry.Size(r * 3.1f, r * 0.76f),
        style = Stroke(width = if (isSelected) 2.2f else 1.2f)
      )

      if (isSelected) {
        drawCircle(
          color = ElectricCyan,
          radius = r * 1.32f,
          center = pCenter,
          style = Stroke(width = 2.4f)
        )
      }

      // Label below planet
      val labelResult = textMeasurer.measure(
        text = planet.world.name,
        style = TextStyle(
          fontFamily = SpaceGroteskFamily,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          color = SilverPrimary
        )
      )
      drawText(
        textLayoutResult = labelResult,
        topLeft = Offset(
          x = pCenter.x - labelResult.size.width / 2f,
          y = pCenter.y + r + 8f
        )
      )
    }

    if (!coreDrawn) {
      drawAetherCore()
    }
  }
}

@Composable
private fun Universe2DFallbackCanvas(
  worlds: List<World>,
  selectedWorldId: String?,
  stars: List<StarParticle>,
  textMeasurer: TextMeasurer,
  onSelectWorld: (World?) -> Unit,
  modifier: Modifier = Modifier,
) {
  var sizeState by remember { mutableStateOf(IntSize.Zero) }

  Canvas(
    modifier = modifier
      .onSizeChanged { sizeState = it }
      .pointerInput(worlds) {
        detectTapGestures { tap ->
          val w = sizeState.width.toFloat()
          val h = sizeState.height.toFloat()
          if (w <= 0f || h <= 0f) return@detectTapGestures
          val cx = w / 2f
          val cy = h / 2f
          val maxR = minOf(w, h) * 0.36f
          val hit = worlds.mapIndexed { i, world ->
            val angle = (i.toFloat() / maxOf(1, worlds.size)) * (2f * PI.toFloat()) - PI.toFloat() / 2f
            val ringR = maxR * (0.45f + (i % 3) * 0.25f)
            val px = cx + ringR * cos(angle)
            val py = cy + ringR * sin(angle)
            world to UniverseMath.distance2D(tap.x, tap.y, px, py)
          }.minByOrNull { it.second }
          onSelectWorld(if (hit != null && hit.second <= 72f) hit.first else null)
        }
      }
  ) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    val maxR = minOf(w, h) * 0.36f

    drawRect(
      brush = Brush.radialGradient(
        colors = listOf(Color(0xFF0B192C), ObsidianVoid),
        center = Offset(cx, cy),
        radius = maxOf(w, h) * 0.7f
      )
    )

    stars.take(50).forEachIndexed { idx, star ->
      val sx = ((idx * 173) % w.toInt()).toFloat()
      val sy = ((idx * 311) % h.toInt()).toFloat()
      drawCircle(SilverSecondary.copy(alpha = star.alpha * 0.35f), 1.5f, Offset(sx, sy))
    }

    // Concentric blueprint rings
    listOf(0.45f, 0.7f, 0.95f).forEach { factor ->
      drawCircle(
        color = ElectricCyan.copy(alpha = 0.14f),
        radius = maxR * factor,
        center = Offset(cx, cy),
        style = Stroke(width = 1.2f)
      )
    }

    // Center core
    drawCircle(ElectricCyan, radius = 16f, center = Offset(cx, cy))

    val positions = worlds.mapIndexed { i, world ->
      val angle = (i.toFloat() / maxOf(1, worlds.size)) * (2f * PI.toFloat()) - PI.toFloat() / 2f
      val ringR = maxR * (0.45f + (i % 3) * 0.25f)
      world to Offset(cx + ringR * cos(angle), cy + ringR * sin(angle))
    }.toMap()

    // Connections
    worlds.forEach { src ->
      val p1 = positions[src] ?: return@forEach
      src.connectedWorldIds.forEach { tid ->
        val dst = worlds.firstOrNull { it.id == tid } ?: return@forEach
        val p2 = positions[dst] ?: return@forEach
        drawLine(ElectricCyan.copy(alpha = 0.55f), p1, p2, strokeWidth = 2f)
      }
    }

    // Nodes
    positions.forEach { (world, pos) ->
      val accent = parseColorHexOrDefault(world.colorHex)
      val selected = world.id == selectedWorldId
      drawCircle(accent.copy(alpha = 0.25f), radius = 34f, center = pos)
      drawCircle(accent, radius = 18f, center = pos)
      if (selected) {
        drawCircle(IceCyan, radius = 25f, center = pos, style = Stroke(2.5f))
      }
      val label = textMeasurer.measure(
        world.name,
        TextStyle(fontFamily = SpaceGroteskFamily, fontSize = 11.sp, color = SilverPrimary)
      )
      drawText(label, topLeft = Offset(pos.x - label.size.width / 2f, pos.y + 24f))
    }
  }
}

private fun findTappedWorld(
  worlds: List<World>,
  tapOffset: Offset,
  yawRad: Float,
  pitchRad: Float,
  zoom: Float,
  panX: Float,
  panY: Float,
  timeSec: Float,
  width: Float,
  height: Float,
): World? {
  var bestWorld: World? = null
  var bestDepth = Float.MAX_VALUE
  worlds.forEachIndexed { index, world ->
    val pos3D = UniverseMath.computeWorldPosition(
      orbitRadius = world.orbitRadius.toFloat(),
      orbitAngle = world.orbitAngle.toFloat(),
      timeSec = timeSec,
      index = index
    )
    val proj = UniverseMath.projectPoint3D(
      point = pos3D,
      yawRad = yawRad,
      pitchRad = pitchRad,
      zoom = zoom,
      panX = panX,
      panY = panY,
      viewportWidth = width,
      viewportHeight = height
    ) ?: return@forEachIndexed
    val r = (18f * world.planetScale.toFloat() * proj.scale).coerceIn(14f, 56f)
    val dist = UniverseMath.distance2D(tapOffset.x, tapOffset.y, proj.screenX, proj.screenY)
    if (dist <= maxOf(44f, r * 1.65f) && proj.depth < bestDepth) {
      bestDepth = proj.depth
      bestWorld = world
    }
  }
  return bestWorld
}
