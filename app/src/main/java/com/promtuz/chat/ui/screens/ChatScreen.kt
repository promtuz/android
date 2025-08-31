package com.promtuz.chat.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.promtuz.chat.ui.theme.PromtuzTheme

@Composable
fun ChatScreen(userId: String, modifier: Modifier = Modifier) {
    PromtuzTheme {
        Scaffold { padding ->
            Box(modifier.padding(padding)) {
                Text("Chat with $userId")
            }
        }
    }
}