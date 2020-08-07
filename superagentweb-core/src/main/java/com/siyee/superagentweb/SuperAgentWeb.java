package com.siyee.superagentweb;

import android.app.Activity;
import android.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.siyee.superagentweb.abs.EventInterceptor;
import com.siyee.superagentweb.abs.IAgentWebSettings;
import com.siyee.superagentweb.abs.IEventHandler;
import com.siyee.superagentweb.abs.IUrlLoader;
import com.siyee.superagentweb.abs.IVideo;
import com.siyee.superagentweb.abs.IWebLayout;
import com.siyee.superagentweb.abs.IWebLifeCycle;
import com.siyee.superagentweb.abs.IndicatorController;
import com.siyee.superagentweb.abs.PermissionInterceptor;
import com.siyee.superagentweb.abs.WebCreator;
import com.siyee.superagentweb.abs.WebListenerManager;
import com.siyee.superagentweb.middleware.MiddlewareWebChromeBase;
import com.siyee.superagentweb.middleware.MiddlewareWebClientBase;
import com.siyee.superagentweb.widget.BaseIndicatorView;

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
     * SuperAgentWeb
     */
    private SuperAgentWeb mSuperAgentWeb = null;

    /**
     * IndicatorController 控制Indicator
     */
    private IndicatorController mIndicatorController;

    /**
     * WebChromeClient
     */
    private com.siyee.superagentweb.WebChromeClient mWebChromeClient;

    /**
     * mWebViewClient
     */
    private com.siyee.superagentweb.WebViewClient mWebViewClient;

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
    private OpenOtherPageWays mUrlHandleWays = OpenOtherPageWays.ASK;

    /**
     * 事件拦截
     */
    private EventInterceptor mEventInterceptor;

    /**
     * MiddlewareWebClientBase WebViewClient 中间件
     */
    private MiddlewareWebClientBase mMiddleWrareWebClientBaseHeader;
    /**
     * MiddlewareWebChromeBase WebChromeClient 中间件
     */
    private MiddlewareWebChromeBase mMiddlewareWebChromeBaseHeader;

    /**
     * @return PermissionInterceptor 权限控制者
     */
    public PermissionInterceptor getPermissionInterceptor() {
        return this.mPermissionInterceptor;
    }

    public IWebLifeCycle getWebLifeCycle() {
        return this.mWebLifeCycle;
    }

    public static Builder with(@NonNull Activity activity) {
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

    /**
     * constructor
     * @param builder
     */
    private SuperAgentWeb(Builder builder) {
        mTagTarget = builder.mTag;
    }

    public SuperAgentWeb ready() {
        return this;
    }

    /**
     * Builder
     */
    public static class Builder {

        /** Context **/
        private Activity mActivity;
        private Fragment mFragment;
        private int mTag;

        /** UI **/
        private ViewGroup mViewGroup;
        private ViewGroup.LayoutParams mLayoutParams;
        private int mIndex;
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
        private OpenOtherPageWays mOpenOtherPageWays = OpenOtherPageWays.ASK;

        private WebView mWebView;
        private IAgentWebSettings mAgentWebSettings;

        /** Client And Middleware **/
        private com.siyee.superagentweb.WebChromeClient mWebChromeClient;
        private com.siyee.superagentweb.WebViewClient mWebViewClient;
        private MiddlewareWebClientBase mMiddlewareWebClientBaseHeader;
        private MiddlewareWebClientBase mMiddlewareWebClientBaseTail;
        private MiddlewareWebChromeBase mMiddlewareWebChromeBaseHeader;
        private MiddlewareWebChromeBase mMiddlewareWebChromeBaseTail;

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

        public Builder setWebView(@Nullable WebView webView) {
            this.mWebView = webView;
            return this;
        }

        public Builder setAgentWebSettings(@Nullable IAgentWebSettings webSettings) {
            this.mAgentWebSettings = webSettings;
            return this;
        }

        public Builder setWebChromeClient(@Nullable com.siyee.superagentweb.WebChromeClient chromeClient) {
            this.mWebChromeClient = chromeClient;
            return this;
        }

        public Builder setWebViewClient(@Nullable com.siyee.superagentweb.WebViewClient webViewClient) {
            this.mWebViewClient = webViewClient;
            return this;
        }

        public Builder useMiddlewareWebClient(@Nullable MiddlewareWebClientBase middlewareWebClientBase) {
            if (middlewareWebClientBase == null) {
                return this;
            }
            if (this.mMiddlewareWebClientBaseHeader == null) {
                this.mMiddlewareWebClientBaseHeader = this.mMiddlewareWebClientBaseTail = middlewareWebClientBase;
            } else {
                this.mMiddlewareWebClientBaseTail.enq(middlewareWebClientBase);
                this.mMiddlewareWebClientBaseTail = middlewareWebClientBase;
            }
            return this;
        }

        public Builder useMiddlewareWebChrome(@Nullable MiddlewareWebChromeBase middlewareWebChromeBase) {
            if (middlewareWebChromeBase == null) {
                return this;
            }
            if (this.mMiddlewareWebChromeBaseHeader == null) {
                this.mMiddlewareWebChromeBaseHeader = this.mMiddlewareWebChromeBaseTail = middlewareWebChromeBase;
            } else {
                this.mMiddlewareWebChromeBaseTail.enq(middlewareWebChromeBase);
                this.mMiddlewareWebChromeBaseTail = middlewareWebChromeBase;
            }
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

}
