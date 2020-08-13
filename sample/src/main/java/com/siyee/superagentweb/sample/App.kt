package com.siyee.superagentweb.sample

import android.app.Application
import com.siyee.superagentweb.AgentWebConfig

/**
 * @author hackycy
 */
class App: Application() {

    override fun onCreate() {
        super.onCreate()
        AgentWebConfig.DEBUG = true
    }

}