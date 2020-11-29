package com.siyee.superagentweb.bridge;

import android.webkit.JavascriptInterface;

import androidx.annotation.Keep;

/**
 * Internal Bridge
 */
public class InternalBridge {

    @Keep
    @JavascriptInterface
    public String invoke() {
        return "";
    }

}
