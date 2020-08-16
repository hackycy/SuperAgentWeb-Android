package com.siyee.superagentweb.abs;

import java.util.Map;

public interface IUrlLoader {

    void loadUrl(String url);

    void loadUrl(String url, Map<String, String> headers);

    void reload();

    void loadData(String data, String mimeType, String encoding);

    void stopLoading();

    void loadDataWithBaseURL(String baseUrl, String data,
                             String mimeType, String encoding, String historyUrl);

    void postUrl(String url, byte[] params);

//    HttpHeaders getHttpHeaders();

}
