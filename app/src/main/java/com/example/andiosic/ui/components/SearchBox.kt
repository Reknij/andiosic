@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.andiosic.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchBox(value: String, label: String, onValueChange: (String)->Unit, onClick: ()->Unit, searchButtonEnabled: Boolean = true) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        TextField(value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        IconButton(modifier = Modifier.padding(start = 5.dp), enabled = searchButtonEnabled, onClick = onClick) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "search button",
                modifier = Modifier.size(64.dp)
            )
        }
    }
}