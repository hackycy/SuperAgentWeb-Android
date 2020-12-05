package com.siyee.superagentweb.impl;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.webkit.DownloadListener;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.siyee.superagentweb.R;
import com.siyee.superagentweb.SuperAgentWebConfig;
import com.siyee.superagentweb.SuperAgentWebPermissions;
import com.siyee.superagentweb.abs.AbsAgentWebUIController;
import com.siyee.superagentweb.abs.Callback;
import com.siyee.superagentweb.abs.PermissionInterceptor;
import com.siyee.superagentweb.utils.LogUtils;
import com.siyee.superagentweb.utils.PermissionUtils;
import com.siyee.superagentweb.utils.SuperAgentWebUtils;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * @author hackycy
 */
public class DefaultDownloadListener implements DownloadListener {

    /**
     * Activity
     */
    private WeakReference<Activity> mActivity;

    /**
     * UIController
     */
    private WeakReference<AbsAgentWebUIController> mAbsAgentWebUIController;

    /**
     * 拦截
     */
    private PermissionInterceptor mPermissionInterceptor;

    /**
     * TAG 用于打印，标识
     */
    private static final String TAG = DefaultDownloadListener.class.getSimpleName();

    protected DefaultDownloadListener(Activity activity, WebView webView, PermissionInterceptor permissionInterceptor) {
        this.mPermissionInterceptor = permissionInterceptor;
        this.mAbsAgentWebUIController = new WeakReference<AbsAgentWebUIController>(SuperAgentWebUtils.getAgentWebUIControllerByWebView(webView));
        this.mActivity = new WeakReference<Activity>(activity);
    }

    @Override
    public void onDownloadStart(final String url, final String userAgent, final String contentDisposition, final String mimetype, final long contentLength) {
        LogUtils.i(TAG, "url: " + url + ", userAgent: " + userAgent + ", mimetype: "
                + mimetype + ", contentDisposition: " + contentDisposition + ", contentLength: " + contentLength);
        SuperAgentWebUtils.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                onDownloadStartInternal(url, userAgent, contentDisposition, mimetype, contentLength);
            }
        });
    }

    /**
     * Use DownloadManager Impl
     * @param url
     * @param userAgent
     * @param contentDisposition
     * @param mimetype
     * @param contentLength
     */
    private void onDownloadStartInternal(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        if (null == this.mActivity.get() || this.mActivity.get().isFinishing()) {
            return;
        }
        if (this.mPermissionInterceptor != null &&
                this.mPermissionInterceptor.intercept(url, SuperAgentWebPermissions.STORAGE, SuperAgentWebPermissions.ACTION_DOWNLOAD)) {
            return;
        }
        if (PermissionUtils.isGranted(SuperAgentWebPermissions.STORAGE)) {
            preDownload(url, userAgent);
        } else {
            PermissionUtils.permission(SuperAgentWebPermissions.STORAGE)
                    .callback(getPermissionCallback(url, userAgent))
                    .request();
        }
    }

    /**
     * 检测是否允许下载
     * @param url
     */
    private void preDownload(String url, String userAgent) {
        if (null == mActivity.get() || mActivity.get().isFinishing()) {
            return;
        }
        if (null != mAbsAgentWebUIController.get()) {
            mAbsAgentWebUIController.get().onForceDownloadAlert(url, getAlertCallback(url, userAgent));
        }
        return;
    }

    /**
     * 确认下载
     * @param url
     */
    private void performDownload(String url, String userAgent) {
        try {
            Uri uri = Uri.parse(url);
            DownloadManager manager = getDownloadManager();
            DownloadManager.Request request = new DownloadManager.Request(uri);
            /*
             * 设置在通知栏是否显示下载通知(下载进度), 有 3 个值可选:
             *    VISIBILITY_VISIBLE:                   下载过程中可见, 下载完后自动消失 (默认)
             *    VISIBILITY_VISIBLE_NOTIFY_COMPLETED:  下载过程中和下载完成后均可见
             *    VISIBILITY_HIDDEN:                    始终不显示通知
             */
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.addRequestHeader("User-Agent", userAgent);
            if (null != this.mAbsAgentWebUIController.get()) {
                this.mAbsAgentWebUIController.get()
                        .onShowMessage(SuperAgentWebUtils.getApp().getResources().getString(R.string.superagentweb_coming_soon_download), "performDownload");
            }
            manager.enqueue(request);
        } catch (Exception e) {
            if (null != this.mAbsAgentWebUIController.get()) {
                this.mAbsAgentWebUIController.get()
                        .onShowMessage(SuperAgentWebUtils.getApp().getResources().getString(R.string.superagentweb_download_fail), "performDownload");
            }
            if (SuperAgentWebConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    private Callback<Integer> getAlertCallback(String url, String userAgent) {
        final String urlCopy = String.copyValueOf(url.toCharArray());
        final String uaCopy = String.copyValueOf(userAgent.toCharArray());
        return new Callback<Integer>() {
            @Override
            public void handleValue(Integer value) {
                performDownload(urlCopy, uaCopy);
            }
        };
    }

    private PermissionUtils.FullCallback getPermissionCallback(String url, String userAgent) {
        final String urlCopy = String.copyValueOf(url.toCharArray());
        final String uaCopy = String.copyValueOf(userAgent.toCharArray());
        return new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(@NonNull List<String> granted) {
                preDownload(urlCopy, uaCopy);
            }

            @Override
            public void onDenied(@NonNull List<String> deniedForever, @NonNull List<String> denied) {
                if (null != mAbsAgentWebUIController.get()) {
                    mAbsAgentWebUIController.get().onPermissionsDeny(SuperAgentWebPermissions.STORAGE, SuperAgentWebPermissions.ACTION_DOWNLOAD);
                }
            }
        };
    }

    /**
     * Get DownloadManager
     * @return
     */
    private DownloadManager getDownloadManager() {
        if (null == this.mActivity.get() || this.mActivity.get().isFinishing()) {
            return null;
        }
        return (DownloadManager) this.mActivity.get().getSystemService(Context.DOWNLOAD_SERVICE);
    }

//    public static class DownloadManagerReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//        }
//
//    }

}
