package com.promtuz.chat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.promtuz.chat.R
import com.promtuz.chat.ui.components.SimpleScreen
import com.promtuz.chat.utils.extensions.then
import com.promtuz.chat.utils.logs.AppLog
import com.promtuz.chat.utils.logs.AppLogger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogsScreen() {
    var wrappingText by remember { mutableStateOf(false) }

    SimpleScreen({ Text("App Logs") }, actions = {
        IconButton(
            {
                wrappingText = !wrappingText
            }, colors = IconButtonDefaults.iconButtonColors(
                containerColor = if (wrappingText) MaterialTheme.colorScheme.surfaceContainer else Color.Unspecified
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.i_wrap_text),
                contentDescription = "Wrap Text",
                Modifier,
                MaterialTheme.colorScheme.onSurface
            )
        }
    }) { padding ->
        val logs by AppLogger.logs.collectAsState()

//        SelectionContainer {
        LazyColumn(
            Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(8.dp, 10.dp)
                .let {
                    if (wrappingText) it.horizontalScroll(rememberScrollState())
                    else it
                },
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom),
            reverseLayout = true
        ) {
            items(logs, key = { it.hashCode() }) { log ->
                SelectionContainer {
                    LogEntry(log)
                }
            }
        }
//        }
    }
}

@Composable
fun LogEntry(log: AppLog) {
    val color = when (log.priority) {
        6 -> MaterialTheme.colorScheme.error
        5 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = prioLabel(log.priority),
                style = MaterialTheme.typography.labelSmall,
                color = color,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = formatTime(log.time),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            log.tag?.let {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "[$it]",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = log.message,
            style = MaterialTheme.typography.bodyMediumEmphasized,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        )

        log.t?.let {
            Text(
                text = it.stackTraceToString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
            )
        }
    }
}

fun prioLabel(p: Int) = when (p) {
    2 -> "V"
    3 -> "D"
    4 -> "I"
    5 -> "W"
    6 -> "E"
    else -> p.toString()
}

fun formatTime(ts: Long): String =
    SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH).format(Date(ts))