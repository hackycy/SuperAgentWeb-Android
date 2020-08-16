package com.siyee.superagentweb.impl;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.siyee.superagentweb.R;
import com.siyee.superagentweb.abs.BaseIndicatorSpec;
import com.siyee.superagentweb.abs.IWebLayout;
import com.siyee.superagentweb.abs.WebCreator;
import com.siyee.superagentweb.utils.AgentWebUtils;
import com.siyee.superagentweb.utils.LogUtils;
import com.siyee.superagentweb.widget.BaseIndicatorView;
import com.siyee.superagentweb.widget.LollipopFixedWebView;
import com.siyee.superagentweb.widget.WebIndicator;
import com.siyee.superagentweb.widget.WebParentLayout;

/**
 * @author hackycy
 */
public class DefaultWebCreator implements WebCreator {

    private Activity mActivity;
    private ViewGroup mViewGroup;
    private boolean mIsNeedDefaultProgress;
    private int mIndex;
    private BaseIndicatorView mProgressView;
    private ViewGroup.LayoutParams mLayoutParams = null;
    private int mColor = -1;
    /**
     * 单位dp
     */
    private int mHeight;
    private boolean mIsCreated = false;
    private IWebLayout mIWebLayout;
    private BaseIndicatorSpec mBaseIndicatorSpec;
    private WebView mWebView = null;
    private FrameLayout mFrameLayout = null;
    private View mTargetProgress;
    private static final String TAG = DefaultWebCreator.class.getSimpleName();


    /**
     * 使用默认的进度条
     *
     * @param activity
     * @param viewGroup
     * @param lp
     * @param index
     * @param color
     * @param mHeight
     * @param webView
     * @param webLayout
     */
    public DefaultWebCreator(@NonNull Activity activity,
                                @Nullable ViewGroup viewGroup,
                                ViewGroup.LayoutParams lp,
                                int index,
                                int color,
                                int mHeight,
                                WebView webView,
                                IWebLayout webLayout) {
        this.mActivity = activity;
        this.mViewGroup = viewGroup;
        this.mIsNeedDefaultProgress = true;
        this.mIndex = index;
        this.mColor = color;
        this.mLayoutParams = lp;
        this.mHeight = mHeight;
        this.mWebView = webView;
        this.mIWebLayout = webLayout;
    }

    /**
     * 关闭进度条
     *
     * @param activity
     * @param viewGroup
     * @param lp
     * @param index
     * @param webView
     * @param webLayout
     */
    public DefaultWebCreator(@NonNull Activity activity,
                                @Nullable ViewGroup viewGroup,
                                ViewGroup.LayoutParams lp,
                                int index,
                                @Nullable WebView webView,
                                IWebLayout webLayout) {
        this.mActivity = activity;
        this.mViewGroup = viewGroup;
        this.mIsNeedDefaultProgress = false;
        this.mIndex = index;
        this.mLayoutParams = lp;
        this.mWebView = webView;
        this.mIWebLayout = webLayout;
    }

    /**
     * 自定义Indicator
     *
     * @param activity
     * @param viewGroup
     * @param lp
     * @param index
     * @param progressView
     * @param webView
     * @param webLayout
     */
    public DefaultWebCreator(@NonNull Activity activity,
                                @Nullable ViewGroup viewGroup,
                                ViewGroup.LayoutParams lp,
                                int index,
                                BaseIndicatorView progressView,
                                WebView webView,
                                IWebLayout webLayout) {
        this.mActivity = activity;
        this.mViewGroup = viewGroup;
        this.mIsNeedDefaultProgress = false;
        this.mIndex = index;
        this.mLayoutParams = lp;
        this.mProgressView = progressView;
        this.mWebView = webView;
        this.mIWebLayout = webLayout;
    }

    public void setWebView(WebView webView) {
        mWebView = webView;
    }

    public FrameLayout getFrameLayout() {
        return mFrameLayout;
    }


    public View getTargetProgress() {
        return mTargetProgress;
    }

    public void setTargetProgress(View targetProgress) {
        this.mTargetProgress = targetProgress;
    }

    @Override
    public WebCreator create() {
        if (mIsCreated) {
            return this;
        }
        mIsCreated = true;
        ViewGroup mViewGroup = this.mViewGroup;
        if (mViewGroup == null) {
            mViewGroup = this.mFrameLayout = (FrameLayout) createLayout();
            mActivity.setContentView(mViewGroup);
        } else {
            if (mIndex == -1) {
                mViewGroup.addView(this.mFrameLayout = (FrameLayout) createLayout(), mLayoutParams);
            } else {
                mViewGroup.addView(this.mFrameLayout = (FrameLayout) createLayout(), mIndex, mLayoutParams);
            }
        }
        return this;
    }

    private ViewGroup createLayout() {
        Activity mActivity = this.mActivity;
        WebParentLayout mFrameLayout = new WebParentLayout(mActivity);
        mFrameLayout.setId(R.id.web_parent_layout_id);
        mFrameLayout.setBackgroundColor(Color.WHITE);
        View target = mIWebLayout == null ? (this.mWebView = createWebView()) : webLayout();
        FrameLayout.LayoutParams mLayoutParams = new FrameLayout.LayoutParams(-1, -1);
        mFrameLayout.addView(target, mLayoutParams);
        mFrameLayout.bindWebView(this.mWebView);
        LogUtils.i(TAG, "  instanceof  AgentWebView:" + (this.mWebView instanceof LollipopFixedWebView));
//        if (this.mWebView instanceof LollipopFixedWebView) {
//            this.mWebViewType = AgentWebConfig.WEBVIEW_AGENTWEB_SAFE_TYPE;
//        }
        ViewStub mViewStub = new ViewStub(mActivity);
        mViewStub.setId(R.id.mainframe_error_viewsub_id);
        mFrameLayout.addView(mViewStub, new FrameLayout.LayoutParams(-1, -1));
        if (mIsNeedDefaultProgress) {
            FrameLayout.LayoutParams lp = null;
            WebIndicator mWebIndicator = new WebIndicator(mActivity);
            if (mHeight > 0) {
                lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, AgentWebUtils.dp2px(mActivity, mHeight));
            } else {
                lp = mWebIndicator.offerLayoutParams();
            }
            if (mColor != -1) {
                mWebIndicator.setColor(mColor);
            }
            lp.gravity = Gravity.TOP;
            mFrameLayout.addView((View) (this.mBaseIndicatorSpec = mWebIndicator), lp);
            mWebIndicator.setVisibility(View.GONE);
        } else if (!mIsNeedDefaultProgress && mProgressView != null) {
            mFrameLayout.addView((View) (this.mBaseIndicatorSpec = (BaseIndicatorSpec) mProgressView), mProgressView.offerLayoutParams());
            mProgressView.setVisibility(View.GONE);
        }
        return mFrameLayout;
    }

    private View webLayout() {
        WebView mWebView = null;
        if ((mWebView = mIWebLayout.getWebView()) == null) {
            mWebView = createWebView();
            mIWebLayout.getLayout().addView(mWebView, -1, -1);
            LogUtils.i(TAG, "add webview");
        }
        this.mWebView = mWebView;
        return mIWebLayout.getLayout();
    }

    private WebView createWebView() {
        WebView mWebView = null;
        if (this.mWebView != null) {
            mWebView = this.mWebView;
        } else {
            mWebView = new LollipopFixedWebView(mActivity);
        }
        return mWebView;
    }

    @Override
    public WebView getWebView() {
        return mWebView;
    }

    @Override
    public FrameLayout getWebParentLayout() {
        return mFrameLayout;
    }

    @Override
    public BaseIndicatorSpec offer() {
        return mBaseIndicatorSpec;
    }
}
