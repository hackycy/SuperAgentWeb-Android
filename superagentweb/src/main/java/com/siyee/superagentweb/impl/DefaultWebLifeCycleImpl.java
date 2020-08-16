package com.siyee.superagentweb.impl;

import android.webkit.WebView;

import com.siyee.superagentweb.abs.IWebLifeCycle;
import com.siyee.superagentweb.utils.AgentWebUtils;

/**
 * @author hackycy
 */
public class DefaultWebLifeCycleImpl implements IWebLifeCycle {

    private WebView mWebView;

    public DefaultWebLifeCycleImpl(WebView webView) {
        this.mWebView = webView;
    }

    @Override
    public void onResume() {
        if (this.mWebView != null) {
            this.mWebView.onResume();
            this.mWebView.resumeTimers();
        }
    }

    @Override
    public void onPause() {
        if (this.mWebView != null) {
            this.mWebView.onPause();
            this.mWebView.pauseTimers();
        }
    }

    @Override
    public void onDestroy() {
        if (this.mWebView != null) {
            this.mWebView.pauseTimers();
            AgentWebUtils.clearWebView(this.mWebView);
        }
    }

}
