package com.siyee.superagentweb.sample.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.widget.FrameLayout
import com.siyee.superagentweb.SuperAgentWeb
import com.siyee.superagentweb.sample.R

class MainActivity : AppCompatActivity() {

    val fl: FrameLayout by lazy  {
        findViewById<FrameLayout>(R.id.parent)
    }

    val agentweb: SuperAgentWeb by lazy {
        SuperAgentWeb.with(this)
            .setAgentWebParent(fl, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
            .interceptUnkownUrl()
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        agentweb.webCreator.webView.loadUrl("https://m.baidu.com")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (agentweb.handleKeyEvent(keyCode, event)) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }


}