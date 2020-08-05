package com.siyee.superagentweb.utils;

import android.content.Context;
import android.webkit.CookieManager;

/**
 * @author hackycy
 */
public class CookieUtils {

    /**
     * 获取Cookie
     * @param url domain
     * @return cookie value
     */
    public static String getCookiesByUrl(String url) {
        return CookieManager.getInstance() == null ? null : CookieManager.getInstance().getCookie(url);
    }

}
