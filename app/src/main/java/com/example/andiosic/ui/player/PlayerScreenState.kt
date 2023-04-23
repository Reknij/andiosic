package com.example.andiosic.ui.player

import androidx.compose.runtime.mutableStateOf
import com.example.andiosic.api.media.getMediaFileUrl
import com.example.andiosic.api.media.getMediaInfo
import com.example.andiosic.dto.MediaInfo
import com.example.andiosic.musicservice.MediaDepository
import com.example.andiosic.musicservice.MusicServiceConnection

class PlayerScreenState {
    var durationPassed = mutableStateOf<Long>(0)
}