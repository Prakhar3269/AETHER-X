package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AetherRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun createAndObserveWorld_authenticatedOwner_succeeds() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repo = AetherRepository(firestore, auth)

    val worldId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.createWorld(
        name = "Obsidian Nexus",
        description = "Core 3D workspace",
        colorHex = "#22D3EE",
        category = "Architecture"
      ).getOrThrow()
    }
    assertTrue(worldId.isNotBlank())

    val worlds = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeWorlds().first { list -> list.any { it.id == worldId } }
    }
    assertTrue(worlds.any { it.id == worldId && it.name == "Obsidian Nexus" })
  }

  @Test
  fun getWorldById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val aliceRepo = AetherRepository(firestore, auth)
    val worldId = withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.createWorld(
        name = "Alice Private World",
        description = "Isolated",
        colorHex = "#38BDF8",
        category = "Research"
      ).getOrThrow()
    }

    signInTestUser(BOB_EMAIL)
    val bobRepo = AetherRepository(firestore, auth)
    val result = withTimeout(DEFAULT_TIMEOUT_MS) { bobRepo.getWorldById(worldId) }
    assertTrue(result.isFailure)
    val fsError = result.exceptionOrNull()?.unwrapFirestoreException()
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError?.code)
  }

  @Test
  fun createProjectAndTask_validParentWorld_succeeds() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repo = AetherRepository(firestore, auth)
    val worldId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.createWorld("Aether Lab", "Lab", "#22D3EE", "Product").getOrThrow()
    }

    val projectId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.createProject(worldId, "3D Engine", "Canvas 3D renderer").getOrThrow()
    }
    assertTrue(projectId.isNotBlank())

    val taskId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.createTask(worldId, projectId, "Optimize starfield instancing", priority = "high").getOrThrow()
    }
    assertTrue(taskId.isNotBlank())

    val projects = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeProjects().first { it.any { p -> p.id == projectId } }
    }
    assertEquals("3D Engine", projects.first { it.id == projectId }.title)
  }

  @Test
  fun observeWorlds_unauthenticatedCaller_throwsIllegalStateOrPermissionDenied() = runBlocking {
    auth.signOut()
    val repo = AetherRepository(firestore, auth)

    try {
      withTimeout(FLOW_TIMEOUT_MS) { repo.observeWorlds().first() }
      fail("Expected unauthenticated observeWorlds to fail")
    } catch (e: Exception) {
      assertTrue(
        e is IllegalStateException ||
          e.unwrapFirestoreException()?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
      )
    }
  }

  private companion object {
    const val ALICE_EMAIL = "alice.aether@test.com"
    const val BOB_EMAIL = "bob.aether@test.com"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
