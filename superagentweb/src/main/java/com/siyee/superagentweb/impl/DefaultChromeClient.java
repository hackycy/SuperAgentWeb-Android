package com.siyee.superagentweb.impl;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebStorage;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.siyee.superagentweb.SuperAgentWebConfig;
import com.siyee.superagentweb.SuperAgentWebPermissions;
import com.siyee.superagentweb.WebChromeClient;
import com.siyee.superagentweb.abs.AbsAgentWebUIController;
import com.siyee.superagentweb.abs.IVideo;
import com.siyee.superagentweb.abs.IndicatorController;
import com.siyee.superagentweb.abs.PermissionInterceptor;
import com.siyee.superagentweb.bridge.InternalBridge;
import com.siyee.superagentweb.utils.FileChooserUtils;
import com.siyee.superagentweb.utils.LogUtils;
import com.siyee.superagentweb.utils.PermissionUtils;
import com.siyee.superagentweb.utils.SuperAgentWebUtils;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

/**
 * @author hackycy
 */
public class DefaultChromeClient extends WebChromeClient {

    /**
     * DefaultChromeClient 's TAG
     */
    private String TAG = DefaultChromeClient.class.getSimpleName();

    /**
     * Activity
     */
    private WeakReference<Activity> mActivityWeakReference;

    /**
     * Android WebChromeClient path ，用于反射，用户是否重写了该方法
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

    private InternalBridge mInternalBridge;

    /**
     * AbsAgentWebUIController
     */
    private WeakReference<AbsAgentWebUIController> mAgentWebUIController = null;

    public DefaultChromeClient(Activity activity,
                               IndicatorController indicatorController,
                               @Nullable IVideo iVideo,
                               PermissionInterceptor permissionInterceptor,
                               @NonNull InternalBridge internalBridge,
                               WebView webView) {
        this.mIndicatorController = indicatorController;
        this.mPermissionInterceptor = permissionInterceptor;
        this.mVideo = iVideo;
        this.mWebView = webView;
        this.mInternalBridge = internalBridge;
        this.mActivityWeakReference = new WeakReference<Activity>(activity);
        mAgentWebUIController = new WeakReference<AbsAgentWebUIController>(SuperAgentWebUtils.getAgentWebUIControllerByWebView(webView));
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
            if (SuperAgentWebConfig.DEBUG) {
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
            if (SuperAgentWebConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        if (this.mInternalBridge.getFactory() != null && !TextUtils.isEmpty(message)
                && message.startsWith(InternalBridge.URL_SCHEME)) {
            // Bridge Invoke
            String func = message.replace(InternalBridge.URL_SCHEME, "");
            String execResult = this.mInternalBridge.getFactory().exec(null, url, func, defaultValue);
            result.confirm(execResult);
            return true;
        }
        try {
            if (this.mAgentWebUIController.get() != null) {
                this.mAgentWebUIController.get().onJsPrompt(view, url, message, defaultValue, result);
            }
        } catch (Exception e) {
            if (SuperAgentWebConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        if (mPermissionInterceptor != null) {
            if (mPermissionInterceptor.intercept(this.mWebView.getUrl(), SuperAgentWebPermissions.LOCATION, SuperAgentWebPermissions.ACTION_LOCATION)) {
                callback.invoke(origin, false, false);
                return;
            }
        }
        Activity mActivity = mActivityWeakReference.get();
        if (mActivity == null) {
            callback.invoke(origin, false, false);
            return;
        }
        if (PermissionUtils.isGranted(SuperAgentWebPermissions.LOCATION)) {
            callback.invoke(origin, true, false);
        } else {
            this.mOrigin = origin;
            this.mCallback = callback;
            PermissionUtils.permission(SuperAgentWebPermissions.LOCATION)
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
                                SuperAgentWebPermissions.LOCATION,
                                SuperAgentWebPermissions.ACTION_LOCATION);
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        return openFileChooserAboveL(webView, filePathCallback, fileChooserParams);
    }

    /**
     * Android  >= 4.1
     *
     * @param uploadFile
     * @param acceptType
     * @param capture
     */
    public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
        createAndOpenCommonFileChooser(uploadFile, acceptType);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected boolean openFileChooserAboveL(WebView webView, ValueCallback<Uri[]> valueCallbacks, FileChooserParams fileChooserParams) {
        LogUtils.i(TAG, "fileChooserParams:" + Arrays.toString(fileChooserParams.getAcceptTypes())
                + "  getTitle:" + fileChooserParams.getTitle() + " accept:"
                + Arrays.toString(fileChooserParams.getAcceptTypes()) + " length:"
                + fileChooserParams.getAcceptTypes().length + "  :"
                + fileChooserParams.isCaptureEnabled() + "  "
                + fileChooserParams.getFilenameHint() + "  intent:"
                + fileChooserParams.createIntent().toString() + "   mode:" + fileChooserParams.getMode());
        Activity mActivity = this.mActivityWeakReference.get();
        if (mActivity == null || mActivity.isFinishing()) {
            return false;
        }
        return FileChooserUtils.showFileChooserCompat(mActivity,
                mWebView,
                valueCallbacks,
                fileChooserParams,
                this.mPermissionInterceptor,
                null,
                null
        );
    }

    protected void createAndOpenCommonFileChooser(ValueCallback<Uri> valueCallback, String mimeType) {
        Activity mActivity = this.mActivityWeakReference.get();
        if (mActivity == null || mActivity.isFinishing()) {
            valueCallback.onReceiveValue(Uri.EMPTY);
            return;
        }
        FileChooserUtils.showFileChooserCompat(mActivity,
                mWebView,
                null,
                null,
                this.mPermissionInterceptor,
                valueCallback,
                mimeType
        );
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
