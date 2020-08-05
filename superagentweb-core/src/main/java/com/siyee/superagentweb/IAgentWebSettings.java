package com.siyee.superagentweb;

import android.webkit.WebSettings;
import android.webkit.WebView;

public interface IAgentWebSettings {

    IAgentWebSettings toSetting(WebView webView);

    WebSettings getWebSettings();

}
