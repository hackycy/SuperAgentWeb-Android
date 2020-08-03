package com.siyee.superagentweb;

import android.app.Activity;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;

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
     * flag security 's mode
     */
    private SecurityType mSecurityType = SecurityType.DEFAULT_CHECK;

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
    private int mUrlHandleWays = -1;

    /**
     * 事件拦截
     */
    private EventInterceptor mEventInterceptor;



}
