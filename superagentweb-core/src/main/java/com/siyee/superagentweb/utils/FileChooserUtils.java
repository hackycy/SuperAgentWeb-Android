package com.siyee.superagentweb.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.siyee.superagentweb.abs.Consumer;

import java.io.File;

import static android.provider.MediaStore.EXTRA_OUTPUT;

/**
 * @author hackycy
 */
public final class FileChooserUtils {

    private static final String TAG = FileChooserUtils.class.getSimpleName();

    private static FileChooserUtils sInstance;

    /**
     * Action List Type
     */
    public static final int ACTION_FILE = 0x01;
    public static final int ACTION_CAMERA = 0x02;
    public static final int ACTION_ALBUM = 0x03;
    public static final int ACTION_VIDEO = 0x04;

    public final static int REQUEST_CODE = 0x234;

    private int mAction = -1;

    private Uri mUri;

    private ChooserListener mChooserListener;

    public static FileChooserUtils chooser(int action) {
        return new FileChooserUtils(action);
    }

    private FileChooserUtils(int action) {
        this.mAction = action;
        sInstance = this;
    }

    public void open() {
        if (this.mAction == ACTION_FILE || this.mAction == ACTION_CAMERA
                || this.mAction == ACTION_VIDEO || this.mAction == ACTION_ALBUM) {
            startChooserActivity();
            return;
        }
        LogUtils.i(TAG, "No action to open");
    }

    private void startChooserActivity() {
        FileChooserActivityImpl.start(this.mAction);
    }

    /**
     * FileChooserActivityImpl
     */
    static final class FileChooserActivityImpl extends UtilsTransActivity.TransActivityDelegate {

        private static final String TYPE = "TYPE";

        private static FileChooserActivityImpl INSTANCE = new FileChooserActivityImpl();

        public static void start(final int type) {
            UtilsTransActivity.start(new Consumer<Intent>() {
                @Override
                public void accept(Intent data) {
                    data.putExtra(TYPE, type);
                }
            }, INSTANCE);
        }

        @Override
        public void onCreated(@NonNull UtilsTransActivity activity, @Nullable Bundle savedInstanceState) {
            int type = activity.getIntent().getIntExtra(TYPE, -1);
            if (type == -1) {
                cancel(activity);
                return;
            }
            if (type == ACTION_CAMERA) {

            }
        }

        /**
         * 访问系统相机
         * @param activity
         */
        private void realOpenCamera(@NonNull UtilsTransActivity activity) {
            try {
                if (sInstance.mChooserListener == null) {
                    activity.finish();
                    return;
                }
                File mFile = AgentWebUtils.createImageFile(activity);
                if (mFile == null) {
                    sInstance.mChooserListener.onChoiceResult(REQUEST_CODE, Activity.RESULT_CANCELED, null);
                    cancel(activity);
                }
                Intent intent = AgentWebUtils.getIntentCaptureCompat(activity, mFile);
                // 指定开启系统相机的Action
                sInstance.mUri = intent.getParcelableExtra(EXTRA_OUTPUT);
                activity.startActivityForResult(intent, REQUEST_CODE);
            } catch (Exception e) {
                LogUtils.e(TAG, "找不到系统相机");
                if (sInstance.mChooserListener != null) {
                    sInstance.mChooserListener.onChoiceResult(REQUEST_CODE, Activity.RESULT_CANCELED, null);
                }
                cancel(activity);
            }
        }

        /**
         * 访问系统相册
         * @param activity
         */
        private void realOpenAlbum(@NonNull UtilsTransActivity activity) {

        }

        /**
         * 访问系统文件管理器
         * @param activity
         */
        private void realOpenFile(@NonNull UtilsTransActivity activity) {

        }

        private void realOpenVideo(@NonNull UtilsTransActivity activity) {

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