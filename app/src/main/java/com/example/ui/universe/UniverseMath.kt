package com.example.ui.universe

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vec3(val x: Float, val y: Float, val z: Float)

data class ProjectedPoint(
  val screenX: Float,
  val screenY: Float,
  val depth: Float,
  val scale: Float,
)

object UniverseMath {

  /**
   * Rotates a 3D world point around Y (yaw) and X (pitch) and projects it using
   * perspective division onto a 2D viewport of size (width, height).
   */
  fun projectPoint3D(
    point: Vec3,
    yawRad: Float,
    pitchRad: Float,
    zoom: Float,
    panX: Float,
    panY: Float,
    viewportWidth: Float,
    viewportHeight: Float,
    cameraDistance: Float = 440f,
  ): ProjectedPoint? {
    val cosY = cos(yawRad)
    val sinY = sin(yawRad)
    val cosP = cos(pitchRad)
    val sinP = sin(pitchRad)

    // Rotate around Y axis (yaw)
    val rx = point.x * cosY - point.z * sinY
    val rz1 = point.x * sinY + point.z * cosY

    // Rotate around X axis (pitch)
    val ry = point.y * cosP - rz1 * sinP
    val rz = point.y * sinP + rz1 * cosP

    val camZ = cameraDistance / zoom.coerceIn(0.45f, 3.2f)
    val denom = camZ + rz
    if (denom <= 25f) return null

    val focal = minOf(viewportWidth, viewportHeight) * 0.85f
    val perspective = focal / denom

    val sx = viewportWidth * 0.5f + panX + rx * perspective
    val sy = viewportHeight * 0.5f + panY + ry * perspective
    return ProjectedPoint(
      screenX = sx,
      screenY = sy,
      depth = denom,
      scale = perspective
    )
  }

  fun computeWorldPosition(
    orbitRadius: Float,
    orbitAngle: Float,
    timeSec: Float,
    index: Int,
  ): Vec3 {
    val speed = 0.09f / (1f + index * 0.22f)
    val theta = orbitAngle + timeSec * speed
    val inclination = ((index % 5) - 2) * 0.16f
    val x = orbitRadius * cos(theta)
    val z = orbitRadius * sin(theta)
    val y = orbitRadius * sin(theta * 0.7f + index) * inclination
    return Vec3(x, y, z)
  }

  fun distance2D(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val dx = x1 - x2
    val dy = y1 - y2
    return sqrt(dx * dx + dy * dy)
  }
}
