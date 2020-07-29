package com.siyee.superagentweb

import android.webkit.WebSettings
import android.webkit.WebView

/**
 * @author hackycy
 */
interface ISuperAgentWebSettings<T: WebSettings> {

    fun toSetting(webView: WebView): ISuperAgentWebSettings<T>

    fun getSetting(): T

}