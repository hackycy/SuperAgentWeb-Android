package com.siyee.superagentweb.utils;

import android.util.Log;

import com.siyee.superagentweb.AgentWebConfig;

public class LogUtils {

    private static final String PREFIX = "superagentweb-";

    static boolean isDebug() {
        return AgentWebConfig.DEBUG;
    }

    static void i(String tag, String message) {

        if (isDebug()) {
            Log.i(PREFIX.concat(tag), message);
        }
    }

    static void v(String tag, String message) {

        if (isDebug()) {
            Log.v(PREFIX.concat(tag), message);
        }

    }

    static void safeCheckCrash(String tag, String msg, Throwable tr) {
        if (isDebug()) {
            throw new RuntimeException(PREFIX.concat(tag) + " " + msg, tr);
        } else {
            Log.e(PREFIX.concat(tag), msg, tr);
        }
    }

    static void e(String tag, String msg, Throwable tr) {
        Log.e(tag, msg, tr);
    }

    static void e(String tag, String message) {

        if (isDebug()) {
            Log.e(PREFIX.concat(tag), message);
        }
    }

}
