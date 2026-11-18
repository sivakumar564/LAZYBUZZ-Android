package com.lazybuzz.features.camera

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraUnavailableException
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.lazybuzz.core.camera.HandDetection
import com.lazybuzz.core.camera.HandLandmarks
import com.lazybuzz.core.camera.LocalFrameAnalyzer
import com.lazybuzz.ui.components.LbCard
import java.util.Locale
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun CameraPreviewScreen(onBack: () -> Unit) {
    val activity = requireNotNull(LocalContext.current.findComponentActivity()) {
        "Camera preview must be hosted by a ComponentActivity."
    }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var permissionRequestAttempted by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var cameraRetry by remember { mutableIntStateOf(0) }
    var handDetection by remember { mutableStateOf<HandDetection?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionRequestAttempted = true
        hasPermission = granted
        cameraError = null
    }

    DisposableEffect(activity) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.CAMERA,
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
        activity.lifecycle.addObserver(observer)
        onDispose { activity.lifecycle.removeObserver(observer) }
    }

    val shouldShowRationale = activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
    val permanentlyDenied = permissionRequestAttempted && !hasPermission && !shouldShowRationale

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (hasPermission) {
                CameraPreview(
                    activity = activity,
                    retryToken = cameraRetry,
                    detection = handDetection,
                    onCameraError = { cameraError = it },
                    onHandDetection = { handDetection = it },
                )
            } else {
                Box(
                    Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center,
                ) {
                    LbCard(Modifier.fillMaxWidth().padding(20.dp)) {
                        Column(
                            Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text("Camera access is needed for the live preview.")
                            Text(
                                if (permanentlyDenied) {
                                    "Camera access is turned off for LAZYBUZZ. You can enable it in app settings."
                                } else {
                                    "Allow camera access to see the preview. Camera frames stay on this device."
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (permanentlyDenied) {
                                Button(onClick = {
                                    activity.startActivity(
                                        Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", activity.packageName, null),
                                        ),
                                    )
                                }) {
                                    Text("Open app settings")
                                }
                            } else {
                                Button(onClick = {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }) {
                                    Text(if (shouldShowRationale) "Try again" else "Allow camera access")
                                }
                            }
                        }
                    }
                }
            }

            Column(
                Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onBack) { Text("Back") }
                        Text("Live camera preview", style = MaterialTheme.typography.titleMedium)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    cameraError?.let { message ->
                        LbCard(Modifier.fillMaxWidth()) {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(message, color = MaterialTheme.colorScheme.error)
                                Button(onClick = {
                                    cameraError = null
                                    cameraRetry++
                                }) {
                                    Text("Retry camera")
                                }
                            }
                        }
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(
                            buildString {
                                if (hasPermission) {
                                    append(handDetection?.let { detection ->
                                        val index = detection.landmarks.indexFingerTip
                                        val thumb = detection.landmarks.thumbTip
                                        String.format(
                                            Locale.US,
                                            "Hand detected · index (%.2f, %.2f) · thumb (%.2f, %.2f)",
                                            index.x,
                                            index.y,
                                            thumb.x,
                                            thumb.y,
                                        )
                                    } ?: "No hand detected. Show one hand to the camera.")
                                    append('\n')
                                }
                                append("Preview is processed on this device only. Nothing is stored or sent.")
                            },
                            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(
    activity: ComponentActivity,
    retryToken: Int,
    detection: HandDetection?,
    onCameraError: (String) -> Unit,
    onHandDetection: (HandDetection?) -> Unit,
) {
    val previewView = remember(activity) {
        PreviewView(activity).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    Box(Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp))) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize(),
        )
        LandmarkOverlay(
            detection = detection,
            modifier = Modifier.fillMaxSize(),
        )
    }

    DisposableEffect(activity, previewView, retryToken) {
        val disposed = AtomicBoolean(false)
        var provider: ProcessCameraProvider? = null
        var preview: Preview? = null
        var imageAnalysis: ImageAnalysis? = null
        val analysisExecutor = Executors.newSingleThreadExecutor()
        val mainExecutor = ContextCompat.getMainExecutor(activity)
        val analyzerReleased = AtomicBoolean(false)
        lateinit var analyzer: LocalFrameAnalyzer

        fun releaseAnalyzer() {
            if (analyzerReleased.compareAndSet(false, true)) {
                imageAnalysis?.clearAnalyzer()
                analysisExecutor.execute { analyzer.close() }
                analysisExecutor.shutdown()
            }
        }

        analyzer = LocalFrameAnalyzer(
            context = activity,
            onDetection = { detection ->
                mainExecutor.execute {
                    if (!disposed.get()) onHandDetection(detection)
                }
            },
            onError = { message ->
                mainExecutor.execute {
                    if (!disposed.get()) {
                        onHandDetection(null)
                        onCameraError(message)
                        releaseAnalyzer()
                    }
                }
            },
        )

        fun reportCameraError() {
            preview?.let { provider?.unbind(it) }
            imageAnalysis?.let { provider?.unbind(it) }
            releaseAnalyzer()
            provider = null
            preview = null
            imageAnalysis = null
            onHandDetection(null)
            onCameraError("The camera or hand tracker couldn't be started. Check that the camera is available, then retry.")
        }

        fun bindCamera(cameraProvider: ProcessCameraProvider) {
            if (disposed.get()) return
            try {
                val previewUseCase = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val analysisUseCase = ImageAnalysis.Builder()
                    .setTargetResolution(LocalFrameAnalyzer.TARGET_SIZE)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysisUseCase.setAnalyzer(analysisExecutor, analyzer)
                provider = cameraProvider
                preview = previewUseCase
                imageAnalysis = analysisUseCase
                cameraProvider.bindToLifecycle(
                    activity,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    previewUseCase,
                    analysisUseCase,
                )
            } catch (_: CameraUnavailableException) {
                reportCameraError()
            } catch (_: ExecutionException) {
                reportCameraError()
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                reportCameraError()
            } catch (_: IllegalArgumentException) {
                reportCameraError()
            } catch (_: IllegalStateException) {
                reportCameraError()
            } catch (_: SecurityException) {
                reportCameraError()
            }
        }

        val providerFuture = try {
            ProcessCameraProvider.getInstance(activity)
        } catch (_: IllegalStateException) {
            reportCameraError()
            null
        }

        providerFuture?.addListener({
            if (!disposed.get()) {
                try {
                    val cameraProvider = providerFuture.get()
                    provider = cameraProvider
                    analysisExecutor.execute {
                        try {
                            analyzer.initialize()
                            mainExecutor.execute { bindCamera(cameraProvider) }
                        } catch (exception: RuntimeException) {
                            mainExecutor.execute {
                                if (!disposed.get()) {
                                    onHandDetection(null)
                                    onCameraError(
                                        "Hand tracking couldn't start: ${exception.message ?: "MediaPipe could not load its model."}",
                                    )
                                    releaseAnalyzer()
                                }
                            }
                        }
                    }
                } catch (_: CameraUnavailableException) {
                    reportCameraError()
                } catch (_: ExecutionException) {
                    reportCameraError()
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                    reportCameraError()
                } catch (_: IllegalArgumentException) {
                    reportCameraError()
                } catch (_: IllegalStateException) {
                    reportCameraError()
                } catch (_: SecurityException) {
                    reportCameraError()
                }
            }
        }, ContextCompat.getMainExecutor(activity))

        onDispose {
            disposed.set(true)
            onHandDetection(null)
            preview?.let { provider?.unbind(it) }
            imageAnalysis?.let { provider?.unbind(it) }
            releaseAnalyzer()
        }
    }
}

@Composable
private fun LandmarkOverlay(detection: HandDetection?, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Canvas(modifier) {
        val hand = detection?.landmarks ?: return@Canvas
        if (detection.imageWidth <= 0 || detection.imageHeight <= 0) return@Canvas
        val scale = maxOf(
            size.width / detection.imageWidth,
            size.height / detection.imageHeight,
        )
        val imageWidth = detection.imageWidth * scale
        val imageHeight = detection.imageHeight * scale
        val offsetX = (size.width - imageWidth) / 2f
        val offsetY = (size.height - imageHeight) / 2f

        fun point(index: Int): Offset {
            val landmark = hand.points[index]
            return Offset(
                x = offsetX + (1f - landmark.x) * imageWidth,
                y = offsetY + landmark.y * imageHeight,
            )
        }

        HAND_CONNECTIONS.forEach { (start, end) ->
            drawLine(
                color = colors.primary,
                start = point(start),
                end = point(end),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
        hand.points.indices.forEach { index ->
            drawCircle(
                color = if (index == HandLandmarks.INDEX_FINGER_TIP || index == HandLandmarks.THUMB_TIP) {
                    colors.secondary
                } else {
                    colors.primary
                },
                radius = if (index == HandLandmarks.INDEX_FINGER_TIP || index == HandLandmarks.THUMB_TIP) {
                    6.dp.toPx()
                } else {
                    4.dp.toPx()
                },
                center = point(index),
            )
        }
    }
}

private val HAND_CONNECTIONS = listOf(
    0 to 1, 1 to 2, 2 to 3, 3 to 4,
    0 to 5, 5 to 6, 6 to 7, 7 to 8,
    5 to 9, 9 to 10, 10 to 11, 11 to 12,
    9 to 13, 13 to 14, 14 to 15, 15 to 16,
    13 to 17, 0 to 17, 17 to 18, 18 to 19, 19 to 20,
)

private fun Context.findComponentActivity(): ComponentActivity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is ComponentActivity) return current
        val base = current.baseContext
        if (base === current) return null
        current = base
    }
    return current as? ComponentActivity
}
