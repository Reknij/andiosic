package com.example.andiosic.api

import com.example.andiosic.ConfigManager
import com.example.andiosic.GlobalHelper
import com.google.gson.Gson
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import kotlin.coroutines.resumeWithException
import java.io.IOException

object Api {
    var client = loadClient();
    val baseUrl get() = "${ConfigManager.serverAddress.toString()}/api"

    private fun loadClient(): OkHttpClient {
        return OkHttpClient().newBuilder().addInterceptor(Interceptor { chain ->
            val token = ConfigManager.authorization
            val original = chain.request()
            val authorized = original.newBuilder()
            if (token != null) {
                authorized.addHeader("Cookie", "authorization=${token}")
            }
            chain.proceed(authorized.build())
        }).build()
    }
}

suspend inline fun Call.await(): Response {
    return suspendCancellableCoroutine { continuation ->
        val callback = ContinuationCallback(this, continuation)
        enqueue(callback)
        continuation.invokeOnCancellation(callback)
    }
}

class ContinuationCallback(
    private val call: Call,
    private val continuation: CancellableContinuation<Response>
) : Callback, CompletionHandler {

    override fun onResponse(call: Call, response: Response) {
        continuation.resume(response, null)
    }

    override fun onFailure(call: Call, e: IOException) {
        if (!call.isCanceled()) {
            continuation.resumeWithException(e)
        }
    }

    override fun invoke(cause: Throwable?) {
        try {
            call.cancel()
        } catch (_: Throwable) {}
    }
}

