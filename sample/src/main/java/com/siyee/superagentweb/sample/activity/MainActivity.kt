package com.siyee.superagentweb.sample.activity

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.widget.FrameLayout
import com.siyee.superagentweb.SuperAgentWeb
import com.siyee.superagentweb.bridge.IExecutorFactory
import com.siyee.superagentweb.sample.R

class MainActivity : AppCompatActivity() {

    val fl: FrameLayout by lazy  {
        findViewById<FrameLayout>(R.id.parent)
    }

    val agentweb: SuperAgentWeb by lazy {
        SuperAgentWeb.with(this)
            .setAgentWebParent(fl, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
            .useDefaultIndicator(Color.BLACK)
            .interceptUnkownUrl()
            .setExecutorFactory(IExecutorFactory { url, func, paramString, _ ->
                if (func == "log") {
                    Log.e("SuperAgentWeb", if (TextUtils.isEmpty(paramString)) "nothing" else paramString)
                    return@IExecutorFactory "{ \"msg\": 2 }"
                } else {
                    Log.e("SuperAgentWeb", "nothong to log func : $func url : $url")
                    return@IExecutorFactory ""
                }

            })
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        agentweb.go("file:///android_asset/bridge-test.html")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (agentweb.handleKeyEvent(keyCode, event)) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }


}