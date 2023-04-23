package com.example.andiosic.musicservice

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.example.andiosic.dto.MediaInfo
import kotlinx.coroutines.*

object MusicServiceConnection {
    private val serviceJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediaBrowser: MediaBrowserCompat
    private lateinit var mediaController: MediaControllerCompat
    private val transportControls get() = mediaController.transportControls

    var initialized = false; private set
    private val _playbackState = mutableStateOf<PlaybackStateCompat?>(null)
    val playbackState: State<PlaybackStateCompat?> = _playbackState
    val isStopped = mutableStateOf(false)
    var isRepeat = mutableStateOf(false)

    fun seekTo(duration: Long) {
        mediaController.transportControls?.seekTo(duration)
    }

    fun connect(context: Context) {
        mediaBrowser = MediaBrowserCompat(
            context,
            ComponentName(context, MusicService::class.java),
            ConnectionCallback(context),
            null // optional Bundle
        )
        mediaBrowser.connect()
    }

    fun disconnect() {
        stop()
        mediaBrowser.disconnect()
    }

    fun playFromMediaId(mediaId: String) {
        Log.i("PlayerViewModel", "play media control")
        transportControls.playFromMediaId(mediaId, null)
        isStopped.value = false
    }

    fun toggleRepeat() {
        if (isRepeat.value) {
            transportControls.setRepeatMode(REPEAT_MODE_NONE)
            isRepeat.value = false
        }
        else {
            transportControls.setRepeatMode(REPEAT_MODE_ONE)
            isRepeat.value = true
        }
    }

    fun pause() = transportControls.pause()
    fun play() = transportControls.play()
    fun togglePlay() = if (playbackState.value?.isPlaying == true) pause() else play()
    fun stop() {
        transportControls.stop()
        isStopped.value = true
    }
    fun skipToNext() = transportControls.skipToNext()
    fun skipToPrevious() = transportControls.skipToPrevious()

    private class ConnectionCallback(private val context: Context): MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            Log.i("MediaBrowserCompat", "Connected!")
            // Get the token for the MediaSession
            mediaBrowser.sessionToken.also { token ->

                // Create a MediaControllerCompat
                mediaController = MediaControllerCompat(context, token).apply {
                    registerCallback(ControllerCallback())
                }
            }
            val parentId = mediaBrowser.root
            mediaBrowser.unsubscribe(parentId)
            mediaBrowser.subscribe(parentId, BrowserSubscriptionCallback())
            initialized = true
        }

        override fun onConnectionSuspended() {
            Log.e("PlayerViewModel", "The Service has crashed. Disable transport controls until it automatically reconnects")
        }

        override fun onConnectionFailed() {
            Log.e("PlayerViewModel", "The Service has refused our connection")
        }
    }

    private class ControllerCallback: MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            _playbackState.value = state
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            metadata?.let {meta->
                meta.description.mediaId?.let { id->
                    updateInformation(id)
                }
            }
        }

        private fun updateInformation(mediaId: String) {
            MediaDepository.updateInformation(
                mediaId,
                onUpdated = {
                    MediaDepository.currentMedia.value?.let { playFromMediaId(mediaId) }
                },
                onUpdatedError = { err ->
                    Log.e(
                        "MusicService",
                        "fetch more medias failed: ${err.message}\nWill retry after 5s."
                    )
                    scope.launch {
                        delay(5000L)
                        updateInformation(mediaId)
                    }
                })
        }
    }

    private class BrowserSubscriptionCallback: MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>,
        ) {
            //service发送回来的媒体数据集合
            for (child in children) {
                Log.i("PlayerViewModel", "onChildrenLoaded: ${child.description.title.toString()}")
            }
            //执行ui刷新

        }
    }
}
