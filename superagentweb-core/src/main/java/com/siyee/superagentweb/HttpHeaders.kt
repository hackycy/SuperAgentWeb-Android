package com.siyee.superagentweb

import android.net.Uri
import android.text.TextUtils

class HttpHeaders {

    private val mHeaders: MutableMap<String, MutableMap<String, String>> by lazy {
        mutableMapOf()
    }

    fun additionalHttpHeader(url: String, k: String, v: String) {
        if (TextUtils.isEmpty(url)) {
            return
        }
        val realUrl = subBaseUrl(url)
        var headersByUrl = mHeaders[realUrl]
        if (headersByUrl == null) {
            headersByUrl = mutableMapOf();
        }
        headersByUrl[k] = v
        mHeaders[realUrl] = headersByUrl
    }

    fun additionalHttpHeaders(url: String, headers: MutableMap<String, String>?) {
        if (TextUtils.isEmpty(url)) {
            return
        }
        val realUrl = subBaseUrl(url)
        mHeaders[realUrl] = headers ?: mutableMapOf()
    }



    private fun subBaseUrl(originUrl: String): String {
        try {
            val originUri = Uri.parse(originUrl)
            return "${originUri.scheme}://${originUri.authority}"
        } catch (e: Exception) {
            return originUrl
        }
    }

}