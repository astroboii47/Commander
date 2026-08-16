package com.astroboii47.commander

object HubKeyBridge {
    @Volatile var sendReply: (() -> Unit)? = null
    @Volatile var canSend: (() -> Boolean)? = null
    @Volatile var navigate: ((Int) -> Boolean)? = null

    fun handleEnter(): Boolean {
        if (canSend?.invoke() == true) {
            sendReply?.invoke()
            return true
        }
        return navigate?.invoke(android.view.KeyEvent.KEYCODE_ENTER) == true
    }

    fun handleKey(keyCode: Int): Boolean = navigate?.invoke(keyCode) == true
}
