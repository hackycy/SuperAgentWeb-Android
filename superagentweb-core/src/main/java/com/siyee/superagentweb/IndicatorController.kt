package com.siyee.superagentweb

import android.webkit.WebView




interface IndicatorController {

    fun progress(v: WebView?, newProgress: Int)

    fun offerIndicator(): BaseIndicatorSpec

    fun showIndicator()

    fun setProgress(newProgress: Int)

    fun finish()

}