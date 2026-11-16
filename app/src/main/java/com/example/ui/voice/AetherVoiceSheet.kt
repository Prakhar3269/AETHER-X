package com.example.ui.voice

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.model.AgentRole
import com.example.data.model.Conversation
import com.example.ui.AiStreamUiState
import com.example.ui.theme.AetherGlassCard
import com.example.ui.theme.CyanGlassBorder
import com.example.ui.theme.DeepSpaceSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SilverPrimary
import com.example.ui.theme.SilverSecondary
import com.example.ui.theme.StatusError
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AetherVoiceSheet(
  voiceReplyState: AiStreamUiState,
  voiceHistory: List<Conversation>,
  autoSpeak: Boolean,
  onAskVoice: (prompt: String, role: AgentRole, onReadyToSpeak: (String) -> Unit) -> Unit,
  onDismiss: () -> Unit,
) {
  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var isListening by remember { mutableStateOf(false) }
  var selectedAgent by remember { mutableStateOf(AgentRole.ARCHITECT) }
  var textInput by remember { mutableStateOf("") }
  var voiceNotice by remember { mutableStateOf<String?>(null) }

  // TextToSpeech lifecycle
  var ttsReady by remember { mutableStateOf(false) }
  val tts = remember {
    var instance: TextToSpeech? = null
    instance = TextToSpeech(context) { status ->
      if (status == TextToSpeech.SUCCESS) {
        instance?.language = Locale.US
        ttsReady = true
      }
    }
    instance
  }

  val speechRecognizer = remember(context) {
    if (SpeechRecognizer.isRecognitionAvailable(context)) {
      SpeechRecognizer.createSpeechRecognizer(context)
    } else null
  }

  DisposableEffect(Unit) {
    onDispose {
      runCatching { speechRecognizer?.destroy() }
      runCatching {
        tts.stop()
        tts.shutdown()
      }
    }
  }

  fun speakText(text: String) {
    if (ttsReady && text.isNotBlank()) {
      tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "aether_voice_${System.currentTimeMillis()}")
    }
  }

  fun startListeningInternal() {
    val recognizer = speechRecognizer
    if (recognizer == null) {
      voiceNotice = "On-device speech service unavailable; use text input below."
      return
    }
    voiceNotice = null
    recognizer.setRecognitionListener(object : RecognitionListener {
      override fun onReadyForSpeech(params: Bundle?) { isListening = true }
      override fun onBeginningOfSpeech() {}
      override fun onRmsChanged(rmsdB: Float) {}
      override fun onBufferReceived(buffer: ByteArray?) {}
      override fun onEndOfSpeech() { isListening = false }
      override fun onError(error: Int) {
        isListening = false
        voiceNotice = "Listening ended (code $error). You can type or tap mic again."
      }
      override fun onResults(results: Bundle?) {
        isListening = false
        val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
        if (!spoken.isNullOrBlank()) {
          textInput = spoken
          onAskVoice(spoken, selectedAgent) { reply ->
            if (autoSpeak) speakText(reply)
          }
        }
      }
      override fun onPartialResults(partialResults: Bundle?) {
        val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
        if (!partial.isNullOrBlank()) textInput = partial
      }
      override fun onEvent(eventType: Int, params: Bundle?) {}
    })

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
      putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
      putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }
    recognizer.startListening(intent)
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { granted ->
    if (granted) {
      startListeningInternal()
    } else {
      voiceNotice = "Microphone permission declined. Text input remains ready."
    }
  }

  val pulse = rememberInfiniteTransition(label = "micPulse")
  val ringScale by pulse.animateFloat(
    initialValue = 1f,
    targetValue = if (isListening) 1.28f else 1.05f,
    animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
    label = "ringScale"
  )

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MidnightBlack,
    contentColor = SilverPrimary
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 20.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("AETHER VOICE", style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
          Text("Neural Audio Interface", style = MaterialTheme.typography.titleLarge, color = SilverPrimary)
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close Voice", tint = SilverSecondary)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Animated Microphone Button
      Box(contentAlignment = Alignment.Center) {
        Box(
          modifier = Modifier
            .size(92.dp)
            .scale(ringScale)
            .background(
              ElectricCyan.copy(alpha = if (isListening) 0.28f else 0.12f),
              CircleShape
            )
        )
        Surface(
          modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .clickable {
              if (isListening) {
                speechRecognizer?.stopListening()
                isListening = false
              } else {
                val hasPermission = ContextCompat.checkSelfPermission(
                  context,
                  Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) {
                  startListeningInternal()
                } else {
                  permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
              }
            }
            .testTag("voice_mic_button"),
          shape = CircleShape,
          color = if (isListening) ElectricCyan else DeepSpaceSurface,
          border = BorderStroke(1.5.dp, CyanGlassBorder)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
              contentDescription = if (isListening) "Stop listening" else "Start voice input",
              tint = if (isListening) ObsidianVoid else ElectricCyan,
              modifier = Modifier.size(30.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = if (isListening) "Listening..." else "Tap microphone or type below",
        style = MaterialTheme.typography.labelSmall,
        color = ElectricCyan
      )
      voiceNotice?.let {
        Spacer(modifier = Modifier.height(4.dp))
        Text(it, style = MaterialTheme.typography.bodyMedium, color = SilverSecondary)
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Agent choice
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        AgentRole.entries.take(4).forEach { role ->
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable { selectedAgent = role },
            color = if (selectedAgent == role) ElectricCyan else DeepSpaceSurface,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, GlassBorder)
          ) {
            Text(
              text = role.shortTitle,
              style = MaterialTheme.typography.labelSmall,
              color = if (selectedAgent == role) ObsidianVoid else SilverPrimary,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Text fallback input
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = textInput,
          onValueChange = { textInput = it },
          placeholder = { Text("Speak or type command...") },
          singleLine = true,
          modifier = Modifier
            .weight(1f)
            .testTag("voice_text_input")
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
          onClick = {
            if (textInput.isNotBlank()) {
              val query = textInput
              textInput = ""
              onAskVoice(query, selectedAgent) { reply ->
                if (autoSpeak) speakText(reply)
              }
            }
          },
          modifier = Modifier
            .size(48.dp)
            .background(ElectricCyan, CircleShape)
            .testTag("voice_send_button")
        ) {
          Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send voice prompt", tint = ObsidianVoid)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      when (voiceReplyState) {
        AiStreamUiState.Idle -> Unit
        AiStreamUiState.Loading -> CircularProgressIndicator(color = ElectricCyan, modifier = Modifier.size(22.dp))
        is AiStreamUiState.Streaming -> {
          AetherGlassCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
            Text(voiceReplyState.text, style = MaterialTheme.typography.bodyMedium, color = SilverPrimary)
          }
        }
        is AiStreamUiState.Success -> {
          AetherGlassCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(voiceReplyState.agent.displayName, style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
              IconButton(onClick = { speakText(voiceReplyState.text) }) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Speak response", tint = ElectricCyan)
              }
            }
            Text(voiceReplyState.text, style = MaterialTheme.typography.bodyMedium, color = SilverPrimary)
          }
        }
        is AiStreamUiState.Error -> {
          Text(voiceReplyState.message, style = MaterialTheme.typography.bodyMedium, color = StatusError)
        }
      }

      if (voiceHistory.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        Text("Voice History", style = MaterialTheme.typography.labelSmall, color = SilverSecondary, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(6.dp))
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(voiceHistory.take(8), key = { it.id }) { item ->
            AetherGlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 10.dp) {
              Text(item.prompt, style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
              Text(item.responseSummary, style = MaterialTheme.typography.bodyMedium, color = SilverSecondary, maxLines = 2)
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(14.dp))
    }
  }
}
