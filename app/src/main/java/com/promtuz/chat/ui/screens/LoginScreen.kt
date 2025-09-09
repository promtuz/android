package com.promtuz.chat.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.promtuz.chat.compositions.LocalBackStack
import com.promtuz.chat.data.remote.NetworkClient
import com.promtuz.chat.data.repository.AuthRepositoryImpl
import com.promtuz.chat.presentation.state.LoginField
import com.promtuz.chat.presentation.state.LoginStatus
import com.promtuz.chat.presentation.viewmodel.LoginViewModel
import com.promtuz.chat.presentation.viewmodel.LoginViewModelFactory
import com.promtuz.chat.ui.components.OutlinedFormElements
import com.promtuz.chat.ui.constants.Tweens
import com.promtuz.chat.ui.theme.PromtuzTheme
import com.promtuz.chat.ui.theme.adjustLight
import com.promtuz.chat.ui.theme.outlinedFormElementsColors

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier
) {
    val vm: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(AuthRepositoryImpl(NetworkClient()))
    )
    
    val state by vm.uiState
    val isTryingToLogin by remember { derivedStateOf { state.status == LoginStatus.Trying } }

    val focusManager = LocalFocusManager.current

    val backStack = LocalBackStack.current

    Box(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectTapGestures { focusManager.clearFocus() }
            }) {

        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center),
        ) {
            // TODO: Move Constants to Separate Files
            Text(
                "Welcome Back!",
                modifier = Modifier.padding(bottom = 6.dp),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(32.dp))

            OutlinedFormElements.TextField(
                value = state.username,
                onValueChange = { vm.onChange(LoginField.Username, it) },
                label = "Username",
                isError = state.errorText != null,
                enabled = !isTryingToLogin,
                readOnly = isTryingToLogin,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Next) }),
            )

            Spacer(Modifier.height(10.dp))

            OutlinedFormElements.TextField(
                value = state.password,
                onValueChange = { vm.onChange(LoginField.Password, it) },
                label = "Password",
                isError = state.errorText != null,
                enabled = !isTryingToLogin,
                readOnly = isTryingToLogin,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done, keyboardType = KeyboardType.Password
                ),
                visualTransformation = PasswordVisualTransformation('\u25CF')
            )

            Spacer(Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                    OutlinedFormElements.Checkbox(
                        state.rememberMe,
                        onCheckedChange = { vm.onChange(LoginField.Remember, it) },
                        enabled = !isTryingToLogin,
                        modifier = Modifier
                    )
                }
                Text(
                    "Remember me?",
                    fontSize = 14.sp,
                    color = outlinedFormElementsColors().focused.labelColor
                )
            }

            Spacer(Modifier.height(10.dp))

            val scope = rememberCoroutineScope()

            AnimatedContent(
                state.errorText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                transitionSpec = {
                    (slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight },
                        animationSpec = Tweens.microInteraction()
                    ) + fadeIn(Tweens.microInteraction())) togetherWith (slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight },
                        animationSpec = Tweens.microInteraction()
                    ) + fadeOut(Tweens.microInteraction()))
                }) { text ->

                Text(
                    text ?: "",
                    fontSize = 14.sp,
                    color = outlinedFormElementsColors().error.labelColor
                )

            }

            Button(
                {
                    vm.login()
                },
                Modifier.fillMaxWidth(),
            ) {
                AnimatedContent(
                    state.status,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RectangleShape),
                    contentAlignment = Alignment.Center,
                    transitionSpec = {
                        (slideInVertically(
                            initialOffsetY = { fullHeight -> fullHeight },
                            animationSpec = Tweens.microInteraction()
                        ) + fadeIn(Tweens.microInteraction())) togetherWith (slideOutVertically(
                            targetOffsetY = { fullHeight -> -fullHeight },
                            animationSpec = Tweens.microInteraction()
                        ) + fadeOut(Tweens.microInteraction()))
                    }) { status ->
                    Text(
                        status.text,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.W500,
                        fontSize = 16.sp,
                        modifier = Modifier.graphicsLayer { // Allow overflow
                            clip = false
                        }
                    )
                }
            }


            TextButton(
                {

                }, Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Text("Create an account", fontWeight = FontWeight.W500, fontSize = 16.sp)
            }
        }

        Row(
            modifier = Modifier.align(BiasAlignment(0f, 0.85f)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Rounded.Lock, "Lock", Modifier.size(16.dp), tint = adjustLight(
                    MaterialTheme.colorScheme.background, 0.6f
                )
            )

            Text(
                "End to End Encrypted",
                fontSize = 12.sp,
                fontWeight = FontWeight.W500,
                color = adjustLight(
                    MaterialTheme.colorScheme.background, 0.6f
                )
            )
        }
    }
}

@Composable
fun outlinedTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = adjustLight(
            MaterialTheme.colorScheme.background, 0.3f
        ), focusedBorderColor = adjustLight(
            MaterialTheme.colorScheme.background, 0.5f
        )
    )
}


@Composable
@Preview
fun LoginScreenPreview(modifier: Modifier = Modifier) {
    PromtuzTheme(darkTheme = true) {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            LoginScreen()
        }
    }
}