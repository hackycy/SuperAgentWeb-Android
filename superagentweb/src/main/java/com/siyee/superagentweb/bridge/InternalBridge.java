package com.siyee.superagentweb.bridge;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Internal Bridge
 */
public class InternalBridge {

    public static String INTERNAL_BRIDGE_NAME = "invokeHandler";

    private IExecutorFactory mFactory;
    private WebView mWebView;

    public InternalBridge(@Nullable IExecutorFactory factory, @NonNull WebView webView) {
        this.mFactory = factory;
        this.mWebView = webView;
    }

    @Keep
    @JavascriptInterface
    public String invoke(String func, String paramString, int callBackId) {
        if (mFactory != null) {
            return mFactory.exec(this.mWebView.getUrl(), func, paramString, callBackId);
        }
        return null;
    }

}
