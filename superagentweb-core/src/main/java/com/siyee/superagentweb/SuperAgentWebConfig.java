package com.siyee.superagentweb;

import android.content.Context;
import android.os.Build;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.siyee.superagentweb.utils.LogUtils;

import java.io.File;

public class SuperAgentWebConfig {

    public static final String FILE_CACHE_PATH = "superagentweb-cache";
    public static final String AGENTWEB_CACHE_PATCH = File.separator + FILE_CACHE_PATH;
    public static final String PROVIDER_SUFFIX = ".SuperAgentWebFileProvider";

    /**
     * Log Debug switch
     */
    public static Boolean DEBUG = false;

    /**
     * 缓存路径
     */
    public static String AGENTWEB_FILE_PATH;

    /**
     * 当前操作系统是否低于 KITKAT
     */
    static final boolean IS_KITKAT_OR_BELOW_KITKAT = Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT;

    private static volatile boolean IS_INITIALIZED = false;

    private static final String TAG = SuperAgentWebConfig.class.getSimpleName();

    public static final String AGENTWEB_NAME = "SuperAgentWeb";

    /**
     * SuperAgentWeb 的版本
     */
    public static final String AGENTWEB_VERSION = AGENTWEB_NAME + "/" + BuildConfig.VERSION_NAME;

    /**
     * 通过JS获取的文件大小， 这里限制最大为5MB ，太大会抛出 OutOfMemoryError
     */
    public static int MAX_FILE_LENGTH = 1024 * 1024 * 5;

    public static void debug() {
        DEBUG = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    public static String getDatabasesCachePath(Context context) {
        return context.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
    }

    private static ValueCallback<Boolean> getDefaultIgnoreCallback() {
        return new ValueCallback<Boolean>() {
            @Override
            public void onReceiveValue(Boolean ignore) {
                LogUtils.i(TAG, "removeExpiredCookies:" + ignore);
            }
        };
    }

}
