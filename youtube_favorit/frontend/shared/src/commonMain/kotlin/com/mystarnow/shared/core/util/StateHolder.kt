package com.mystarnow.shared.core.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

open class StateHolder(
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
) {
    protected val scope = CoroutineScope(SupervisorJob() + dispatcher)

    open fun clear() {
        scope.cancel()
    }
}
