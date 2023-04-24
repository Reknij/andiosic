package com.example.andiosic.musicservice

import android.app.PendingIntent
import android.graphics.Bitmap
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.andiosic.R
import com.example.andiosic.musicservice.callbacks.MusicNotificationListener
import com.example.andiosic.util.CHANNEL_ID
import com.example.andiosic.util.NOTIFICATION_ID
import com.example.andiosic.util.loadImageBitmap
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class MusicNotificationManager(
    private val musicService: MusicService,
    sessionToken: MediaSessionCompat.Token
) {

    private val mediaController = MediaControllerCompat(musicService, sessionToken)
    private val cacheBitmap = HashMap<String, Bitmap>()

    private val notificationManager = PlayerNotificationManager.Builder(
        musicService, NOTIFICATION_ID, CHANNEL_ID
    ).apply {

        setChannelNameResourceId(R.string.notification_channel_name)
        setChannelDescriptionResourceId(R.string.notification_channel_description)
        setMediaDescriptionAdapter(MusicMediaDescriptionAdapter(mediaController))
        setNotificationListener(MusicNotificationListener(musicService))
    }.build().apply {
        setMediaSessionToken(sessionToken)
    }

    fun showNotification(player: Player) {
        notificationManager.setPlayer(player)
    }

    private inner class MusicMediaDescriptionAdapter(private val mediaController: MediaControllerCompat) :
        PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): CharSequence {
            return mediaController.metadata.description.title.toString()
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return mediaController.sessionActivity
        }

        override fun getCurrentContentText(player: Player): CharSequence {
            return mediaController.metadata.description.subtitle.toString()
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            if (cacheBitmap.count() > 20) {
                cacheBitmap.clear() // clear cache if more than 20
            }
            val coverUrl = mediaController.metadata.description.iconUri // instead MediaDepository.currentMedia.value?.getCoverUrl()
            val coverUrlString = coverUrl.toString()

            cacheBitmap[coverUrlString]?.let {
                Log.i("MusicNotificationManager", "use cache.")
                return it
            }
            musicService.loadImageBitmap(if (coverUrl != null) coverUrlString else R.drawable.default_cover) {
                Log.i("MusicNotificationManager", "load bitmap.")

                cacheBitmap[coverUrlString] = it
                callback.onBitmap(it)
            }

            return null
        }
    }
}