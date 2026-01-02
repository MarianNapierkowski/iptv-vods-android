package com.streamviewer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.streamviewer.api.NetworkClient
import com.streamviewer.data.LoginRequest
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var serverUrl by remember { mutableStateOf("http://192.168.0.177:5000") }
    var username by remember { mutableStateOf("zko4dqjgmg") }
    var password by remember { mutableStateOf("53f3cd892599") }
    var xtreamUrl by remember { mutableStateOf("http://line.smurfmarkt.tv") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Stream Viewer Login", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = serverUrl,
            onValueChange = { serverUrl = it },
            label = { Text("Server URL (http://ip:5000)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = xtreamUrl,
            onValueChange = { xtreamUrl = it },
            label = { Text("Xtream Provider URL") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        try {
                            NetworkClient.baseUrl = serverUrl
                            NetworkClient.username = username
                            NetworkClient.password = password
                            NetworkClient.xtreamUrl = xtreamUrl
                            NetworkClient.resetApi() // Rebuild Retrofit with new Base URL

                            val api = NetworkClient.getApi()
                            val request = LoginRequest(username, password, xtreamUrl)
                            val response = api.login(request)

                            if (response.isSuccessful && response.body()?.success == true) {
                                onLoginSuccess()
                            } else {
                                errorMessage = response.body()?.error ?: "Login failed: ${response.code()}"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
    }
}
