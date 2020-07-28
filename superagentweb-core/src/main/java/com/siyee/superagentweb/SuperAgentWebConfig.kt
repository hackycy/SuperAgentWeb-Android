package com.siyee.superagentweb

import android.content.Context
import android.os.Build
import android.webkit.CookieManager
import java.io.File

val FILE_CACHE_PATH = "agentweb-cache"
val SUPERAGENTWEB_CACHE_PATCH = "${File.separator}$FILE_CACHE_PATH"

/**
 * Name
 */
val SUPERAGENTWEB_NAME = "SuperAgentWeb"

/**
 * SuperAgentWeb版本
 */
val SUPERAGENTWEB_VERSION = "$SUPERAGENTWEB_NAME/${BuildConfig.VERSION_NAME}"

/**
 * DEBUG 模式 ， 如果需要查看日志请设置为 true
 */
var DEBUG: Boolean = false

/**
 * 缓存路径
 */
var SUPERAGENTWEB_FILE_PATH: String = ""

/**
 * 当前操作系统是否低于 KITKAT
 */
val IS_KITKAT_OR_BELOW_KITKAT: Boolean = Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT

/**
 * 通过JS获取的文件大小， 这里限制最大为5MB ，太大会抛出 OutOfMemoryError
 */
var MAX_FILE_LENGTH: Int = 1024 * 1024 * 5

/**
 * 获取Cookie
 */
fun getCookiesByUrl(url: String): String? {
    return if (CookieManager.getInstance() == null) null else CookieManager.getInstance().getCookie(url);
}

fun getDatabasesCachePath(context: Context): String {
    return context.applicationContext.getDir("database", Context.MODE_PRIVATE).path
}