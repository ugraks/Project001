package com.ugraks.project1 // Kendi paket adınız

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.* // remember, mutableStateOf, LaunchedEffect, getValue, setValue, rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel // ViewModel için import
import androidx.localbroadcastmanager.content.LocalBroadcastManager // BroadcastReceiver için
import androidx.navigation.NavHostController
import com.ugraks.project1.AppNavigation.Screens // Navigasyon ekranları
import com.ugraks.project1.Pedometerr.StepCounterService // StepCounterService sınıfınızın olduğu paket ve sınıf
// Eski dosya kaydetme fonksiyonu artık GEREKMEZ: import com.ugraks.project1.Pedometerr.saveDailyStepCount
import com.ugraks.project1.ui.viewmodels.PedometerViewModel // KENDİ PedometerViewModel'ınız

@RequiresApi(Build.VERSION_CODES.O) // VibrationEffect.createOneShot ve LocalDate (eğer kullanılacaksa) için gerekebilir
@OptIn(ExperimentalMaterial3Api::class) // Material 3 opt-in gerektiriyorsa
@Composable
fun StepCounterPage(
    navController: NavHostController,
    viewModel: PedometerViewModel = hiltViewModel() // YENİ: PedometerViewModel'ı Hilt ile inject et
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    val vibrator = remember {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    // Shared Preferences hala UI state'ini (başladı mı, hedef nedir) saklamak için kullanılabilir
    // Room bu UI state'ini yönetmek için değil, veriyi kalıcılaştırmak için kullanılır.
    val sharedPreferences = remember {
        context.getSharedPreferences("step_counter_prefs", Context.MODE_PRIVATE)
    }

    // State'ler - Mantık aynı kalıyor (Adım sayısı ve hedef UI state'i olarak burada tutulur)
    // Not: Eğer servis adım sayısını Room'a yazıyorsa, adım sayısını da ViewModel'dan Flow ile almayı düşünebilirsiniz.
    // Ancak mevcut kodunuzda BroadcastReceiver kullandığınız için bu şekilde kalması mantıklı.
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
    var targetStepCount by remember { mutableStateOf("") } // Hedef adım sayısı UI state'i
    var goalReached by remember { mutableStateOf(false) } // Hedefe ulaşıldı mı UI state'i
    var isSettingGoal by remember { mutableStateOf(true) } // Hedef belirleniyor mu UI state'i

    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    val onPrimaryColor = colorScheme.onPrimary
    val backgroundColor = colorScheme.background
    val onBackgroundColor = colorScheme.onBackground
    val secondaryColor = colorScheme.secondary
    val onSecondaryColor = colorScheme.onSecondary
    val errorColor = colorScheme.error
    val successColor = Color.Green // Hedefe ulaşılınca yeşil renk

    // BroadcastReceiver - Adım sayısını servisten alıp UI state'ini günceller (aynı kalır)
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

    // Shared Preferences'tan kayıtlı hedef verilerini yükleme - Mantık aynı kalıyor
    // Bu, UI state'ini dosya (SharedPrefs) üzerinden yeniden kurar.
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

    // İlerleme Yüzdesi Hesaplama ve Animasyon aynı kalır
    val target = targetStepCount.toIntOrNull() ?: 0
    val progress = if (target > 0) minOf(stepCount.toFloat() / target, 1.0f) else 0.0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, delayMillis = 100),
        label = "progressAnimation" // Animasyon label eklendi (önerilir)
    )

    // Hedefe ulaşıldığında çubuk rengi animasyonu aynı kalır
    val progressColor by animateColorAsState(
        targetValue = if (goalReached) successColor else primaryColor,
        animationSpec = tween(durationMillis = 500),
        label = "progressColorAnimation" // Animasyon label eklendi
    )
    // Hedefe ulaşıldığında adım sayısı rengi animasyonu aynı kalır
    val stepCountColor by animateColorAsState(
        targetValue = if (goalReached) successColor else onBackgroundColor,
        animationSpec = tween(durationMillis = 500),
        label = "stepCountColorAnimation" // Animasyon label eklendi
    )


    // Arka plan gradyanı aynı kalır
    val backgroundBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                colorScheme.primaryContainer,
                colorScheme.secondaryContainer
            )
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

            // Başlık ve Geri Butonu aynı kalır
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

            // Hedef Belirleme veya Gösterim Alanı aynı kalır
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
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                )
                            )
                            Button(
                                onClick = {
                                    if (targetStepCountInput.isNotEmpty() && targetStepCountInput.toIntOrNull() != null && targetStepCountInput.toInt() > 0) { // Hedef 0'dan büyük olmalı kontrolü
                                        targetStepCount = targetStepCountInput
                                        isSettingGoal = false
                                        // UI state'ini kalıcılaştırmak için SharedPrefs kullanılıyor
                                        sharedPreferences.edit()
                                            .putString("target_step_count", targetStepCount).apply()
                                        val targetVal = targetStepCount.toInt()
                                        goalReached = stepCount >= targetVal && targetVal > 0
                                    } else {
                                        Toast.makeText(context, "Please enter a valid target greater than 0.", Toast.LENGTH_SHORT).show()
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
                            // Hedef ikonu aynı kalır
                            Icon(
                                imageVector = Icons.Default.Star,
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

            // *** ADIM SAYISI VE İLERLEME ÇUBUĞU ALANI aynı kalır ***
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(250.dp) // Çap daha da büyütüldü
                    .padding(16.dp) // Çevresine boşluk
            ) {
                // Arka plan çemberi aynı kalır
                CircularProgressIndicator(
                    progress = 1f,
                    strokeWidth = 16.dp,
                    color = colorScheme.surface,
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            2.dp,
                            colorScheme.onSurface.copy(alpha = 0.1f),
                            CircleShape
                        )
                )

                // İlerleme çubuğu aynı kalır
                CircularProgressIndicator(
                    progress = animatedProgress,
                    strokeWidth = 16.dp,
                    color = progressColor,
                    modifier = Modifier.fillMaxSize(),
                    strokeCap = StrokeCap.Round
                )

                // Adım sayısı ve hedef metinleri aynı kalır
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$stepCount",
                        style = MaterialTheme.typography.displayMedium,
                        color = stepCountColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (target > 0) { // Hedef varsa göster
                        Text(
                            text = "out of $target",
                            style = MaterialTheme.typography.titleMedium,
                            color = onBackgroundColor
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
                // Start/Stop Butonu aynı kalır (Servis ile etkileşimde)
                Button(
                    onClick = {
                        isStarted = !isStarted
                        val serviceIntent = Intent(context, StepCounterService::class.java)
                        if (isStarted) {
                            serviceIntent.action = StepCounterService.ACTION_START
                        } else {
                            serviceIntent.action = StepCounterService.ACTION_STOP
                        }
                        // Build.VERSION.SDK_INT >= Build.VERSION_CODES.O kontrolü ile
                        // startForegroundService kullanmak daha doğru olabilir production uygulamalarında
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

                // Reset and Save Butonu - İŞLEM DEĞİŞTİ
                Button(
                    onClick = {
                        // Adım sayısı 0 ise kontrolü aynı kalır
                        if (stepCount == 0) {
                            Toast.makeText(context, "No steps to save or reset.", Toast.LENGTH_SHORT).show()
                        } else {
                            val targetVal = targetStepCount.toIntOrNull() // Hedefi al (Int? tipinde)

                            // YENİ: saveDailyStepCount yerine ViewModel metodunu çağır
                            // Bu, mevcut adım sayısını, hedefi ve durumunu Room'a yeni bir giriş olarak ekler.
                            viewModel.addStepEntry(
                                steps = stepCount, // Mevcut adım sayısını gönder
                                target = targetVal, // Hedef adım sayısını gönder (Int?)
                                goalReached = goalReached // Hedefe ulaşılıp ulaşılmadığını gönder (Boolean)
                            )
                            // ESKİ: saveDailyStepCount(context = context, stepCount = stepCountSave, targetStep = target, goalReached = goalReached) kaldırıldı

                            // Servise reset komutunu gönder (adım sayacını sıfırlar, bu kısım aynı kalır)
                            val serviceIntent = Intent(context, StepCounterService::class.java)
                            serviceIntent.action = StepCounterService.ACTION_RESET
                            context.startService(serviceIntent)

                            // UI state'lerini sıfırla (bu kısım aynı kalır)
                            stepCount = 0
                            isStarted = false
                            goalReached = false
                            targetStepCount = ""
                            targetStepCountInput = ""
                            isSettingGoal = true
                            sharedPreferences.edit().remove("target_step_count").apply()

                            // Titreşim aynı kalır
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
                // Show Daily Summary butonu aynı kalır
                Button(
                    onClick = {
                        // Navigasyon aynı kalır, DailySummaryPage artık PedometerViewModel kullanacak
                        navController.navigate(Screens.PedometerDailySummary)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(
                        "Show Daily Summary",
                        color = onBackgroundColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }


            Spacer(modifier = Modifier.height(32.dp))

            // Hedefe Ulaşıldı Mesajı veya Diğer Bilgiler aynı kalır
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (goalReached) {
                    Text(
                        text = "Goal Reached! Congratulations!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = successColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    val headlineSmallLineHeight = with(density) { MaterialTheme.typography.headlineSmall.fontSize.toDp() * 1.5f }
                    Spacer(modifier = Modifier.height(headlineSmallLineHeight))
                }
            }
        } // Ana Column sonu
    }
}