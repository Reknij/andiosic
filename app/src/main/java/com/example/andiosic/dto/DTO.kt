package com.example.andiosic.dto

import android.support.v4.media.MediaMetadataCompat
import com.example.andiosic.ConfigManager
import com.example.andiosic.api.Api
import com.example.andiosic.api.media.getMediaFileUrl


class ServerInfo {
    var version = "0.0.0"
    var author = "Unknown"
    var time_running = 0
}

class UserInfo {
    var username = "Unknown"
    var alias = "Unknown"
    var password = "Unknown"
    var is_admin = false
}

class MediaInfo {
    var id = "Unknown"
    var title = "Unknown"
    var album = "Unknown"
    var artist = "Unknown"
    var genre = "Unknown"
    var year = "Unknown"
    var library = "Unknown"
    private var cover : String? = null
    var categories = listOf<String>()
    var simple_rate: Int? = null
    var bit_depth: Byte? = null
    var audio_bitrate: Int? = null
    var overall_bitrate: Int? = null
    var channels: Byte? = null
    var duration_milliseconds: Long = 0

    fun getCoverUrl(): String? {
        if (this.cover != null) {
            if (this.cover!!.startsWith("/api")) return "${ConfigManager.serverAddress.toString()}${this.cover}?auth=${ConfigManager.authorization}"
            else return this.cover
        }
        return null
    }

    fun asMediaMetaCompat(): MediaMetadataCompat {
        var meta = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
            .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
        if (cover != null) {
            meta.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, cover)
        }
        try {
            val yearNum = year.toLong()
            meta.putLong(MediaMetadataCompat.METADATA_KEY_YEAR, yearNum)
        }
        catch (err: java.lang.NumberFormatException) {

        }
        return meta.build()
    }

    fun getFileUrl(): String = getMediaFileUrl(id)
    fun contains(value: String): Boolean {
        return title.contains(value) ||
                artist.contains(value) ||
                album.contains(value) ||
                genre.contains(value) ||
                year.contains(value) ||
                library.contains(value) ||
                categories.find {
                    it.contains(value)
                } != null
    }
}

class MediaSourceInfo {
    var title = "Unknown"
    var length = 0
}

class GetMediasQuery {
    var limit = 0
    var index = 0
    var filter =  "Unknown"
    var source = "Unknown"
}

class GetUsersQuery {
    var limit = 0
    var index = 0
}

class LoginQuery {
    var username = "Unknown"
    var password = "Unknown"
}

class LogoutQuery {
    var token = "Unknown"
}

class LoginUser {
    var current = UserInfo()
    var token = "Unknown"
}

class ToSetup {
    var admin = UserInfo()
}

class GetSourceContentQuery {
    var title = "Unknown"
}

class SearchMediaQuery {
    var content = ""
    var source: String? = null
    var filter: String? = null
    var index = 0
    var limit = 0
}

class SearchResult<T> {
    var content = listOf<T>()
    var length = 0
}

class SearchUserQuery {
    var content = "Unknown"
    var index = 0
    var limit = 0
}

class SetupInfo {
    var admin_required = false
    var guest_enable = false
    var guest_password_required = false
}