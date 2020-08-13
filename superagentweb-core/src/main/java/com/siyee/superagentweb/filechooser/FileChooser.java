package com.siyee.superagentweb.filechooser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.siyee.superagentweb.SuperAgentWebPermissions;
import com.siyee.superagentweb.R;
import com.siyee.superagentweb.abs.AbsAgentWebUIController;
import com.siyee.superagentweb.abs.Callback;
import com.siyee.superagentweb.abs.PermissionInterceptor;
import com.siyee.superagentweb.utils.AgentWebUtils;
import com.siyee.superagentweb.utils.FileChooserUtils;
import com.siyee.superagentweb.utils.PermissionUtils;

import java.io.File;
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
     * 11 >> 2 -> Album
     * 11 >> 3 -> Files -> Default
     */
    private int mCurrentFromIntentCode = FROM_INTENT_CODE_FILES;
    public static final int FROM_INTENT_CODE_CAMERA = 11 >> 1;
    public static final int FROM_INTENT_CODE_ALBUM = 11 >> 2;
    public static final int FROM_INTENT_CODE_FILES = 11 >> 3;

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
    @SuppressLint("NewApi")
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
        if (this.mIsAboveLollipop && this.mFileChooserParams != null && this.mFileChooserParams.getAcceptTypes() != null) {
            String[] types = this.mFileChooserParams.getAcceptTypes();
            for (String typeTmp : types) {
                if (TextUtils.isEmpty(typeTmp)) {
                    continue;
                }
//                if (typeTmp.contains("*/") || typeTmp.contains("image/")) {  //这是拍照模式
//                    break;
//                }
                if (typeTmp.contains("video/")) {  //调用摄像机拍摄
                    mVideoState = true;
                    break;
                }
            }
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
            aboveLollipopCheckFilesAndCallback(mCameraState ? new Uri[]{data.getParcelableExtra(KEY_URI)} : processData(data), mCameraState);
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
            belowLollipopUriCallback(data);
        }

        /*if (mIsAboveLollipop)
            aboveLollipopCheckFilesAndCallback(mCameraState ? new Uri[]{data.getParcelableExtra(KEY_URI)} : processData(data));
        else if (mJsChannel)
            convertFileAndCallback(mCameraState ? new Uri[]{data.getParcelableExtra(KEY_URI)} : processData(data));
        else {
            if (mCameraState && mUriValueCallback != null)
                mUriValueCallback.onReceiveValue((Uri) data.getParcelableExtra(KEY_URI));
            else
                belowLollipopUriCallback(data);
        }*/
    }

    private Uri[] processData(Intent data) {
        Uri[] datas = null;
        if (data == null) {
            return datas;
        }
        String target = data.getDataString();
        if (!TextUtils.isEmpty(target)) {
            return datas = new Uri[]{ Uri.parse(target) };
        }
        ClipData mClipData = data.getClipData();
        if (mClipData != null && mClipData.getItemCount() > 0) {
            datas = new Uri[mClipData.getItemCount()];
            for (int i = 0; i < mClipData.getItemCount(); i++) {
                ClipData.Item mItem = mClipData.getItemAt(i);
                datas[i] = mItem.getUri();
            }
        }
        return datas;
    }

    /**
     * 经过多次的测试，在小米 MIUI ， 华为 ，多部分为 Android 6.0 左右系统相机获取到的文件
     * length为0 ，导致前端 ，获取到的文件， 作预览的时候不正常 ，等待5S左右文件又正常了 ， 所以这里做了阻塞等待处理，
     *
     * @param datas
     * @param isCamera
     */
    private void aboveLollipopCheckFilesAndCallback(final Uri[] datas, boolean isCamera) {
        if (mUriValueCallbacks == null) {
            return;
        }
        if (!isCamera) {
            mUriValueCallbacks.onReceiveValue(datas == null ? new Uri[]{} : datas);
            return;
        }

        if (mAgentWebUIController.get() == null) {
            mUriValueCallbacks.onReceiveValue(null);
            return;
        }
        String[] paths = AgentWebUtils.uriToPath(mActivity, datas);
        if (paths == null || paths.length == 0) {
            mUriValueCallbacks.onReceiveValue(null);
            return;
        }
        final String path = paths[0];
        mAgentWebUIController.get().onLoading(mActivity.getString(R.string.agentweb_loading));
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new WaitPhotoRunnable(path, new AboveLCallback(mUriValueCallbacks, datas, mAgentWebUIController)));

    }

    private static final class AboveLCallback implements Handler.Callback {
        private ValueCallback<Uri[]> mValueCallback;
        private Uri[] mUris;
        private WeakReference<AbsAgentWebUIController> controller;

        private AboveLCallback(ValueCallback<Uri[]> valueCallbacks, Uri[] uris, WeakReference<AbsAgentWebUIController> controller) {
            this.mValueCallback = valueCallbacks;
            this.mUris = uris;
            this.controller = controller;
        }

        @Override
        public boolean handleMessage(final Message msg) {

            AgentWebUtils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    FileChooser.AboveLCallback.this.safeHandleMessage(msg);
                }
            });
            return false;
        }

        private void safeHandleMessage(Message msg) {
            if (mValueCallback != null) {
                mValueCallback.onReceiveValue(mUris);
            }
            if (controller != null && controller.get() != null) {
                controller.get().onCancelLoading();
            }
        }
    }

    private static final class WaitPhotoRunnable implements Runnable {
        private String path;
        private Handler.Callback mCallback;

        private WaitPhotoRunnable(String path, Handler.Callback callback) {
            this.path = path;
            this.mCallback = callback;
        }

        @Override
        public void run() {

            if (TextUtils.isEmpty(path) || !new File(path).exists()) {
                if (mCallback != null) {
                    mCallback.handleMessage(Message.obtain(null, -1));
                }
                return;
            }
            int ms = 0;

            while (ms <= MAX_WAIT_PHOTO_MS) {

                ms += 300;
                SystemClock.sleep(300);
                File mFile = new File(path);
                if (mFile.length() > 0) {

                    if (mCallback != null) {
                        mCallback.handleMessage(Message.obtain(null, 1));
                        mCallback = null;
                    }
                    break;
                }

            }

            if (ms > MAX_WAIT_PHOTO_MS) {
                if (mCallback != null) {
                    mCallback.handleMessage(Message.obtain(null, -1));
                }
            }
            mCallback = null;
            path = null;

        }
    }

    /**
     * Lollipop以下的处理
     * @param data
     */
    private void belowLollipopUriCallback(Intent data) {
        if (data == null) {
            if (mUriValueCallback != null) {
                mUriValueCallback.onReceiveValue(Uri.EMPTY);
            }
            return;
        }
        Uri mUri = data.getData();
        if (mUriValueCallback != null) {
            mUriValueCallback.onReceiveValue(mUri);
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
                    this.mAgentWebUIController.get().onPermissionsDeny(SuperAgentWebPermissions.CAMERA, SuperAgentWebPermissions.ACTION_CAMERA);
                }
            }
        } else if (this.mCurrentFromIntentCode == FROM_INTENT_CODE_ALBUM) {
            if (isGranted) {
                openAlbumAction(this.mVideoState);
            } else {
                cancel();
                if (null != this.mAgentWebUIController.get()) {
                    this.mAgentWebUIController.get().onPermissionsDeny(SuperAgentWebPermissions.STORAGE, SuperAgentWebPermissions.ACTION_STORAGE);
                }
            }
        } else if (this.mCurrentFromIntentCode == FROM_INTENT_CODE_FILES) {
            if (isGranted) {
                openFilesAction();
            } else {
                cancel();
                if (null != this.mAgentWebUIController.get()) {
                    this.mAgentWebUIController.get().onPermissionsDeny(SuperAgentWebPermissions.STORAGE, SuperAgentWebPermissions.ACTION_STORAGE);
                }
            }
        }
    }

    /**
     * UIController Callback
     * value state
     * 0 -> Cemera
     * 1 -> Album
     * 2 -> Files
     * -1 Or Other -> Cancel
     * @return
     */
    private Callback<Integer> getCallback() {
        return new Callback<Integer>() {
            @Override
            public void handleValue(Integer value) {
                switch (value) {
                    case 0:
                        mCameraState = true;
                        openCameraAction(mVideoState);
                        break;
                    case 1:
                        mCameraState = false;
                        openAlbumAction(mVideoState);
                        break;
                    case 2:
                        mCameraState = false;
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
                this.mPermissionInterceptor.intercept(this.mWebView.getUrl(), SuperAgentWebPermissions.CAMERA, SuperAgentWebPermissions.ACTION_CAMERA)) {
            cancel();
            return;
        }
        if (PermissionUtils.isGranted(SuperAgentWebPermissions.CAMERA)) {
            int actionCode = videoState ? FileChooserUtils.ACTION_VIDEO : FileChooserUtils.ACTION_CAMERA;
            FileChooserUtils.chooser(actionCode).callback(mChooserListener).open();
        } else {
            this.mCurrentFromIntentCode = FROM_INTENT_CODE_CAMERA;
            PermissionUtils.permission(SuperAgentWebPermissions.CAMERA)
                    .callback(mPermissionListener)
                    .request();
        }
    }

    /**
     * 打开图库
     */
    private void openAlbumAction(boolean videoState) {
        if (this.mActivity == null) {
            cancel();
            return;
        }
        if (this.mPermissionInterceptor != null &&
                this.mPermissionInterceptor.intercept(this.mWebView.getUrl(), SuperAgentWebPermissions.STORAGE, SuperAgentWebPermissions.ACTION_STORAGE)) {
            cancel();
            return;
        }
        if (PermissionUtils.isGranted(SuperAgentWebPermissions.STORAGE)) {
            String acceptType = videoState ? "video/*" : "image/*";
            FileChooserUtils.chooser(FileChooserUtils.ACTION_ALBUM, acceptType).callback(mChooserListener).open();
        } else {
            this.mCurrentFromIntentCode = FROM_INTENT_CODE_ALBUM;
            PermissionUtils.permission(SuperAgentWebPermissions.STORAGE).callback(mPermissionListener).request();
        }
    }

    /**
     * 打开文件选择器
     */
    private void openFilesAction() {
        if (this.mActivity == null) {
            cancel();
            return;
        }
        if (this.mPermissionInterceptor != null &&
                this.mPermissionInterceptor.intercept(this.mWebView.getUrl(), SuperAgentWebPermissions.STORAGE, SuperAgentWebPermissions.ACTION_STORAGE)) {
            cancel();
            return;
        }
        if (PermissionUtils.isGranted(SuperAgentWebPermissions.STORAGE)) {
            FileChooserUtils.chooser(FileChooserUtils.ACTION_FILE,
                    AgentWebUtils.getCommonFileIntentCompat(this.mIsAboveLollipop, this.mFileChooserParams, this.mAcceptType))
                    .callback(mChooserListener)
                    .open();
        } else {
            this.mCurrentFromIntentCode = FROM_INTENT_CODE_FILES;
            PermissionUtils.permission(SuperAgentWebPermissions.STORAGE).callback(mPermissionListener).request();
        }
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
