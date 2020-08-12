package com.siyee.superagentweb.filechooser;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.siyee.superagentweb.AgentWebPermissions;
import com.siyee.superagentweb.R;
import com.siyee.superagentweb.abs.AbsAgentWebUIController;
import com.siyee.superagentweb.abs.Callback;
import com.siyee.superagentweb.abs.PermissionInterceptor;
import com.siyee.superagentweb.utils.AgentWebUtils;
import com.siyee.superagentweb.utils.FileChooserUtils;
import com.siyee.superagentweb.utils.PermissionUtils;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.siyee.superagentweb.utils.FileChooserUtils.KEY_URI;

/**
 * @author hackycy
 */
public class FileChooser {

    /**
     * TAG
     */
    private static final String TAG = FileChooser.class.getSimpleName();

    /**
     * Activity
     */
    private Activity mActivity;

    /**
     * ValueCallback
     */
    private ValueCallback<Uri> mUriValueCallback;

    /**
     * ValueCallback<Uri[]> After LOLLIPOP
     */
    private ValueCallback<Uri[]> mUriValueCallbacks;

    /**
     * 当前系统是否高于 Android 5.0 ；
     */
    private boolean mIsAboveLollipop = false;

    /**
     * WebChromeClient.FileChooserParams 封装了 Intent ，mAcceptType  等参数
     */
    private WebChromeClient.FileChooserParams mFileChooserParams;

    /**
     * 当前 WebView
     */
    private WebView mWebView;

    /**
     * 是否为 Camera State
     */
    private boolean mCameraState = false;

    /**
     * 是否调用摄像头后  调用的是摄像模式  默认是拍照
     */
    private boolean mVideoState = false;

    /**
     * FROM_INTENTION_CODE 用于表示当前Action
     * 11 >> 1 -> Camera
     * 11 >> 2 -> Album -> Default
     */
    private int mCurrentFromIntentCode = 11 >> 3;
    private static final int FROM_INTENT_CODE_CAMERA = 11 >> 1;
    private static final int FROM_INTENT_CODE_ALBUM = 11 >> 2;

    /**
     * 权限拦截
     */
    private PermissionInterceptor mPermissionInterceptor;

    /**
     * 当前 AbsAgentWebUIController
     */
    private WeakReference<AbsAgentWebUIController> mAgentWebUIController = null;

    /**
     * 选择文件类型
     */
    private String mAcceptType;

    /**
     * 修复某些特定手机拍照后，立刻获取照片为空的情况
     */
    public static int MAX_WAIT_PHOTO_MS = 8 * 1000;

    private FileChooser(Builder builder) {
        this.mUriValueCallback = builder.mUriValueCallback;
        this.mUriValueCallbacks = builder.mUriValueCallbacks;
        this.mAcceptType = builder.mAcceptType;
        this.mActivity = builder.mActivity;
        this.mFileChooserParams = builder.mFileChooserParams;
        this.mWebView = builder.mWebView;
        this.mPermissionInterceptor = builder.mPermissionInterceptor;
        this.mIsAboveLollipop = builder.mIsAboveLollipop;
        this.mAgentWebUIController = new WeakReference<>(AgentWebUtils.getAgentWebUIControllerByWebView(builder.mWebView));
    }

    /**
     * 启动FileChooser
     */
    public void startChooser() {
        if (!AgentWebUtils.isUIThread()) {
            AgentWebUtils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    startChooser();
                }
            });
            return;
        }
        if (null != this.mAgentWebUIController.get()) {
            // 1、相机 2、图库 3、文件
            this.mAgentWebUIController
                    .get()
                    .onSelectItemsPrompt(this.mWebView, mWebView.getUrl(),
                            new String[]{
                                    mActivity.getString(R.string.agentweb_camera),
                                    mActivity.getString(R.string.agentweb_album),
                                    mActivity.getString(R.string.agentweb_file_chooser)},
                            getCallback());
        }
    }

    /**
     * ChooserListener
     */
    private FileChooserUtils.ChooserListener mChooserListener = new FileChooserUtils.ChooserListener() {
        @Override
        public void onChoiceResult(int requestCode, int resultCode, Intent data) {
            onIntentResultInternal(requestCode, resultCode, data);
        }
    };

    private void onIntentResultInternal(int requestCode, int resultCode, @Nullable Intent data) {
        if (FileChooserUtils.REQUEST_CODE != requestCode) {
            return;
        }

        //用户已经取消
        if (resultCode == Activity.RESULT_CANCELED || data == null) {
            cancel();
            return;
        }

        if (resultCode != Activity.RESULT_OK) {
            cancel();
            return;
        }

        //5.0以上系统通过input标签获取文件
        if (mIsAboveLollipop) {
//            aboveLollipopCheckFilesAndCallback(mCameraState ? new Uri[]{data.getParcelableExtra(KEY_URI)} : processData(data), mCameraState);
            return;
        }

        //4.4以下系统通过input标签获取文件
        if (mUriValueCallback == null) {
            cancel();
            return;
        }

        if (mCameraState) {
            mUriValueCallback.onReceiveValue((Uri) data.getParcelableExtra(KEY_URI));
        } else {
//            belowLollipopUriCallback(data);
        }

    }

    /**
     * PermissionListener
     */
    private PermissionUtils.FullCallback mPermissionListener = new PermissionUtils.FullCallback() {

        @Override
        public void onGranted(@NonNull List<String> granted) {
            onPermissionResultInternal(true);
        }

        @Override
        public void onDenied(@NonNull List<String> deniedForever, @NonNull List<String> denied) {
            onPermissionResultInternal(false);
        }

    };

    private void onPermissionResultInternal(boolean isGranted) {
        if (this.mCurrentFromIntentCode == FROM_INTENT_CODE_CAMERA) {
            if (isGranted) {
                openCameraAction(this.mVideoState);
            } else {
                cancel();
                if (null != this.mAgentWebUIController.get()) {
                    this.mAgentWebUIController.get().onPermissionsDeny(AgentWebPermissions.CAMERA, AgentWebPermissions.ACTION_CAMERA);
                }
            }
        } else if (this.mCurrentFromIntentCode == FROM_INTENT_CODE_ALBUM) {
            if (isGranted) {
                openAlbumAction();
            } else {
                cancel();
                if (null != this.mAgentWebUIController.get()) {
                    this.mAgentWebUIController.get().onPermissionsDeny(AgentWebPermissions.CAMERA, AgentWebPermissions.ACTION_CAMERA);
                }
            }
        }
    }

    /**
     * UIController Callback
     * value state
     * 1 -> Cemera
     * 2 -> Album
     * 3 -> Files
     * -1 -> Cancel
     * @return
     */
    private Callback<Integer> getCallback() {
        return new Callback<Integer>() {
            @Override
            public void handleValue(Integer value) {
                switch (value) {
                    case 1:
                        if (mAcceptType.contains("video/")) {
                            openCameraAction(true);
                        } else {
                            openCameraAction(false);
                        }
                        break;
                    case 2:
                        openAlbumAction();
                        break;
                    case 3:
                        openFilesAction();
                        break;
                    default:
                        cancel();
                }
            }
        };
    }

    /**
     * 打开相机
     * @param videoState
     */
    private void openCameraAction(boolean videoState) {
        if (this.mActivity == null) {
            cancel();
            return;
        }
        if (this.mPermissionInterceptor != null &&
                this.mPermissionInterceptor.intercept(this.mWebView.getUrl(), AgentWebPermissions.CAMERA, AgentWebPermissions.ACTION_CAMERA)) {
            cancel();
            return;
        }
        this.mVideoState = videoState;
        if (PermissionUtils.isGranted(AgentWebPermissions.CAMERA)) {
            int actionCode = videoState ? FileChooserUtils.ACTION_VIDEO : FileChooserUtils.ACTION_CAMERA;
            FileChooserUtils.chooser(actionCode).callback(mChooserListener).open();
        } else {
            this.mCurrentFromIntentCode = FROM_INTENT_CODE_CAMERA;
            PermissionUtils.permission(AgentWebPermissions.CAMERA)
                    .callback(mPermissionListener)
                    .request();
        }
    }

    /**
     * 打开图库
     */
    private void openAlbumAction() {
        if (this.mActivity == null) {
            cancel();
            return;
        }
        if (this.mPermissionInterceptor != null &&
                this.mPermissionInterceptor.intercept(this.mWebView.getUrl(), AgentWebPermissions.STORAGE, AgentWebPermissions.ACTION_STORAGE)) {
            cancel();
            return;
        }
        if (PermissionUtils.isGranted(AgentWebPermissions.STORAGE)) {
            FileChooserUtils.chooser(FileChooserUtils.ACTION_ALBUM).callback(mChooserListener).open();
        } else {
            this.mCurrentFromIntentCode = FROM_INTENT_CODE_ALBUM;
            PermissionUtils.permission(AgentWebPermissions.STORAGE).callback(mPermissionListener).request();
        }
    }

    /**
     * 打开文件选择器
     */
    private void openFilesAction() {
        FileChooserUtils.chooser(FileChooserUtils.ACTION_FILE,
                AgentWebUtils.getCommonFileIntentCompat(this.mIsAboveLollipop, this.mFileChooserParams, this.mAcceptType))
                .callback(mChooserListener)
                .open();
    }

    /**
     * 回调，取消时也需要传null回去，以免下一次点击无效
     */
    private void cancel() {
        if (mUriValueCallback != null) {
            mUriValueCallback.onReceiveValue(null);
        }
        if (mUriValueCallbacks != null) {
            mUriValueCallbacks.onReceiveValue(null);
        }
        return;
    }

    /**
     * New Builder By Activity & WebView
     * @param activity
     * @param webView
     * @return
     */
    public static Builder newBuilder(Activity activity, WebView webView) {
        return new Builder().setActivity(activity).setWebView(webView);
    }

    /**
     * Builder
     */
    public static class Builder {

        private Activity mActivity;
        private ValueCallback<Uri> mUriValueCallback;
        private ValueCallback<Uri[]> mUriValueCallbacks;
        private boolean mIsAboveLollipop = false;
        private WebChromeClient.FileChooserParams mFileChooserParams;
        private WebView mWebView;
        private PermissionInterceptor mPermissionInterceptor;
        private String mAcceptType = "*/*";

        public Builder setAcceptType(String acceptType) {
            this.mAcceptType = acceptType;
            return this;
        }

        public Builder setPermissionInterceptor(PermissionInterceptor permissionInterceptor) {
            mPermissionInterceptor = permissionInterceptor;
            return this;
        }

        public Builder setActivity(Activity activity) {
            mActivity = activity;
            return this;
        }

        public Builder setUriValueCallback(ValueCallback<Uri> uriValueCallback) {
            mUriValueCallback = uriValueCallback;
            mIsAboveLollipop = false;
            mUriValueCallbacks = null;
            return this;
        }

        public Builder setUriValueCallbacks(ValueCallback<Uri[]> uriValueCallbacks) {
            mUriValueCallbacks = uriValueCallbacks;
            mIsAboveLollipop = true;
            mUriValueCallback = null;
            return this;
        }

        public Builder setFileChooserParams(WebChromeClient.FileChooserParams fileChooserParams) {
            mFileChooserParams = fileChooserParams;
            return this;
        }

        public Builder setWebView(WebView webView) {
            mWebView = webView;
            return this;
        }

        public FileChooser build() {
            return new FileChooser(this);
        }

    }

}
