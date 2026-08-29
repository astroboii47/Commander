package com.astroboii47.commander

import android.view.KeyEvent

object SwitcherKeyBridge {
    @Volatile var handler: ((KeyEvent) -> Boolean)? = null

    fun dispatch(event: KeyEvent): Boolean = handler?.invoke(event) == true
}
