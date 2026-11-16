package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.aetherDataStore by preferencesDataStore(name = "aether_x_prefs")

enum class UniverseRenderQuality(val label: String) {
  HIGH("High 3D"),
  BALANCED("Balanced 3D"),
  TWO_D_FALLBACK("2D Constellation");

  companion object {
    fun fromName(value: String?): UniverseRenderQuality =
      entries.firstOrNull { it.name == value } ?: HIGH
  }
}

data class AetherUserSettings(
  val reducedMotion: Boolean = false,
  val renderQuality: UniverseRenderQuality = UniverseRenderQuality.HIGH,
  val voiceAutoSpeak: Boolean = true,
)

class AetherPreferences(context: Context) {
  private val dataStore = context.applicationContext.aetherDataStore

  val settingsFlow: Flow<AetherUserSettings> = dataStore.data.map { prefs ->
    AetherUserSettings(
      reducedMotion = prefs[KEY_REDUCED_MOTION] ?: false,
      renderQuality = UniverseRenderQuality.fromName(prefs[KEY_RENDER_QUALITY]),
      voiceAutoSpeak = prefs[KEY_VOICE_AUTO_SPEAK] ?: true,
    )
  }

  suspend fun setReducedMotion(enabled: Boolean) {
    dataStore.edit { it[KEY_REDUCED_MOTION] = enabled }
  }

  suspend fun setRenderQuality(quality: UniverseRenderQuality) {
    dataStore.edit { it[KEY_RENDER_QUALITY] = quality.name }
  }

  suspend fun setVoiceAutoSpeak(enabled: Boolean) {
    dataStore.edit { it[KEY_VOICE_AUTO_SPEAK] = enabled }
  }

  private companion object {
    val KEY_REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
    val KEY_RENDER_QUALITY = stringPreferencesKey("render_quality")
    val KEY_VOICE_AUTO_SPEAK = booleanPreferencesKey("voice_auto_speak")
  }
}
