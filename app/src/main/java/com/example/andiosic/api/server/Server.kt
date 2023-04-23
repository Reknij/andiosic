package com.example.andiosic.api.server

import com.example.andiosic.api.Api.baseUrl
import com.example.andiosic.api.Api.client
import com.example.andiosic.api.await
import com.example.andiosic.dto.ServerInfo
import com.example.andiosic.dto.SetupInfo
import com.google.gson.Gson
import okhttp3.Request

suspend fun setup_info(): SetupInfo? {
    val req = Request.Builder().url("$baseUrl/setup_info").build()

    val resp = client.newCall(req).await()
    val body = resp.body?.string()
    if  (resp.isSuccessful && body != null) {
        val gson = Gson()
        return gson.fromJson(body, SetupInfo::class.java)
    }

    return null
}

suspend fun scanLibraries(): Boolean {
    val req = Request.Builder().url("$baseUrl/scan_libraries").build()

    val resp = client.newCall(req).await()
    return resp.isSuccessful

    return false
}

suspend fun getServerInfo(): ServerInfo? {
    val req = Request.Builder().url("$baseUrl/info").build()

    val resp = client.newCall(req).await()
    val body = resp.body?.string()
    if  (resp.isSuccessful && body != null) {
        val gson = Gson()
        return gson.fromJson(body, ServerInfo::class.java)
    }

    return null
}