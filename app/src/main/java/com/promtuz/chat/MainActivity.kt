package com.promtuz.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.promtuz.chat.compositions.LocalHazeState
import com.promtuz.chat.ui.components.BlurredBars
import com.promtuz.chat.ui.components.BottomBar
import com.promtuz.chat.ui.components.HomeListItem
import com.promtuz.chat.ui.components.TopBar
import com.promtuz.chat.ui.theme.PromtuzTheme
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PromtuzTheme(dynamicColor = true) {
                val localHazeState = rememberHazeState()
                val hazeState = rememberHazeState()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(localHazeState),
                    topBar = { TopBar() },
                    bottomBar = { BottomBar() }
                ) { innerPadding ->
                    CompositionLocalProvider(LocalHazeState provides localHazeState) {
                        Box {
                            Column(Modifier.hazeSource(hazeState)) {
                                val listState = rememberLazyListState()

                                val chats = arrayOf(
                                    "Averal Purwar",
                                    "Aftab Shaikh",
                                    "Criminal",
                                    "Shaurya Ranjan",
                                    "Kabir",
                                    "Dynoxy",
                                    "Ankush",
                                    "Martin Trevolsky",
                                    "Lennox",
                                    "Madhav",
                                    "Ankush",
                                    "Martin Trevolsky",
                                    "Lennox",
                                    "Kabir",
                                    "Dynoxy",
                                    "Ankush",
                                    "Martin Trevolsky",
                                    "Lennox",
                                    "Madhav",
                                    "Ankush",
                                    "Martin Trevolsky",
                                    "Lennox",
                                )

                                LazyColumn(
                                    state = listState,
                                    contentPadding = PaddingValues(
                                        top = innerPadding.calculateTopPadding() + 24.dp,
                                        bottom = innerPadding.calculateBottomPadding() + 24.dp
                                    ),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(chats.size) { index ->
                                        val peer = chats[index];

                                        HomeListItem(peer)
                                    }
                                }
                            }

                            BlurredBars(
                                hazeState,
                                innerPadding.calculateTopPadding(),
                                Alignment.TopCenter
                            )
                            BlurredBars(
                                hazeState,
                                innerPadding.calculateBottomPadding(),
                                Alignment.BottomCenter
                            )
                        }
                    }
                }
            }
        }
    }
}