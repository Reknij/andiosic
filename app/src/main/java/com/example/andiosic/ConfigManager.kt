package com.example.andiosic

object ConfigManager {
    val serverAddress get() = GlobalHelper.getSharedPreferences().getString("config.serverAddress", null)
    val username get() = GlobalHelper.getSharedPreferences().getString("config.username", null)
    val password get() = GlobalHelper.getSharedPreferences().getString("config.password", null)
    val authorization get() = GlobalHelper.getSharedPreferences().getString("config.authorization", null)
}