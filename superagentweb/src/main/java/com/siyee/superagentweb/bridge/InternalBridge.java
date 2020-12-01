package com.siyee.superagentweb.bridge;

import android.webkit.JavascriptInterface;
import androidx.annotation.Keep;

/**
 * Internal Bridge
 */
public class InternalBridge {

    private IExecutorFactory mFactory;

    public InternalBridge(IExecutorFactory factory) {
        this.mFactory = factory;
    }

    @Keep
    @JavascriptInterface
    public String invoke(String func, String paramString, int callBackId) {
        return mFactory.exec(func, paramString, callBackId);
    }

}
