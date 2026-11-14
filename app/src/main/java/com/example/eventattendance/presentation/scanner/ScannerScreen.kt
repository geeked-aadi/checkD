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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import com.example.eventattendance.presentation.theme.statusColors
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
    val isBulkMode = viewModel.isBulkMode

    val pendingList by viewModel.pendingList.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val bulkSummary by viewModel.bulkSummary.collectAsStateWithLifecycle()
    val bulkHint by viewModel.bulkHint.collectAsStateWithLifecycle()

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
                        } catch (_: Exception) {}
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Viewfinder Overlay
            ScannerOverlay()

            // Top Camera Controls (Back, Flash, Flip)
            CameraControls(
                isTorchOn = isTorchOn,
                onTorchToggle = viewModel::toggleTorch,
                onCameraSwitch = viewModel::switchCamera,
                onBackClick = onBackClick,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Hint Banner for Bulk (e.g. "Already added")
            AnimatedVisibility(
                visible = bulkHint != null,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 90.dp, start = 20.dp, end = 20.dp)
            ) {
                bulkHint?.let { hint ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Text(
                            text = hint,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.statusColors.warning,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        )
                    }
                }
            }

            // Bottom Feedback Banner for single scan or errors
            ScanFeedbackBanner(
                scanResult = scanResult,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // Bulk Mode Bottom Queue Panel
            if (isBulkMode) {
                Card(
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Header: Title + Clear + Submit
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Bulk Scan Queue",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${pendingList.size} pending attendee${if (pendingList.size == 1) "" else "s"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (pendingList.isNotEmpty()) {
                                TextButton(
                                    onClick = viewModel::clearPendingList,
                                    enabled = !isSubmitting
                                ) {
                                    Text(
                                        text = "Clear",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))
                            }


                            Button(
                                onClick = viewModel::submitBulkAttendance,
                                enabled = pendingList.isNotEmpty() && !isSubmitting,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Submit (${pendingList.size})",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // List of Scanned Attendees in Queue
                        if (pendingList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 1.dp)

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 180.dp),
                                contentPadding = PaddingValues(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(
                                    items = pendingList,
                                    key = { it.pId }
                                ) { attendee ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = attendee.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            val sub = buildString {
                                                append(attendee.pId)
                                                val team = attendee.teamName ?: attendee.teamId
                                                if (!team.isNullOrBlank()) {
                                                    append(" • $team")
                                                }
                                            }
                                            Text(
                                                text = sub,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.removePendingAttendee(attendee.pId) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                                }
                            }
                        }
                    }
                }
            }

            // Bulk Summary Dialog
            bulkSummary?.let { summary ->
                AlertDialog(
                    onDismissRequest = viewModel::dismissBulkSummary,
                    title = {
                        Text(
                            text = "Attendance Summary",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "Successfully checked in: ${summary.markedCount}",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (summary.duplicateNames.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Already Checked In (${summary.duplicateNames.size}):",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.statusColors.warning
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = summary.duplicateNames.joinToString(", "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = viewModel::dismissBulkSummary,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("OK")
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }

        } else {
            // Camera Permission fallback
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
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MonochromeWhite,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "checkD needs camera access to scan attendee QR codes quickly and continuously.",
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
