package com.example.andiosic.ui.player

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.andiosic.dto.MediaInfo
import com.example.andiosic.musicservice.MediaDepository
import com.example.andiosic.musicservice.MusicService
import com.example.andiosic.musicservice.MusicServiceConnection
import com.example.andiosic.musicservice.currentPlaybackPosition
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class PlayerViewModel: ViewModel() {
    @SuppressLint("RestrictedApi")
    private val lastPosition = flow<Long> {
        while (true) {
            if (MusicServiceConnection.initialized) {
                MusicServiceConnection.playbackState.value?.let {
                    emit(it.currentPlaybackPosition)
                }
            }
            delay(1000L)
        }
    }
    private val _uiState = mutableStateOf(PlayerScreenState())
    val uiState: State<PlayerScreenState> = _uiState

    init {
        collectCurrentDuration()
    }

    private fun collectCurrentDuration() = viewModelScope.launch {
        lastPosition.collectLatest {
            uiState.value.durationPassed.value = it
        }
    }
}