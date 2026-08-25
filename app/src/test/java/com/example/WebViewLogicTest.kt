package com.example

import org.junit.Test
import org.junit.Assert.*
import java.net.URI

class WebViewLogicTest {
    @Test
    fun testUrlRebuild() {
        val rawUrl = "google.com"
        val fixedUrl = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://") && !rawUrl.startsWith("file://")) {
            "https://$rawUrl"
        } else rawUrl
        
        assertEquals("https://google.com", fixedUrl)
        
        val uri = URI(fixedUrl)
        assertEquals("google.com", uri.host)
    }
}
