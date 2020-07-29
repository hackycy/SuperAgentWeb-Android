package com.siyee.superagentweb

interface IUrlLoader {

    fun loadUrl(url: String)

    fun loadUrl(url: String, headers: Map<String, String>)

    fun reload()

    fun loadData(data: String, mimeType: String, encoding: String)

    fun stopLoading()

    fun loadDataWithBaseURL(baseUrl: String, data: String, mimeType: String, encoding: String, historyUrl: String)

    fun postUrl(url: String, params: ByteArray)

//    fun getHttpHeaders(): HttpH

}