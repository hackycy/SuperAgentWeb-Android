package com.siyee.superagentweb.abs;

/**
 * @author hackycy
 */
public interface PermissionInterceptor {

    boolean intercept(String url, String[] permissions, String action);

}
