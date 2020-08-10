package com.siyee.superagentweb;

import android.Manifest;

/**
 * @author hackycy
 */
public class AgentWebPermissions {

    public static final String[] CAMERA;
    public static final String[] LOCATION;
    public static final String[] STORAGE;

    public static final String ACTION_CAMERA = "Camera";
    public static final String ACTION_LOCATION = "Location";
    public static final String ACTION_STORAGE = "Storage";

    static {
        CAMERA = new String[]{
                Manifest.permission.CAMERA
        };
        LOCATION = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        STORAGE = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

}
