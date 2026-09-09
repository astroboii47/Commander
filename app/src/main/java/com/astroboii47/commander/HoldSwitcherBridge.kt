package com.astroboii47.commander

import androidx.compose.runtime.mutableStateOf

data class HoldSwitcherState(
    val sessionId: Int = 0,
    val steps: Int = 0,
    val dismissRequests: Int = 0,
    val sourcePackage: String? = null,
    val active: Boolean = false,
    val committed: Boolean = false,
)

object HoldSwitcherBridge {
    val state = mutableStateOf(HoldSwitcherState())

    fun start(sourcePackage: String?) {
        state.value = HoldSwitcherState(
            sessionId = state.value.sessionId + 1,
            active = true,
            sourcePackage = sourcePackage,
        )
    }

    fun advance() {
        if (state.value.active) state.value = state.value.copy(steps = state.value.steps + 1)
    }

    fun reverse() {
        if (state.value.active) state.value = state.value.copy(steps = state.value.steps - 1)
    }

    fun dismiss() {
        if (state.value.active) state.value = state.value.copy(dismissRequests = state.value.dismissRequests + 1)
    }

    fun commit() {
        if (state.value.active) state.value = state.value.copy(committed = true)
    }

    fun clearCommit() {
        state.value = state.value.copy(active = false, committed = false)
    }
}
