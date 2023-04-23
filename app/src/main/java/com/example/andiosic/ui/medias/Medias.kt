@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.andiosic.ui.medias

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.andiosic.api.media.getMedias
import com.example.andiosic.api.media.searchMedia
import com.example.andiosic.dto.GetMediasQuery
import com.example.andiosic.dto.MediaInfo
import com.example.andiosic.dto.SearchMediaQuery
import com.example.andiosic.musicservice.MediaDepository
import com.example.andiosic.ui.components.ErrorPage
import com.example.andiosic.ui.components.LoadingPage
import com.example.andiosic.ui.components.SearchBox
import com.example.andiosic.ui.player.Player
import com.example.andiosic.util.GET_MEDIAS_LIMIT
import com.example.andiosic.util.MEDIA_ID_EXTRA
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Medias : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MediaDepository.currentMediaSourceInfo.value?.let { sourceInfo ->
                val scope = rememberCoroutineScope()
                var mediasQuery: GetMediasQuery? = null
                var medias = remember {
                    mutableStateListOf<MediaInfo>()
                }
                var isError by remember {
                    mutableStateOf(false)
                }
                var isLoading by remember {
                    mutableStateOf(false)
                }
                var isSearching by remember {
                    mutableStateOf(false)
                }
                var lock by remember {
                    mutableStateOf(ReentrantLock())
                }
                var searchMediaQuery by remember {
                    mutableStateOf(SearchMediaQuery())
                }
                var activeScroll by remember {
                    mutableStateOf(LocalDateTime.now())
                }
                val initMedias: suspend ()->Unit = {
                    isLoading = true

                    val q = GetMediasQuery().apply {
                        source = sourceInfo.type
                        filter = sourceInfo.info.title
                        index = 0
                        limit = GET_MEDIAS_LIMIT
                    }
                    try {
                        medias.clear()
                        medias.addAll(getMedias(q))
                        mediasQuery = q
                        isLoading = false
                    } catch (e: Exception) {
                        Log.e("MediasActivity", e.message.toString())
                        isError = true
                    }
                }
                LaunchedEffect(key1 = Unit) {
                    scope.launch {
                        initMedias()
                    }
                }

                Scaffold(topBar = {
                    TopAppBar(title = {
                        Text(
                            sourceInfo.info.title, maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }, navigationIcon = {
                        IconButton(onClick = { this.finish() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "back button"
                            )
                        }
                    })
                }, floatingActionButton = {
                    FilledTonalButton(onClick = {
                        activeScroll = LocalDateTime.now()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowUp,
                            contentDescription = "To top button"
                        )
                    }
                }) { paddingValues ->
                    ErrorPage(
                        visible = isError,
                        description = "Get medias failed. Please ensure your network is working.",
                        paddingValues = paddingValues
                    ) {
                        FilledTonalIconButton(onClick = {
                            isError = false

                            scope.launch {
                                initMedias()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "refresh button"
                            )
                        }
                    }
                    LoadingPage(
                        visible = isLoading,
                        description = "Loading the medias...",
                        paddingValues = paddingValues
                    )
                    MediaList(visible = !isLoading && !isError,
                        medias = medias,
                        paddingValues = paddingValues,
                        scrollIfChanged = activeScroll,
                        scrollToIndex = 0,
                        header = {
                            SearchBox(value = searchMediaQuery.content,
                                searchButtonEnabled = searchMediaQuery.content.isNotBlank(),
                                label = "Search media",
                                onValueChange = {
                                    searchMediaQuery = SearchMediaQuery().apply {
                                        content = it
                                    }
                                    if (it.isNullOrBlank()) {
                                        isSearching = false
                                        MediaDepository.autoLoadMoreMedia = true
                                        scope.launch {
                                            initMedias()
                                        }
                                    }
                                },
                                onClick = {
                                    MediaDepository.currentMediaSourceInfo.value?.let { s ->
                                        scope.launch {
                                            Log.i("MediasActivity", "Searching medias...")
                                            isLoading = true
                                            isSearching = true
                                            MediaDepository.autoLoadMoreMedia = false
                                            searchMediaQuery.also {
                                                it.index = 0
                                                it.limit = GET_MEDIAS_LIMIT
                                                it.source = s.type
                                                it.filter = s.info.title
                                            }
                                            val result = searchMedia(searchMediaQuery)
                                            if (result != null) {
                                                medias.clear()
                                                medias.addAll(result.content)
                                            } else Log.i(
                                                "MediasActivity", "Result response is null!"
                                            )
                                            isLoading = false
                                        }
                                    }
                                })
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                FilledTonalButton(onClick = {
                                    scope.launch {
                                        val toPlayMedias = getAllMediaInfo(sourceInfo)
                                        if (toPlayMedias.isNotEmpty()) startPlayerAndPlay(
                                            this@Medias, toPlayMedias, toPlayMedias.first()
                                        )
                                    }
                                }) {
                                    Text(text = "Play all")
                                }
                                Button(onClick = {
                                    scope.launch {
                                        val toPlayMedias = getAllMediaInfo(sourceInfo).shuffled()
                                        if (toPlayMedias.isNotEmpty()) startPlayerAndPlay(
                                            this@Medias, toPlayMedias, toPlayMedias.first()
                                        )
                                    }
                                }, modifier = Modifier.padding(start = 10.dp)) {
                                    Text(text = "Shuffle play")
                                }
                            }
                            Row {
                                Text(
                                    text = "Have ${sourceInfo.info.length} media. ${medias.count()} medias loaded.",
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.LightGray,
                                    modifier = Modifier.padding(start = 15.dp),
                                )
                            }
                        },
                        mediaClicked = {
                            startPlayerAndPlay(this@Medias, medias, it)
                        },
                        toListEnd = {
                            if (!isSearching) mediasQuery?.let {
                                if (!lock.isLocked) lock.withLock {
                                    scope.launch {
                                        try {
                                            if (medias.count() < sourceInfo.info.length) {
                                                it.index++
                                                val m = getMedias(it)
                                                medias.addAll(m)
                                            }
                                        } catch (e: Exception) {
                                            Log.e(packageName, e.message.toString())
                                            isError = true
                                        }
                                    }
                                }
                            }
                            else searchMediaQuery.also {
                                if (!lock.isLocked) lock.withLock {
                                    scope.launch {
                                        try {
                                            if (medias.count() < sourceInfo.info.length) {
                                                it.index++
                                                val result = searchMedia(it)
                                                if (result != null) {
                                                    medias.addAll(result.content)
                                                } else Log.i(
                                                    "MediasActivity",
                                                    "To end list update failed because result response is null"
                                                )
                                            }
                                        } catch (e: Exception) {
                                            Log.e(packageName, e.message.toString())
                                            isError = true
                                        }
                                    }
                                }
                            }
                        })
                }
            }
        }
    }
}

@Synchronized
fun startPlayerAndPlay(context: Context, medias: List<MediaInfo>, toPlay: MediaInfo) {
    Log.i("MediasActivity", "medias length: ${medias.count()}")
    MediaDepository.resetMedias(medias)
    val intent = Intent(context, Player::class.java)
    intent.putExtra(MEDIA_ID_EXTRA, toPlay.id)
    context.startActivity(intent)
}

@Synchronized
suspend fun getAllMediaInfo(sourceInfo: MediaDepository.MediaSourceInfoWithType): List<MediaInfo> {
    val q = GetMediasQuery().apply {
        source = sourceInfo.type
        filter = sourceInfo.info.title
        index = 0
        limit = sourceInfo.info.length
    }
    return getMedias(q)
}