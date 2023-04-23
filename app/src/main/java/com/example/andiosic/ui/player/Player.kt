package com.example.andiosic.ui.player

import android.media.AudioManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.example.andiosic.musicservice.MediaDepository
import com.example.andiosic.musicservice.MusicServiceConnection
import com.example.andiosic.musicservice.isPlaying
import com.example.andiosic.util.MEDIA_ID_EXTRA

class Player : AppCompatActivity() {
    private var viewModel = PlayerViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var openPlaylist by remember {
                mutableStateOf(false)
            }
            LaunchedEffect(key1 = Unit) {
                setContent {
                    PlayerUI(viewModel, openPlayListClick = {
                        openPlaylist = true
                    }, backBtnClick = {
                        this@Player.finish()
                    })
                    PlaylistUI(visible = openPlaylist,
                        scrollToIndex = MediaDepository.currentMediaIndex,
                        mediaItemClicked = {
                            if (MediaDepository.currentMedia.value?.id != it.id || MusicServiceConnection.playbackState.value?.isPlaying == false) {
                                MusicServiceConnection.playFromMediaId(it.id)
                            }
                        },
                        closeClicked = { openPlaylist = false })
                }
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        val mediaId = intent.extras?.getString(MEDIA_ID_EXTRA)
        mediaId?.let {
            intent.removeExtra(MEDIA_ID_EXTRA)
            if (MediaDepository.currentMedia.value?.id != mediaId || MusicServiceConnection.playbackState.value?.isPlaying == false) {
                MusicServiceConnection.playFromMediaId(it)
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    public override fun onStop() {
        super.onStop()
    }
}