package com.siyee.superagentweb.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.ContentUris;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.core.os.EnvironmentCompat;

import com.siyee.superagentweb.R;
import com.siyee.superagentweb.SuperAgentWebConfig;
import com.siyee.superagentweb.abs.AbsAgentWebUIController;
import com.siyee.superagentweb.widget.WebParentLayout;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.siyee.superagentweb.SuperAgentWebConfig.AGENTWEB_CACHE_PATCH;

/**
 * @author hackycy
 * @date 2020/8/3
 */
public class AgentWebUtils {

    private static final String TAG = AgentWebUtils.class.getSimpleName();

    private static Toast mToast = null;

    private static Handler mHandler = null;

    @SuppressLint("StaticFieldLeak")
    private static Application sApp;

    private AgentWebUtils() {
        throw new UnsupportedOperationException("SuperAgentWebUtils can' t init");
    }

    /**
     * Init utils.
     * <p>Init it in the class of UtilsFileProvider.</p>
     *
     * @param app application
     */
    public static void init(final Application app) {
        if (app == null) {
            Log.e("Utils", "app is null.");
            return;
        }
        if (sApp == null) {
            sApp = app;
            return;
        }
        if (sApp.equals(app)) return;
        sApp = app;
    }

    /**
     * Return the Application object.
     * <p>Main process get app by UtilsFileProvider,
     * and other process get app by reflect.</p>
     *
     * @return the Application object
     */
    public static Application getApp() {
        if (sApp == null) throw new NullPointerException("reflect failed.");
        return sApp;
    }

    /**
     * Return whether the intent is available.
     *
     * @param intent The intent.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isIntentAvailable(final Intent intent) {
        return getApp()
                .getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                .size() > 0;
    }

    private static Intent getIntent(final Intent intent, final boolean isNewTask) {
        return isNewTask ? intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) : intent;
    }

    /**
     * Return the intent of launch app details settings.
     *
     * @param pkgName The name of the package.
     * @return the intent of launch app details settings
     */
    public static Intent getLaunchAppDetailsSettingsIntent(final String pkgName, final boolean isNewTask) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + pkgName));
        return getIntent(intent, isNewTask);
    }

    public static Intent getIntentCaptureCompat(Context context, File file) {
        Intent mIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri mUri = getUriFromFile(context, file);
        mIntent.addCategory(Intent.CATEGORY_DEFAULT);
        mIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
        return mIntent;
    }

    public static Intent getCommonFileIntentCompat(boolean isAboveLollipop,
                                                   WebChromeClient.FileChooserParams fileChooserParams,
                                                   String acceptType) {
        Intent mIntent = null;
        if (isAboveLollipop && fileChooserParams != null && (mIntent = fileChooserParams.createIntent()) != null) {
            // 多选
			/*if (mFileChooserParams.getMode() == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE) {
			    mIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            }*/
//			mIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && mIntent.getAction().equals(Intent.ACTION_GET_CONTENT)) {
                mIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            }
            return mIntent;
        }

        Intent i = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            i.setAction(Intent.ACTION_OPEN_DOCUMENT);
        } else {
            i.setAction(Intent.ACTION_GET_CONTENT);
        }
        i.addCategory(Intent.CATEGORY_OPENABLE);
        if (TextUtils.isEmpty(acceptType)) {
            i.setType("*/*");
        } else {
            i.setType(acceptType);
        }
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return mIntent = Intent.createChooser(i, "");
    }

    /**
     * type is image/* or video/*
     * @param context
     * @param acceptType
     * @return
     */
    public static Intent getIntentAlbumCompat(Context context, @Nullable String acceptType) {
        Intent mIntent = new Intent(Intent.ACTION_PICK);
        if (TextUtils.isEmpty(acceptType)) {
            acceptType = "image/*";
        }
        mIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, acceptType);
        return mIntent;
    }

    public static Intent getIntentVideoCompat(Context context, File file){
        Intent mIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        Uri mUri = getUriFromFile(context, file);
        mIntent.addCategory(Intent.CATEGORY_DEFAULT);
        mIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
        return mIntent;
    }

    private static void setIntentDataAndType(Context context,
                                             Intent intent,
                                             String type,
                                             File file,
                                             boolean writeAble) {
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setDataAndType(getUriFromFile(context, file), type);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (writeAble) {
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        } else {
            intent.setDataAndType(Uri.fromFile(file), type);
        }
    }

    public static Uri getUriFromFile(Context context, File file) {
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = getUriFromFileForN(context, file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    public static Uri getUriFromFileForN(Context context, File file) {
        // AndroidManifest.xml -> authorities
        return FileProvider.getUriForFile(context, context.getPackageName() + SuperAgentWebConfig.PROVIDER_SUFFIX, file);
    }

    public static String[] uriToPath(Activity activity, Uri[] uris) {
        if (activity == null || uris == null || uris.length == 0) {
            return null;
        }
        try {
            String[] paths = new String[uris.length];
            int i = 0;
            for (Uri mUri : uris) {
                paths[i++] = getFileAbsolutePath(activity, mUri);

            }
            return paths;
        } catch (Throwable throwable) {
            if (LogUtils.isDebug()) {
                throwable.printStackTrace();
            }
        }
        return null;

    }

    @TargetApi(19)
    public static String getFileAbsolutePath(Activity context, Uri fileUri) {
        if (context == null || fileUri == null) {
            return null;
        }
        LogUtils.i(TAG, "getAuthority:" + fileUri.getAuthority() + "  getHost:" +
                fileUri.getHost() + "   getPath:" + fileUri.getPath() +
                "  getScheme:" + fileUri.getScheme() + "  query:" + fileUri.getQuery());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, fileUri)) {
            if (isExternalStorageDocument(fileUri)) {
                String docId = DocumentsContract.getDocumentId(fileUri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(fileUri)) {
                String id = DocumentsContract.getDocumentId(fileUri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(fileUri)) {
                String docId = DocumentsContract.getDocumentId(fileUri);
                String[] split = docId.split(":");
                String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            } else {
            }
        } // MediaStore (and general)
        else if (fileUri.getAuthority().equalsIgnoreCase(context.getPackageName() + SuperAgentWebConfig.PROVIDER_SUFFIX)) {
            String path = fileUri.getPath();
            int index = path.lastIndexOf("/");
            return getAgentWebFilePath(context) + File.separator + path.substring(index + 1, path.length());
        } else if ("content".equalsIgnoreCase(fileUri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(fileUri)) {
                return fileUri.getLastPathSegment();
            }
            return getDataColumn(context, fileUri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(fileUri.getScheme())) {
            return fileUri.getPath();
        }
        return null;
    }

    static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
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

    public static File createImageFile(Context context) {
        File mFile = null;
        try {
            String timeStamp =
                    new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
            String imageName = String.format("aw_%s.jpg", timeStamp);
            mFile = createFileByName(context, imageName, true);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return mFile;
    }

    public static File createVideoFile(Context context){
        File mFile = null;
        try {
            String timeStamp =
                    new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
            String imageName = String.format("aw_%s.mp4", timeStamp);  //默认生成mp4
            mFile = createFileByName(context, imageName, true);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return mFile;
    }

    public static File createFileByName(Context context, String name, boolean cover) throws IOException {
        String path = getAgentWebFilePath(context);
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        File mFile = new File(path, name);
        if (mFile.exists()) {
            if (cover) {
                mFile.delete();
                mFile.createNewFile();
            }
        } else {
            mFile.createNewFile();
        }
        return mFile;
    }

    public static int checkNetworkType(Context context) {
        int netType = 0;
        //连接管理对象
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //获取NetworkInfo对象
        @SuppressLint("MissingPermission")
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        switch (networkInfo.getType()) {
            case ConnectivityManager.TYPE_WIFI:
            case ConnectivityManager.TYPE_WIMAX:
            case ConnectivityManager.TYPE_ETHERNET:
                return 1;
            case ConnectivityManager.TYPE_MOBILE:
                switch (networkInfo.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_LTE:  // 4G
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                        return 2;
                    case TelephonyManager.NETWORK_TYPE_UMTS: // 3G
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        return 3;
                    case TelephonyManager.NETWORK_TYPE_GPRS: // 2G
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        return 4;
                    default:
                        return netType;
                }

            default:
                return netType;
        }
    }

    public static AbsAgentWebUIController getAgentWebUIControllerByWebView(WebView webView) {
        WebParentLayout mWebParentLayout = getWebParentLayoutByWebView(webView);
        return mWebParentLayout.provide();
    }

    public static WebParentLayout getWebParentLayoutByWebView(WebView webView) {
        ViewGroup mViewGroup = null;
        if (!(webView.getParent() instanceof ViewGroup)) {
            throw new IllegalStateException("please check webcreator's create method was be called ?");
        }
        mViewGroup = (ViewGroup) webView.getParent();
        AbsAgentWebUIController mAgentWebUIController;
        while (mViewGroup != null) {

            LogUtils.i(TAG, "ViewGroup:" + mViewGroup);
            if (mViewGroup.getId() == R.id.web_parent_layout_id) {
                WebParentLayout mWebParentLayout = (WebParentLayout) mViewGroup;
                LogUtils.i(TAG, "found WebParentLayout");
                return mWebParentLayout;
            } else {
                ViewParent mViewParent = mViewGroup.getParent();
                if (mViewParent instanceof ViewGroup) {
                    mViewGroup = (ViewGroup) mViewParent;
                } else {
                    mViewGroup = null;
                }
            }
        }
        throw new IllegalStateException("please check webcreator's create method was be called ?");
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

    public static Method isExistMethod(Object o, String methodName, Class... clazzs) {
        if (null == o) {
            return null;
        }
        try {
            Class clazz = o.getClass();
            Method mMethod = clazz.getDeclaredMethod(methodName, clazzs);
            mMethod.setAccessible(true);
            return mMethod;
        } catch (Throwable ignore) {}
        return null;

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
        if (!TextUtils.isEmpty(SuperAgentWebConfig.AGENTWEB_FILE_PATH)) {
            return SuperAgentWebConfig.AGENTWEB_FILE_PATH;
        }
        String dir = getDiskExternalCacheDir(context);
        File mFile = new File(dir, SuperAgentWebConfig.FILE_CACHE_PATH);
        try {
            if (!mFile.exists()) {
                mFile.mkdirs();
            }
        } catch (Throwable throwable) {
            LogUtils.i(TAG, "create dir exception");
        }
        LogUtils.i(TAG, "path:" + mFile.getAbsolutePath() + "  path:" + mFile.getPath());
        return SuperAgentWebConfig.AGENTWEB_FILE_PATH = mFile.getAbsolutePath();
    }

    public static String getDiskExternalCacheDir(Context context) {
        File mFile = context.getExternalCacheDir();
        if (Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(mFile))) {
            return mFile.getAbsolutePath();
        }
        return null;
    }

}
