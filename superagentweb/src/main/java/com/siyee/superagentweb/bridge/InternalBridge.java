package com.siyee.superagentweb.bridge;

import android.webkit.JavascriptInterface;
import androidx.annotation.Keep;
import androidx.annotation.Nullable;

/**
 * Internal Bridge
 */
public class InternalBridge {

    private IExecutorFactory mFactory;

    public InternalBridge(@Nullable IExecutorFactory factory) {
        this.mFactory = factory;
    }

    @Keep
    @JavascriptInterface
    public String invoke(String func, String paramString, int callBackId) {
        if (mFactory != null) {
            return mFactory.exec(func, paramString, callBackId);
        }
        return null;
    }

}
