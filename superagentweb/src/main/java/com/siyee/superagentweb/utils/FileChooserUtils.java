package com.siyee.superagentweb.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.siyee.superagentweb.SuperAgentWebConfig;
import com.siyee.superagentweb.abs.Consumer;
import com.siyee.superagentweb.abs.PermissionInterceptor;
import com.siyee.superagentweb.filechooser.FileChooser;

import java.io.File;

import static android.provider.MediaStore.EXTRA_OUTPUT;

/**
 * @author hackycy
 */
public final class FileChooserUtils {

    private static final String TAG = FileChooserUtils.class.getSimpleName();

    public static final String KEY_URI = "KEY_URI";
    public static final String KEY_ACTION = "KEY_ACTION";
    public static final String KEY_FILE_CHOOSER_TYPE = "KEY_FILE_CHOOSER_TYPE";
    public static final String KEY_FILE_CHOOSER_INTENT = "KEY_FILE_CHOOSER_INTENT";
    private static FileChooserUtils sInstance;

    /**
     * Action List Type
     */
    public static final int ACTION_FILE = 0x01;
    public static final int ACTION_CAMERA = 0x02;
    public static final int ACTION_ALBUM = 0x03;
    public static final int ACTION_VIDEO = 0x04;

    public static final int REQUEST_CODE = 0x234;

    private int mAction = -1;
    private String mAcceptType = "*/*";
    private Intent mChooserIntent = null;

    private Uri mUri;

    private ChooserListener mChooserListener;

    public static FileChooserUtils chooser(int action) {
        return new FileChooserUtils(action, null, null);
    }

    public static FileChooserUtils chooser(int action, Intent chooserIntent) {
        return new FileChooserUtils(action, null, chooserIntent);
    }

    public static FileChooserUtils chooser(int action, String acceptType) {
        return new FileChooserUtils(action, acceptType, null);
    }

    private FileChooserUtils(@NonNull int action, @Nullable String acceptType, @Nullable Intent chooserIntent) {
        this.mAction = action;
        if (!TextUtils.isEmpty(acceptType)) {
            this.mAcceptType = acceptType;
        }
        this.mChooserIntent = chooserIntent;
        sInstance = this;
    }

    /**
     * Set ChooserListener
     * @param listener
     * @return
     */
    public FileChooserUtils callback(ChooserListener listener) {
        mChooserListener = listener;
        return this;
    }

    /**
     * Start Chooser
     */
    public void open() {
        if (this.mAction == ACTION_FILE || this.mAction == ACTION_CAMERA
                || this.mAction == ACTION_VIDEO || this.mAction == ACTION_ALBUM) {
            startChooserActivity();
            return;
        }
        LogUtils.i(TAG, "No action to open");
    }

    /**
     * showFileChooser
     * @param activity
     * @param webView
     * @param valueCallbacks
     * @param fileChooserParams
     * @param permissionInterceptor
     * @param valueCallback
     * @param mimeType
     * @return
     */
    public static boolean showFileChooserCompat(Activity activity,
                                                WebView webView,
                                                @Nullable ValueCallback<Uri[]> valueCallbacks,
                                                @Nullable WebChromeClient.FileChooserParams fileChooserParams,
                                                @Nullable PermissionInterceptor permissionInterceptor,
                                                @Nullable ValueCallback<Uri> valueCallback,
                                                @Nullable String mimeType) {
        FileChooser.Builder builder = FileChooser.newBuilder(activity, webView);
        if (valueCallbacks != null) {
            builder.setUriValueCallbacks(valueCallbacks);
        }
        if (valueCallback != null) {
            builder.setUriValueCallback(valueCallback);
        }
        if (fileChooserParams != null) {
            builder.setFileChooserParams(fileChooserParams);
        }
        if (permissionInterceptor != null) {
            builder.setPermissionInterceptor(permissionInterceptor);
        }
        if (!TextUtils.isEmpty(mimeType)) {
            builder.setAcceptType(mimeType);
        }
        FileChooser fileChooser = builder.build();
        fileChooser.startChooser();
        return true;
    }

    private void startChooserActivity() {
        FileChooserActivityImpl.start(this.mAction, this.mAcceptType, this.mChooserIntent);
    }

    /**
     * FileChooserActivityImpl
     */
    static final class FileChooserActivityImpl extends UtilsTransActivity.TransActivityDelegate {

        private static FileChooserActivityImpl INSTANCE = new FileChooserActivityImpl();

        private static void start(@NonNull final int type, @Nullable final String acceptType, @Nullable final Intent chooserIntent) {
            UtilsTransActivity.start(new Consumer<Intent>() {
                @Override
                public void accept(Intent data) {
                    data.putExtra(KEY_ACTION, type);
                    if (chooserIntent != null) {
                        data.putExtra(KEY_FILE_CHOOSER_INTENT, chooserIntent);
                    }
                    if (!TextUtils.isEmpty(acceptType)) {
                        data.putExtra(KEY_FILE_CHOOSER_TYPE, acceptType);
                    }
                }
            }, INSTANCE);
        }

        @Override
        public void onCreated(@NonNull UtilsTransActivity activity, @Nullable Bundle savedInstanceState) {
            int type = activity.getIntent().getIntExtra(KEY_ACTION, -1);
            if (type == -1) {
                cancel(activity);
                return;
            }
            // Do Action By Type
            if (type == ACTION_CAMERA) {
                realOpenCamera(activity);
            } else if (type == ACTION_ALBUM) {
                realOpenAlbum(activity);
            } else if (type == ACTION_VIDEO) {
                realOpenVideo(activity);
            } else {
                realOpenFileChooser(activity);
            }
        }

        @Override
        public void onActivityResult(@NonNull UtilsTransActivity activity, int requestCode, int resultCode, Intent data) {
            if (requestCode == REQUEST_CODE) {
                chooserActionCallback(activity, resultCode, sInstance.mUri != null ? new Intent().putExtra(KEY_URI, sInstance.mUri) : data);
            }
        }

        private void chooserActionCallback(@NonNull UtilsTransActivity activity, int resultCode, Intent data) {
            if (sInstance.mChooserListener != null) {
                sInstance.mChooserListener.onChoiceResult(REQUEST_CODE, resultCode, data);
            }
            cancel(activity);
        }

        /**
         * 访问系统相机
         * @param activity
         */
        private void realOpenCamera(@NonNull UtilsTransActivity activity) {
            try {
                if (sInstance.mChooserListener == null) {
                    cancel(activity);
                    return;
                }
                File mFile = SuperAgentWebUtils.createImageFile(activity);
                if (mFile == null) {
                    sInstance.mChooserListener.onChoiceResult(REQUEST_CODE, Activity.RESULT_CANCELED, null);
                    cancel(activity);
                    return;
                }
                Intent intent = SuperAgentWebUtils.getIntentCaptureCompat(activity, mFile);
                // 指定开启系统相机的Action
                sInstance.mUri = intent.getParcelableExtra(EXTRA_OUTPUT);
                activity.startActivityForResult(intent, REQUEST_CODE);
            } catch (Exception e) {
                LogUtils.e(TAG, "无法打开系统相机");
                if (sInstance.mChooserListener != null) {
                    sInstance.mChooserListener.onChoiceResult(REQUEST_CODE, Activity.RESULT_CANCELED, null);
                }
                cancel(activity);
                if (SuperAgentWebConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 访问系统相册
         * @param activity
         */
        private void realOpenAlbum(@NonNull UtilsTransActivity activity) {
            try {
                if (sInstance.mChooserListener == null) {
                    cancel(activity);
                    return;
                }
                String acceptType = activity.getIntent().getStringExtra(KEY_FILE_CHOOSER_TYPE);
                Intent intent = SuperAgentWebUtils.getIntentAlbumCompat(activity, acceptType);
                activity.startActivityForResult(intent, REQUEST_CODE);
            } catch (Exception e) {
                LogUtils.e(TAG, "无法打开系统相册");
                if (sInstance.mChooserListener != null) {
                    sInstance.mChooserListener.onChoiceResult(REQUEST_CODE, Activity.RESULT_CANCELED, null);
                }
                cancel(activity);
                if (SuperAgentWebConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 访问相机并直接设置为摄像模式
         * @param activity
         */
        private void realOpenVideo(@NonNull UtilsTransActivity activity) {
            try {
                if (sInstance.mChooserListener == null) {
                    cancel(activity);
                    return;
                }
                File mFile = SuperAgentWebUtils.createVideoFile(activity);
                if (mFile == null) {
                    sInstance.mChooserListener.onChoiceResult(REQUEST_CODE, Activity.RESULT_CANCELED, null);
                    cancel(activity);
                    return;
                }
                Intent intent = SuperAgentWebUtils.getIntentVideoCompat(activity, mFile);
                sInstance.mUri = intent.getParcelableExtra(EXTRA_OUTPUT);
                activity.startActivityForResult(intent, REQUEST_CODE);
            } catch (Exception e) {
                LogUtils.e(TAG, "无法打开系统相机");
                if (sInstance.mChooserListener != null) {
                    sInstance.mChooserListener.onChoiceResult(REQUEST_CODE, Activity.RESULT_CANCELED, null);
                }
                cancel(activity);
                if (SuperAgentWebConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 访问系统文件管理器
         * @param activity
         */
        private void realOpenFileChooser(@NonNull UtilsTransActivity activity) {
            try {
                if (sInstance.mChooserListener == null) {
                    cancel(activity);
                    return;
                }
                // 在FileChooser中传参
                Intent mIntent = activity.getIntent().getParcelableExtra(KEY_FILE_CHOOSER_INTENT);
                if (mIntent == null) {
                    cancel(activity);
                    return;
                }
                activity.startActivityForResult(mIntent, REQUEST_CODE);
            } catch (Exception e) {
                LogUtils.e(TAG, "无法打开文件选择器");
                if (sInstance.mChooserListener != null) {
                    sInstance.mChooserListener.onChoiceResult(REQUEST_CODE, Activity.RESULT_CANCELED, null);
                }
                cancel(activity);
                if (SuperAgentWebConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * clear
         */
        private void cancel(@NonNull UtilsTransActivity activity) {
            sInstance.mChooserListener = null;
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // interface
    ///////////////////////////////////////////////////////////////////////////

    public interface ChooserListener {

        void onChoiceResult(int requestCode, int resultCode, Intent data);

    }

}
