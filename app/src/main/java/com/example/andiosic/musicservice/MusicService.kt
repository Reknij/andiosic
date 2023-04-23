package com.example.andiosic.musicservice

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import com.example.andiosic.dto.MediaInfo
import com.example.andiosic.musicservice.callbacks.PlayBackPreparerCallback
import com.example.andiosic.musicservice.callbacks.QueueNavigatorCallback
import com.example.andiosic.ui.player.Player
import com.example.andiosic.util.MEDIA_ROOT_ID
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.upstream.DefaultDataSource
import kotlinx.coroutines.*

class MusicService : MediaBrowserServiceCompat() {
    private lateinit var musicNotificationManager: MusicNotificationManager
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private val serviceJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + serviceJob)
    var isRunning = false

    override fun onCreate() {
        super.onCreate()
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.let {
            it.putExtra("toActivity", Player::class.qualifiedName)
            PendingIntent.getActivity(this, 0, it, FLAG_IMMUTABLE)
        }
        exoPlayer = ExoPlayer.Builder(this).build()
        mediaSession = MediaSessionCompat(this, "MY_MEDIA_SESSION").apply {
            setSessionToken(sessionToken)
            setSessionActivity(intent)
            isActive = true
        }
        mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
            setPlayer(exoPlayer)
            setPlaybackPreparer(PlayBackPreparerCallback {
                MediaDepository.currentMedia.value = it
                Log.i(packageName, "prepare and play media `${it.title}`")
                prepareMusic(true)
            })
            setQueueNavigator(QueueNavigatorCallback(mediaSession))
        }
        musicNotificationManager = MusicNotificationManager(this, mediaSession.sessionToken)

        musicNotificationManager.showNotification(exoPlayer)
    }

    private fun prepareMusic(
        playNow: Boolean,
        isUpdated: Boolean = false,
    ) {
        val songIndex =
            if (MediaDepository.currentMedia.value == null) 0
            else MediaDepository.currentMediaIndex

        exoPlayer.setMediaSource(MediaDepository.asMediaSource(DefaultDataSource.Factory(this)))
        exoPlayer.prepare()
        if (songIndex != -1) {
            if (isUpdated) exoPlayer.seekTo(songIndex, exoPlayer.currentPosition)
            else exoPlayer.seekTo(songIndex, 0L)
        }
        exoPlayer.playWhenReady = playNow
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        //  Browsing not allowed
        if (parentId != MEDIA_ROOT_ID) {
            result.sendResult(null)
            return
        }
        result.sendResult(MediaDepository.mediasAsMediaItem.toMutableList())
    }
}