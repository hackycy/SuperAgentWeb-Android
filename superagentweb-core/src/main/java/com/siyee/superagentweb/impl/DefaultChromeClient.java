package com.siyee.superagentweb.impl;

import android.app.Activity;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.siyee.superagentweb.AgentWebConfig;
import com.siyee.superagentweb.AgentWebPermissions;
import com.siyee.superagentweb.abs.AbsAgentWebUIController;
import com.siyee.superagentweb.abs.IVideo;
import com.siyee.superagentweb.abs.IndicatorController;
import com.siyee.superagentweb.abs.PermissionInterceptor;
import com.siyee.superagentweb.middleware.MiddlewareWebChromeBase;
import com.siyee.superagentweb.utils.AgentWebUtils;
import com.siyee.superagentweb.utils.PermissionUtils;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * @author hackycy
 */
public class DefaultChromeClient extends MiddlewareWebChromeBase {

    /**
     * Activity
     */
    private WeakReference<Activity> mActivityWeakReference;

    /**
     * Android WebChromeClient path ，用于反射，用户是否重写来该方法
     */
    public static final String ANDROID_WEBCHROMECLIENT_PATH = "android.webkit.WebChromeClient";

    /**
     * IndicatorController 进度条控制器
     */
    private IndicatorController mIndicatorController;

    /**
     * Video 处理类
     */
    private IVideo mVideo;

    /**
     * PermissionInterceptor 权限拦截器
     */
    private PermissionInterceptor mPermissionInterceptor;

    /**
     * Web端触发的定位 mOrigin
     */
    private String mOrigin = null;

    /**
     * Web端触发的定位 mCallback
     */
    private GeolocationPermissions.Callback mCallback = null;

    /**
     * WebView
     */
    private WebView mWebView;

    /**
     * AbsAgentWebUIController
     */
    private WeakReference<AbsAgentWebUIController> mAgentWebUIController = null;

    public DefaultChromeClient(Activity activity,
                               IndicatorController indicatorController,
                               WebChromeClient chromeClient,
                               @Nullable IVideo iVideo,
                               PermissionInterceptor permissionInterceptor,
                               WebView webView) {
        super(chromeClient);
        this.mIndicatorController = indicatorController;
        this.mPermissionInterceptor = permissionInterceptor;
        this.mVideo = iVideo;
        this.mWebView = webView;
        this.mActivityWeakReference = new WeakReference<Activity>(activity);
        mAgentWebUIController = new WeakReference<AbsAgentWebUIController>(AgentWebUtils.getAgentWebUIControllerByWebView(webView));
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        if (mIndicatorController != null) {
            mIndicatorController.setProgress(newProgress);
        }
    }

    @Override
    public void onExceededDatabaseQuota(String url, String databaseIdentifier, long quota, long estimatedDatabaseSize, long totalQuota, WebStorage.QuotaUpdater quotaUpdater) {
        quotaUpdater.updateQuota(totalQuota * 2);
    }

    @Override
    public void onReachedMaxAppCacheSize(long requiredStorage, long quota, WebStorage.QuotaUpdater quotaUpdater) {
        quotaUpdater.equals(requiredStorage * 2);
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        try {
            if (this.mAgentWebUIController.get() != null) {
                this.mAgentWebUIController.get().onJsConfirm(view, url, message, result);
            }
        } catch (Exception e) {
            if (AgentWebConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        try {
            if (this.mAgentWebUIController.get() != null) {
                this.mAgentWebUIController.get().onJsAlert(view, url, message);
            }
            result.confirm();
        } catch (Exception e) {
            if (AgentWebConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        if (mPermissionInterceptor != null) {
            if (mPermissionInterceptor.intercept(this.mWebView.getUrl(), AgentWebPermissions.LOCATION, AgentWebPermissions.ACTION_LOCATION)) {
                callback.invoke(origin, false, false);
                return;
            }
        }
        Activity mActivity = mActivityWeakReference.get();
        if (mActivity == null) {
            callback.invoke(origin, false, false);
            return;
        }
        if (PermissionUtils.isGranted(AgentWebPermissions.LOCATION)) {
            callback.invoke(origin, true, false);
        } else {
            this.mOrigin = origin;
            this.mCallback = callback;
            PermissionUtils.permission(AgentWebPermissions.LOCATION)
                    .callback(mLocationPermissionFullCallback)
                    .request();
        }
    }

    /**
     * 监听权限
     */
    private PermissionUtils.FullCallback mLocationPermissionFullCallback = new PermissionUtils.FullCallback() {

        @Override
        public void onGranted(@NonNull List<String> granted) {
            if (mCallback != null) {
                mCallback.invoke(mOrigin, true, false);
            }
            mCallback = null;
            mOrigin = null;
        }

        @Override
        public void onDenied(@NonNull List<String> deniedForever, @NonNull List<String> denied) {
            if (mCallback != null) {
                mCallback.invoke(mOrigin, false, false);
            }
            mCallback = null;
            mOrigin = null;
            if (mAgentWebUIController.get() != null) {
                mAgentWebUIController
                        .get()
                        .onPermissionsDeny(
                                AgentWebPermissions.LOCATION,
                                AgentWebPermissions.ACTION_LOCATION);
            }
        }
    };

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        try {
            if (this.mAgentWebUIController.get() != null) {
                this.mAgentWebUIController.get().onJsPrompt(view, url, message, defaultValue, result);
            }
        } catch (Exception e) {
            if (AgentWebConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        if (this.mVideo != null) {
            this.mVideo.onShowCustomView(view, callback);
        }
    }

    @Override
    public void onHideCustomView() {
        if (this.mVideo != null) {
            this.mVideo.onHideCustomView();
        }
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        super.onConsoleMessage(consoleMessage);
        return true;
    }
}
