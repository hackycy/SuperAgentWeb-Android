package com.siyee.superagentweb.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.AppOpsManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.os.EnvironmentCompat;

import com.siyee.superagentweb.AgentWebConfig;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.siyee.superagentweb.AgentWebConfig.AGENTWEB_CACHE_PATCH;

/**
 * @author hackycy
 * @date 2020/8/3
 */
public class AgentWebUtils {

    private static final String TAG = AgentWebUtils.class.getSimpleName();

    private static Toast mToast = null;

    private static Handler mHandler = null;

    private AgentWebUtils() {
        throw new UnsupportedOperationException("SuperAgentWebUtils can' t init");
    }

    public static int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static void clearWebView(WebView m) {
        if (m == null) {
            return;
        }
        if (Looper.myLooper() != Looper.getMainLooper()) {
            return;
        }
        m.loadUrl("about:blank");
        m.stopLoading();
        if (m.getHandler() != null) {
            m.getHandler().removeCallbacksAndMessages(null);
        }
        m.removeAllViews();
        ViewGroup mViewGroup = null;
        if ((mViewGroup = ((ViewGroup) m.getParent())) != null) {
            mViewGroup.removeView(m);
        }
        m.setWebChromeClient(null);
        m.setWebViewClient(null);
        m.setTag(null);
        m.clearHistory();
        m.destroy();
        m = null;
    }

    public static String getMIMEType(File f) {
        String type = "";
        String fName = f.getName();
        /* 取得扩展名 */
        String end = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();
        /* 依扩展名的类型决定MimeType */
        if (end.equals("pdf")) {
            type = "application/pdf";//
        } else if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") ||
                end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
            type = "audio/*";
        } else if (end.equals("3gp") || end.equals("mp4")) {
            type = "video/*";
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png") ||
                end.equals("jpeg") || end.equals("bmp")) {
            type = "image/*";
        } else if (end.equals("apk")) {
            type = "application/vnd.android.package-archive";
        } else if (end.equals("pptx") || end.equals("ppt")) {
            type = "application/vnd.ms-powerpoint";
        } else if (end.equals("docx") || end.equals("doc")) {
            type = "application/vnd.ms-word";
        } else if (end.equals("xlsx") || end.equals("xls")) {
            type = "application/vnd.ms-excel";
        } else {
            type = "*/*";
        }
        return type;
    }

    public static boolean hasPermission(@NonNull Context context, @NonNull String... permissions) {
        return hasPermission(context, Arrays.asList(permissions));
    }

    public static boolean hasPermission(@NonNull Context context, @NonNull List<String> permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        for (String permission : permissions) {
            int result = ContextCompat.checkSelfPermission(context, permission);
            if (result == PackageManager.PERMISSION_DENIED) {
                return false;
            }
            String op = AppOpsManagerCompat.permissionToOp(permission);
            if (TextUtils.isEmpty(op)) {
                continue;
            }
            result = AppOpsManagerCompat.noteProxyOp(context, op, context.getPackageName());
            if (result != AppOpsManagerCompat.MODE_ALLOWED) {
                return false;
            }
        }
        return true;
    }

    public static List<String> getDeniedPermissions(Activity activity, String[] permissions) {
        if (permissions == null || permissions.length == 0) {
            return null;
        }
        List<String> deniedPermissions = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            if (!hasPermission(activity, permissions[i])) {
                deniedPermissions.add(permissions[i]);
            }
        }
        return deniedPermissions;
    }

    public static boolean isUIThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static void runOnUIThread(Runnable runnable) {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        mHandler.post(runnable);
    }

    @SuppressLint("ShowToast")
    public static void toastShowShort(Context context, String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(msg);
        }
        mToast.show();
    }

    public static String md5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            if (LogUtils.isDebug()) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static int clearCacheFolder(final File dir, final int numDays) {
        int deletedFiles = 0;
        if (dir != null) {
            Log.i("Info", "dir:" + dir.getAbsolutePath());
        }
        if (dir != null && dir.isDirectory()) {
            try {
                for (File child : dir.listFiles()) {

                    //first delete subdirectories recursively
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, numDays);
                    }

                    //then delete the files and subdirectories in this dir
                    //only empty directories can be deleted, so subdirs have been done first
                    if (child.lastModified() < new Date().getTime() - numDays * DateUtils.DAY_IN_MILLIS) {
                        Log.i(TAG, "file name:" + child.getName());
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("Info", String.format("Failed to clean the cache, result %s", e.getMessage()));
            }
        }
        return deletedFiles;
    }

    public static String getApplicationName(Context context) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        String applicationName = "";
        try {
            packageManager = context.getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
        } catch (Exception ignored) {}
        return applicationName;
    }

    public static boolean checkWifi(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        }
        @SuppressLint("MissingPermission") NetworkInfo info = connectivity.getActiveNetworkInfo();
        return info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static boolean checkNetwork(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        }
        @SuppressLint("MissingPermission") NetworkInfo info = connectivity.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    /**
     * Copy from com.blankj.utilcode.util.ActivityUtils#getActivityByView
     */
    public Activity getActivityByContext(Context context) {
        if (context instanceof Activity) return (Activity) context;
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    public static String getDatabasesCachePath(Context context) {
        return context.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
    }

    /**
     * @param context
     * @return WebView 的缓存路径
     */
    public static String getCachePath(Context context) {
        return context.getCacheDir().getAbsolutePath() + AGENTWEB_CACHE_PATCH;
    }

    /**
     * @param context
     * @return AgentWeb 缓存路径
     */
    public static String getExternalCachePath(Context context) {
        return getAgentWebFilePath(context);
    }

    public static String getAgentWebFilePath(Context context) {
        if (!TextUtils.isEmpty(AgentWebConfig.AGENTWEB_FILE_PATH)) {
            return AgentWebConfig.AGENTWEB_FILE_PATH;
        }
        String dir = getDiskExternalCacheDir(context);
        File mFile = new File(dir, AgentWebConfig.FILE_CACHE_PATH);
        try {
            if (!mFile.exists()) {
                mFile.mkdirs();
            }
        } catch (Throwable throwable) {
            LogUtils.i(TAG, "create dir exception");
        }
        LogUtils.i(TAG, "path:" + mFile.getAbsolutePath() + "  path:" + mFile.getPath());
        return AgentWebConfig.AGENTWEB_FILE_PATH = mFile.getAbsolutePath();
    }

    public static String getDiskExternalCacheDir(Context context) {
        File mFile = context.getExternalCacheDir();
        if (Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(mFile))) {
            return mFile.getAbsolutePath();
        }
        return null;
    }

}
