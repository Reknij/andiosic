package com.example.andiosic.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.andiosic.R

fun <T> Context.loadImageBitmap(img: T, onImageLoaded: (Bitmap) -> Unit) {
    Glide.with(this).asBitmap().load(img)
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(
                resource: Bitmap,
                transition: Transition<in Bitmap>?
            ) {
                onImageLoaded(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) = Unit
        })
}