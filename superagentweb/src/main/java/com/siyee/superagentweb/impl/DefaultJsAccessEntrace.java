package com.siyee.superagentweb.impl;

import android.os.Build;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.siyee.superagentweb.abs.JsAccessEntrace;
import com.siyee.superagentweb.utils.SuperAgentWebUtils;

/**
 * @author hackycy
 */
public class DefaultJsAccessEntrace implements JsAccessEntrace {

    private WebView mWebView;

    public DefaultJsAccessEntrace(WebView webView) {
        this.mWebView = webView;
    }

    @Override
    public void callJs(String js, ValueCallback<String> callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.evaluateJs(js, callback);
        } else {
            this.loadJs(js);
        }
    }

    @Override
    public void callJs(String js) {
        this.callJs(js, null);
    }

    @Override
    public void quickCallJs(String method, ValueCallback<String> callback, String... params) {
        StringBuilder sb=new StringBuilder();
        sb.append("javascript:").append(method);
        if(params==null||params.length==0){
            sb.append("()");
        }else{
            sb.append("(").append(concat(params)).append(")");
        }
        callJs(sb.toString(),callback);
    }

    @Override
    public void quickCallJs(String method, String... params) {
        this.quickCallJs(method,null, params);
    }

    @Override
    public void quickCallJs(String method) {
        this.quickCallJs(method, null, (String[]) null);
    }

    private String concat(String...params){
        StringBuilder mStringBuilder=new StringBuilder();
        for(int i=0;i<params.length;i++){
            String param=params[i];
            if(!SuperAgentWebUtils.isJson(param)){
                mStringBuilder.append("\"").append(param).append("\"");
            }else{
                mStringBuilder.append(param);
            }
            if(i!=params.length-1){
                mStringBuilder.append(" , ");
            }
        }
        return mStringBuilder.toString();
    }

    /**
     * Below KITKAT
     * @param js
     */
    private void loadJs(String js) {
        mWebView.loadUrl(js);
    }

    /**
     * Above KITKAT
     * @param js
     * @param callback
     */
    private void evaluateJs(String js, final ValueCallback<String>callback){
        mWebView.evaluateJavascript(js, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if (callback != null){
                    callback.onReceiveValue(value);
                }
            }
        });
    }

}
