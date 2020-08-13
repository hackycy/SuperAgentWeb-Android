package com.siyee.superagentweb.sample

import android.app.Application
import com.siyee.superagentweb.SuperAgentWebConfig

/**
 * @author hackycy
 */
class App: Application() {

    override fun onCreate() {
        super.onCreate()
        SuperAgentWebConfig.DEBUG = true
    }

}