package com.siyee.superagentweb.bridge;

import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.siyee.superagentweb.abs.JsAccessEntrace;
import com.siyee.superagentweb.utils.SuperAgentWebUtils;

/**
 * Internal Bridge
 */
public class InternalBridge {

    public static String URL_SCHEME = "jsbridge://";
    public static String INTERNAL_BRIDGE_NAME = "_invokeHandler";

    private IExecutorFactory mFactory;
    private WebView mWebView;
    private JsAccessEntrace mJsAccessEntrace;

    private static String CALL_BRIDGE_FUNC = "window._handleMessageFromNative('%s', '%s')";
    private static String INVOKE_CALLBACK_BRIDGE_FUNC = "window._handleInvokeCallbackFromNative(%d, '%s')";
    private static String CONSOLE_ERROR = "console.log('%s')";

    public InternalBridge(@Nullable IExecutorFactory factory, @NonNull WebView webView, @NonNull JsAccessEntrace jsAccessEntrace) {
        this.mFactory = factory;
        this.mWebView = webView;
        this.mJsAccessEntrace = jsAccessEntrace;
    }

    public IExecutorFactory getFactory() {
        return mFactory;
    }

    public JsAccessEntrace getJsAccessEntrace() {
        return mJsAccessEntrace;
    }

    private void invokeCallbackToJs(int callbackId, @Nullable String result) {
        this.mJsAccessEntrace.callJs(String
                .format(INVOKE_CALLBACK_BRIDGE_FUNC, callbackId, TextUtils.isEmpty(result) ? "{}" : result));
    }

    public void consoleError(String message) {
        this.mJsAccessEntrace.callJs(String.format(CONSOLE_ERROR, message));
    }

    @Keep
    @JavascriptInterface
    public void invoke(final String func, final String paramString, final int callbackId) {
        if (mFactory == null) {
            return;
        }
        SuperAgentWebUtils.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                Promise p = new Promise(callbackId);
                mFactory.exec(p, mWebView.getUrl(), func, paramString);
            }
        });
    }

    /**
     * async invoke need this
     */
    public class Promise {

        private int callbackId;

        private Promise() { }

        private Promise(int callbackId) {
            this.callbackId = callbackId;
        }

        public void resolve(String result) {
            invokeCallbackToJs(this.callbackId, result);
        }

    }

}
