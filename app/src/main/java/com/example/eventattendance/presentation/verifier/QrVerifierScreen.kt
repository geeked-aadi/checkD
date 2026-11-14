package com.example.eventattendance.presentation.verifier

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import com.example.eventattendance.R
import com.example.eventattendance.domain.model.CachedParticipant
import com.example.eventattendance.presentation.scanner.BarcodeAnalyzer
import com.example.eventattendance.presentation.scanner.components.ScannerOverlay
import com.example.eventattendance.presentation.theme.CheckdTheme
import com.example.eventattendance.presentation.theme.MonochromeBlack
import com.example.eventattendance.presentation.theme.MonochromeWhite
import com.example.eventattendance.presentation.theme.ScannerBackground
import com.example.eventattendance.presentation.theme.statusColors
import java.util.concurrent.Executors

@Composable
fun QrVerifierScreen(
    onBackClick: () -> Unit,
    viewModel: QrVerifierViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val verifierState by viewModel.verifierState.collectAsStateWithLifecycle()
    val isTorchOn by viewModel.isTorchOn.collectAsStateWithLifecycle()
    val isBackCamera by viewModel.isBackCamera.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val cachedCount by viewModel.totalCachedCount.collectAsStateWithLifecycle()

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

                        val preview = CameraPreview.Builder().build().also {
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
                        } catch (_: Exception) {}
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MonochromeWhite
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Camera access is needed to verify participant QR codes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MonochromeWhite.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = MonochromeWhite, contentColor = MonochromeBlack)
                ) {
                    Text("Grant Permission")
                }
            }
        }

        // Overlay with scanning frame and anim line
        ScannerOverlay(
            modifier = Modifier.fillMaxSize()
        )

        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MonochromeBlack.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MonochromeWhite
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "QR Verifier",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = MonochromeWhite
                )
                Text(
                    text = if (isRefreshing) "Refreshing participants..." else "Database: $cachedCount loaded",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = if (isRefreshing) MaterialTheme.statusColors.success else MonochromeWhite.copy(alpha = 0.7f)
                )
            }

            // Refresh roster from cloud button
            IconButton(
                onClick = { viewModel.refreshRoster() },
                enabled = !isRefreshing,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MonochromeBlack.copy(alpha = 0.6f))
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.statusColors.success,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Roster",
                        tint = MonochromeWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Flashlight button
            IconButton(
                onClick = { viewModel.toggleTorch() },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isTorchOn) MaterialTheme.statusColors.success.copy(alpha = 0.8f) else MonochromeBlack.copy(alpha = 0.6f))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_pixel_flash),
                    contentDescription = "Flashlight",
                    tint = MonochromeWhite,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Flip camera button
            IconButton(
                onClick = { viewModel.switchCamera() },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MonochromeBlack.copy(alpha = 0.6f))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_pixel_flip_camera),
                    contentDescription = "Flip Camera",
                    tint = MonochromeWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Bottom Result Card (Animated slide in)
        AnimatedVisibility(
            visible = verifierState !is VerifierState.Idle,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            when (val state = verifierState) {
                is VerifierState.Valid -> {
                    VerifiedParticipantCard(
                        participant = state.participant,
                        onDismiss = { viewModel.dismissResult() }
                    )
                }
                is VerifierState.Invalid -> {
                    UnregisteredParticipantCard(
                        token = state.scannedToken,
                        onDismiss = { viewModel.dismissResult() }
                    )
                }
                VerifierState.Idle -> {}
            }
        }
    }
}

@Composable
private fun VerifiedParticipantCard(
    participant: CachedParticipant,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.statusColors.success, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.statusColors.successContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.statusColors.onSuccessContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "VERIFIED PARTICIPANT",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.statusColors.onSuccessContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Participant Name & ID
            Text(
                text = participant.name?.takeIf { it.isNotBlank() } ?: "Participant ${participant.pId}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "ID: ${participant.pId}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(12.dp))

            // Team Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TEAM",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = participant.teamName?.takeIf { it.isNotBlank() } ?: participant.teamId?.takeIf { it.isNotBlank() } ?: "No Team Assigned",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (!participant.teamLeader.isNullOrBlank() && participant.teamLeader == participant.pId) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "Leader",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // SPOC Info if present
            if (!participant.spocName.isNullOrBlank() || !participant.spocId.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column {
                    Text(
                        text = "ASSIGNED SPOC",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = participant.spocName?.takeIf { it.isNotBlank() } ?: participant.spocId?.takeIf { it.isNotBlank() } ?: "SPOC",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!participant.spocPhonenumber.isNullOrBlank()) {
                        Text(
                            text = "Phone: ${participant.spocPhonenumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.statusColors.success,
                    contentColor = MaterialTheme.statusColors.onSuccess
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Scan Next QR",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun UnregisteredParticipantCard(
    token: String,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.statusColors.danger, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.statusColors.dangerContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.statusColors.onDangerContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "NOT REGISTERED",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.statusColors.onDangerContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Scanned Token: $token",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "This unique ID was not found in the participants database. It may belong to another event or an unregistered badge.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.statusColors.danger,
                    contentColor = MaterialTheme.statusColors.onDanger
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Try Again",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun VerifiedParticipantCardLightPreview() {
    CheckdTheme(darkTheme = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            VerifiedParticipantCard(
                participant = CachedParticipant(
                    pId = "P101",
                    name = "Alice Johnson",
                    teamName = "CyberKnights",
                    teamId = "T01",
                    teamLeader = "P101",
                    spocName = "Prof. Smith",
                    spocPhonenumber = "555-0199"
                ),
                onDismiss = {}
            )
            Spacer(modifier = Modifier.height(16.dp))
            UnregisteredParticipantCard(
                token = "UNKNOWN-TOKEN-1234",
                onDismiss = {}
            )
        }
    }
}

@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun VerifiedParticipantCardDarkPreview() {
    CheckdTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(16.dp)) {
            VerifiedParticipantCard(
                participant = CachedParticipant(
                    pId = "P101",
                    name = "Alice Johnson",
                    teamName = "CyberKnights",
                    teamId = "T01",
                    teamLeader = "P101",
                    spocName = "Prof. Smith",
                    spocPhonenumber = "555-0199"
                ),
                onDismiss = {}
            )
            Spacer(modifier = Modifier.height(16.dp))
            UnregisteredParticipantCard(
                token = "UNKNOWN-TOKEN-1234",
                onDismiss = {}
            )
        }
    }
}
