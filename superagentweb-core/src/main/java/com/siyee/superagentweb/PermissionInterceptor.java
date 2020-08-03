package com.siyee.superagentweb;

/**
 * @author hackycy
 */
public interface PermissionInterceptor {

    boolean intercept(String url, String[] permissions, String action);

}
