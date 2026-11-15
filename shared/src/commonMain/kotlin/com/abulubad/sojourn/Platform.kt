package com.abulubad.sojourn

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform