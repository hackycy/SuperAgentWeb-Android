package com.siyee.superagentweb.bridge;

/**
 * @author hackycy
 */
public interface IExecutorFactory {

    String exec(String func, String paramString, int callbackId);

}
