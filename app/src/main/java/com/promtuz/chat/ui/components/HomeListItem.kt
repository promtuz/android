package com.promtuz.chat.ui.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.promtuz.chat.compositions.LocalHazeState
import com.promtuz.chat.ui.theme.adjustLight
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun HomeListItem(
    peer: String
) {
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                enabled = true,
                indication = ripple(
                    color = adjustLight(
                        MaterialTheme.colorScheme.background, 0.3f
                    )
                ),
                onClick = {
                    // TODO
                },
                onLongClick = {
                    showBottomSheet = true
                },
                onClickLabel = null,
                onLongClickLabel = null,
                onDoubleClick = null,
                role = Role.Button,
                interactionSource = interactionSource,
                hapticFeedbackEnabled = true,
            )
            .indication(interactionSource, null)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(peer)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(peer, fontSize = 16.sp, lineHeight = 18.sp, fontWeight = FontWeight.W500)
                Text(
                    "Acha",
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(
                        0.75f
                    )
                )
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            dragHandle = null,
            sheetState = sheetState,
            containerColor = Color.Transparent,
            modifier = Modifier,
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
        ) {
            Column(
                Modifier
                    .hazeEffect(
                        state = LocalHazeState.current,
                        style = HazeMaterials.thick(MaterialTheme.colorScheme.background)
                    )
                    .padding(top = 12.dp + 6.dp, bottom = 18.dp + 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
//                Text(
//                    "@$peer",
//                    fontSize = 10.sp,
//                    fontWeight = FontWeight.W700,
//                    color = MaterialTheme.colorScheme.onBackground
//                )

                ModalSheetButton("Mark as Read") {}

                ModalSheetButton("Pin Chat") {}

                ModalSheetButton("Archive Chat") {}

                ModalSheetButton("Hide Chat") {}

                ModalSheetButton("Block User") {}

                ModalSheetButton("Copy User ID") {}

                ModalSheetButton("Copy Channel ID") {}

                /*
                * Mark as Read
                * Pin Chat
                * Archive Chat
                * Hide Chat
                * Block User
                * Copy User ID
                * Copy Channel ID
                * */
            }
        }
    }
}

@Composable
fun ModalSheetButton(
    text: String,
    onClickLabel: String? = null,
    onClick: (() -> Unit),
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(Modifier.padding(horizontal = 14.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    adjustLight(
                        MaterialTheme.colorScheme.background,
                        0.02f
                    )
                )
                .clickable(
                    enabled = true,
                    interactionSource = interactionSource,
                    indication = ripple(
                        color = adjustLight(
                            MaterialTheme.colorScheme.background,
                            0.25f
                        )
                    ),
                    role = Role.Button,
                    onClickLabel = onClickLabel,
                    onClick = onClick
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(text, fontSize = 16.sp)
        }
    }
}