package com.siyee.superagentweb.impl;

import android.view.KeyEvent;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.siyee.superagentweb.abs.EventInterceptor;
import com.siyee.superagentweb.abs.IEventHandler;

/**
 * IEventHandler 对事件的处理，主要是针对
 * 视屏状态进行了处理 ， 如果当前状态为 视频状态
 * 则先退出视频。
 * @author hackycy
 */
public class EventHandlerImpl implements IEventHandler {

    private WebView mWebView;
    private EventInterceptor mEventInterceptor;

    public static EventHandlerImpl getInstantce(@NonNull WebView view, @Nullable EventInterceptor eventInterceptor) {
        return new EventHandlerImpl(view, eventInterceptor);
    }

    public EventHandlerImpl(WebView webView, EventInterceptor eventInterceptor) {
        this.mWebView = webView;
        this.mEventInterceptor = eventInterceptor;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return back();
        }
        return false;
    }

    @Override
    public boolean back() {
        if (this.mEventInterceptor != null && this.mEventInterceptor.event()) {
            return true;
        }
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return false;
    }

}
