package com.siyee.superagentweb

import android.view.KeyEvent

/**
 * @author hackycy
 */
interface IEventHandler {

    fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean

    fun back()

}