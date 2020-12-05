package com.siyee.superagentweb.bridge;

/**
 * @author hackycy
 */
public interface IExecutorFactory {

    /**
     * IExecutorFactory执行派发
     * @param async true为异步调用，false为同步
     * @param url 当前页面的url
     * @param func 派发名，唯一性
     * @param paramString 参数JSON字符串
     * @return
     */
    String exec(boolean async, String url, String func, String paramString);

}
