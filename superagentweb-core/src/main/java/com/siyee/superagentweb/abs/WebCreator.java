package com.siyee.superagentweb.abs;

import android.webkit.WebView;
import android.widget.FrameLayout;

/**
 * @author hackycy
 */
public interface WebCreator extends IWebIndicator {

    WebCreator create();

    WebView getWebView();

    FrameLayout getWebParentLayout();

}
