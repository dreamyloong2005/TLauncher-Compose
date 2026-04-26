package com.dreamyloong.tlauncher.core.update

actual suspend fun platformHttpGetText(
    url: String,
    headers: Map<String, String>,
): String? {
    return null
}
