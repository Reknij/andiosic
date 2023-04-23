package com.example.andiosic.musicservice

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import com.example.andiosic.api.media.getMedias
import com.example.andiosic.dto.GetMediasQuery
import com.example.andiosic.dto.MediaInfo
import com.example.andiosic.dto.MediaSourceInfo
import com.example.andiosic.util.GET_MEDIAS_LIMIT
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

object MediaDepository {
    private val depositoryJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + depositoryJob)
    var medias = mutableStateListOf<MediaInfo>()
    val mediasAsMediaItem: List<MediaBrowserCompat.MediaItem>
        get() = medias.map { getMediaItem(it) }
    val mediasAsMediaMetadataCompat: List<MediaMetadataCompat>
        get() = medias.map { it.asMediaMetaCompat() }

    var currentMedia = mutableStateOf<MediaInfo?>(null)
    var currentMediaSourceInfo = mutableStateOf<MediaSourceInfoWithType?>(null)

    val currentMediaIndex get() = medias.indexOfFirst { it.id == currentMedia.value?.id }
    var autoLoadMoreMedia = true
    private var fetching = false

    fun resetMedias(list: List<MediaInfo>) {
        medias.clear()
        medias.addAll(list)
    }

    private fun getMediaItem(info: MediaInfo) = MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder().apply {
            setMediaUri(info.getFileUrl().toUri())
            setTitle(info.title)
            setSubtitle(info.artist)
            setMediaId(info.id)
            setIconUri(info.getCoverUrl()?.toUri())
        }.build(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
    )

    fun asMediaSource(dataSourceFactory: DefaultDataSource.Factory): MediaSource {
        val mediaSources = medias.map {
            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(it.getFileUrl()))
        }
        return ConcatenatingMediaSource().apply { addMediaSources(mediaSources) }
    }

    @Synchronized
    fun updateInformation(
        mediaId: String?, onUpdated: () -> Unit, onUpdatedError: (Exception) -> Unit
    ) {
        if (autoLoadMoreMedia && !fetching) {
            fetching = true

            scope.launch {
                val index = medias.indexOfFirst { it.id == mediaId }
                if (index == -1) return@launch

                currentMedia.value = medias[index]
                val mediasLen = medias.count()

                Log.i("updateInformation", "medias total $mediasLen, current index $index")
                if (currentMedia.value != null) {
                    if (currentMediaSourceInfo.value == null) {
                        Log.w("MediaDepository", "currentMediaSourceInfo is null")
                        return@launch
                    }
                    currentMediaSourceInfo.value?.let { source ->
                        if (index == (mediasLen - 1) && mediasLen < source.info.length) {
                            Log.i(
                                "MediaDepository", "trying fetch more medias, now total $mediasLen"
                            )
                            val query = GetMediasQuery().apply {
                                this.source = source.type
                                this.filter = source.info.title
                                this.index = mediasLen / GET_MEDIAS_LIMIT
                                this.limit = GET_MEDIAS_LIMIT
                            }
                            try {
                                val moreMedias = getMedias(query)
                                Log.i("MediaDepository", "got more medias ${moreMedias.count()}")
                                medias.addAll(moreMedias)
                                Log.i("MediaDepository", "medias updated, total ${medias.count()}")
                                onUpdated()
                            } catch (e: Exception) {
                                onUpdatedError(e)
                            }
                        }
                    }
                }
                fetching = false
            }
        }
    }

    class MediaSourceInfoWithType(val type: String, val info: MediaSourceInfo)
}