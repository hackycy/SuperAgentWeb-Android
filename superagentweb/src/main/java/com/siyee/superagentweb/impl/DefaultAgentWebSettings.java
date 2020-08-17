package com.siyee.superagentweb.impl;

import android.webkit.DownloadListener;
import android.webkit.WebView;

import com.siyee.superagentweb.SuperAgentWeb;
import com.siyee.superagentweb.abs.AbsAgentWebSettings;
import com.siyee.superagentweb.abs.WebListenerManager;

/**
 * @author hackycy
 */
public class DefaultAgentWebSettings extends AbsAgentWebSettings {

    @Override
    protected void bindAgentWebSupport(SuperAgentWeb superAgentWeb) {
        // Nothing To Do
    }

    @Override
    public WebListenerManager setDownloader(WebView webView, DownloadListener downloadListener) {
        if (downloadListener == null) {
            webView.setDownloadListener(new DefaultDownloadListener(this.mSuperAgentWeb.getActivity(), webView, this.mSuperAgentWeb.getPermissionInterceptor()));
            return this;
        }
        return super.setDownloader(webView, downloadListener);
    }
}
