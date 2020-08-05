package com.siyee.superagentweb.abs;

import android.webkit.WebSettings;
import android.webkit.WebView;

public interface IAgentWebSettings {

    IAgentWebSettings toSetting(WebView webView);

    WebSettings getWebSettings();

}
