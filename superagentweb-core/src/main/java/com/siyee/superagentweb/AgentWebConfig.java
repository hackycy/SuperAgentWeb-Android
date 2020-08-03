package com.siyee.superagentweb;

import android.content.Context;
import android.os.Build;
import android.webkit.CookieManager;

import java.io.File;

public class AgentWebConfig {

    static final String FILE_CACHE_PATH = "superagentweb-cache";
    static final String AGENTWEB_CACHE_PATCH = File.separator + "superagentweb-cache";

    /**
     * Log Debug switch
     */
    public static Boolean DEBUG = false;

    /**
     * 缓存路径
     */
    static String AGENTWEB_FILE_PATH;

    /**
     * 当前操作系统是否低于 KITKAT
     */
    static final boolean IS_KITKAT_OR_BELOW_KITKAT = Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT;

    /**
     * 默认 WebView  类型 。
     */
    public static final int WEBVIEW_DEFAULT_TYPE = 1;

    /**
     * 使用 AgentWebView
     */
    public static final int WEBVIEW_AGENTWEB_SAFE_TYPE = 2;

    /**
     * 自定义 WebView
     */
    public static final int WEBVIEW_CUSTOM_TYPE = 3;

    private static volatile boolean IS_INITIALIZED = false;

    private static final String TAG = AgentWebConfig.class.getSimpleName();

    public static final String AGENTWEB_NAME = "SuperAgentWeb";

    /**
     * SuperAgentWeb 的版本
     */
    public static final String AGENTWEB_VERSION = AGENTWEB_NAME + "/" + BuildConfig.VERSION_NAME;

    /**
     * 通过JS获取的文件大小， 这里限制最大为5MB ，太大会抛出 OutOfMemoryError
     */
    public static int MAX_FILE_LENGTH = 1024 * 1024 * 5;

    /**
     * 获取Cookie
     * @param url domain
     * @return cookie value
     */
    public static String getCookiesByUrl(String url) {
        return CookieManager.getInstance() == null ? null : CookieManager.getInstance().getCookie(url);
    }

    static String getDatabasesCachePath(Context context) {
        return context.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
    }

}
