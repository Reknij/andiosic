package com.example.andiosic.ui.medias

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.andiosic.R
import com.example.andiosic.api.media.getMedias
import com.example.andiosic.dto.GetMediasQuery
import com.example.andiosic.dto.MediaInfo
import com.example.andiosic.dto.MediaSourceInfo
import com.example.andiosic.musicservice.MediaDepository
import com.example.andiosic.musicservice.MusicServiceConnection
import com.example.andiosic.musicservice.isPlaying
import com.example.andiosic.ui.player.Player
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun MediaList(
    visible: Boolean,
    medias: List<MediaInfo>,
    paddingValues: PaddingValues,
    mediaClicked: (MediaInfo) -> Unit,
    toListEnd: () -> Unit,
    scrollToIndex: Int = -1,
    scrollAnimate: Boolean = false,
    header: @Composable () -> Unit = {},
    footer: @Composable () -> Unit = {},
    modifier: Modifier = Modifier
) = MediaList(visible, medias, paddingValues, mediaClicked, toListEnd, null, scrollToIndex, scrollAnimate, header, footer, modifier)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun<T: Any> MediaList(
    visible: Boolean,
    medias: List<MediaInfo>,
    paddingValues: PaddingValues,
    mediaClicked: (MediaInfo) -> Unit,
    toListEnd: () -> Unit,
    scrollIfChanged: T? = null,
    scrollToIndex: Int = -1,
    scrollAnimate: Boolean = false,
    header: @Composable () -> Unit = {},
    footer: @Composable () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible, enter = fadeIn(), exit = fadeOut()
    ) {
        val listState = rememberLazyListState()
        LazyColumn(contentPadding = paddingValues, state = listState) {
            item {
                header()
            }
            items(medias) { media ->
                ListItem(headlineText = {
                    Text(
                        media.title,
                        color = if (MediaDepository.currentMedia.value?.id == media.id && MusicServiceConnection.playbackState.value?.isPlaying == true) Color.Blue
                        else Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }, supportingText = {
                    Text(media.artist)
                }, overlineText = {
                    Text(media.album)
                }, leadingContent = {
                    GlideImage(
                        model = if (media.getCoverUrl() != null) media.getCoverUrl() else R.drawable.default_cover,
                        contentDescription = "cover media",
                        modifier = Modifier.size(64.dp)
                    )
                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .clickable {
                        mediaClicked(media)
                    })
                Divider()
            }
            item {
                footer()
            }
        }
        listState.onBottomReached(toListEnd)

        if (scrollToIndex >= 0) LaunchedEffect(key1 = scrollIfChanged) {
            if (scrollAnimate) listState.animateScrollToItem(scrollToIndex)
            else listState.scrollToItem(scrollToIndex)
        }
    }
}

@Composable
fun LazyListState.onBottomReached(
    loadMore : () -> Unit
){
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf true

            lastVisibleItem.index == layoutInfo.totalItemsCount - 1
        }
    }

    // Convert the state into a cold flow and collect
    LaunchedEffect(shouldLoadMore){
        snapshotFlow { shouldLoadMore.value }
            .collect {
                // if should load more, then invoke loadMore
                if (it) loadMore()
            }
    }
}