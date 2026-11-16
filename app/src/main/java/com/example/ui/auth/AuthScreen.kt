package com.example.ui.auth

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.core.auth.attemptAutoSignIn
import com.example.core.auth.onGoogleSignInClicked
import com.example.ui.theme.AetherBrandEmblem
import com.example.ui.theme.AetherGlassCard
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SilverPrimary
import com.example.ui.theme.SilverSecondary
import com.example.ui.theme.StatusError

@Composable
fun AuthScreen(
  onAuthSuccess: () -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) {
    attemptAutoSignIn(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = onAuthSuccess,
      onUnauthenticated = { /* Interactive sign-in remains visible */ },
      scope = scope
    )
  }

  val infiniteTransition = rememberInfiniteTransition(label = "authPulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.65f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(2200, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(ObsidianVoid)
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_hero_universe),
      contentDescription = "AETHER X digital universe backdrop",
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .fillMaxSize()
        .alpha(0.36f)
    )
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              ObsidianVoid.copy(alpha = 0.72f),
              MidnightBlack.copy(alpha = 0.65f),
              ObsidianVoid.copy(alpha = 0.96f),
            )
          )
        )
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .systemBarsPadding()
        .padding(horizontal = 24.dp, vertical = 24.dp)
        .widthIn(max = 520.dp)
        .align(Alignment.Center),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Spacer(modifier = Modifier.height(12.dp))

      Column(
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        AetherBrandEmblem(size = 86.dp, pulseAlpha = pulseAlpha)
        Spacer(modifier = Modifier.height(18.dp))
        Text(
          text = "AETHER X",
          style = MaterialTheme.typography.displayLarge,
          color = SilverPrimary,
          letterSpacing = 5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "BEYOND THE INTERFACE.",
          style = MaterialTheme.typography.labelSmall,
          color = ElectricCyan,
          letterSpacing = 2.4.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = "Enter your living digital universe. Orbit active Worlds, orchestrate five specialized Gemini agents, and collaborate in real time.",
          style = MaterialTheme.typography.bodyLarge,
          color = SilverSecondary,
          textAlign = TextAlign.Center
        )
      }

      AetherGlassCard(
        modifier = Modifier.fillMaxWidth(),
        highlighted = true,
        contentPadding = 18.dp
      ) {
        AuthPillarRow(
          icon = Icons.Default.Public,
          title = "3D Infinity Engine",
          subtitle = "Explore your Worlds as interconnected celestial nodes"
        )
        Spacer(modifier = Modifier.height(12.dp))
        AuthPillarRow(
          icon = Icons.Default.AutoAwesome,
          title = "AI Civilization",
          subtitle = "Architect, Researcher, Developer, Strategist & Creator"
        )
        Spacer(modifier = Modifier.height(12.dp))
        AuthPillarRow(
          icon = Icons.Default.Groups,
          title = "Live Worlds & Nexus Market",
          subtitle = "Role-governed teams, voice interface & curated templates"
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (errorMessage != null) {
          Text(
            text = errorMessage!!,
            style = MaterialTheme.typography.bodyMedium,
            color = StatusError,
            modifier = Modifier.padding(bottom = 12.dp)
          )
        }

        Button(
          onClick = {
            errorMessage = null
            isLoading = true
            onGoogleSignInClicked(
              context = context,
              credentialManager = credentialManager,
              onAuthSuccess = {
                isLoading = false
                onAuthSuccess()
              },
              onAuthError = { msg ->
                isLoading = false
                errorMessage = msg
              },
              scope = scope,
              onAuthCancelled = {
                isLoading = false
              }
            )
          },
          enabled = !isLoading,
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("google_sign_in_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = ElectricCyan,
            contentColor = ObsidianVoid,
            disabledContainerColor = ElectricCyan.copy(alpha = 0.5f)
          )
        ) {
          if (isLoading) {
            CircularProgressIndicator(
              modifier = Modifier.size(22.dp),
              color = ObsidianVoid,
              strokeWidth = 2.5.dp
            )
          } else {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Login,
              contentDescription = null,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Sign in with Google",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun AuthPillarRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth()
  ) {
    Box(
      modifier = Modifier
        .size(38.dp)
        .background(ElectricCyan.copy(alpha = 0.14f), RoundedCornerShape(10.dp)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = ElectricCyan,
        modifier = Modifier.size(20.dp)
      )
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = SilverPrimary
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = SilverSecondary
      )
    }
  }
}
