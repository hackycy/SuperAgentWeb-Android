package com.siyee.superagentweb.abs;

import android.os.Build;
import android.webkit.ValueCallback;

import androidx.annotation.RequiresApi;

/**
 * @author hackycy
 */
public interface JsAccessEntrace {

    void callJs(String js, ValueCallback<String> callback);

    void callJs(String js);

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    void quickCallJs(String method, ValueCallback<String> callback, String... params);

    void quickCallJs(String method, String... params);

    void quickCallJs(String method);

}
