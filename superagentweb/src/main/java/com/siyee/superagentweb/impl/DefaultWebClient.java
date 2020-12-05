package com.siyee.superagentweb.impl;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;

import com.alipay.sdk.app.H5PayCallback;
import com.alipay.sdk.app.PayTask;
import com.alipay.sdk.util.H5PayResultModel;
import com.siyee.superagentweb.OpenOtherPageWays;
import com.siyee.superagentweb.SuperAgentWebConfig;
import com.siyee.superagentweb.WebViewClient;
import com.siyee.superagentweb.abs.AbsAgentWebUIController;
import com.siyee.superagentweb.abs.Callback;
import com.siyee.superagentweb.utils.LogUtils;
import com.siyee.superagentweb.utils.SuperAgentWebUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author hackycy
 */
public class DefaultWebClient extends WebViewClient {

    private WeakReference<Activity> mWeakReference = null;

    private static final String TAG = DefaultWebClient.class.getSimpleName();

    /**
     * 缩放
     */
    private static final int CONSTANTS_ABNORMAL_BIG = 7;

    /**
     * WebView
     */
    private WebView mWebView;

    /**
     * mWebClientHelper
     */
    private boolean mWebClientHelper;

    /**
     * intent ' s scheme
     */
    public static final String INTENT_SCHEME = "intent://";

    /**
     * Wechat pay scheme ，用于唤醒微信支付
     * https://www.cnblogs.com/Alex80/p/6747894.html
     */
    public static final String WEBCHAT_PAY_SCHEME = "weixin://wap/pay?";

    /**
     * 支付宝
     */
    public static final String ALIPAYS_SCHEME = "alipays://";

    /**
     * http scheme
     */
    public static final String HTTP_SCHEME = "http://";

    /**
     * https scheme
     */
    public static final String HTTPS_SCHEME = "https://";

    /**
     * SMS scheme
     */
    public static final String SCHEME_SMS = "sms:";

    /**
     * true 表示当前应用内依赖了 alipay library , false  反之
     */
    private static final boolean HAS_ALIPAY_LIB;

    /**
     * 默认为咨询用户
     */
    private OpenOtherPageWays mOpenOtherPageWays = OpenOtherPageWays.ASK;

    /**
     * 是否拦截找不到相应页面的Url，默认拦截
     */
    private boolean mIsInterceptUnkownUrl;

    /**
     * MainFrameErrorMethod
     */
    private Method onMainFrameErrorMethod = null;

    /**
     * AbsAgentWebUIController
     */
    private WeakReference<AbsAgentWebUIController> mAgentWebUIController = null;

    /**
     * 弹窗回调
     */
    private Handler.Callback mCallback = null;

    /**
     * Alipay PayTask 对象
     */
    private Object mPayTask;

    /**
     * 缓存当前出现错误的页面
     */
    private Set<String> mErrorUrlsSet = new HashSet<>();
    /**
     * 缓存等待加载完成的页面 onPageStart()执行之后 ，onPageFinished()执行之前
     */
    private Set<String> mWaittingFinishSet = new HashSet<>();

    static {
        boolean tag = true;
        try {
            Class.forName("com.alipay.sdk.app.PayTask");
        } catch (Throwable ignore) {
            tag = false;
        }
        HAS_ALIPAY_LIB = tag;
    }

    private DefaultWebClient(Builder builder) {
        this.mWebView = builder.mWebView;
        mWeakReference = new WeakReference<Activity>(builder.mActivity);
        this.mWebClientHelper = builder.mWebClientHelper;
        mAgentWebUIController = new WeakReference<AbsAgentWebUIController>(SuperAgentWebUtils.getAgentWebUIControllerByWebView(builder.mWebView));
        this.mIsInterceptUnkownUrl = builder.mIsInterceptUnkownUrl;
        if (builder.mOpenOtherPageWays != null) {
            this.mOpenOtherPageWays = builder.mOpenOtherPageWays;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        if (url.startsWith(HTTP_SCHEME) || url.startsWith(HTTPS_SCHEME)) {
            return (this.mWebClientHelper && HAS_ALIPAY_LIB && isAlipay(view, url));
        }
        if (!mWebClientHelper) {
            return super.shouldOverrideUrlLoading(view, url);
        }
        // 电话 ， 邮箱 ， 短信
        if (handleCommonLink(url)) {
            return true;
        }
        // Intent
        if (url.startsWith(INTENT_SCHEME)) {
            handleIntentUrl(url);
            return true;
        }
        // 微信支付
        if (url.startsWith(WEBCHAT_PAY_SCHEME)) {
            startActivity(url);
            return true;
        }
        // 支付宝
        if (url.startsWith(ALIPAYS_SCHEME) && lookup(url)) {
            return true;
        }
        // DeepLink
        if (queryActiviesNumber(url) > 0 && deepLink(url)) {
            return true;
        }
        // 手机里面没有页面能匹配到该链接，则判断是否需要拦截。
        if (this.mIsInterceptUnkownUrl) {
            LogUtils.i(TAG, "intercept InterceptUnkownScheme : " + url);
            return true;
        }
        return super.shouldOverrideUrlLoading(view, request);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.startsWith(HTTP_SCHEME) || url.startsWith(HTTPS_SCHEME)) {
            return (this.mWebClientHelper && HAS_ALIPAY_LIB && isAlipay(view, url));
        }
        if (!mWebClientHelper) {
            return super.shouldOverrideUrlLoading(view, url);
        }
        // 电话 ， 邮箱 ， 短信
        if (handleCommonLink(url)) {
            return true;
        }
        // Intent
        if (url.startsWith(INTENT_SCHEME)) {
            handleIntentUrl(url);
            return true;
        }
        // 微信支付
        if (url.startsWith(WEBCHAT_PAY_SCHEME)) {
            startActivity(url);
            return true;
        }
        // 支付宝
        if (url.startsWith(ALIPAYS_SCHEME) && lookup(url)) {
            return true;
        }
        // DeepLink
        if (queryActiviesNumber(url) > 0 && deepLink(url)) {
            return true;
        }
        // 手机里面没有页面能匹配到该链接，则判断是否需要拦截。
        if (mIsInterceptUnkownUrl) {
            LogUtils.i(TAG, "intercept InterceptUnkownScheme : " + url);
            return true;
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        mWaittingFinishSet.add(url);
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (!mErrorUrlsSet.contains(url) && mWaittingFinishSet.contains(url)) {
            if (mAgentWebUIController.get() != null) {
                mAgentWebUIController.get().onShowMainFrame();
            }
        }
        mWaittingFinishSet.remove(url);
        if (!mErrorUrlsSet.isEmpty()) {
            mErrorUrlsSet.clear();
        }
        super.onPageFinished(view, url);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        // 获取当前的网络请求是否是为main frame创建的.
        if (request.isForMainFrame()) {
            onMainFrameError(view,
                    error.getErrorCode(), error.getDescription().toString(),
                    request.getUrl().toString());
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        onMainFrameError(view, errorCode, description, failingUrl);
    }

    /**
     * https://blog.csdn.net/barryhappy/article/details/52733406
     * @param view
     * @param errorCode
     * @param description
     * @param failingUrl
     */
    protected void onMainFrameError(WebView view, int errorCode, String description, String failingUrl) {
        LogUtils.e(TAG, "onReceivedError：" + description + "  Code:" + errorCode);
        mErrorUrlsSet.add(failingUrl);
        // 下面逻辑判断开发者是否重写了 onMainFrameError 方法 ， 优先交给开发者处理
//        if (mWebClientHelper) {
//            Method mMethod = this.onMainFrameErrorMethod;
//            if (mMethod != null || (this.onMainFrameErrorMethod =
//                    mMethod = SuperAgentWebUtils.isExistMethod(mDelegate,
//                            "onMainFrameError", AbsAgentWebUIController.class,
//                            WebView.class, int.class, String.class, String.class)) != null) {
//                try {
//                    mMethod.invoke(mAgentWebUIController.get(), view, errorCode, description, failingUrl);
//                } catch (Throwable ignore) {
//                    if (LogUtils.isDebug()) {
//                        ignore.printStackTrace();
//                    }
//                }
//                return;
//            }
//        }
        if (mAgentWebUIController.get() != null) {
            mAgentWebUIController.get().onMainFrameError(view, errorCode, description, failingUrl);
        }
    }

    /**
     * 处理支付宝支付
     * @param view
     * @param url
     * @return
     */
    protected boolean isAlipay(final WebView view, String url) {
        try {
            Activity mActivity = null;
            if ((mActivity = mWeakReference.get()) == null) {
                return false;
            }
            /**
             * 推荐采用的新的二合一接口(payInterceptorWithUrl),只需调用一次
             */
            if (mPayTask == null) {
                Class<?> clazz = Class.forName("com.alipay.sdk.app.PayTask");
                Constructor<?> mConstructor = clazz.getConstructor(Activity.class);
                mPayTask = mConstructor.newInstance(mActivity);
            }
            final PayTask task = (PayTask) mPayTask;
            boolean isIntercepted = task.payInterceptorWithUrl(url, true, new H5PayCallback() {
                @Override
                public void onPayResult(final H5PayResultModel result) {
                    final String url = result.getReturnUrl();
                    if (!TextUtils.isEmpty(url)) {
                        SuperAgentWebUtils.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                view.loadUrl(url);
                            }
                        });
                    }
                }
            });
            if (isIntercepted) {
                LogUtils.i(TAG, "alipay-isIntercepted:" + isIntercepted + "  url:" + url);
            }
            return isIntercepted;
        } catch (Throwable ignore) {}
        return false;
    }

    /**
     * 处理 SMS、TEL、GEO、MAIL
     * @param url
     * @return
     */
    protected boolean handleCommonLink(String url) {
        if (url.startsWith(WebView.SCHEME_TEL)
                || url.startsWith(SCHEME_SMS)
                || url.startsWith(WebView.SCHEME_MAILTO)
                || url.startsWith(WebView.SCHEME_GEO)) {
            try {
                Activity mActivity = null;
                if ((mActivity = mWeakReference.get()) == null) {
                    return false;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                mActivity.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                if (SuperAgentWebConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        return false;
    }

    /**
     * handleIntentUrl
     * @param intentUrl
     */
    protected void handleIntentUrl(String intentUrl) {
        try {
            Intent intent = null;
            if (TextUtils.isEmpty(intentUrl) || !intentUrl.startsWith(INTENT_SCHEME)) {
                return;
            }
            if (lookup(intentUrl)) {
                return;
            }
        } catch (Throwable e) {
            if (LogUtils.isDebug()) {
                e.printStackTrace();
            }
        }
    }

    /**
     * DeepLink
     * @param url
     * @return
     */
    protected boolean deepLink(String url) {
        switch (mOpenOtherPageWays) {
            // 直接打开其他App
            case DERECT:
                lookup(url);
                return true;
            // 咨询用户是否打开其他App
            case ASK:
                Activity mActivity = null;
                if ((mActivity = mWeakReference.get()) == null) {
                    return false;
                }
                ResolveInfo resolveInfo = lookupResolveInfo(url);
                if (null == resolveInfo) {
                    return false;
                }
                ActivityInfo activityInfo = resolveInfo.activityInfo;
                LogUtils.e(TAG, "resolve package:" + resolveInfo.activityInfo.packageName + " app package:" + mActivity.getPackageName());
                if (activityInfo != null
                        && !TextUtils.isEmpty(activityInfo.packageName)
                        && activityInfo.packageName.equals(mActivity.getPackageName())) {
                    return lookup(url);
                }
                if (mAgentWebUIController.get() != null) {
                    mAgentWebUIController.get()
                            .onOpenPagePrompt(this.mWebView,
                                    mWebView.getUrl(),
                                    getCallback(url));
                }
                return true;
            // 默认不打开
            default:
                return false;
        }
    }

    /**
     * Intent lookup
     * @param url
     * @return
     */
    protected boolean lookup(String url) {
        try {
            Intent intent;
            Activity mActivity = null;
            if ((mActivity = mWeakReference.get()) == null) {
                return true;
            }
            PackageManager packageManager = mActivity.getPackageManager();
            intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            // 跳到该应用
            if (info != null) {
                mActivity.startActivity(intent);
                return true;
            }
        } catch (Throwable ignore) {
            if (LogUtils.isDebug()) {
                ignore.printStackTrace();
            }
        }
        return false;
    }



    /**
     * lookupResolveInfo
     * @param url
     * @return
     */
    protected ResolveInfo lookupResolveInfo(String url) {
        try {
            Intent intent;
            Activity mActivity = null;
            if ((mActivity = mWeakReference.get()) == null) {
                return null;
            }
            PackageManager packageManager = mActivity.getPackageManager();
            intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            return info;
        } catch (Throwable ignore) {
            if (LogUtils.isDebug()) {
                ignore.printStackTrace();
            }
        }
        return null;
    }

    /**
     * ASK 回调
     * @param url
     * @return
     */
    protected Callback<Integer> getCallback(String url) {
        final String copyUrl = String.copyValueOf(url.toCharArray());
        return new Callback<Integer>() {

            @Override
            public void handleValue(Integer value) {
                if (value == 1) {
                    lookup(copyUrl);
                }
            }

        };
    }

    /**
     *q ueryActiviesNumber
     * @param url
     * @return
     */
    protected int queryActiviesNumber(String url) {
        try {
            if (mWeakReference.get() == null) {
                return 0;
            }
            Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            PackageManager mPackageManager = mWeakReference.get().getPackageManager();
            List<ResolveInfo> mResolveInfos = mPackageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            return mResolveInfos == null ? 0 : mResolveInfos.size();
        } catch (URISyntaxException ignore) {
            if (LogUtils.isDebug()) {
                ignore.printStackTrace();
            }
            return 0;
        }
    }

    /**
     * startActivity
     * @param url
     */
    protected void startActivity(String url) {
        try {
            if (mWeakReference.get() == null) {
                return;
            }
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            mWeakReference.get().startActivity(intent);

        } catch (Exception e) {
            if (LogUtils.isDebug()) {
                e.printStackTrace();
            }
        }
    }

    public static Builder createBuilder() {
        return new Builder();
    }

    /**
     * Builder
     */
    public static class Builder {
        private Activity mActivity;
        private WebViewClient mClient;
        private boolean mWebClientHelper = true;
        private WebView mWebView;
        private boolean mIsInterceptUnkownUrl = true;
        private OpenOtherPageWays mOpenOtherPageWays;

        public Builder setActivity(Activity activity) {
            this.mActivity = activity;
            return this;
        }

        public Builder setClient(WebViewClient client) {
            this.mClient = client;
            return this;
        }

        public Builder setWebClientHelper(boolean webClientHelper) {
            this.mWebClientHelper = webClientHelper;
            return this;
        }

        public Builder setWebView(WebView webView) {
            this.mWebView = webView;
            return this;
        }

        public Builder setInterceptUnkownUrl(boolean isInterceptUnkownUrl) {
            this.mIsInterceptUnkownUrl = isInterceptUnkownUrl;
            return this;
        }

        public Builder setOpenOtherPageWays(OpenOtherPageWays openOtherPageWays) {
            this.mOpenOtherPageWays = openOtherPageWays;
            return this;
        }

        public DefaultWebClient build() {
            return new DefaultWebClient(this);
        }
    }

}
