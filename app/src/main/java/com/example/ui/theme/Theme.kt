package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val AetherDarkColorScheme = darkColorScheme(
  primary = ElectricCyan,
  onPrimary = ObsidianVoid,
  primaryContainer = DeepCyanContainer,
  onPrimaryContainer = IceCyan,
  secondary = SoftCyanGlow,
  onSecondary = ObsidianVoid,
  secondaryContainer = ElevatedGlassSurface,
  onSecondaryContainer = SilverPrimary,
  tertiary = SilverPrimary,
  onTertiary = ObsidianVoid,
  background = ObsidianVoid,
  onBackground = SilverPrimary,
  surface = MidnightBlack,
  onSurface = SilverPrimary,
  surfaceVariant = DeepSpaceSurface,
  onSurfaceVariant = SilverSecondary,
  outline = GlassBorder,
  outlineVariant = CyanGlassBorder,
  error = StatusError,
  onError = ObsidianVoid,
)

val AetherShapes = Shapes(
  small = RoundedCornerShape(10.dp),
  medium = RoundedCornerShape(16.dp),
  large = RoundedCornerShape(22.dp),
  extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun AetherXTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = AetherDarkColorScheme,
    typography = Typography,
    shapes = AetherShapes,
    content = content,
  )
}

@Composable
fun AetherGlassCard(
  modifier: Modifier = Modifier,
  highlighted: Boolean = false,
  cornerRadius: Dp = 18.dp,
  contentPadding: Dp = 16.dp,
  content: @Composable ColumnScope.() -> Unit,
) {
  val borderColor = if (highlighted) CyanGlassBorder else GlassBorder
  val gradientBrush = Brush.linearGradient(
    colors = listOf(
      ElevatedGlassSurface.copy(alpha = 0.88f),
      DeepSpaceSurface.copy(alpha = 0.78f),
    )
  )
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(cornerRadius),
    color = Color.Transparent,
    border = BorderStroke(1.dp, borderColor),
    tonalElevation = if (highlighted) 4.dp else 1.dp,
  ) {
    Box(
      modifier = Modifier
        .background(gradientBrush)
        .padding(contentPadding)
    ) {
      Column(content = content)
    }
  }
}

@Composable
fun AetherBrandEmblem(
  modifier: Modifier = Modifier,
  size: Dp = 44.dp,
  pulseAlpha: Float = 1f,
) {
  Canvas(modifier = modifier.size(size)) {
    val w = this.size.width
    val h = this.size.height
    val cx = w / 2f
    val cy = h / 2f
    val r = minOf(w, h) * 0.42f

    // Subtle radial halo
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          ElectricCyan.copy(alpha = 0.24f * pulseAlpha),
          Color.Transparent
        ),
        center = Offset(cx, cy),
        radius = r * 1.45f
      ),
      radius = r * 1.45f,
      center = Offset(cx, cy)
    )

    // Outer orbital ring
    drawCircle(
      color = SilverSecondary.copy(alpha = 0.55f),
      radius = r,
      center = Offset(cx, cy),
      style = Stroke(width = w * 0.035f)
    )

    // Inner cyan arc ring
    drawArc(
      color = ElectricCyan.copy(alpha = 0.9f * pulseAlpha),
      startAngle = -35f,
      sweepAngle = 220f,
      useCenter = false,
      topLeft = Offset(cx - r * 0.76f, cy - r * 0.76f),
      size = androidx.compose.ui.geometry.Size(r * 1.52f, r * 1.52f),
      style = Stroke(width = w * 0.042f, cap = StrokeCap.Round)
    )

    // Geometric X core
    val arm = r * 0.42f
    val xPath = Path().apply {
      moveTo(cx - arm, cy - arm)
      lineTo(cx + arm, cy + arm)
      moveTo(cx + arm, cy - arm)
      lineTo(cx - arm, cy + arm)
    }
    drawPath(
      path = xPath,
      color = IceCyan,
      style = Stroke(width = w * 0.068f, cap = StrokeCap.Round)
    )

    // Core singularity dot
    drawCircle(
      color = ElectricCyan,
      radius = w * 0.055f,
      center = Offset(cx, cy)
    )
  }
}
