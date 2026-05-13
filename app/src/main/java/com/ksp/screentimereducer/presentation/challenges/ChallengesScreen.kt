package com.ksp.screentimereducer.presentation.challenges

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ksp.screentimereducer.domain.model.Challenge
import com.ksp.screentimereducer.presentation.components.GradientCard
import com.ksp.screentimereducer.ui.theme.AppGradients
import com.ksp.screentimereducer.ui.theme.SoftPurpleLight

@Composable
fun ChallengesScreen() {
    val vm: ChallengesViewModel = hiltViewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "Challenges",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            item {
                XpHero(xp = state.totalXp, streak = state.streak)
            }
            items(state.challenges, key = { it.id }) { c ->
                ChallengeRow(
                    challenge = c,
                    onStart = { vm.start(c.id) },
                    onComplete = { vm.complete(c.id) },
                    onAbandon = { vm.abandon(c.id) },
                )
            }
        }
    }
}

@Composable
private fun XpHero(xp: Int, streak: Int) {
    GradientCard(brush = AppGradients.Mint, modifier = Modifier.fillMaxWidth(), contentPadding = 20.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("LEVEL", color = SoftPurpleLight, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${1 + xp / 500} ✦",
                    color = Color.White,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$xp XP", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${streak}d streak", color = SoftPurpleLight, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ChallengeRow(
    challenge: Challenge,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onAbandon: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(challenge.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        challenge.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "+${challenge.xpReward} XP",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (challenge.isActive) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { challenge.progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onComplete, modifier = Modifier.weight(1f)) { Text("Mark done") }
                    OutlinedButton(onClick = onAbandon, modifier = Modifier.weight(1f)) { Text("Give up") }
                }
            } else {
                Spacer(Modifier.height(12.dp))
                if (challenge.streak > 0) {
                    Text(
                        "Best streak: ${challenge.streak}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(46.dp)) {
                    Text("Start challenge", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
