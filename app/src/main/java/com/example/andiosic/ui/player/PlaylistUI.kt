package com.example.andiosic.ui.player

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.andiosic.api.media.searchMedia
import com.example.andiosic.dto.MediaInfo
import com.example.andiosic.dto.SearchMediaQuery
import com.example.andiosic.musicservice.MediaDepository
import com.example.andiosic.ui.components.SearchBox
import com.example.andiosic.ui.medias.MediaList
import com.example.andiosic.util.GET_MEDIAS_LIMIT
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun PlaylistUI(
    visible: Boolean,
    scrollToIndex: Int,
    mediaItemClicked: ((MediaInfo) -> Unit),
    closeClicked: (() -> Unit),
    modifier: Modifier = Modifier
) {
    var activeScroll by remember {
        mutableStateOf(LocalDateTime.now())
    }
    var searchContent by remember {
        mutableStateOf("")
    }

    var scrollToIndex by mutableStateOf(scrollToIndex)
    AnimatedVisibility(
        visible = visible, enter = scaleIn(), exit = scaleOut()
    ) {
        MediaDepository.currentMediaSourceInfo.value?.let { sourceInfo ->
            Scaffold(topBar = {
                TopAppBar(title = {
                    Text("Playlist")
                }, navigationIcon = {
                    IconButton(onClick = { closeClicked() }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close play list"
                        )
                    }
                })
            }, floatingActionButton = {
                FilledTonalButton(onClick = {
                    scrollToIndex = 0
                    activeScroll = LocalDateTime.now()
                }) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "To top button"
                    )
                }
            }) { paddingValues ->
                MediaDepository.medias.let { medias ->
                    MediaList(medias = medias,
                        visible = visible,
                        scrollToIndex = scrollToIndex,
                        scrollIfChanged = activeScroll,
                        header = {
                            SearchBox(value = searchContent,
                                label = "Search media",
                                onValueChange = {
                                    searchContent = it
                                },
                                onClick = {
                                    val index = MediaDepository.medias.indexOfFirst {
                                        it.contains(searchContent)
                                    }
                                    if (index != -1) {
                                        scrollToIndex = index
                                        activeScroll = LocalDateTime.now()
                                    }
                                })
                            Row {
                                Text(
                                    text = "Have ${medias.count()} medias to play.",
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.LightGray,
                                    modifier = Modifier.padding(start = 15.dp),
                                )
                            }
                        },
                        paddingValues = paddingValues,
                        mediaClicked = { mediaItemClicked(it) },
                        toListEnd = { /*TODO*/ })
                }
            }
            BackHandler() {
                closeClicked()
            }
        }
    }
}