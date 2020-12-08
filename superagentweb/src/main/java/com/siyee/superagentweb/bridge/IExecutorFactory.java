package com.siyee.superagentweb.bridge;

/**
 * @author hackycy
 */
public interface IExecutorFactory {

    /**
     * IExecutorFactory执行派发，异步执行时返回值无效
     * @param promise 如果为空则为异步执行，需要手动调用，否则无法执行回调，同步执行为null
     * @param url 当前页面的url
     * @param func 派发名，唯一性
     * @param paramString 参数JSON字符串
     * @return
     */
    String exec(InternalBridge.Promise promise, String url, String func, String paramString);

}
