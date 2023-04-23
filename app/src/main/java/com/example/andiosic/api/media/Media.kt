package com.example.andiosic.api.media

import android.util.Log
import com.example.andiosic.ConfigManager
import com.example.andiosic.GlobalHelper.fromJsonString
import com.example.andiosic.api.Api
import com.example.andiosic.api.await
import com.example.andiosic.dto.GetMediasQuery
import com.example.andiosic.dto.MediaInfo
import com.example.andiosic.dto.SearchMediaQuery
import com.example.andiosic.dto.SearchResult
import com.google.gson.Gson
import okhttp3.Request

fun getMediaFileUrl(id: String): String {
    return "${Api.baseUrl}/media_file/$id?auth=${ConfigManager.authorization}"
}

suspend fun getMediaInfo(id: String): MediaInfo? {
    val req = Request.Builder().url("${Api.baseUrl}/media_info/$id").build()

    val resp = Api.client.newCall(req).await()
    val body = resp.body?.string()
    if  (resp.isSuccessful && body != null) {
        val gson = Gson()
        return gson.fromJson(body, MediaInfo::class.java)
    }

    return null
}

suspend fun searchMedia(q: SearchMediaQuery): SearchResult<MediaInfo>? {
    val req = Request.Builder().url("${Api.baseUrl}/medias/search?index=${q.index}&limit=${q.limit}&content=${q.content}").build()

    val resp = Api.client.newCall(req).await()
    val body = resp.body?.string()
    if  (resp.isSuccessful && body != null) {
        val gson = Gson()
        return gson.fromJsonString(body)
    }

    return null
}

suspend fun getMedias(q: GetMediasQuery): List<MediaInfo> {
    val req = Request.Builder().url("${Api.baseUrl}/medias?index=${q.index}&limit=${q.limit}&source=${q.source}&filter=${q.filter}").build()

    val resp = Api.client.newCall(req).await()
    val body = resp.body?.string()
    if  (resp.isSuccessful && body != null) {
        val gson = Gson()
        return gson.fromJsonString(body)
    }
    else {
        Log.e("getMedias", "${req.url}\n${body.toString()}")
    }

    return listOf()
}