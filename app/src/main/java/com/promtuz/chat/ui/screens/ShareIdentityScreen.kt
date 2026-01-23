package com.promtuz.chat.ui.screens

import android.content.Intent
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.promtuz.chat.R
import com.promtuz.chat.presentation.viewmodel.ShareIdentityVM
import com.promtuz.chat.ui.activities.QrScanner
import com.promtuz.chat.ui.components.BackTopBar
import com.promtuz.chat.ui.components.IdentityQrCode
import dev.shreyaspatil.capturable.capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShareIdentityScreen(
    viewModel: ShareIdentityVM
) {
    val captureController = rememberCaptureController()
    val colors = MaterialTheme.colorScheme

    Scaffold(
        topBar = { BackTopBar("Share Identity") }) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.background),
        ) {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(48.dp, Alignment.CenterVertically)
            ) {
                val identityRequest by viewModel.identityRequest.collectAsState()

                if (identityRequest != null) {
                    val identityRequest = identityRequest!!
                    Dialog(onDismissRequest = { viewModel.rejectRequest() }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Text(
                                text = identityRequest.name,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .wrapContentSize(Alignment.Center),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    Box(
                        Modifier
                            .wrapContentSize()
                            .align(Alignment.CenterHorizontally)
                            .capturable(captureController)
                    ) {
                        IdentityQrCode(viewModel.qrData.collectAsState())
                    }
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ScanQRButton()
                    }
                }
            }
        }
    }
}


@Composable
private fun ColumnScope.ShareQRButton(modifier: Modifier = Modifier, onShare: () -> Unit) {
    Button(
        onShare,
        modifier = modifier
            .fillMaxWidth(0.8f)
            .align(Alignment.CenterHorizontally),
    ) {
        Text(
            "Share QR Code",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLargeEmphasized.copy(fontSize = MaterialTheme.typography.labelLargeEmphasized.fontSize),
        )
    }
}


@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
private fun ColumnScope.ScanQRButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Button(
        {
            context.startActivity(Intent(context, QrScanner::class.java))
        },
        modifier = modifier
            .fillMaxWidth(0.8f)
            .align(Alignment.CenterHorizontally),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.i_qr_code_scanner), "QR Code Scanner Icon"
            )

            Text(
                "Scan QR Code",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLargeEmphasized.copy(fontSize = MaterialTheme.typography.labelLargeEmphasized.fontSize),
            )
        }
    }
}