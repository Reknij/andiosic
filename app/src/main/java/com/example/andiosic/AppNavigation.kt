package com.example.andiosic

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.andiosic.api.mediasource.*
import com.example.andiosic.dto.MediaSourceInfo
import com.example.andiosic.ui.medias.Medias
import kotlinx.coroutines.launch
import com.example.andiosic.GlobalHelper.Constant.Source
import com.example.andiosic.api.user.logoutUser
import com.example.andiosic.musicservice.MediaDepository
import com.example.andiosic.musicservice.MusicServiceConnection
import com.example.andiosic.ui.components.ErrorPage
import com.example.andiosic.ui.components.LoadingPage
import com.example.andiosic.ui.player.PlayPauseButton
import com.example.andiosic.ui.player.Player

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun AppNavigation(logoutClicked: (() -> Unit)) {
    val scope = rememberCoroutineScope()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val sourceTypeTitle = listOf("Libraries", "Albums", "Categories", "Artists", "Genres", "Years")
    val itemIcons = listOf(
        R.drawable.library_music_48px,
        R.drawable.album_48px,
        R.drawable.perm_media_48px,
        R.drawable.person_48px,
        R.drawable.hotel_class_48px,
        R.drawable.date_range_48px
    )
    val selectedItemIndex = remember { mutableStateOf(0) }
    val context = LocalContext.current

    var isLoading by remember {
        mutableStateOf(false)
    }
    var isError by remember {
        mutableStateOf(false)
    }
    var sources by remember {
        mutableStateOf(listOf<MediaSourceInfo>())
    }
    LaunchedEffect(key1 = Unit) {
        scope.launch {
            isLoading = true
            try {
                sources = getSourceWithItem(sourceTypeTitle[selectedItemIndex.value])
                isLoading = false
            } catch (e: Exception) {
                isError = true
            }
        }
    }
    ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
        ModalDrawerSheet {
            Spacer(Modifier.height(12.dp))
            sourceTypeTitle.forEachIndexed { index, text ->
                NavigationDrawerItem(icon = {
                    Icon(
                        painterResource(id = itemIcons[index]),
                        contentDescription = text,
                        modifier = Modifier.size(30.dp)
                    )
                },
                    label = { Text(text) },
                    selected = index == selectedItemIndex.value,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            isError = false
                            isLoading = true
                            try {
                                sources =
                                    getSourceWithItem(sourceTypeTitle[selectedItemIndex.value])
                                isLoading = false
                            } catch (e: Exception) {
                                isError = true
                            }
                        }
                        selectedItemIndex.value = index
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
            Divider()
            NavigationDrawerItem(icon = {
                Icon(
                    painterResource(id = R.drawable.logout_48px),
                    contentDescription = "logout",
                    modifier = Modifier.size(30.dp)
                )
            }, selected = false, label = { Text("Logout") }, onClick = {
                scope.launch {
                    drawerState.close()
                    logoutUser()
                    logoutClicked()
                }
            }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }, content = {
        Scaffold(topBar = {
            TopAppBar(title = { Text("Diosic Android") }, navigationIcon = {
                IconButton(onClick = {
                    scope.launch { drawerState.open() }
                }) {
                    Icon(
                        Icons.Default.Menu, contentDescription = "Localized description"
                    )
                }
            })
        }, floatingActionButtonPosition = FabPosition.Center, floatingActionButton = {
            if (!MusicServiceConnection.isStopped.value) MediaDepository.currentMedia.value?.let { media ->
                Surface(
                    onClick = {
                        context.startActivity(
                            Intent(
                                context, Player::class.java
                            )
                        )
                    },
                    tonalElevation = 4.dp,
                    shadowElevation = 4.dp,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(10.dp, 0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        GlideImage(
                            model = if (media.getCoverUrl() != null) media.getCoverUrl() else R.drawable.default_cover,
                            contentDescription = "cover media",
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = media.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(0.dp))
                            Text(
                                text = media.artist,
                                style = MaterialTheme.typography.labelSmall,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        PlayPauseButton()
                    }
                }
            }
        }, content = { innerPadding ->
            ErrorPage(
                visible = isError,
                description = "Get sources failed. Please ensure your network is working.",
                paddingValues = innerPadding
            ) {
                FilledTonalIconButton(onClick = {
                    isError = false
                    scope.launch {
                        isLoading = true
                        try {
                            sources = getSourceWithItem(sourceTypeTitle[selectedItemIndex.value])
                            isLoading = false
                        } catch (e: Exception) {
                            Log.e("AppNavigation", e.message.toString())
                            isError = true
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh, contentDescription = "refresh button"
                    )
                }
            }
            LoadingPage(
                visible = isLoading && !isError,
                description = "Loading the source...",
                paddingValues = innerPadding
            )
            if (!isLoading && !isError) LazyColumn(
                // consume insets as scaffold doesn't do it by default
                contentPadding = innerPadding
            ) {
                for (source in sources) {
                    item {
                        ListItem(headlineText = { Text(source.title) },
                            modifier = Modifier.clickable {
                                MediaDepository.currentMediaSourceInfo.value =
                                    MediaDepository.MediaSourceInfoWithType(
                                        GlobalHelper.parseSourceFromText(
                                            sourceTypeTitle[selectedItemIndex.value]
                                        ), source
                                    )
                                val intent = Intent(context, Medias::class.java)
                                context.startActivity(intent)
                            },
                            leadingContent = {
                                Icon(
                                    painterResource(id = itemIcons[selectedItemIndex.value]),
                                    contentDescription = "Localized description",
                                )
                            })
                        Divider()
                    }
                }
            }
        })
    })
}

suspend fun getSourceWithItem(item: String): List<MediaSourceInfo> {
    return when (GlobalHelper.parseSourceFromText(item)) {
        Source.LIBRARY -> getLibraries()
        Source.ALBUM -> getAlbums()
        Source.CATEGORY -> getCategories()
        Source.ARTIST -> getArtists()
        Source.GENRE -> getGenres()
        Source.YEAR -> getYears()
        else -> listOf()
    }
}