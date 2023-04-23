package com.example.andiosic.api.user

import com.example.andiosic.ConfigManager
import com.example.andiosic.GlobalHelper
import com.example.andiosic.api.Api
import com.example.andiosic.api.await
import com.example.andiosic.dto.LoginQuery
import com.example.andiosic.dto.LoginUser
import com.example.andiosic.dto.LogoutQuery
import com.example.andiosic.dto.UserInfo
import com.google.gson.Gson
import okhttp3.Request

suspend fun loginUser(query: LoginQuery): Boolean {
    val req = Request.Builder().url("${Api.baseUrl}/login?username=${query.username}&password=${query.password}").build()
    val response = Api.client.newCall(req).await()
    val body = response.body?.string()
    if  (response.isSuccessful && body != null) {
        val gson = Gson()
        val loginUser = gson.fromJson(body, LoginUser::class.java)
        if (loginUser != null) {
            val pref = GlobalHelper.getSharedPreferences()
            with (pref.edit()) {
                putString("config.authorization", loginUser.token)
                apply()
            }
            return true
        }
    }

    return false
}

suspend fun logoutUser(): Boolean {
    ConfigManager.authorization?.let {token->
        val req = Request.Builder().url("${Api.baseUrl}/logout?token=${token}").build()

        val resp = Api.client.newCall(req).await()
        if  (resp.isSuccessful && resp.body != null) {
            val gson = Gson()
            val result = gson.fromJson(resp.body.toString(), Boolean::class.java)
            if (result) {
                val pref = GlobalHelper.getSharedPreferences()
                with (pref.edit()) {
                    remove("config.authorization")
                    apply()
                }
                return true
            }
        }
    }

    return false
}

suspend fun getCurrentUser(): UserInfo? {
    val req = Request.Builder().url("${Api.baseUrl}/current_user").build()
    val response = Api.client.newCall(req).await()
    val body = response.body?.string()
    if  (response.isSuccessful && body != null) {
        val gson = Gson()
        return gson.fromJson(body, UserInfo::class.java)
    }

    return null
}