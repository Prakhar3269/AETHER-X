package com.example.ui.universe

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UniverseMathTest {

  @Test
  fun projectPoint3D_originProjectsToViewportCenter() {
    val projected = UniverseMath.projectPoint3D(
      point = Vec3(0f, 0f, 0f),
      yawRad = 0.5f,
      pitchRad = -0.3f,
      zoom = 1.0f,
      panX = 0f,
      panY = 0f,
      viewportWidth = 1080f,
      viewportHeight = 2200f
    )
    assertNotNull(projected)
    assertEquals(540f, projected!!.screenX, 0.01f)
    assertEquals(1100f, projected.screenY, 0.01f)
    assertTrue(projected.scale > 0f)
  }

  @Test
  fun projectPoint3D_pointBehindCameraReturnsNull() {
    val projected = UniverseMath.projectPoint3D(
      point = Vec3(0f, 0f, -600f),
      yawRad = 0f,
      pitchRad = 0f,
      zoom = 1.0f,
      panX = 0f,
      panY = 0f,
      viewportWidth = 1080f,
      viewportHeight = 2200f,
      cameraDistance = 440f
    )
    assertNull(projected)
  }

  @Test
  fun computeWorldPosition_maintainsApproximateOrbitRadius() {
    val pos = UniverseMath.computeWorldPosition(
      orbitRadius = 120f,
      orbitAngle = 0.4f,
      timeSec = 10f,
      index = 0
    )
    assertTrue(pos.x * pos.x + pos.z * pos.z > 100f)
  }
}
