package com.example.andiosic.api.mediasource

import com.example.andiosic.GlobalHelper.fromJsonString
import com.example.andiosic.dto.GetSourceContentQuery
import com.example.andiosic.dto.MediaSourceInfo
import com.google.gson.Gson
import okhttp3.Request
import com.example.andiosic.api.Api
import com.example.andiosic.api.await

private suspend fun getSourcesInfo(sourcesTag: String): List<MediaSourceInfo> {
    val req = Request.Builder().url("${Api.baseUrl}/${sourcesTag}").build()

    val resp = Api.client.newCall(req).await()
    val body = resp.body?.string()
    if  (resp.isSuccessful && body != null) {
        val gson = Gson()
        return gson.fromJsonString(body)
    }

    return listOf()
}

private suspend fun getSourceInfo(sourceTag: String, query: GetSourceContentQuery): MediaSourceInfo? {
    val req = Request.Builder().url("${Api.baseUrl}/${sourceTag}_info?title=${query.title}").build()

    val resp = Api.client.newCall(req).await()
    val body = resp.body?.string()
    if  (resp.isSuccessful && body != null) {
        val gson = Gson()
        return gson.fromJson(body, MediaSourceInfo::class.java)
    }

    return null
}

suspend fun getLibraries(): List<MediaSourceInfo> {
    return getSourcesInfo("libraries")
}

suspend fun getLibrary(query: GetSourceContentQuery): MediaSourceInfo? {
    return getSourceInfo("library", query)
}

suspend fun getAlbums(): List<MediaSourceInfo> {
    return getSourcesInfo("albums")
}

suspend fun getAlbum(query: GetSourceContentQuery): MediaSourceInfo? {
    return getSourceInfo("album", query)
}

suspend fun getCategories(): List<MediaSourceInfo> {
    return getSourcesInfo("categories")
}

suspend fun getCategory(query: GetSourceContentQuery): MediaSourceInfo? {
    return getSourceInfo("category", query)
}

suspend fun getArtists(): List<MediaSourceInfo> {
    return getSourcesInfo("artists")
}

suspend fun getArtist(query: GetSourceContentQuery): MediaSourceInfo? {
    return getSourceInfo("artist", query)
}

suspend fun getGenres(): List<MediaSourceInfo> {
    return getSourcesInfo("genres")
}

suspend fun getGenre(query: GetSourceContentQuery): MediaSourceInfo? {
    return getSourceInfo("genre", query)
}

suspend fun getYears(): List<MediaSourceInfo> {
    return getSourcesInfo("years")
}

suspend fun getYear(query: GetSourceContentQuery): MediaSourceInfo? {
    return getSourceInfo("year", query)
}