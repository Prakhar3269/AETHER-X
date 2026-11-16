package com.example.data.ai

import android.util.Log
import com.example.data.model.AgentRole
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

data class OrchestrationPlan(
  val selectedAgents: List<AgentRole>,
  val scopedPrompts: Map<String, String>,
  val rationale: String,
)

data class OrchestrationOutcome(
  val plan: OrchestrationPlan,
  val agentOutputs: Map<String, String>,
  val consolidatedResult: String,
)

class AetherAiService {

  private val routingSchema = Schema.obj(
    properties = mapOf(
      "rationale" to Schema.string(description = "Brief explanation of why these agents were selected"),
      "assignments" to Schema.array(
        items = Schema.obj(
          properties = mapOf(
            "agentId" to Schema.enumeration(
              values = listOf("architect", "researcher", "developer", "strategist", "creator"),
              description = "Agent identifier"
            ),
            "scopedTask" to Schema.string(description = "Tailored sub-task prompt for this agent")
          )
        ),
        description = "2 to 4 relevant agent assignments"
      )
    )
  )

  private val routerModel by lazy {
    Firebase.ai.generativeModel(
      modelName = DEFAULT_MODEL,
      generationConfig = generationConfig {
        responseMimeType = "application/json"
        responseSchema = routingSchema
      },
      systemInstruction = content {
        text(
          "You are the AETHER X Orchestrator. Analyze the user's request and route appropriately scoped sub-tasks to 2 to 4 relevant specialized agents among: architect, researcher, developer, strategist, creator."
        )
      }
    )
  }

  private fun agentModel(role: AgentRole) = Firebase.ai.generativeModel(
    modelName = DEFAULT_MODEL,
    systemInstruction = content { text(role.systemPrompt) }
  )

  private val synthesizerModel by lazy {
    Firebase.ai.generativeModel(
      modelName = DEFAULT_MODEL,
      systemInstruction = content {
        text(
          "You are the AETHER X Council Synthesizer. Merge the outputs from specialized AI agents into one cohesive, executive summary with clear next steps. Never fabricate actions that were not executed."
        )
      }
    )
  }

  fun streamAgentResponse(
    role: AgentRole,
    prompt: String,
    worldContext: String = "",
  ): Flow<String> = flow {
    val fullPrompt = buildString {
      if (worldContext.isNotBlank()) {
        appendLine("World Workspace Context:")
        appendLine(worldContext)
        appendLine()
      }
      appendLine("User Request:")
      append(prompt)
    }

    val model = agentModel(role)
    val accumulated = StringBuilder()
    withTimeout(AI_TIMEOUT_MS) {
      model.generateContentStream(fullPrompt).collect { chunk ->
        val delta = chunk.text.orEmpty()
        if (delta.isNotEmpty()) {
          accumulated.append(delta)
          emit(accumulated.toString())
        }
      }
    }
    if (accumulated.isBlank()) {
      throw IllegalStateException("${role.displayName} returned an empty response.")
    }
  }

  suspend fun runMultiAgentOrchestration(
    taskPrompt: String,
    worldContext: String = "",
    preferredAgents: List<AgentRole> = emptyList(),
  ): OrchestrationOutcome {
    val plan = if (preferredAgents.size >= 2) {
      OrchestrationPlan(
        selectedAgents = preferredAgents,
        scopedPrompts = preferredAgents.associate { role ->
          role.id to "From your perspective as ${role.displayName} (${role.specialty}), analyze and address this task:\n$taskPrompt"
        },
        rationale = "User-selected multi-agent council (${preferredAgents.joinToString { it.shortTitle }})."
      )
    } else {
      determineRoutingPlan(taskPrompt, worldContext)
    }

    val outputs = coroutineScope {
      plan.selectedAgents.map { role ->
        async {
          val scoped = plan.scopedPrompts[role.id] ?: taskPrompt
          val promptWithContext = buildString {
            if (worldContext.isNotBlank()) {
              appendLine("World Context: $worldContext")
              appendLine()
            }
            append(scoped)
          }
          val response = withTimeout(AI_TIMEOUT_MS) {
            agentModel(role).generateContent(promptWithContext)
          }
          role.id to (response.text?.trim().orEmpty().ifBlank { "No output generated." })
        }
      }.awaitAll().toMap()
    }

    val synthesisInput = buildString {
      appendLine("Original User Task: $taskPrompt")
      if (worldContext.isNotBlank()) {
        appendLine("World Context: $worldContext")
      }
      appendLine()
      appendLine("Agent Contributions:")
      plan.selectedAgents.forEach { role ->
        appendLine("### ${role.displayName}")
        appendLine(outputs[role.id].orEmpty())
        appendLine()
      }
      appendLine("Produce a clear, consolidated AETHER X summary and recommended next steps for user approval.")
    }

    val consolidated = withTimeout(AI_TIMEOUT_MS) {
      synthesizerModel.generateContent(synthesisInput).text?.trim().orEmpty()
    }.ifBlank {
      outputs.entries.joinToString("\n\n") { "${AgentRole.fromId(it.key).displayName}:\n${it.value}" }
    }

    return OrchestrationOutcome(
      plan = plan,
      agentOutputs = outputs,
      consolidatedResult = consolidated,
    )
  }

  private suspend fun determineRoutingPlan(
    taskPrompt: String,
    worldContext: String,
  ): OrchestrationPlan {
    return try {
      val prompt = buildString {
        if (worldContext.isNotBlank()) appendLine("Context: $worldContext")
        append("Task: $taskPrompt")
      }
      val response = withTimeout(AI_TIMEOUT_MS) {
        routerModel.generateContent(prompt)
      }
      val jsonText = response.text ?: throw IllegalStateException("Empty routing response")
      val json = JSONObject(jsonText)
      val rationale = json.optString("rationale", "Routed to relevant specialists.")
      val assignments = json.optJSONArray("assignments")
      val roles = mutableListOf<AgentRole>()
      val prompts = mutableMapOf<String, String>()
      if (assignments != null) {
        for (i in 0 until assignments.length()) {
          val obj = assignments.optJSONObject(i) ?: continue
          val id = obj.optString("agentId")
          val scoped = obj.optString("scopedTask")
          val role = AgentRole.fromId(id)
          if (!roles.contains(role)) {
            roles.add(role)
            if (scoped.isNotBlank()) prompts[role.id] = scoped
          }
        }
      }
      if (roles.isEmpty()) {
        OrchestrationPlan(
          selectedAgents = listOf(AgentRole.ARCHITECT, AgentRole.STRATEGIST),
          scopedPrompts = emptyMap(),
          rationale = "Default architectural and strategic routing."
        )
      } else {
        OrchestrationPlan(roles, prompts, rationale)
      }
    } catch (e: TimeoutCancellationException) {
      throw e
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.w(TAG, "Router schema fallback triggered: ${e.message}")
      OrchestrationPlan(
        selectedAgents = listOf(AgentRole.ARCHITECT, AgentRole.DEVELOPER, AgentRole.STRATEGIST),
        scopedPrompts = emptyMap(),
        rationale = "Fallback multi-agent routing."
      )
    }
  }

  private companion object {
    const val TAG = "AetherAiService"
    const val DEFAULT_MODEL = "gemini-3.8-flash"
    const val AI_TIMEOUT_MS = 60_000L
  }
}
