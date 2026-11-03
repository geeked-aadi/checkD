package com.example.eventattendance.presentation.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eventattendance.presentation.scanner.components.CameraControls
import com.example.eventattendance.presentation.scanner.components.ScanFeedbackBanner
import com.example.eventattendance.presentation.scanner.components.ScannerOverlay
import com.example.eventattendance.presentation.theme.MonochromeBlack
import com.example.eventattendance.presentation.theme.MonochromeWhite
import com.example.eventattendance.presentation.theme.ScannerBackground
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(
    onBackClick: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val scanResult by viewModel.scanResult.collectAsStateWithLifecycle()
    val isTorchOn by viewModel.isTorchOn.collectAsStateWithLifecycle()
    val isBackCamera by viewModel.isBackCamera.collectAsStateWithLifecycle()

    var cameraControl: Camera? by remember { mutableStateOf(null) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Toggle torch on cameraControl
    LaunchedEffect(isTorchOn, cameraControl) {
        cameraControl?.cameraControl?.enableTorch(isTorchOn)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScannerBackground)
    ) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    val cameraExecutor = Executors.newSingleThreadExecutor()

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        val cameraSelector = if (isBackCamera) {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        } else {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(
                                    cameraExecutor,
                                    BarcodeAnalyzer { qrToken ->
                                        viewModel.onQrCodeScanned(qrToken)
                                    }
                                )
                            }

                        try {
                            cameraProvider.unbindAll()
                            cameraControl = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            // Camera binding failure
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                update = {
                    // Update camera selector on camera switch
                },
                modifier = Modifier.fillMaxSize()
            )

            // Scanning Viewfinder Overlay with Animated Laser Line
            ScannerOverlay()

            // Top Bar Controls (Back, Flash, Flip)
            CameraControls(
                isTorchOn = isTorchOn,
                onTorchToggle = viewModel::toggleTorch,
                onCameraSwitch = viewModel::switchCamera,
                onBackClick = onBackClick,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Slide-up Feedback Banner at Bottom matching reference mockup
            ScanFeedbackBanner(
                scanResult = scanResult,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        } else {
            // Permission request fallback view
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .align(Alignment.Center)
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MonochromeWhite,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Checkd needs camera access to scan attendee QR codes quickly and continuously.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MonochromeWhite.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MonochromeWhite,
                        contentColor = MonochromeBlack
                    )
                ) {
                    Text(text = "Grant Permission", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
