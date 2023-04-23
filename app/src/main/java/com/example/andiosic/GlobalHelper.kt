package com.example.andiosic

import android.app.Application
import android.content.SharedPreferences
import androidx.compose.ui.text.toLowerCase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


object GlobalHelper {
    object Constant {
        object Source {
            const val LIBRARY = "library"
            const val ALBUM = "album"
            const val ARTIST = "artist"
            const val CATEGORY = "category"
            const val GENRE = "genre"
            const val YEAR = "year"
        }
    }
    lateinit var application: Application

    fun getSharedPreferences(): SharedPreferences {
        return application.getSharedPreferences("andiosic.global.preferences", 0)
    }

    fun parseSourceFromText(text: String): String {
        return when (text.lowercase()) {
            Constant.Source.LIBRARY, "libraries"-> Constant.Source.LIBRARY
            Constant.Source.ALBUM, "albums"-> Constant.Source.ALBUM
            Constant.Source.ARTIST, "artists"-> Constant.Source.ARTIST
            Constant.Source.CATEGORY, "categories"-> Constant.Source.CATEGORY
            Constant.Source.GENRE, "genres"-> Constant.Source.GENRE
            Constant.Source.YEAR, "years"-> Constant.Source.YEAR
            else-> throw Exception("Unknown source.")
        }
    }

    internal inline fun <reified T> Gson.fromJsonString(json: String) =
        fromJson<T>(json, object : TypeToken<T>() {}.type)
}