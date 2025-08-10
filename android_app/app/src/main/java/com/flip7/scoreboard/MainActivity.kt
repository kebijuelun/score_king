package com.flip7.scoreboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flip7.scoreboard.ui.theme.Flip7Theme
import kotlinx.coroutines.launch

// Data model to mirror web app state
data class Player(
    val name: String,
    val rounds: MutableList<Int> = mutableListOf(),
) {
    val total: Int get() = rounds.sum()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Flip7Theme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ScoreboardScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreboardScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val players = remember { mutableStateOf(mutableListOf<Player>()) }
    val winThreshold = rememberSaveable { mutableIntStateOf(200) }
    val winner = remember { mutableStateOf<String?>(null) }

    val showResetScoresDialog = remember { mutableStateOf(false) }
    val showResetGameDialog = remember { mutableStateOf(false) }

    fun recomputeWinner() {
        val w = players.value.firstOrNull { it.total >= winThreshold.intValue }?.name
        winner.value = w
    }

    LaunchedEffect(players.value.size, winThreshold.intValue) {
        recomputeWinner()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                )
            )
            .padding(16.dp)
    ) {
        TopAppBar(
            title = { Text("🎯 多人计分游戏", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFee5a52),
                titleContentColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                ThresholdDisplay(winThreshold = winThreshold.intValue)
                Spacer(Modifier.height(8.dp))
                ControlsRow(
                    onResetScores = { showResetScoresDialog.value = true },
                    onResetGame = { showResetGameDialog.value = true },
                    onSetThreshold = { newValue ->
                        if (newValue > 0) {
                            winThreshold.intValue = newValue
                            recomputeWinner()
                            scope.launch { snackbarHostState.showSnackbar("胜利阈值已设置为 $newValue 分") }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("请输入有效的阈值") }
                        }
                    },
                    onAddPlayer = { name ->
                        val trimmed = name.trim()
                        if (trimmed.isEmpty()) {
                            scope.launch { snackbarHostState.showSnackbar("请输入玩家姓名") }
                        } else if (players.value.any { it.name == trimmed }) {
                            scope.launch { snackbarHostState.showSnackbar("玩家已存在") }
                        } else {
                            players.value = (players.value + Player(trimmed)).toMutableList()
                            scope.launch { snackbarHostState.showSnackbar("玩家 $trimmed 已添加") }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            if (players.value.isEmpty()) {
                Text(
                    text = "暂无玩家，请添加玩家开始游戏",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                PlayersList(
                    players = players.value,
                    winThreshold = winThreshold.intValue,
                    onAddScore = { name, score ->
                        if (score == 0) {
                            scope.launch { snackbarHostState.showSnackbar("请输入有效分数") }
                            return@PlayersList
                        }
                        val updated = players.value.map { p ->
                            if (p.name == name) {
                                val newRounds = p.rounds.toMutableList()
                                newRounds.add(score)
                                p.copy(rounds = newRounds)
                            } else p
                        }.toMutableList()
                        players.value = updated
                        recomputeWinner()
                        scope.launch { snackbarHostState.showSnackbar("$name 获得 $score 分") }
                    },
                    onRemovePlayer = { name ->
                        players.value = players.value.filter { it.name != name }.toMutableList()
                        recomputeWinner()
                        scope.launch { snackbarHostState.showSnackbar("玩家 $name 已移除") }
                    }
                )
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.fillMaxWidth())
    }

    if (winner.value != null) {
        WinnerDialog(
            winner = winner.value!!,
            onContinue = { winner.value = null },
            onStartNew = {
                // reset scores only
                players.value = players.value.map { it.copy(rounds = mutableListOf()) }.toMutableList()
                winner.value = null
            }
        )
    }

    if (showResetScoresDialog.value) {
        ConfirmDialog(
            title = "清零分数",
            message = "确定要清零所有玩家的分数吗？",
            onConfirm = {
                players.value = players.value.map { it.copy(rounds = mutableListOf()) }.toMutableList()
                winner.value = null
                showResetScoresDialog.value = false
            },
            onDismiss = { showResetScoresDialog.value = false }
        )
    }

    if (showResetGameDialog.value) {
        ConfirmDialog(
            title = "重置游戏",
            message = "确定要重置整个游戏吗？这将删除所有玩家。",
            onConfirm = {
                players.value = mutableListOf()
                winner.value = null
                showResetGameDialog.value = false
            },
            onDismiss = { showResetGameDialog.value = false }
        )
    }
}

@Composable
fun ThresholdDisplay(winThreshold: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(listOf(Color(0xFF17A2B8), Color(0xFF138496))),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = "🏆 当前胜利阈值：$winThreshold 分",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.CenterStart)
        )
    }
}

@Composable
fun ControlsRow(
    onResetScores: () -> Unit,
    onResetGame: () -> Unit,
    onSetThreshold: (Int) -> Unit,
    onAddPlayer: (String) -> Unit,
) {
    val playerName = remember { mutableStateOf("") }
    val thresholdText = remember { mutableStateOf("200") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = playerName.value,
                onValueChange = { playerName.value = it },
                label = { Text("输入玩家姓名") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { onAddPlayer(playerName.value); playerName.value = "" }) {
                Text("添加玩家")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = thresholdText.value,
                onValueChange = { thresholdText.value = it.filter { ch -> ch.isDigit() } },
                label = { Text("输入分数阈值") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { onSetThreshold(thresholdText.value.toIntOrNull() ?: 0) }) {
                Text("设置阈值")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745)), onClick = onResetScores, modifier = Modifier.weight(1f)) {
                Text("清零分数")
            }
            Button(colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545)), onClick = onResetGame, modifier = Modifier.weight(1f)) {
                Text("重置游戏")
            }
        }
    }
}

@Composable
fun PlayersList(
    players: List<Player>,
    winThreshold: Int,
    onAddScore: (name: String, score: Int) -> Unit,
    onRemovePlayer: (name: String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(players, key = { it.name }) { player ->
            PlayerCard(
                player = player,
                isWinner = player.total >= winThreshold,
                winThreshold = winThreshold,
                onAddScore = { score -> onAddScore(player.name, score) },
                onRemove = { onRemovePlayer(player.name) }
            )
        }
    }
}

@Composable
fun PlayerCard(
    player: Player,
    isWinner: Boolean,
    winThreshold: Int,
    onAddScore: (Int) -> Unit,
    onRemove: () -> Unit
) {
    val scoreText = remember { mutableStateOf("") }
    val progress = (player.total.toFloat() / winThreshold.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(player.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = onRemove) { Text("移除", color = Color(0xFFDC3545)) }
        }
        Text(
            text = player.total.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF0056B3),
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        ProgressBar(progress = progress)
        Text(
            text = "${player.total} / $winThreshold (${String.format("%.1f", progress * 100)}%)",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = scoreText.value,
                onValueChange = { scoreText.value = it.filter { ch -> ch.isDigit() } },
                label = { Text("输入本轮分数") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { onAddScore(scoreText.value.toIntOrNull() ?: 0); scoreText.value = "" }) {
                Text("添加分数")
            }
        }
        if (player.rounds.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text("历史得分：" + player.rounds.joinToString(" ") { it.toString() }, color = Color(0xFF007BFF))
        }
        if (isWinner) {
            Spacer(Modifier.height(6.dp))
            Text("🏆 获胜者", color = Color(0xFF28A745), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProgressBar(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .background(color = Color(0xFFE9ECEF), shape = RoundedCornerShape(5.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(10.dp)
                .background(
                    brush = Brush.linearGradient(listOf(Color(0xFF28A745), Color(0xFF20C997))),
                    shape = RoundedCornerShape(5.dp)
                )
        )
    }
}

@Composable
fun WinnerDialog(winner: String, onContinue: () -> Unit, onStartNew: () -> Unit) {
    AlertDialog(
        onDismissRequest = onContinue,
        title = { Text("🎉 恭喜获胜！") },
        text = { Text("恭喜 $winner 获胜！") },
        confirmButton = {
            TextButton(onClick = onContinue) { Text("继续游戏") }
        },
        dismissButton = {
            TextButton(onClick = onStartNew) { Text("开始新局") }
        }
    )
}

@Composable
fun ConfirmDialog(title: String, message: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("确定") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}