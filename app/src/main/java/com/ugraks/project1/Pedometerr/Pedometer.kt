import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity // LocalDensity eklendi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavHostController
import com.ugraks.project1.AppNavigation.Screens
import com.ugraks.project1.Pedometerr.StepCounterService // StepCounterService ve diğer ilgili sınıfların projenizde tanımlı olduğunu varsayıyorum.
import com.ugraks.project1.Pedometerr.saveDailyStepCount

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepCounterPage(navController: NavHostController) {


    val context = LocalContext.current
    val density = LocalDensity.current // Mevcut ekran yoğunluğunu al
    val scrollState = rememberScrollState()
    val vibrator = remember {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    val sharedPreferences = remember {
        context.getSharedPreferences("step_counter_prefs", Context.MODE_PRIVATE)
    }

    // State'ler - Mantık aynı kalıyor
    var stepCount by remember {
        mutableStateOf(
            sharedPreferences.getInt(
                StepCounterService.STEP_COUNT_KEY,
                0
            )
        )
    }
    var isStarted by remember {
        mutableStateOf(
            sharedPreferences.getBoolean(
                StepCounterService.IS_STARTED_KEY,
                false
            )
        )
    }
    var targetStepCountInput by remember { mutableStateOf("") }
    var targetStepCount by remember { mutableStateOf("") }
    var goalReached by remember { mutableStateOf(false) }
    var isSettingGoal by remember { mutableStateOf(true) }

    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    val onPrimaryColor = colorScheme.onPrimary
    val backgroundColor = colorScheme.background
    val onBackgroundColor = colorScheme.onBackground
    val secondaryColor = colorScheme.secondary
    val onSecondaryColor = colorScheme.onSecondary
    val errorColor = colorScheme.error
    val successColor = Color.Green // Hedefe ulaşılınca yeşil renk

    // BroadcastReceiver - Mantık aynı kalıyor
    val stepCountReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.getIntExtra(StepCounterService.STEP_COUNT_KEY, 0)?.let { newStepCount ->
                    stepCount = newStepCount
                    if (targetStepCount.isNotEmpty()) {
                        val target = targetStepCount.toIntOrNull() ?: 0
                        val oldGoalReached = goalReached // Önceki durumu sakla
                        goalReached = newStepCount >= target && target > 0
                        if (goalReached && !oldGoalReached) { // Hedefe yeni ulaşıldıysa titreşim ver
                            vibrator.vibrate(
                                VibrationEffect.createOneShot(
                                    500,
                                    VibrationEffect.DEFAULT_AMPLITUDE
                                )
                            )
                        }
                    } else {
                        goalReached = false
                    }
                }
            }
        }
    }

    // Receiver kaydı ve kaydın silinmesi - Mantık aynı kalıyor
    DisposableEffect(context) {
        val filter = IntentFilter("STEP_COUNT_UPDATED")
        LocalBroadcastManager.getInstance(context).registerReceiver(stepCountReceiver, filter)
        onDispose {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(stepCountReceiver)
        }
    }

    // Shared Preferences'tan kayıtlı verileri yükleme - Mantık aynı kalıyor
    LaunchedEffect(Unit) {
        stepCount = sharedPreferences.getInt(StepCounterService.STEP_COUNT_KEY, 0)
        isStarted = sharedPreferences.getBoolean(StepCounterService.IS_STARTED_KEY, false)
        val savedTarget = sharedPreferences.getString("target_step_count", "") ?: ""
        if (savedTarget.isNotEmpty()) {
            targetStepCount = savedTarget
            isSettingGoal = false
            val target = targetStepCount.toIntOrNull() ?: 0
            goalReached = stepCount >= target && target > 0
        }
    }

    // İlerleme Yüzdesi Hesaplama ve Animasyon
    val target = targetStepCount.toIntOrNull() ?: 0
    val progress = if (target > 0) minOf(stepCount.toFloat() / target, 1.0f) else 0.0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 1000,
            delayMillis = 100
        ) // Animasyon süresi ve gecikme eklendi
    )

    // Hedefe ulaşıldığında çubuk rengi animasyonu
    val progressColor by animateColorAsState(
        targetValue = if (goalReached) successColor else primaryColor,
        animationSpec = tween(durationMillis = 500)
    )
    // Hedefe ulaşıldığında adım sayısı rengi animasyonu
    val stepCountColor by animateColorAsState(
        targetValue = if (goalReached) successColor else onBackgroundColor, // Adım sayısını Card dışına taşıdık, rengini onBackground yapalım
        animationSpec = tween(durationMillis = 500)
    )


    // Arka plan gradyanı
    val backgroundBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                colorScheme.primaryContainer,
                colorScheme.secondaryContainer
            ) // Temanın belirgin renklerini kullan
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundBrush) // Arka plana gradyan uygulandı
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Başlık ve Geri Butonu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = onBackgroundColor // Gradyan üzerinde iyi durması için onBackground
                    )
                }

                Text(
                    text = "Pedometer",
                    style = MaterialTheme.typography.headlineMedium,
                    color = onBackgroundColor, // Gradyan üzerinde iyi durması için onBackground
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Hedef Belirleme veya Gösterim Alanı
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface) // Sade yüzey rengi
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isSettingGoal) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = targetStepCountInput,
                                onValueChange = {
                                    if (it.all { char -> char.isDigit() }) {
                                        targetStepCountInput = it
                                    }
                                },
                                label = { Text("Enter Target Steps", color = primaryColor) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = colorScheme.surface,
                                    unfocusedContainerColor = colorScheme.surface,
                                    focusedIndicatorColor = primaryColor,
                                    unfocusedIndicatorColor = Color.Gray, // Daha nötr gri
                                    cursorColor = primaryColor
                                ),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                )
                            )
                            Button(
                                onClick = {
                                    if (targetStepCountInput.isNotEmpty() && targetStepCountInput.toIntOrNull() != null && targetStepCountInput.toInt() > 0) { // Hedef 0'dan büyük olmalı kontrolü
                                        targetStepCount = targetStepCountInput
                                        isSettingGoal = false
                                        sharedPreferences.edit()
                                            .putString("target_step_count", targetStepCount).apply()
                                        val targetVal = targetStepCount.toInt()
                                        goalReached = stepCount >= targetVal && targetVal > 0
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = secondaryColor),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Set Goal", color = onSecondaryColor)
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Hedef ikonu
                            Icon(
                                imageVector = Icons.Default.Star, // Yıldız ikonu
                                contentDescription = "Goal Icon",
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp)) // İkon ile metin arası boşluk

                            Text(
                                text = "Goal: $targetStepCount Steps",
                                style = MaterialTheme.typography.titleLarge,
                                color = onBackgroundColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f) // Metnin yerleşimi için ağırlık
                            )
                            Button(
                                onClick = {
                                    isSettingGoal = true
                                    targetStepCountInput = targetStepCount
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = secondaryColor),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Update", color = onSecondaryColor)
                            }
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(32.dp))

            // *** ADIM SAYISI VE İLERLEME ÇUBUĞU ALANI - Geliştirilmiş ***
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(250.dp) // Çap daha da büyütüldü
                    .padding(16.dp) // Çevresine boşluk
            ) {
                // Arka plan çemberi
                CircularProgressIndicator(
                    progress = 1f,
                    strokeWidth = 16.dp, // Çubuk kalınlığı artırıldı
                    color = colorScheme.surface, // Arka plan rengi surface
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            2.dp,
                            colorScheme.onSurface.copy(alpha = 0.1f),
                            CircleShape
                        ) // Hafif kenarlık
                )

                // İlerleme çubuğu
                CircularProgressIndicator(
                    progress = animatedProgress,
                    strokeWidth = 16.dp, // Çubuk kalınlığı artırıldı
                    color = progressColor, // Animasyonlu renk kullanıldı (Hedefe ulaşılınca yeşil olur)
                    modifier = Modifier.fillMaxSize(),
                    strokeCap = StrokeCap.Round // Çubuk uçlarını yuvarlak yap
                )

                // Adım sayısı ve hedef metinleri
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$stepCount",
                        style = MaterialTheme.typography.displayMedium, // Boyut ayarlandı
                        color = stepCountColor, // Animasyonlu renk kullanıldı (Hedefe ulaşılınca yeşil olur)
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (target > 0) { // Hedef varsa göster
                        Text(
                            text = "out of $target",
                            style = MaterialTheme.typography.titleMedium, // Boyut ayarlandı
                            color = onBackgroundColor // Gradyan üzerinde iyi durması için onBackground
                        )
                    }
                }
            }
            // ****************************************************


            Spacer(modifier = Modifier.height(40.dp))

            // Butonlar
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp) // Butonlar arasına boşluk
            ) {
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isStarted) errorColor else primaryColor, // Duruma göre renk değiştir
                        contentColor = if (isStarted) colorScheme.onError else onPrimaryColor
                    ),
                    shape = RoundedCornerShape(12.dp), // Yuvarlak köşeler
                    modifier = Modifier.fillMaxWidth().height(56.dp) // Genişlik ve yükseklik ayarı
                ) {
                    Text(
                        text = if (isStarted) "Stop" else "Start",
                        fontSize = 18.sp, // Yazı boyutu
                        fontWeight = FontWeight.Bold // Kalın yazı
                    )
                }

                Button(
                    onClick = {
                        // *** BURASI DEĞİŞTİ - Adım sayısı 0 ise kontrolü eklendi ***
                         if (stepCount == 0) {
                            // Adım sayısı 0 ise Toast mesajı göster
                            Toast.makeText(context, "No steps to save or reset.", Toast.LENGTH_SHORT).show()
                        } else {
                            // Adım sayısı 0'dan büyükse mevcut işlemi yap
                            val serviceIntent = Intent(context, StepCounterService::class.java)
                            serviceIntent.action = StepCounterService.ACTION_RESET
                            val target = targetStepCount.toIntOrNull()
                            val stepCountSave = stepCount // Adım sayısını al
                            saveDailyStepCount(
                                context = context,
                                stepCount = stepCountSave,
                                targetStep = target,
                                goalReached = goalReached
                            )  // Günlük adım sayısını kaydet
                            context.startService(serviceIntent)
                            stepCount = 0
                            isStarted = false
                            goalReached = false
                            targetStepCount = ""
                            targetStepCountInput = ""
                            isSettingGoal = true
                            sharedPreferences.edit().remove("target_step_count").apply()
                            vibrator.vibrate(
                                VibrationEffect.createOneShot(
                                    200,
                                    VibrationEffect.DEFAULT_AMPLITUDE
                                )
                            )
                        }
                    },
                    // ... geri kalan buton özellikleri aynı kalıyor
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        "Reset and Save",
                        color = onBackgroundColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = {
                        navController.navigate(Screens.PedometerDailySummary)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.surfaceVariant), // Farklı renk tonu
                    shape = RoundedCornerShape(12.dp), // Yuvarlak köşeler
                    modifier = Modifier.fillMaxWidth().height(56.dp) // Genişlik ve yükseklik ayarı
                ) {
                    Text(
                        "Show Daily Summary",
                        color = onBackgroundColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ) // Yazı rengi ve stili
                }
            }


            Spacer(modifier = Modifier.height(32.dp))

            // Hedefe Ulaşıldı Mesajı veya Diğer Bilgiler
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (goalReached) {
                    Text(
                        text = "Goal Reached! Congratulations!",
                        style = MaterialTheme.typography.headlineSmall, // Boyut ayarlandı
                        color = successColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    // Hedefe ulaşılmadıysa farklı bir mesaj veya boşluk
                    // Spacer yüksekliğini MaterialTheme.typography.headlineSmall.fontSize'ı kullanarak hesaplayalım
                    val headlineSmallLineHeight =
                        with(density) { MaterialTheme.typography.headlineSmall.fontSize.toDp() * 1.5f } // Yaklaşık satır yüksekliği
                    Spacer(modifier = Modifier.height(headlineSmallLineHeight)) // Yer tutması için
                }
            }


            // Alt kısımda kalan boşluğu doldurmak için Spacer(Modifier.weight(1f)) eklenebilir,
            // ancak bu durumda tüm üstteki öğeler yukarı sıkışır. Mevcut padding ve boşluklarla
            // orta alana odaklanmak daha iyi olabilir.
            // Eğer ekran çok büyükse ve altı boş kalıyorsa weight kullanışlı olabilir.
            // Spacer(modifier = Modifier.weight(1f))
        } // Ana Column sonu
    }

}