package com.promtuz.chat.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.promtuz.chat.R
import com.promtuz.chat.ui.theme.adjustLight


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomBar() {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background.copy(0f),
        contentColor = adjustLight(MaterialTheme.colorScheme.background, 0.4f),
    ) {
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = {
                Icon(
                    painter = painterResource(R.drawable.i_home),
                    contentDescription = "Home",
                )
            },
            label = {
                Text("Home")
            }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = {
                Icon(
                    painter = painterResource(R.drawable.i_friends),
                    contentDescription = "Friends",
                )
            },
            label = {
                Text("Friends")
            }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = {
                Icon(
                    painter = painterResource(R.drawable.i_phone),
                    contentDescription = "Calls",
                )
            },
            label = {
                Text("Calls")
            }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = {
                Icon(
                    painter = painterResource(R.drawable.i_settings),
                    contentDescription = "Settings",
                )
            },
            label = {
                Text("Settings")
            }
        )
    }
}