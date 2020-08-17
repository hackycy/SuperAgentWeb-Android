package com.siyee.superagentweb.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.File;

/**
 * @author hackycy
 */
public class CookieUtils {

    private static final String TAG = CookieUtils.class.getSimpleName();

    private static volatile boolean IS_INITIALIZED = false;

    public static synchronized void initCookiesManager(Context context) {
        if (!IS_INITIALIZED) {
            createCookiesSyncInstance(context);
            IS_INITIALIZED = true;
        }
    }

    private static void createCookiesSyncInstance(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(context);
        }
    }

    /**
     * 解决兼容 Android 4.4 java.lang.NoSuchMethodError: android.webkit.CookieManager.removeSessionCookies
     */
    public static void removeSessionCookies() {
        removeSessionCookies(null);
    }

    public static void removeSessionCookies(ValueCallback<Boolean> callback) {
        if (callback == null) {
            callback = getDefaultIgnoreCallback();
        }
        if (CookieManager.getInstance() == null) {
            callback.onReceiveValue(Boolean.FALSE);
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeSessionCookie();
            toSyncCookies();
            callback.onReceiveValue(Boolean.TRUE);
            return;
        }
        CookieManager.getInstance().removeSessionCookies(callback);
        toSyncCookies();
    }

    /**
     * 同步cookie
     *
     * @param url
     * @param cookies
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void syncCookie(String url, String cookies) {
        CookieManager mCookieManager = CookieManager.getInstance();
        if (mCookieManager != null) {
            mCookieManager.setCookie(url, cookies);
            toSyncCookies();
        }
    }

    /**
     * 获取Cookie
     * @param url domain
     * @return cookie value
     */
    public static String getCookiesByUrl(String url) {
        return CookieManager.getInstance() == null ? null : CookieManager.getInstance().getCookie(url);
    }

    /**
     * 删除所有 Cookies
     */
    public static void removeAllCookies() {
        removeAllCookies(null);
    }

    /**
     * Android  4.4  NoSuchMethodError: android.webkit.CookieManager.removeAllCookies
     * @param callback
     */
    public static void removeAllCookies(@Nullable ValueCallback<Boolean> callback) {
        if (callback == null) {
            callback = getDefaultIgnoreCallback();
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookie();
            toSyncCookies();
            callback.onReceiveValue(!CookieManager.getInstance().hasCookies());
            return;
        }
        CookieManager.getInstance().removeAllCookies(callback);
        toSyncCookies();
    }

    /**
     * 清空缓存
     *
     * @param context
     */
    public static synchronized void clearDiskCache(Context context) {
        try {
            SuperAgentWebUtils.clearCacheFolder(new File(SuperAgentWebUtils.getCachePath(context)), 0);
            String path = SuperAgentWebUtils.getExternalCachePath(context);
            if (!TextUtils.isEmpty(path)) {
                File mFile = new File(path);
                SuperAgentWebUtils.clearCacheFolder(mFile, 0);
            }
        } catch (Throwable throwable) {
            if (LogUtils.isDebug()) {
                throwable.printStackTrace();
            }
        }
    }

    /**
     * 删除所有已经过期的 Cookies
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void removeExpiredCookies() {
        CookieManager mCookieManager = null;
        if ((mCookieManager = CookieManager.getInstance()) != null) { //同步清除
            mCookieManager.removeExpiredCookie();
            toSyncCookies();
        }
    }

    /**
     * 同步Cookies
     */
    private static void toSyncCookies() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().sync();
            return;
        }
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {

            @SuppressLint("NewApi")
            @Override
            public void run() {
                CookieManager.getInstance().flush();
            }

        });
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
