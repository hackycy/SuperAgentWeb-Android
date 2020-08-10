package com.siyee.superagentweb.abs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Handler;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebView;

import com.siyee.superagentweb.impl.DefaultUIController;
import com.siyee.superagentweb.widget.WebParentLayout;

/**
 * 该类统一控制了与用户交互的界面
 *
 * @author hackycy
 */
public abstract class AbsAgentWebUIController {

    private Activity mActivity;
    private WebParentLayout mWebParentLayout;
    private volatile boolean mIsBindWebParent = false;

    protected AbsAgentWebUIController mAgentWebUIControllerDelegate;

    protected AbsAgentWebUIController create() {
        return new DefaultUIController();
    }

    protected AbsAgentWebUIController getDelegate() {
        AbsAgentWebUIController mAgentWebUIController = this.mAgentWebUIControllerDelegate;
        if (mAgentWebUIController == null) {
            this.mAgentWebUIControllerDelegate = mAgentWebUIController = create();
        }
        return mAgentWebUIController;
    }

    public final synchronized void bindWebParent(WebParentLayout webParentLayout, Activity activity) {
        if (!mIsBindWebParent) {
            mIsBindWebParent = true;
            this.mWebParentLayout = webParentLayout;
            this.mActivity = activity;
            bindSupportWebParent(webParentLayout, activity);
        }
    }

    protected void toDismissDialog(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    protected void toShowDialog(Dialog dialog) {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    protected abstract void bindSupportWebParent(WebParentLayout webParentLayout, Activity activity);

    /**
     * WebChromeClient#onJsAlert
     *
     * @param view
     * @param url
     * @param message
     */
    public abstract void onJsAlert(WebView view, String url, String message);

    /**
     * 咨询用户是否前往其他页面
     *
     * @param view
     * @param url
     * @param callback
     */
    public abstract void onOpenPagePrompt(WebView view, String url, Callback<Integer> callback);

    /**
     * WebChromeClient#onJsConfirm
     *
     * @param view
     * @param url
     * @param message
     * @param jsResult
     */
    public abstract void onJsConfirm(WebView view, String url, String message, JsResult jsResult);

    public abstract void onSelectItemsPrompt(WebView view, String url, String[] ways, Handler.Callback callback);

    /**
     * 强制下载弹窗
     *
     * @param url      当前下载地址。
     * @param callback 用户操作回调回调
     */
    public abstract void onForceDownloadAlert(String url, Handler.Callback callback);

    /**
     * WebChromeClient#onJsPrompt
     *
     * @param view
     * @param url
     * @param message
     * @param defaultValue
     * @param jsPromptResult
     */
    public abstract void onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult jsPromptResult);

    /**
     * 显示错误页
     *
     * @param view
     * @param errorCode
     * @param description
     * @param failingUrl
     */
    public abstract void onMainFrameError(WebView view, int errorCode, String description, String failingUrl);

    /**
     * 隐藏错误页
     */
    public abstract void onShowMainFrame();

    /**
     * 正在加载...
     *
     * @param msg
     */
    public abstract void onLoading(String msg);

    /**
     * 取消正在加载...
     */
    public abstract void onCancelLoading();

    /**
     * @param message 消息
     * @param intent  说明message的来源，意图
     */
    public abstract void onShowMessage(String message, String intent);

    /**
     * 当权限被拒回调该方法
     *
     * @param permissions
     * @param action
     */
    public abstract void onPermissionsDeny(String[] permissions, String action);

}
