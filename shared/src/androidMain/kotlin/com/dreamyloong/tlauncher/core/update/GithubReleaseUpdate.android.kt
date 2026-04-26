package com.dreamyloong.tlauncher.core.update

import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual suspend fun platformHttpGetText(
    url: String,
    headers: Map<String, String>,
): String? = suspendCoroutine { continuation ->
    thread(isDaemon = true) {
        val result = runCatching {
            val connection = URL(url).openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 10000
                headers.forEach { (key, value) ->
                    connection.setRequestProperty(key, value)
                }
                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    null
                } else {
                    connection.inputStream.bufferedReader().use { reader ->
                        reader.readText()
                    }
                }
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
        continuation.resume(result)
    }
}
