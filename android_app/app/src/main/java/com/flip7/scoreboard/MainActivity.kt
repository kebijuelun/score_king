package com.flip7.scoreboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flip7.scoreboard.ui.theme.Flip7Theme
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

// Data model to mirror web app state
data class Player(
    val name: String,
    val rounds: MutableList<Int> = mutableListOf(),
) {
    val total: Int get() = rounds.sum()
}

class ScoreboardViewModel : ViewModel() {
    // Players list survives configuration changes
    val players = mutableStateListOf<Player>()

    // Game settings and derived state
    var winThreshold by mutableStateOf(200)
    var winner by mutableStateOf<String?>(null)

    private fun recomputeWinner() {
        winner = players.firstOrNull { it.total >= winThreshold }?.name
    }

    fun setThreshold(value: Int) {
        if (value <= 0) return
        winThreshold = value
        recomputeWinner()
    }

    fun addPlayer(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return false
        if (players.any { it.name == trimmed }) return false
        players.add(Player(trimmed))
        return true
    }

    fun removePlayer(name: String) {
        players.removeAll { it.name == name }
        recomputeWinner()
    }

    fun addScore(name: String, score: Int): Boolean {
        if (score == 0) return false
        val index = players.indexOfFirst { it.name == name }
        if (index == -1) return false
        val player = players[index]
        val updatedRounds = player.rounds.toMutableList()
        updatedRounds.add(score)
        players[index] = player.copy(rounds = updatedRounds)
        recomputeWinner()
        return true
    }

    fun resetScores() {
        for (i in players.indices) {
            val p = players[i]
            players[i] = p.copy(rounds = mutableListOf())
        }
        winner = null
    }

    fun resetGame() {
        players.clear()
        winner = null
    }
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
fun ScoreboardScreen(viewModel: ScoreboardViewModel = viewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val players = viewModel.players
    val winThreshold = viewModel.winThreshold
    val winner = viewModel.winner

    val showResetScoresDialog = remember { mutableStateOf(false) }
    val showResetGameDialog = remember { mutableStateOf(false) }

    LaunchedEffect(players.size, winThreshold) {
        // ensure derived state consistent when entering composition
    }

    val isLandscape = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎯 桌游计分王", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        val bgModifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                )
            )
            .padding(innerPadding)
            .padding(16.dp)

        if (isLandscape) {
            Row(modifier = bgModifier) {
                // Left panel: controls
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ThresholdDisplay(winThreshold = winThreshold)
                        Spacer(Modifier.height(8.dp))
                        ControlsRow(
                            onResetScores = { showResetScoresDialog.value = true },
                            onResetGame = { showResetGameDialog.value = true },
                            onSetThreshold = { newValue ->
                                if (newValue > 0) {
                                    viewModel.setThreshold(newValue)
                                    scope.launch { snackbarHostState.showSnackbar("胜利阈值已设置为 $newValue 分") }
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("请输入有效的阈值") }
                                }
                            },
                            onAddPlayer = { name ->
                                val ok = viewModel.addPlayer(name)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        when {
                                            name.trim().isEmpty() -> "请输入玩家姓名"
                                            !ok && players.any { it.name == name.trim() } -> "玩家已存在"
                                            ok -> "玩家 ${name.trim()} 已添加"
                                            else -> "请输入玩家姓名"
                                        }
                                    )
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Right panel: list
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    if (players.isEmpty()) {
                        Text(
                            text = "暂无玩家，请添加玩家开始游戏",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    } else {
                        PlayersList(
                            players = players,
                            winThreshold = winThreshold,
                            onAddScore = { name, score ->
                                if (!viewModel.addScore(name, score)) {
                                    scope.launch { snackbarHostState.showSnackbar("请输入有效分数") }
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("$name 获得 $score 分") }
                                }
                            },
                            onRemovePlayer = { name ->
                                viewModel.removePlayer(name)
                                scope.launch { snackbarHostState.showSnackbar("玩家 $name 已移除") }
                            }
                        )
                    }
                }
            }
        } else {
            Column(modifier = bgModifier) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ThresholdDisplay(winThreshold = winThreshold)
                        Spacer(Modifier.height(8.dp))
                        ControlsRow(
                            onResetScores = { showResetScoresDialog.value = true },
                            onResetGame = { showResetGameDialog.value = true },
                            onSetThreshold = { newValue ->
                                if (newValue > 0) {
                                    viewModel.setThreshold(newValue)
                                    scope.launch { snackbarHostState.showSnackbar("胜利阈值已设置为 $newValue 分") }
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("请输入有效的阈值") }
                                }
                            },
                            onAddPlayer = { name ->
                                val ok = viewModel.addPlayer(name)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        when {
                                            name.trim().isEmpty() -> "请输入玩家姓名"
                                            !ok && players.any { it.name == name.trim() } -> "玩家已存在"
                                            ok -> "玩家 ${name.trim()} 已添加"
                                            else -> "请输入玩家姓名"
                                        }
                                    )
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
                    if (players.isEmpty()) {
                        Text(
                            text = "暂无玩家，请添加玩家开始游戏",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    } else {
                        PlayersList(
                            players = players,
                            winThreshold = winThreshold,
                            onAddScore = { name, score ->
                                if (!viewModel.addScore(name, score)) {
                                    scope.launch { snackbarHostState.showSnackbar("请输入有效分数") }
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("$name 获得 $score 分") }
                                }
                            },
                            onRemovePlayer = { name ->
                                viewModel.removePlayer(name)
                                scope.launch { snackbarHostState.showSnackbar("玩家 $name 已移除") }
                            }
                        )
                    }
                }
            }
        }
    }

    if (winner != null) {
        WinnerDialog(
            winner = winner!!,
            onContinue = { viewModel.winner = null },
            onStartNew = {
                viewModel.resetScores()
            }
        )
    }

    if (showResetScoresDialog.value) {
        ConfirmDialog(
            title = "清零分数",
            message = "确定要清零所有玩家的分数吗？",
            onConfirm = {
                viewModel.resetScores()
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
                viewModel.resetGame()
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
                brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.secondary)),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = "🏆 当前胜利阈值：$winThreshold 分",
            color = MaterialTheme.colorScheme.onPrimary,
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
                color = MaterialTheme.colorScheme.surface,
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
            color = MaterialTheme.colorScheme.primary,
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