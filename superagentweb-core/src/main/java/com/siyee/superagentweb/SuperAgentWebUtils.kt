package com.siyee.superagentweb

import android.content.Context
import android.os.Environment
import android.os.Looper
import android.text.TextUtils
import android.view.ViewGroup
import android.webkit.WebView
import java.io.File

fun cleanWebView(m: WebView?) {
    if (m == null) {
        return
    }
    if (Looper.myLooper() != Looper.getMainLooper()) {
        return
    }
    m.loadUrl("about:blank")
    m.stopLoading()
    if (m.handler != null) {
        m.handler.removeCallbacksAndMessages(null)
    }
    m.removeAllViews()
    val mViewGroup: ViewGroup? = m.parent as? ViewGroup
    mViewGroup?.removeView(m)
    m.setWebChromeClient(null)
    m.webViewClient = null
    m.tag = null
    m.clearHistory()
    m.destroy()
}

fun getSuperAgentWebFilePath(context: Context): String {
    if (!TextUtils.isEmpty(SUPERAGENTWEB_FILE_PATH)) {
        return SUPERAGENTWEB_FILE_PATH
    }
    return ""
}

fun getDiskExternalCacheDir(context: Context): String? {
    val file = context.externalCacheDir
    if (Environment.MEDIA_MOUNTED.equals(Environment.getStorageState(file))) {
        return file?.absolutePath
    }
    return null
}

fun getMIMEType(file: File): String {
    var type = ""
    val fName: String = file.getName()
    /* 取得扩展名 */
    /* 取得扩展名 */
    val end = fName.substring(fName.lastIndexOf(".") + 1, fName.length).toLowerCase()
    /* 依扩展名的类型决定MimeType */
    /* 依扩展名的类型决定MimeType */type = if (end == "pdf") {
        "application/pdf" //
    } else if (end == "m4a" || end == "mp3" || end == "mid" || end == "xmf" || end == "ogg" || end == "wav") {
        "audio/*"
    } else if (end == "3gp" || end == "mp4") {
        "video/*"
    } else if (end == "jpg" || end == "gif" || end == "png" || end == "jpeg" || end == "bmp") {
        "image/*"
    } else if (end == "apk") {
        "application/vnd.android.package-archive"
    } else if (end == "pptx" || end == "ppt") {
        "application/vnd.ms-powerpoint"
    } else if (end == "docx" || end == "doc") {
        "application/vnd.ms-word"
    } else if (end == "xlsx" || end == "xls") {
        "application/vnd.ms-excel"
    } else {
        "*/*"
    }
    return type
}

fun isUIThread(): Boolean {
    return Looper.myLooper() == Looper.getMainLooper()
}