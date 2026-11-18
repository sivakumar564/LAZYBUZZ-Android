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
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.lazybuzz.ui.components.LbCard
import java.util.concurrent.ExecutionException

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
                    onCameraError = { cameraError = it },
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
                            "Preview is processed on this device only. Nothing is stored or sent.",
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
    onCameraError: (String) -> Unit,
) {
    val previewView = remember(activity) {
        PreviewView(activity).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)),
    )

    DisposableEffect(activity, previewView, retryToken) {
        var disposed = false
        var provider: ProcessCameraProvider? = null
        var preview: Preview? = null

        fun reportCameraError() {
            preview?.let { provider?.unbind(it) }
            provider = null
            preview = null
            onCameraError("The camera couldn't be started. Check that it is available, then try again.")
        }

        val providerFuture = try {
            ProcessCameraProvider.getInstance(activity)
        } catch (_: IllegalStateException) {
            reportCameraError()
            null
        }

        providerFuture?.addListener({
            if (!disposed) {
                try {
                    val cameraProvider = providerFuture.get()
                    val previewUseCase = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    provider = cameraProvider
                    preview = previewUseCase
                    cameraProvider.bindToLifecycle(
                        activity,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        previewUseCase,
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
        }, ContextCompat.getMainExecutor(activity))

        onDispose {
            disposed = true
            preview?.let { provider?.unbind(it) }
        }
    }
}

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
