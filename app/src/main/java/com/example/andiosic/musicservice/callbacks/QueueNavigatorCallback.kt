package com.example.andiosic.musicservice.callbacks

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import com.example.andiosic.musicservice.MediaDepository
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator

class QueueNavigatorCallback(mediaSessionCompat: MediaSessionCompat): TimelineQueueNavigator(mediaSessionCompat) {
    override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
        return MediaDepository.mediasAsMediaMetadataCompat[windowIndex].description
    }
}