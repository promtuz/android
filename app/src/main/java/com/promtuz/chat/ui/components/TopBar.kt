package com.promtuz.chat.ui.components

//import com.promtuz.chat.data.remote.ConnectionStatus
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.promtuz.chat.R
import com.promtuz.chat.data.remote.QuicClient
import com.promtuz.chat.presentation.viewmodel.AppVM
import com.promtuz.chat.ui.constants.Buttonimations
import com.promtuz.chat.ui.constants.Tweens
import com.promtuz.chat.ui.text.calSansfamily
import com.promtuz.chat.ui.theme.gradientScrim
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import com.promtuz.chat.presentation.state.ConnectionState as CS


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(appViewModel: AppVM, quicClient: QuicClient = koinInject()) {
    val context = LocalContext.current
    val staticTitle = stringResource(R.string.app_name)
    val dynamicTitle = remember { MutableStateFlow(staticTitle) }
    var job by remember { mutableStateOf<Job?>(null) }
    val menuExpanded = remember { mutableStateOf(false) }

    LaunchedEffect(quicClient) {
        snapshotFlow { quicClient.status.value }.collect { newStatus ->
            dynamicTitle.emit(
                when (newStatus) {
                    CS.Idle -> staticTitle

                    CS.Connecting, CS.Failed, CS.Handshaking, CS.Reconnecting, CS.Resolving, CS.Offline -> context.getString(
                        newStatus.text
                    )

                    CS.Connected -> {
                        context.getString(newStatus.text).also {
                            job?.cancel()
                            job = launch {
                                delay(1200)
                                if (quicClient.status.value == CS.Connected) {
                                    dynamicTitle.emit(staticTitle)
                                }
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
            )
        },
        title = {
            AppBarDynamicTitle(dynamicTitle)

//            AnimatedContent(
//                dynamicTitle,
//                modifier = Modifier
//                    .fillMaxWidth(),
//                contentAlignment = Alignment.Center,
//                transitionSpec = {
//                    (slideInVertically(
//                        initialOffsetY = { fullHeight -> fullHeight },
//                        animationSpec = Tweens.microInteraction(300)
//                    ) + fadeIn(Tweens.microInteraction(300))) togetherWith (slideOutVertically(
//                        targetOffsetY = { fullHeight -> -fullHeight },
//                        animationSpec = Tweens.microInteraction(300)
//                    ) + fadeOut(Tweens.microInteraction(300)))
//                }) { text ->
//                Text(
//                    text,
//                    fontFamily = calSansfamily,
//                    fontSize = 26.sp,
//                    modifier = Modifier.graphicsLayer { // Allow overflow
//                        clip = false
//                    })
//            }
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