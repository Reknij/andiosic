@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)

package com.example.andiosic.ui.player

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.andiosic.R
import com.example.andiosic.musicservice.MediaDepository
import com.example.andiosic.musicservice.MusicServiceConnection
import com.example.andiosic.musicservice.isPlaying

@Composable
fun PlayerUI(viewModel: PlayerViewModel, backBtnClick: (() -> Unit), openPlayListClick: (()->Unit)) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Music detail",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = backBtnClick) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "back button"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(contentPadding = paddingValues) {
                MediaDepository.currentMedia.value?.let {media->
                    item {
                        Surface(
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(40.dp)
                        ) {
                            GlideImage(
                                model = if (media.getCoverUrl() != null) media.getCoverUrl() else R.drawable.default_cover,
                                contentDescription = "cover image",
                                alignment = Alignment.Center,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp)
                            )
                        }
                    }
                    item {
                        Text(
                            media.title,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(10.dp, 0.dp)
                        )
                    }
                    item {
                        Text(
                            media.artist,
                            modifier = Modifier.padding(10.dp, 0.dp)
                        )
                    }
                    item {
                        Slider(
                            modifier = Modifier.padding(10.dp, 0.dp).semantics {
                                contentDescription = "Localized Description"
                            },
                            value = viewModel.uiState.value.durationPassed.value.toFloat(),
                            onValueChange = {
                                viewModel.uiState.value.durationPassed.value = it.toLong()
                            },
                            valueRange = 0f..media.duration_milliseconds.toFloat(),
                            onValueChangeFinished = {
                                // launch some business logic update with the state you hold
                                // viewModel.updateSelectedSliderValue(sliderPosition)
                                MusicServiceConnection.seekTo(viewModel.uiState.value.durationPassed.value)
                            }
                        )
                    }
                    item {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .fillMaxWidth()
                        ) {
                            Text(text = viewModel.uiState.value.durationPassed.value.asFormatDuration())
                            Text(text = media.duration_milliseconds.asFormatDuration())
                        }
                    }
                    item {
                        Row(
                            horizontalArrangement = Arrangement.SpaceAround,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            IconButton(onClick = { MusicServiceConnection.toggleRepeat() }) {
                                Icon(
                                    painterResource(
                                        if (MusicServiceConnection.isRepeat.value) R.drawable.repeat_on_48px
                                        else R.drawable.repeat_48px
                                    ),
                                    contentDescription = "loop button"
                                )
                            }

                            IconButton(onClick = { MusicServiceConnection.skipToPrevious() }) {
                                Icon(
                                    painterResource(R.drawable.skip_previous_48px),
                                    contentDescription = "previous button"
                                )
                            }

                            PlayPauseButton()

                            IconButton(
                                enabled = !MusicServiceConnection.isStopped.value,
                                onClick = { MusicServiceConnection.stop() }) {
                                Icon(
                                    painterResource(R.drawable.stop_circle_48px),
                                    contentDescription = "stop button"
                                )
                            }

                            IconButton(onClick = { MusicServiceConnection.skipToNext() }) {
                                Icon(
                                    painterResource(R.drawable.skip_next_48px),
                                    contentDescription = "next button"
                                )
                            }

                            IconButton(onClick = {
                                openPlayListClick()
                            }) {
                                Icon(
                                    painterResource(R.drawable.playlist_play_48px),
                                    contentDescription = "music list button"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayPauseButton() {
    IconButton(onClick = {
        if (MusicServiceConnection.isStopped.value) {
            MediaDepository.currentMedia.value?.let {
                MusicServiceConnection.playFromMediaId(it.id)
                Log.i("PlayerUI", "play from media id when stopped")
            }
        } else {
            MusicServiceConnection.togglePlay()
        }
    }) {
        Icon(
            painterResource(
                if (MusicServiceConnection.playbackState.value?.isPlaying == true) R.drawable.pause_circle_48px
                else R.drawable.play_circle_48px
            ), contentDescription = "play button"
        )
    }
}

fun Long.asFormatDuration(): String {
    val thisAsSeconds = this / 1000
    val seconds = thisAsSeconds % 60
    val minutes = (thisAsSeconds - seconds) / 60

    return "$minutes:${seconds.toString().padStart(2, '0')}"
}