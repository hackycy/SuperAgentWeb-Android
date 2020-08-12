package com.siyee.superagentweb.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import com.siyee.superagentweb.SuperAgentWeb

class MainActivity : AppCompatActivity() {

    val fl: FrameLayout by lazy  {
        findViewById<FrameLayout>(R.id.parent)
    }

    val agentweb: SuperAgentWeb by lazy {
        SuperAgentWeb.with(this)
            .setAgentWebParent(fl, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        agentweb.webCreator.webView.loadUrl("https://m.baidu.com")
    }


}