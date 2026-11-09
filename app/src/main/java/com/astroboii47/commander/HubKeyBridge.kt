package com.astroboii47.commander

object HubKeyBridge {
    @Volatile var sendReply: (() -> Unit)? = null
    @Volatile var canSend: (() -> Boolean)? = null
    private val navigateHandlers = LinkedHashMap<Any, (Int) -> Boolean>()

    @Synchronized
    fun registerNavigate(owner: Any, handler: (Int) -> Boolean) {
        navigateHandlers.remove(owner)
        navigateHandlers[owner] = handler
    }

    @Synchronized
    fun unregisterNavigate(owner: Any) {
        navigateHandlers.remove(owner)
    }

    @Synchronized
    private fun activeNavigate(): ((Int) -> Boolean)? = navigateHandlers.values.lastOrNull()

    @Synchronized
    fun ownsNavigation(): Boolean = navigateHandlers.isNotEmpty()

    fun handleEnter(): Boolean {
        if (canSend?.invoke() == true) {
            sendReply?.invoke()
            return true
        }
        return activeNavigate()?.invoke(android.view.KeyEvent.KEYCODE_ENTER) == true
    }

    fun handleKey(keyCode: Int): Boolean = activeNavigate()?.invoke(keyCode) == true
}
