package com.siyee.superagentweb.impl;

import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.siyee.superagentweb.abs.IUrlLoader;
import com.siyee.superagentweb.utils.AgentWebUtils;

import java.util.Map;

/**
 * @author hackycy
 */
public class UrlLoaderImpl implements IUrlLoader {

    private WebView mWebView;

    public UrlLoaderImpl(@NonNull WebView webView) {
        this.mWebView = webView;
    }

    @Override
    public void loadUrl(final String url) {
        if (!AgentWebUtils.isUIThread()) {
            AgentWebUtils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    loadUrl(url);
                }
            });
            return;
        }
        this.mWebView.loadUrl(url);
    }

    @Override
    public void loadUrl(final String url, final Map<String, String> headers) {
        if (!AgentWebUtils.isUIThread()) {
            AgentWebUtils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    loadUrl(url, headers);
                }
            });
            return;
        }
        if (headers == null || headers.isEmpty()) {
            loadUrl(url);
        } else {
            loadUrl(url, headers);
        }
    }

    @Override
    public void reload() {
        if (!AgentWebUtils.isUIThread()) {
            AgentWebUtils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    reload();
                }
            });
            return;
        }
        this.mWebView.reload();
    }

    @Override
    public void loadData(final String data, final String mimeType, final String encoding) {
        if (!AgentWebUtils.isUIThread()) {
            AgentWebUtils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    loadData(data, mimeType, encoding);
                }
            });
            return;
        }
        this.mWebView.loadData(data, mimeType, encoding);
    }

    @Override
    public void stopLoading() {
        if (!AgentWebUtils.isUIThread()) {
            AgentWebUtils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    stopLoading();
                }
            });
            return;
        }
        this.mWebView.stopLoading();
    }

    @Override
    public void loadDataWithBaseURL(final String baseUrl, final String data, final String mimeType, final String encoding, final String historyUrl) {
        if (!AgentWebUtils.isUIThread()) {
            AgentWebUtils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
                }
            });
            return;
        }
        this.mWebView.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
    }

    @Override
    public void postUrl(final String url, final byte[] params) {
        if (!AgentWebUtils.isUIThread()) {
            AgentWebUtils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    postUrl(url, params);
                }
            });
            return;
        }
        this.mWebView.postUrl(url, params);
    }

}
