package com.promtuz.chat.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.promtuz.chat.ui.components.BlurredBars
import com.promtuz.chat.ui.components.HomeListItem
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun HomeScreen(hazeState: HazeState, innerPadding: PaddingValues) {
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