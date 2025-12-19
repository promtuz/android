package com.promtuz.chat.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.promtuz.chat.R
import com.promtuz.chat.navigation.Routes
import com.promtuz.chat.presentation.viewmodel.AppVM
import com.promtuz.chat.ui.theme.gradientScrim
import com.promtuz.core.API
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import com.promtuz.chat.presentation.state.ConnectionState as CS


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    appViewModel: AppVM,
    api: API = koinInject(),
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val staticTitle = stringResource(R.string.app_name)
    val dynamicTitle = remember { MutableStateFlow(staticTitle) }
    var job by remember { mutableStateOf<Job?>(null) }
    val menuExpanded = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        appViewModel.connStateFlow.collect { newStatus ->
            dynamicTitle.emit(
                when (newStatus) {
                    CS.Idle -> staticTitle
                    CS.Connecting, CS.Failed, CS.Handshaking, CS.Reconnecting, CS.Resolving, CS.NoInternet -> resources.getString(
                        newStatus.text
                    )

                    CS.Connected -> {
                        resources.getString(newStatus.text).also {
                            job?.cancel()
                            job = launch {
                                delay(1200)
                                // FIXME: add current connection status getter in `API`
//                                if (quicClient.status.value == CS.Connected) {
//                                    dynamicTitle.emit(staticTitle)
//                                }
                            }
                        }
                    }
                })
        }
    }

    TopAppBar(
        modifier = Modifier.background(gradientScrim()),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        navigationIcon = {
            Image(
                painterResource(R.drawable.logo_colored),
                contentDescription = "Promtuz App Logo",
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .width(32.dp)
                    .combinedClickable(
                        indication = null,
                        interactionSource = null,
                        onClick = {},
                        onDoubleClick = {
                            appViewModel.connection()
                        }
                    )
            )
        },
        title = {
            AppBarDynamicTitle(
                dynamicTitle,
                Modifier.combinedClickable(
                    enabled = true,
                    interactionSource = null,
                    indication = null,
                    onClick = {},
                    onLongClick = {
                        appViewModel.navigator.push(Routes.Logs)
                    })
            )
        },
        actions = {
            Box {
                IconButton({ menuExpanded.value = !menuExpanded.value }) {
                    DrawableIcon(R.drawable.i_ellipsis_vertical)
                }
                HomeMoreMenu(appViewModel, menuExpanded)
            }
        })
}