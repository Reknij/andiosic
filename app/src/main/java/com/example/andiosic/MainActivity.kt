package com.example.andiosic

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.andiosic.api.Api
import com.example.andiosic.api.user.getCurrentUser
import com.example.andiosic.api.user.logoutUser
import com.example.andiosic.musicservice.MusicServiceConnection
import com.example.andiosic.ui.components.ErrorPage
import com.example.andiosic.ui.login.LoginActivity
import com.example.andiosic.ui.player.Player
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var loginHandler: ((ActivityResult) -> Unit)? = null
    private val loginLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            loginHandler?.let { it(result) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalHelper.application = this.application
        if (!MusicServiceConnection.initialized) MusicServiceConnection.connect(this)

        setContent {
            var needLogin by remember {
                mutableStateOf(ConfigManager.serverAddress.isNullOrBlank() || ConfigManager.authorization.isNullOrBlank())
            }
            var isError by remember {
                mutableStateOf(false)
            }
            val scope = rememberCoroutineScope()
            LaunchedEffect(key1 = Unit) {
                if (!ConfigManager.authorization.isNullOrBlank()) {
                    try {
                        if (getCurrentUser() == null) {
                            needLogin = true
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "getCurrentUser failed: ${e.message}")
                        isError = true
                    }
                }
            }
            ErrorPage(
                visible = isError,
                description = "Get current user failed. Please ensure your network is working."
            ) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = {
                        isError = false
                        scope.launch {
                            if (!ConfigManager.authorization.isNullOrBlank()) {
                                try {
                                    if (getCurrentUser() == null) {
                                        needLogin = true
                                    }
                                } catch (e: Exception) {
                                    Log.e("MainActivity", "getCurrentUser failed: ${e.message}")
                                    isError = true
                                }
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh, contentDescription = "refresh button"
                        )
                    }
                    FilledTonalIconButton(onClick = {
                        isError = false
                        scope.launch {
                            try {
                                logoutUser()
                            }
                            catch (e: Exception) {
                                with(GlobalHelper.getSharedPreferences().edit()) {
                                    remove("config.authorization")
                                    apply()
                                }
                            }
                            needLogin = true
                        }
                    }) {
                        Icon(
                            painterResource(id = R.drawable.logout_48px),
                            contentDescription = "logout",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
            if (!isError) {
                if (needLogin) {
                    loginHandler = { result ->
                        if (result.data?.extras?.getBoolean("isLogin", false) == true) {
                            needLogin =
                                ConfigManager.serverAddress.isNullOrBlank() || ConfigManager.authorization.isNullOrBlank()
                        }
                        recreate()
                    }
                    loginLauncher.launch(Intent(this, LoginActivity::class.java))
                } else {
                    AppNavigation(logoutClicked = {
                        needLogin = true
                    })
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MusicServiceConnection.disconnect()
    }
}