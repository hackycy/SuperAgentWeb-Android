package com.siyee.superagentweb;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.siyee.superagentweb.abs.AbsAgentWebSettings;
import com.siyee.superagentweb.abs.AbsAgentWebUIController;
import com.siyee.superagentweb.abs.EventInterceptor;
import com.siyee.superagentweb.abs.IAgentWebSettings;
import com.siyee.superagentweb.abs.IEventHandler;
import com.siyee.superagentweb.abs.IUrlLoader;
import com.siyee.superagentweb.abs.IVideo;
import com.siyee.superagentweb.abs.IWebLayout;
import com.siyee.superagentweb.abs.IWebLifeCycle;
import com.siyee.superagentweb.abs.IndicatorController;
import com.siyee.superagentweb.abs.JsAccessEntrace;
import com.siyee.superagentweb.abs.PermissionInterceptor;
import com.siyee.superagentweb.abs.WebCreator;
import com.siyee.superagentweb.abs.WebListenerManager;
import com.siyee.superagentweb.bridge.IExecutorFactory;
import com.siyee.superagentweb.bridge.InternalBridge;
import com.siyee.superagentweb.impl.AgentWebUIControllerImplBase;
import com.siyee.superagentweb.impl.DefaultAgentWebSettings;
import com.siyee.superagentweb.impl.DefaultChromeClient;
import com.siyee.superagentweb.impl.DefaultJsAccessEntrace;
import com.siyee.superagentweb.impl.DefaultWebClient;
import com.siyee.superagentweb.impl.DefaultWebCreator;
import com.siyee.superagentweb.impl.DefaultWebLifeCycle;
import com.siyee.superagentweb.impl.EventHandlerImpl;
import com.siyee.superagentweb.impl.UrlLoaderImpl;
import com.siyee.superagentweb.impl.VideoImpl;
import com.siyee.superagentweb.utils.CookieUtils;
import com.siyee.superagentweb.utils.SuperAgentWebUtils;
import com.siyee.superagentweb.widget.BaseIndicatorView;
import com.siyee.superagentweb.widget.WebParentLayout;

import java.lang.ref.WeakReference;

/**
 * @author hackycy
 */
public class SuperAgentWeb {

    /**
     * SuperAgentWeb 's TAG
     */
    private static final String TAG = SuperAgentWeb.class.getSimpleName();

    /**
     * Activity
     */
    private Activity mActivity;

    /**
     * 承载 WebParentLayout 的 ViewGroup
     */
    private ViewGroup mViewGroup;

    /**
     * 负责创建布局 WebView ，WebParentLayout  Indicator等。
     */
    private WebCreator mWebCreator;

    /**
     * 管理 WebSettings
     */
    private IAgentWebSettings mAgentWebSettings;

    /**
     * IndicatorController 控制Indicator
     */
    private IndicatorController mIndicatorController;

    /**
     * WebChromeClient
     */
    private WebChromeClient mWebChromeClient;

    /**
     * mWebViewClient
     */
    private WebViewClient mWebViewClient;

    /**
     * is show indicator
     */
    private boolean mEnableIndicator;

    /**
     * 处理WebView相关返回事件
     */
    private IEventHandler mEventHandler;

    /**
     * flag
     */
    private int mTagTarget = 0;
    /**
     * WebListenerManager
     */
    private WebListenerManager mWebListenerManager;

    /**
     * Activity
     */
    private static final int ACTIVITY_TAG = 0;
    /**
     * Fragment
     */
    private static final int FRAGMENT_TAG = 1;

    /**
     * URL Loader ， 提供了 WebView#loadUrl(url) reload() stopLoading（） postUrl()等方法
     */
    private IUrlLoader mIUrlLoader = null;

    /**
     * WebView 生命周期 ， 跟随生命周期释放CPU
     */
    private IWebLifeCycle mWebLifeCycle;

    /**
     * Video 视屏播放管理类
     */
    private IVideo mIVideo = null;

    /**
     * PermissionInterceptor 权限拦截
     */
    private PermissionInterceptor mPermissionInterceptor;

    /**
     * 是否拦截未知的Url
     */
    private boolean mIsInterceptUnkownUrl = false;

    /**
     * deepLink 打开方式
     */
    private OpenOtherPageWays mOpenOtherPageWays = OpenOtherPageWays.ASK;

    /**
     * WebViewClient 辅助控制开关
     */
    private boolean mWebClientHelper = true;

    /**
     * 事件拦截
     */
    private EventInterceptor mEventInterceptor;

    /**
     * 提供快速JS方法调用
     */
    private JsAccessEntrace mJsAccessEntrace;

    /**
     * Internal JS Bridge
     */
    private InternalBridge mInternalBridge;

    /**
     * constructor
     * @param builder
     */
    private SuperAgentWeb(Builder builder) {
        mTagTarget = builder.mTag;
        this.mActivity = builder.mActivity;
        this.mViewGroup = builder.mViewGroup;
        // call init something pre
        init();
        this.mEventHandler = builder.mEventHandler;
        this.mEnableIndicator = builder.mEnableIndicator;
        this.mWebCreator = builder.mWebCreator == null ? configWebCreator(builder.mBaseIndicatorView, builder.mIndex, builder.mLayoutParams,
                builder.mIndicatorColor, builder.mHeight, builder.mWebView, builder.mWebLayout) : builder.mWebCreator;
        this.mIndicatorController = builder.mIndicatorController;
        this.mWebChromeClient = builder.mWebChromeClient;
        this.mWebViewClient = builder.mWebViewClient;
        this.mAgentWebSettings = builder.mAgentWebSettings;
        this.mPermissionInterceptor = builder.mPermissionInterceptor == null ? null : new PermissionInterceptorWrapper(builder.mPermissionInterceptor);
        this.mIUrlLoader = new UrlLoaderImpl(mWebCreator.create().getWebView());
        if (this.mWebCreator.getWebParentLayout() instanceof WebParentLayout) {
            WebParentLayout mWebParentLayout = (WebParentLayout) this.mWebCreator.getWebParentLayout();
            mWebParentLayout.bindController(builder.mAgentWebUIController == null ? AgentWebUIControllerImplBase.build() : builder.mAgentWebUIController);
            mWebParentLayout.setErrorLayoutRes(builder.mErrorLayout, builder.mReloadId);
            mWebParentLayout.setErrorView(builder.mErrorView);
        }
        this.mWebLifeCycle = new DefaultWebLifeCycle(this.mWebCreator.getWebView());
        this.mWebClientHelper = builder.mWebClientHelper;
        this.mIsInterceptUnkownUrl = builder.mIsInterceptUnkownUrl;
        if (builder.mOpenOtherPageWays != null) {
            this.mOpenOtherPageWays = builder.mOpenOtherPageWays;
        }
        this.mInternalBridge = new InternalBridge(builder.mExecutorFactory, this.mWebCreator.getWebView(), this.getJsAccessEntrace());
    }

    /**
     * init
     */
    private void init() {
        if (this.mActivity != null) {
            SuperAgentWebUtils.init(mActivity.getApplication());
        }
    }

    /**
     * prepare webview
     * @return
     */
    private SuperAgentWeb ready() {
        CookieUtils.initCookiesManager(mActivity.getApplicationContext());
        IAgentWebSettings agentWebSettings = this.mAgentWebSettings;
        if (agentWebSettings == null) {
            this.mAgentWebSettings = agentWebSettings = new DefaultAgentWebSettings();
        }
        if (agentWebSettings instanceof AbsAgentWebSettings) {
            ((AbsAgentWebSettings) agentWebSettings).bindAgentWeb(this);
        }
        if (this.mWebListenerManager == null && agentWebSettings instanceof AbsAgentWebSettings) {
            mWebListenerManager = (WebListenerManager) agentWebSettings;
        }
        agentWebSettings.toSetting(mWebCreator.getWebView());
        if (mWebListenerManager != null) {
            mWebListenerManager.setDownloader(mWebCreator.getWebView(), null);
            mWebListenerManager.setWebChromeClient(mWebCreator.getWebView(), getWebChromeClient());
            mWebListenerManager.setWebViewClient(mWebCreator.getWebView(), getWebViewClient());
        }
        // Inject JSBridge
        this.mWebCreator.getWebView().addJavascriptInterface(this.mInternalBridge, InternalBridge.INTERNAL_BRIDGE_NAME);
        return this;
    }

    /**
     * create WebCreator
     * @param progressView
     * @param index
     * @param lp
     * @param indicatorColor
     * @param height_dp
     * @param webView
     * @param webLayout
     * @return
     */
    private WebCreator configWebCreator(BaseIndicatorView progressView,
                                        int index,
                                        ViewGroup.LayoutParams lp,
                                        int indicatorColor,
                                        int height_dp,
                                        WebView webView,
                                        IWebLayout webLayout) {
        if (progressView != null && mEnableIndicator) {
            return new DefaultWebCreator(mActivity, mViewGroup, lp, index, progressView, webView, webLayout);
        }
        return mEnableIndicator ?
                new DefaultWebCreator(mActivity, mViewGroup, lp, index, indicatorColor, height_dp, webView, webLayout)
                : new DefaultWebCreator(mActivity, mViewGroup, lp, index, webView, webLayout);
    }

    /**
     * get WebViewClient
     * @return
     */
    private android.webkit.WebViewClient getWebViewClient() {
        DefaultWebClient defaultWebClient = DefaultWebClient.createBuilder()
                .setActivity(this.mActivity)
                .setWebClientHelper(this.mWebClientHelper)
                .setWebView(this.mWebCreator.getWebView())
                .setInterceptUnkownUrl(this.mIsInterceptUnkownUrl)
                .setOpenOtherPageWays(this.mOpenOtherPageWays)
                .build();
        return this.mWebViewClient != null ? this.mWebViewClient : defaultWebClient;
    }

    /**
     * get WebChromeClient
     * @return
     */
    private android.webkit.WebChromeClient getWebChromeClient() {
        IndicatorController mIndicatorController =
                (this.mIndicatorController == null) ?
                        IndicatorHandler.getInstance().inJectIndicator(mWebCreator.offer())
                        : this.mIndicatorController;
        DefaultChromeClient defaultChromeClient =
                new DefaultChromeClient(this.mActivity,
                        this.mIndicatorController = mIndicatorController, this.mIVideo = getIVideo(),
                        this.mPermissionInterceptor, mWebCreator.getWebView());
        return this.mWebChromeClient != null ? this.mWebChromeClient : defaultChromeClient;
    }

    /**
     * Video
     * @return
     */
    private IVideo getIVideo() {
        return mIVideo == null ? new VideoImpl(mActivity, mWebCreator.getWebView()) : mIVideo;
    }

    /**
     * EventInterceptor
     * @return
     */
    private EventInterceptor getInterceptor() {
        if (this.mEventInterceptor != null) {
            return this.mEventInterceptor;
        }
        if (mIVideo instanceof VideoImpl) {
            return this.mEventInterceptor = (EventInterceptor) this.mIVideo;
        }
        return null;
    }

    //----------------------- Expose -------------------------------

    public static Builder with(@NonNull Activity activity) {
        //noinspection ConstantConditions
        if (activity == null) {
            throw new NullPointerException("activity can not be null .");
        }
        return new Builder(activity);
    }

    public static Builder with(@NonNull Fragment fragment) {
        Activity mActivity = null;
        if ((mActivity = fragment.getActivity()) == null) {
            throw new NullPointerException("activity can not be null .");
        }
        return new Builder(mActivity, fragment);
    }

    public PermissionInterceptor getPermissionInterceptor() {
        return this.mPermissionInterceptor;
    }

    public SuperAgentWeb clearWebCache() {
        if (this.getWebCreator().getWebView() != null) {
            SuperAgentWebUtils.clearWebViewAllCache(mActivity, this.getWebCreator().getWebView());
        } else {
            SuperAgentWebUtils.clearWebViewAllCache(mActivity);
        }
        return this;
    }

    public boolean back() {
        if (this.mEventHandler == null) {
            mEventHandler = EventHandlerImpl.getInstantce(mWebCreator.getWebView(), getInterceptor());
        }
        return mEventHandler.back();
    }

    @SuppressLint("JavascriptInterface")
    public void addJavascriptInterface(Object object, String name) {
        this.getWebCreator().getWebView().addJavascriptInterface(object, name);
    }

    public boolean handleKeyEvent(int keyCode, KeyEvent keyEvent) {
        if (this.mEventHandler == null) {
            this.mEventHandler = EventHandlerImpl.getInstantce(mWebCreator.getWebView(), getInterceptor());
        }
        return this.mEventHandler.onKeyDown(keyCode, keyEvent);
    }

    public WebCreator getWebCreator() {
        return this.mWebCreator;
    }

    public IEventHandler getIEventHandler() {
        return this.mEventHandler == null ? (this.mEventHandler = EventHandlerImpl.getInstantce(mWebCreator.getWebView(), getInterceptor())) : this.mEventHandler;
    }

    public IUrlLoader getUrlLoader() {
        return this.mIUrlLoader;
    }

    public IndicatorController getIndicatorController() {
        return this.mIndicatorController;
    }

    public IAgentWebSettings getAgentWebSettings() {
        return this.mAgentWebSettings;
    }

    public JsAccessEntrace getJsAccessEntrace() {
        if (this.mJsAccessEntrace == null) {
            this.mJsAccessEntrace = new DefaultJsAccessEntrace(this.mWebCreator.getWebView());
        }
        return this.mJsAccessEntrace;
    }

    public IWebLifeCycle getWebLifeCycle() {
        return this.mWebLifeCycle;
    }

    public Activity getActivity() {
        return this.mActivity;
    }

    public void go(String url) {
        this.mIUrlLoader.loadUrl(url);
    }

    public void onResume() {
        this.mWebLifeCycle.onResume();
    }

    public void onPause() {
        this.mWebLifeCycle.onPause();
    }

    public void onDestroy() {
        this.mWebLifeCycle.onDestroy();
    }

    /**
     * Builder
     */
    public static class Builder {

        /** Context **/
        private Activity mActivity;
        private Fragment mFragment;
        private int mTag = -1;

        /** UI **/
        private ViewGroup mViewGroup;
        private ViewGroup.LayoutParams mLayoutParams;
        private int mIndex;
        private WebCreator mWebCreator;
        private IndicatorController mIndicatorController;
        /** 进度条默认显示 **/
        private boolean mEnableIndicator = true;
        private int mIndicatorColor = -1;
        private int mHeight = -1;
        private BaseIndicatorView mBaseIndicatorView;
        private boolean mIsNeedDefaultProgress = true;
        /** Parent **/
        private IWebLayout mWebLayout;
        private View mErrorView;
        @LayoutRes
        private int mErrorLayout;
        @IdRes
        private int mReloadId;

        /** 功能相关 **/
        private boolean mIsInterceptUnkownUrl = false;
        private OpenOtherPageWays mOpenOtherPageWays;
        private boolean mWebClientHelper = true;
        private WebView mWebView;
        private IAgentWebSettings mAgentWebSettings;
        private PermissionInterceptor mPermissionInterceptor;
        private AbsAgentWebUIController mAgentWebUIController;
        private IEventHandler mEventHandler;
        private IExecutorFactory mExecutorFactory;

        /** Client And Middleware **/
        private WebChromeClient mWebChromeClient;
        private WebViewClient mWebViewClient;

        public Builder(@NonNull Activity activity, @NonNull Fragment fragment) {
            mActivity = activity;
            mFragment = fragment;
            mTag = SuperAgentWeb.FRAGMENT_TAG;
        }

        public Builder(@NonNull Activity activity) {
            mActivity = activity;
            mTag = SuperAgentWeb.ACTIVITY_TAG;
        }

        public Builder setAgentWebParent(@NonNull ViewGroup viewGroup, ViewGroup.LayoutParams lp) {
            this.mViewGroup = viewGroup;
            this.mLayoutParams = lp;
            return this;
        }

        public Builder setAgentWebParent(@NonNull ViewGroup viewGroup, int index, ViewGroup.LayoutParams lp) {
            this.mViewGroup = viewGroup;
            this.mIndex = index;
            this.mLayoutParams = lp;
            return this;
        }

        public Builder useDefaultIndicator() {
            this.mEnableIndicator = true;
            return this;
        }

        public Builder useDefaultIndicator(int color) {
            this.mEnableIndicator = true;
            this.mIndicatorColor = color;
            return this;
        }

        public Builder useDefaultIndicator(int color, int height_dp) {
            this.mEnableIndicator = true;
            this.mIndicatorColor = color;
            this.mHeight = height_dp;
            return this;
        }

        public Builder setCustomIndicator(@Nullable BaseIndicatorView indicatorView) {
            if (indicatorView != null) {
                this.mEnableIndicator = true;
                this.mBaseIndicatorView = indicatorView;
                this.mIsNeedDefaultProgress = false;
            } else {
                this.mEnableIndicator = true;
                this.mIsNeedDefaultProgress = true;
            }
            return this;
        }

        public Builder closeIndicator() {
            this.mEnableIndicator = false;
            return this;
        }

        public Builder setWebLayout(@Nullable IWebLayout webLayout) {
            this.mWebLayout = webLayout;
            return this;
        }

        public Builder setMainFrameErrorView(@Nullable View errorView) {
            this.mErrorView = errorView;
            return this;
        }

        public Builder setMainFrameErrorView(@LayoutRes int errorLayout, @IdRes int clickId) {
            this.mErrorLayout = errorLayout;
            this.mReloadId = clickId;
            return this;
        }

        public Builder interceptUnkownUrl() {
            this.mIsInterceptUnkownUrl = true;
            return this;
        }

        public Builder setOpenOtherPageWays(@Nullable OpenOtherPageWays openOtherPageWays) {
            this.mOpenOtherPageWays = openOtherPageWays;
            return this;
        }

        public Builder closeWebViewClientHelper() {
            this.mWebClientHelper = false;
            return this;
        }

        public Builder setWebView(@Nullable WebView webView) {
            this.mWebView = webView;
            return this;
        }

        public Builder setAgentWebSettings(@Nullable IAgentWebSettings webSettings) {
            this.mAgentWebSettings = webSettings;
            return this;
        }

        public Builder setPermissionInterceptor(@Nullable PermissionInterceptor permissionInterceptor) {
            this.mPermissionInterceptor = permissionInterceptor;
            return this;
        }

        public Builder setAgentWebUIController(@Nullable AgentWebUIControllerImplBase agentWebUIController) {
            this.mAgentWebUIController = agentWebUIController;
            return this;
        }

        public Builder setEventHanadler(@Nullable IEventHandler iEventHandler) {
            this.mEventHandler = iEventHandler;
            return this;
        }

        public Builder setExecutorFactory(@Nullable IExecutorFactory executorFactory) {
            this.mExecutorFactory = executorFactory;
            return this;
        }

        public Builder setWebChromeClient(@Nullable WebChromeClient chromeClient) {
            this.mWebChromeClient = chromeClient;
            return this;
        }

        public Builder setWebViewClient(@Nullable WebViewClient webViewClient) {
            this.mWebViewClient = webViewClient;
            return this;
        }

        /**
         * create
         * @return
         */
        public SuperAgentWeb build() {
            if (mTag == SuperAgentWeb.FRAGMENT_TAG && this.mViewGroup == null) {
                throw new NullPointerException("ViewGroup is null,Please check your parameters .");
            }
            return new SuperAgentWeb(this).ready();
        }

    }

    /**
     * PermissionInterceptorWrapper
     */
    private static final class PermissionInterceptorWrapper implements PermissionInterceptor {

        private WeakReference<PermissionInterceptor> mWeakReference;

        private PermissionInterceptorWrapper(PermissionInterceptor permissionInterceptor) {
            this.mWeakReference = new WeakReference<PermissionInterceptor>(permissionInterceptor);
        }

        @Override
        public boolean intercept(String url, String[] permissions, String a) {
            if (this.mWeakReference.get() == null) {
                return false;
            }
            return mWeakReference.get().intercept(url, permissions, a);
        }
    }

}
