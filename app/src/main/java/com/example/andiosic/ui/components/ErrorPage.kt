package com.example.andiosic.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ErrorPage(visible: Boolean, title: String = "An error occurred.", description: String = "", paddingValues: PaddingValues = PaddingValues(10.dp), content: @Composable ()->Unit = {}) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(text = title, modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 15.dp))
            Text(text = description, modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 10.dp))

            content()
        }
    }
}