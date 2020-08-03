package com.siyee.superagentweb;

import android.webkit.WebSettings;
import android.webkit.WebView;

public interface IAgentWebSettings<WS extends WebSettings> {

    IAgentWebSettings toSetting(WebView webView);

    WS getSetting();

}
