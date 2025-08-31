package com.promtuz.chat.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.promtuz.chat.nativex.CoreBridge
import com.promtuz.chat.navigation.ScrollState
import com.promtuz.chat.ui.components.HomeListItem
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun HomeScreen(
    hazeState: HazeState,
    innerPadding: PaddingValues,
    scrollHandler: (scrollState: ScrollState) -> Unit
) {

    val coreBridge = remember { CoreBridge.getInstance() }

    Box {
        Column(Modifier.hazeSource(hazeState)) {
            val listState = rememberLazyListState()

            LaunchedEffect(listState) {
                snapshotFlow {
                    listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
                }.collect {
                    val layoutInfo = listState.layoutInfo
                    val visibleItemsInfo = layoutInfo.visibleItemsInfo

                    if (visibleItemsInfo.isEmpty()) {
                        scrollHandler(ScrollState(0, 0))
                    } else {
                        val firstVisibleItem = visibleItemsInfo.first()

                        val currentScroll = firstVisibleItem.offset
                        val totalItems = layoutInfo.totalItemsCount
                        val visibleItems = visibleItemsInfo.size
                        val averageItemHeight =
                            visibleItemsInfo.sumOf { it.size } / visibleItems
                        val estimatedTotalHeight = totalItems * averageItemHeight
                        val viewportHeight = layoutInfo.viewportSize.height
                        val maxScroll = (estimatedTotalHeight - viewportHeight).coerceAtLeast(0)

                        scrollHandler(ScrollState(currentScroll, maxScroll))
                    }
                }
            }

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
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                ),
                modifier = Modifier.fillMaxSize()
            ) {

                item {
                    Text(coreBridge.getStaticKey().toHexString(HexFormat.UpperCase))
                }

                items(chats.size) { index ->
                    val peer = chats[index];
                    HomeListItem(peer)
                }
            }
        }
    }
}