@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.andiosic.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import com.example.andiosic.ConfigManager
import com.example.andiosic.GlobalHelper
import com.example.andiosic.api.user.loginUser
import com.example.andiosic.dto.LoginQuery
import kotlinx.coroutines.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scope = rememberCoroutineScope()
            var openDialog = remember {mutableStateOf(false)}
            val activity = this
            val snackbarHostState = remember { SnackbarHostState() }
            val isSuccess = remember {
                mutableStateOf(false)
            }
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                floatingActionButton = { },
                content = { innerPadding ->
                    LazyColumn(
                        contentPadding = innerPadding,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isSuccess.value) {
                            item {
                                Text(text = "Hi, ${ConfigManager.username}.", fontSize = 20.sp)
                            }
                            item {
                                Text(text = "Welcome to Diosic Android!", fontSize = 30.sp)
                            }
                        }
                        else {
                            item {
                                Text(text = "Please login first", fontSize = 40.sp)
                            }
                            item {
                                ConfigField("Server address", "serverAddress")
                            }
                            item {
                                ConfigField("Username", "username")
                            }
                            item {
                                ConfigField("Password", "password")
                            }
                            item {
                                Button(onClick = {
                                    scope.launch {
                                        try {
                                            if (saveAndLogin()) {
                                                isSuccess.value = true
                                                val job = launch {
                                                    snackbarHostState.showSnackbar("Login success", duration = SnackbarDuration.Indefinite)
                                                }
                                                delay(1500)
                                                job.cancel()
                                                val intent = Intent()
                                                intent.putExtra("isLogin", true)
                                                setResult(Activity.RESULT_OK, intent)
                                                activity.finish()
                                            }
                                            else {
                                                openDialog.value = true
                                            }
                                        }
                                        catch (e: Exception) {
                                            Log.e("LoginActivity", e.message.toString())
                                            openDialog.value = true
                                        }
                                    }

                                }) {
                                    Text("Save and login now")
                                }
                            }
                            item {
                                if (openDialog.value) {
                                    AlertDialog(onDismissRequest = {
                                        openDialog.value = false
                                    },
                                        title = {
                                            Text("Can't login!")
                                        },
                                        text = {
                                            Text("When login account, an error occurred.")
                                        },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                openDialog.value = false
                                            }) {
                                                Text("Ok")
                                            }
                                        })
                                }
                            }
                        }
                    }
                },
            )
        }
    }

    private suspend fun saveAndLogin(): Boolean {
        var q = LoginQuery()
        q.username = ConfigManager.username.toString()
        q.password = ConfigManager.password.toString()
        return loginUser(q)
    }
}

@Composable
fun ConfigField(label: String, configKey: String) {
    var text by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(GlobalHelper.getSharedPreferences().getString("config.$configKey", "").toString(), TextRange(0, 7)))
    }

    TextField(
        value = text,
        onValueChange = {
            text = it
            val pref = GlobalHelper.getSharedPreferences()
            with(pref.edit()) {
                putString("config.$configKey", text.text)
                apply()
            }
        },
        singleLine = true,
        label = { Text(label) }
    )
}