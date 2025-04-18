import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.VibrationEffect
import android.os.Vibrator
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavHostController
import com.ugraks.project1.StepCounterService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepCounterPage(navController: NavHostController) {
    val context = LocalContext.current
    val vibrator = remember {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    val sharedPreferences = remember {
        context.getSharedPreferences("step_counter_prefs", Context.MODE_PRIVATE)
    }
    var stepCount by remember { mutableStateOf(sharedPreferences.getInt(StepCounterService.STEP_COUNT_KEY, 0)) }
    var isStarted by remember { mutableStateOf(sharedPreferences.getBoolean(StepCounterService.IS_STARTED_KEY, false)) }
    var targetStepCountInput by remember { mutableStateOf("") }
    var targetStepCount by remember { mutableStateOf("") }
    var goalReached by remember { mutableStateOf(false) }
    var isSettingGoal by remember { mutableStateOf(true) } // Başlangıçta hedef belirleme modunda

    val stepCountReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.getIntExtra(StepCounterService.STEP_COUNT_KEY, 0)?.let { newStepCount ->
                    stepCount = newStepCount
                    if (targetStepCount.isNotEmpty() && newStepCount >= targetStepCount.toInt()) {
                        goalReached = true
                    } else {
                        goalReached = false
                    }
                }
            }
        }
    }

    DisposableEffect(context) {
        val filter = IntentFilter("STEP_COUNT_UPDATED")
        LocalBroadcastManager.getInstance(context).registerReceiver(stepCountReceiver, filter)
        onDispose {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(stepCountReceiver)
        }
    }

    LaunchedEffect(Unit) {
        stepCount = sharedPreferences.getInt(StepCounterService.STEP_COUNT_KEY, 0)
        isStarted = sharedPreferences.getBoolean(StepCounterService.IS_STARTED_KEY, false)
        val savedTarget = sharedPreferences.getString("target_step_count", "") ?: ""
        if (savedTarget.isNotEmpty()) {
            targetStepCount = savedTarget
            isSettingGoal = false
            if (stepCount >= targetStepCount.toInt()) {
                goalReached = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Step Counter",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(16.dp)) // Başlık ile hedef arası boşluk

        if (isSettingGoal) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = targetStepCountInput,
                    onValueChange = { targetStepCountInput = it },
                    label = { Text("Target") },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (targetStepCountInput.isNotEmpty()) {
                            targetStepCount = targetStepCountInput
                            isSettingGoal = false
                            sharedPreferences.edit().putString("target_step_count", targetStepCount).apply()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Set Goal", color = MaterialTheme.colorScheme.onSecondary)
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Goal: $targetStepCount Steps",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        isSettingGoal = true
                        targetStepCountInput = targetStepCount // Mevcut hedefi inputa geri yükle
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Update", color = MaterialTheme.colorScheme.onSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp)) // Hedef ile adım sayısı arası boşluk

        Text(
            text = "Steps: $stepCount",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally) // Adım sayısını ortaladık
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                isStarted = !isStarted
                val serviceIntent = Intent(context, StepCounterService::class.java)
                if (isStarted) {
                    serviceIntent.action = StepCounterService.ACTION_START
                } else {
                    serviceIntent.action = StepCounterService.ACTION_STOP
                }
                context.startService(serviceIntent)
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        100,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth() // Butonları genişlettik
        ) {
            Text(
                text = if (isStarted) "Stop" else "Start",
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val serviceIntent = Intent(context, StepCounterService::class.java)
                serviceIntent.action = StepCounterService.ACTION_RESET
                context.startService(serviceIntent)
                stepCount = 0
                isStarted = false
                goalReached = false
                targetStepCount = ""
                targetStepCountInput = ""
                isSettingGoal = true // Sıfırlama durumunda tekrar hedef belirleme moduna geç
                sharedPreferences.edit().remove("target_step_count").apply()
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        200,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            modifier = Modifier.fillMaxWidth() // Butonları genişlettik
        ) {
            Text("Reset", color = Color.White)
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (goalReached) {
            Text(
                text = "Congratulations!",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Green,
                modifier = Modifier.align(Alignment.CenterHorizontally) // Tebrik mesajını ortaladık
            )
        }
    }
}